package ru.nes.otp.model.dto.response;

import com.google.gson.annotations.SerializedName;

/**
 * Единый формат ответа с ошибкой.
 */
public class ErrorResponse {

    @SerializedName("status")
    private String status = "error";

    @SerializedName("message")
    private String message;

    @SerializedName("code")
    private int code;

    @SerializedName("timestamp")
    private String timestamp;

    @SerializedName("path")
    private String path;

    public ErrorResponse() {
        this.timestamp = java.time.LocalDateTime.now().toString();
    }

    public ErrorResponse(String message, int code, String path) {
        this();
        this.message = message;
        this.code = code;
        this.path = path;
    }

    // Getters and Setters
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}