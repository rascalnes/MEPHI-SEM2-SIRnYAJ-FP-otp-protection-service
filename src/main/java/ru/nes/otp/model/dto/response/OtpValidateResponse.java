package ru.nes.otp.model.dto.response;

import com.google.gson.annotations.SerializedName;

/**
 * Ответ при валидации OTP-кода.
 */
public class OtpValidateResponse {

    @SerializedName("valid")
    private boolean valid;

    @SerializedName("message")
    private String message;

    public OtpValidateResponse(boolean valid, String message) {
        this.valid = valid;
        this.message = message;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}