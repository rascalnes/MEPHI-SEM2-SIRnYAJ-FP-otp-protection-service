package ru.nes.otp.model.dto.request;

import com.google.gson.annotations.SerializedName;

/**
 * Запрос на генерацию OTP-кода.
 */
public class OtpGenerateRequest {

    @SerializedName("operationId")
    private String operationId;

    @SerializedName("channel")
    private String channel;

    @SerializedName("destination")
    private String destination;

    public OtpGenerateRequest() {}

    public OtpGenerateRequest(String operationId, String channel, String destination) {
        this.operationId = operationId;
        this.channel = channel;
        this.destination = destination;
    }

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

    public boolean isValid() {
        if (operationId == null || operationId.trim().isEmpty()) {
            return false;
        }
        if (channel == null || channel.trim().isEmpty()) {
            return false;
        }
        if (destination == null || destination.trim().isEmpty()) {
            return false;
        }

        String channelUpper = channel.toUpperCase();
        return channelUpper.equals("SMS") ||
                channelUpper.equals("EMAIL") ||
                channelUpper.equals("TELEGRAM") ||
                channelUpper.equals("FILE");
    }
}