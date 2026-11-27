package org.delcom.ifs25005springbootcashflow.usecase;

import org.delcom.ifs25005springbootcashflow.domain.PasswordHasher;
import org.delcom.ifs25005springbootcashflow.domain.entity.User;
import org.delcom.ifs25005springbootcashflow.domain.exception.DomainException;
import org.delcom.ifs25005springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25005springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25005springbootcashflow.domain.repository.IUserRepository;
import java.util.Comparator;
import java.util.List;

/** Manajemen pengguna & profil. */
public class UserUseCase {

    private final IUserRepository users;

    public UserUseCase(IUserRepository users) {
        this.users = users;
    }

    public List<User> listUsers() {
        return users.findAll().stream().sorted(Comparator.comparingInt(User::getId)).toList();
    }

    public User getUser(int id) throws DomainException {
        return users.findById(id).orElseThrow(() -> new EntityNotFoundException("Pengguna", id));
    }

    /** Ubah nama; password baru opsional (kosong = tidak diubah). */
    public User updateProfile(int userId, String fullName, String newPassword) throws DomainException {
        User user = getUser(userId);
        if (fullName == null || fullName.isBlank()) {
            throw new ValidationException("Nama lengkap wajib diisi");
        }
        boolean changePassword = newPassword != null && !newPassword.isBlank();
        if (changePassword && newPassword.length() < AuthUseCase.MIN_PASSWORD_LENGTH) {
            throw new ValidationException("Password minimal " + AuthUseCase.MIN_PASSWORD_LENGTH + " karakter");
        }
        user.setFullName(fullName.trim());
        if (changePassword) {
            user.setPasswordHash(PasswordHasher.hash(newPassword));
        }
        return users.update(user);
    }

    public User updatePhoto(int userId, String photoPath) throws DomainException {
        User user = getUser(userId);
        if (photoPath == null || photoPath.isBlank()) {
            throw new ValidationException("Path foto wajib diisi");
        }
        user.setPhotoPath(photoPath);
        return users.update(user);
    }
}
