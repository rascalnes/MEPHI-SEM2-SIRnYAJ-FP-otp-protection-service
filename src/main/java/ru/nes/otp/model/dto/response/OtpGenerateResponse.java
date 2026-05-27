package ru.nes.otp.model.dto.response;

import com.google.gson.annotations.SerializedName;

/**
 * Ответ при генерации OTP-кода.
 */
public class OtpGenerateResponse {

    @SerializedName("operationId")
    private String operationId;

    @SerializedName("channel")
    private String channel;

    @SerializedName("destination")
    private String destination;

    @SerializedName("expiresInSeconds")
    private int expiresInSeconds;

    @SerializedName("message")
    private String message;

    public OtpGenerateResponse(String operationId, String channel, String destination,
                               int expiresInSeconds, String message) {
        this.operationId = operationId;
        this.channel = channel;
        this.destination = destination;
        this.expiresInSeconds = expiresInSeconds;
        this.message = message;
    }

    // Getters and Setters
    public String getOperationId() {
        return operationId;
    }

    public void setOperationId(String operationId) {
        this.operationId = operationId;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public int getExpiresInSeconds() {
        return expiresInSeconds;
    }

    public void setExpiresInSeconds(int expiresInSeconds) {
        this.expiresInSeconds = expiresInSeconds;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}