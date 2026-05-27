package ru.nes.otp.model.dto.request;

import com.google.gson.annotations.SerializedName;

/**
 * Запрос на регистрацию пользователя.
 */
public class RegisterRequest {

    @SerializedName("login")
    private String login;

    @SerializedName("password")
    private String password;

    public RegisterRequest() {}

    public RegisterRequest(String login, String password) {
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
                && password != null && !password.trim().isEmpty()
                && login.length() >= 3 && login.length() <= 50
                && password.length() >= 6;
    }
}