package ru.nes.otp.model.dto.request;

import com.google.gson.annotations.SerializedName;

/**
 * Запрос на обновление конфигурации OTP.
 */
public class ConfigUpdateRequest {

    @SerializedName("lifetimeSeconds")
    private Integer lifetimeSeconds;

    @SerializedName("codeLength")
    private Integer codeLength;

    public ConfigUpdateRequest() {}

    public ConfigUpdateRequest(Integer lifetimeSeconds, Integer codeLength) {
        this.lifetimeSeconds = lifetimeSeconds;
        this.codeLength = codeLength;
    }

    public Integer getLifetimeSeconds() {
        return lifetimeSeconds;
    }

    public void setLifetimeSeconds(Integer lifetimeSeconds) {
        this.lifetimeSeconds = lifetimeSeconds;
    }

    public Integer getCodeLength() {
        return codeLength;
    }

    public void setCodeLength(Integer codeLength) {
        this.codeLength = codeLength;
    }

    public boolean isValid() {
        return lifetimeSeconds != null && lifetimeSeconds >= 30 && lifetimeSeconds <= 3600
                && codeLength != null && codeLength >= 4 && codeLength <= 8;
    }
}