package org.delcom.ifs25005springbootcashflow.usecase;

import org.delcom.ifs25005springbootcashflow.domain.PasswordHasher;
import org.delcom.ifs25005springbootcashflow.domain.entity.Role;
import org.delcom.ifs25005springbootcashflow.domain.entity.User;
import org.delcom.ifs25005springbootcashflow.domain.exception.DomainException;
import org.delcom.ifs25005springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25005springbootcashflow.domain.repository.IUserRepository;
import java.util.Locale;

/** Registrasi, login, dan seed admin. */
public class AuthUseCase {

    public static final int MIN_PASSWORD_LENGTH = 4;

    private final IUserRepository users;

    public AuthUseCase(IUserRepository users) {
        this.users = users;
    }

    public User register(String username, String password, String fullName) throws DomainException {
        String name = normalizeUsername(username);
        if (fullName == null || fullName.isBlank()) {
            throw new ValidationException("Nama lengkap wajib diisi");
        }
        if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
            throw new ValidationException("Password minimal " + MIN_PASSWORD_LENGTH + " karakter");
        }
        if (users.findByUsername(name).isPresent()) {
            throw new ValidationException("Username sudah dipakai");
        }
        return users.save(name, PasswordHasher.hash(password), fullName.trim(), Role.USER);
    }

    public User login(String username, String password) throws DomainException {
        String name = username == null ? "" : username.trim().toLowerCase(Locale.ROOT);
        User user = users.findByUsername(name).orElse(null);
        if (user == null || password == null || !PasswordHasher.matches(password, user.getPasswordHash())) {
            throw new ValidationException("Username atau password salah");
        }
        return user;
    }

    /** Membuat admin dari konfigurasi ROOT_* bila belum ada. */
    public User ensureRootAdmin(String username, String password) throws DomainException {
        String name = normalizeUsername(username);
        return users.findByUsername(name)
                .orElseGet(() -> users.save(name, PasswordHasher.hash(password), "Administrator", Role.ADMIN));
    }

    private String normalizeUsername(String username) throws ValidationException {
        if (username == null || username.isBlank()) {
            throw new ValidationException("Username wajib diisi");
        }
        String name = username.trim().toLowerCase(Locale.ROOT);
        if (!name.matches("[a-z0-9._-]{3,30}")) {
            throw new ValidationException(
                    "Username 3-30 karakter: huruf, angka, titik, garis bawah, atau minus");
        }
        return name;
    }
}
