package ru.nes.otp.model.dto.response;

import com.google.gson.annotations.SerializedName;

/**
 * Единый формат успешного ответа.
 */
public class SuccessResponse<T> {

    @SerializedName("status")
    private String status = "success";

    @SerializedName("data")
    private T data;

    @SerializedName("message")
    private String message;

    @SerializedName("timestamp")
    private String timestamp;

    public SuccessResponse() {
        this.timestamp = java.time.LocalDateTime.now().toString();
    }

    public SuccessResponse(T data) {
        this();
        this.data = data;
    }

    public SuccessResponse(String message) {
        this();
        this.message = message;
    }

    public SuccessResponse(T data, String message) {
        this();
        this.data = data;
        this.message = message;
    }

    // Getters and Setters
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}