package org.delcom.ifs25005springbootcashflow.framework.auth;

import org.delcom.ifs25005springbootcashflow.domain.entity.User;
import org.delcom.ifs25005springbootcashflow.domain.repository.IUserRepository;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinResponse;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinSession;
import jakarta.servlet.http.Cookie;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.Optional;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Sesi login: menyimpan id pengguna di VaadinSession + cookie bertanda tangan (HMAC) supaya sesi
 * bertahan setelah refresh/restart. LOGGED_OUT_KEY mencegah pemulihan otomatis setelah logout.
 * Rahasia penanda tangan: SESSION_SECRET (.env); bila kosong dibuat acak tiap start.
 */
public final class AuthSession {

    public static final String COOKIE_NAME = "cashflow_uid";
    public static final String LOGGED_OUT_KEY = "cashflow.logged-out";
    private static final String USER_ID_KEY = "cashflow.user-id";
    private static final int COOKIE_MAX_AGE = 8 * 60 * 60;
    private static final byte[] SECRET = loadSecret();

    private AuthSession() {
    }

    public static void login(User user) {
        VaadinSession session = VaadinSession.getCurrent();
        session.setAttribute(USER_ID_KEY, user.getId());
        session.setAttribute(LOGGED_OUT_KEY, Boolean.FALSE);
        writeCookie(user.getId() + "." + sign(String.valueOf(user.getId())), COOKIE_MAX_AGE);
    }

    public static void logout() {
        VaadinSession session = VaadinSession.getCurrent();
        session.setAttribute(USER_ID_KEY, null);
        session.setAttribute(LOGGED_OUT_KEY, Boolean.TRUE);
        writeCookie("", 0);
    }

    /** Pengguna yang sedang login, atau null. Memulihkan sesi dari cookie setelah refresh. */
    public static User currentUser(IUserRepository users) {
        VaadinSession session = VaadinSession.getCurrent();
        if (session == null || Boolean.TRUE.equals(session.getAttribute(LOGGED_OUT_KEY))) {
            return null;
        }
        if (session.getAttribute(USER_ID_KEY) instanceof Integer id) {
            Optional<User> user = users.findById(id);
            if (user.isPresent()) {
                return user.get();
            }
        }
        Integer cookieId = readCookieUserId();
        if (cookieId != null) {
            Optional<User> user = users.findById(cookieId);
            if (user.isPresent()) {
                session.setAttribute(USER_ID_KEY, cookieId);
                return user.get();
            }
        }
        return null;
    }

    private static Integer readCookieUserId() {
        VaadinRequest request = VaadinService.getCurrentRequest();
        if (request == null || request.getCookies() == null) {
            return null;
        }
        for (Cookie cookie : request.getCookies()) {
            if (COOKIE_NAME.equals(cookie.getName())) {
                return verify(cookie.getValue());
            }
        }
        return null;
    }

    /** Nilai cookie "id.tandatangan" -> id bila tanda tangan sah, selain itu null. */
    private static Integer verify(String value) {
        int dot = value == null ? -1 : value.indexOf('.');
        if (dot <= 0) {
            return null;
        }
        String id = value.substring(0, dot);
        String expected = sign(id);
        boolean valid = MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8),
                value.substring(dot + 1).getBytes(StandardCharsets.UTF_8));
        if (!valid) {
            return null;
        }
        try {
            return Integer.valueOf(id);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static void writeCookie(String value, int maxAge) {
        VaadinResponse response = VaadinService.getCurrentResponse();
        if (response == null) {
            return;
        }
        Cookie cookie = new Cookie(COOKIE_NAME, value);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setMaxAge(maxAge);
        response.addCookie(cookie);
    }

    private static String sign(String text) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(SECRET, "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(text.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException | java.security.InvalidKeyException e) {
            throw new IllegalStateException(e);
        }
    }

    private static byte[] loadSecret() {
        String configured = System.getProperty("SESSION_SECRET", System.getenv("SESSION_SECRET"));
        if (configured != null && !configured.isBlank()) {
            return configured.getBytes(StandardCharsets.UTF_8);
        }
        byte[] random = new byte[32];
        new SecureRandom().nextBytes(random);
        return random;
    }
}