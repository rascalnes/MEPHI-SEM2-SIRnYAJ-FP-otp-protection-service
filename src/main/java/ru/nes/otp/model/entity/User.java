package ru.nes.otp.model.entity;

import ru.nes.otp.model.entity.enums.UserRole;

import java.time.LocalDateTime;

/**
 * Сущность пользователя.
 */
public class User {
    private Long id;
    private String login;
    private String passwordHash;
    private UserRole role;
    private LocalDateTime createdAt;

    public User() {}

    public User(String login, String passwordHash, UserRole role) {
        this.login = login;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", login='" + login + '\'' +
                ", role=" + role +
                ", createdAt=" + createdAt +
                '}';
    }
}