package org.delcom.ifs25005springbootcashflow.domain.entity;

/** Pengguna aplikasi. */
public class User {

    private final int id;
    private final String username;
    private String passwordHash;
    private String fullName;
    private final Role role;
    private String photoPath;

    public User(int id, String username, String passwordHash, String fullName, Role role, String photoPath) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.role = role;
        this.photoPath = photoPath;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Role getRole() {
        return role;
    }

    public String getPhotoPath() {
        return photoPath;
    }

    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }

    public boolean hasPhoto() {
        return photoPath != null && !photoPath.isBlank();
    }
}
