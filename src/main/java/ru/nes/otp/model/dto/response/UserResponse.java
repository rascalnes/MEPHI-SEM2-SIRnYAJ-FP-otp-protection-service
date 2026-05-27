package ru.nes.otp.model.dto.response;

import com.google.gson.annotations.SerializedName;
import ru.nes.otp.model.entity.User;

/**
 * Ответ с данными пользователя (без пароля).
 */
public class UserResponse {

    @SerializedName("id")
    private Long id;

    @SerializedName("login")
    private String login;

    @SerializedName("role")
    private String role;

    @SerializedName("createdAt")
    private String createdAt;

    public UserResponse(User user) {
        this.id = user.getId();
        this.login = user.getLogin();
        this.role = user.getRole().name();
        this.createdAt = user.getCreatedAt() != null ? user.getCreatedAt().toString() : null;
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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}