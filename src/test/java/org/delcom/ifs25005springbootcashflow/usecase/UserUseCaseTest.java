package org.delcom.ifs25005springbootcashflow.usecase;

import static org.junit.jupiter.api.Assertions.*;

import org.delcom.ifs25005springbootcashflow.adapter.repository.UserRepository;
import org.delcom.ifs25005springbootcashflow.domain.PasswordHasher;
import org.delcom.ifs25005springbootcashflow.domain.entity.Role;
import org.delcom.ifs25005springbootcashflow.domain.entity.User;
import org.delcom.ifs25005springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25005springbootcashflow.domain.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserUseCaseTest {

    private UserRepository repo;
    private UserUseCase users;
    private User budi;

    @BeforeEach
    void setUp() {
        repo = new UserRepository();
        users = new UserUseCase(repo);
        budi = repo.save("budi", PasswordHasher.hash("lama"), "Budi", Role.USER);
        repo.save("admin", PasswordHasher.hash("admin"), "Admin", Role.ADMIN);
    }

    @Test
    void listAndGet() throws Exception {
        assertEquals(2, users.listUsers().size());
        assertEquals("budi", users.listUsers().get(0).getUsername());
        assertSame(budi, users.getUser(budi.getId()));
        assertThrows(EntityNotFoundException.class, () -> users.getUser(99));
    }

    @Test
    void updateProfile() throws Exception {
        assertThrows(ValidationException.class, () -> users.updateProfile(budi.getId(), null, null));
        assertThrows(ValidationException.class, () -> users.updateProfile(budi.getId(), " ", null));
        assertThrows(ValidationException.class, () -> users.updateProfile(budi.getId(), "Baru", "abc"));
        assertThrows(EntityNotFoundException.class, () -> users.updateProfile(99, "Baru", null));
        assertEquals("Budi", budi.getFullName());

        users.updateProfile(budi.getId(), " Budi Baru ", null);
        assertEquals("Budi Baru", budi.getFullName());
        assertEquals(PasswordHasher.hash("lama"), budi.getPasswordHash());
        users.updateProfile(budi.getId(), "Budi Baru", "   ");
        assertEquals(PasswordHasher.hash("lama"), budi.getPasswordHash());
        users.updateProfile(budi.getId(), "Budi Baru", "passbaru");
        assertEquals(PasswordHasher.hash("passbaru"), budi.getPasswordHash());
    }

    @Test
    void updatePhoto() throws Exception {
        assertThrows(ValidationException.class, () -> users.updatePhoto(budi.getId(), null));
        assertThrows(ValidationException.class, () -> users.updatePhoto(budi.getId(), " "));
        assertEquals("foto.png", users.updatePhoto(budi.getId(), "foto.png").getPhotoPath());
        assertThrows(EntityNotFoundException.class, () -> users.updatePhoto(99, "foto.png"));
    }
}
