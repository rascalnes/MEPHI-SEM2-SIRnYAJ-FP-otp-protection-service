package ru.nes.otp.model.dto.response;

import com.google.gson.annotations.SerializedName;

/**
 * Ответ при успешном логине.
 */
public class LoginResponse {

    @SerializedName("token")
    private String token;

    @SerializedName("login")
    private String login;

    @SerializedName("role")
    private String role;

    @SerializedName("expiresIn")
    private long expiresIn;

    public LoginResponse(String token, String login, String role, long expiresIn) {
        this.token = token;
        this.login = login;
        this.role = role;
        this.expiresIn = expiresIn;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
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

    public long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
    }
}