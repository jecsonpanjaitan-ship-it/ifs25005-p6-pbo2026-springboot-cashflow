package org.delcom.ifs25005springbootcashflow.domain;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/** Hash dan verifikasi password dengan SHA-256 (heksadesimal huruf kecil). */
public final class PasswordHasher {

    private static final String ALGORITHM = "SHA-256";

    private PasswordHasher() {
    }

    public static String hash(String rawPassword) {
        return digestHex(ALGORITHM, rawPassword);
    }

    public static boolean matches(String rawPassword, String expectedHash) {
        return hash(rawPassword).equals(expectedHash);
    }

    static String digestHex(String algorithm, String text) {
        try {
            byte[] digest = MessageDigest.getInstance(algorithm).digest(text.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Algoritma hash tidak tersedia: " + algorithm, e);
        }
    }
}
