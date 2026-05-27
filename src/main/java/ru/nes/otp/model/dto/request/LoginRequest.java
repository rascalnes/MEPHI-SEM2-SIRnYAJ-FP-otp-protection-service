package ru.nes.otp.model.dto.request;

import com.google.gson.annotations.SerializedName;

/**
 * Запрос на логин.
 */
public class LoginRequest {

    @SerializedName("login")
    private String login;

    @SerializedName("password")
    private String password;

    public LoginRequest() {}

    public LoginRequest(String login, String password) {
        this.login = login;
        this.password = password;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isValid() {
        return login != null && !login.trim().isEmpty()
                && password != null && !password.trim().isEmpty();
    }
}