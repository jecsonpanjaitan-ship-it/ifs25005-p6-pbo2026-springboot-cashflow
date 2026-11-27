package org.delcom.ifs25005springbootcashflow.usecase;

import static org.junit.jupiter.api.Assertions.*;

import org.delcom.ifs25005springbootcashflow.adapter.repository.UserRepository;
import org.delcom.ifs25005springbootcashflow.domain.PasswordHasher;
import org.delcom.ifs25005springbootcashflow.domain.entity.Role;
import org.delcom.ifs25005springbootcashflow.domain.entity.User;
import org.delcom.ifs25005springbootcashflow.domain.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AuthUseCaseTest {

    private UserRepository users;
    private AuthUseCase auth;

    @BeforeEach
    void setUp() {
        users = new UserRepository();
        auth = new AuthUseCase(users);
    }

    @Test
    void registerSuccessNormalizesAndHashes() throws Exception {
        User u = auth.register("  Budi_01 ", "rahasia", " Budi Santoso ");
        assertEquals("budi_01", u.getUsername());
        assertEquals("Budi Santoso", u.getFullName());
        assertEquals(Role.USER, u.getRole());
        assertEquals(PasswordHasher.hash("rahasia"), u.getPasswordHash());
    }

    @Test
    void registerValidation() throws Exception {
        assertThrows(ValidationException.class, () -> auth.register(null, "rahasia", "B"));
        assertThrows(ValidationException.class, () -> auth.register("  ", "rahasia", "B"));
        assertThrows(ValidationException.class, () -> auth.register("a b", "rahasia", "B"));
        assertThrows(ValidationException.class, () -> auth.register("budi", "rahasia", null));
        assertThrows(ValidationException.class, () -> auth.register("budi", "rahasia", " "));
        assertThrows(ValidationException.class, () -> auth.register("budi", null, "B"));
        assertThrows(ValidationException.class, () -> auth.register("budi", "abc", "B"));
        auth.register("budi", "rahasia", "B");
        ValidationException dup = assertThrows(ValidationException.class, () -> auth.register("BUDI", "rahasia", "B"));
        assertEquals("Username sudah dipakai", dup.getMessage());
    }

    @Test
    void login() throws Exception {
        User u = auth.register("budi", "rahasia", "Budi");
        assertSame(u, auth.login(" Budi ", "rahasia"));
        assertThrows(ValidationException.class, () -> auth.login(null, "rahasia"));
        assertThrows(ValidationException.class, () -> auth.login("tidakada", "rahasia"));
        assertThrows(ValidationException.class, () -> auth.login("budi", null));
        ValidationException ex = assertThrows(ValidationException.class, () -> auth.login("budi", "salah"));
        assertEquals("Username atau password salah", ex.getMessage());
    }

    @Test
    void ensureRootAdmin() throws Exception {
        User admin = auth.ensureRootAdmin("admin", "admin");
        assertEquals(Role.ADMIN, admin.getRole());
        assertTrue(auth.login("admin", "admin").isAdmin());
        assertSame(admin, auth.ensureRootAdmin("ADMIN", "lain"));
        assertEquals(1, users.findAll().size());
        assertThrows(ValidationException.class, () -> auth.ensureRootAdmin("", "x"));
    }
}
