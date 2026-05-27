package ru.nes.otp.model.dto.request;

import com.google.gson.annotations.SerializedName;

/**
 * Запрос на валидацию OTP-кода.
 */
public class OtpValidateRequest {

    @SerializedName("operationId")
    private String operationId;

    @SerializedName("code")
    private String code;

    public OtpValidateRequest() {}

    public OtpValidateRequest(String operationId, String code) {
        this.operationId = operationId;
        this.code = code;
    }

    public String getOperationId() {
        return operationId;
    }

    public void setOperationId(String operationId) {
        this.operationId = operationId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public boolean isValid() {
        return operationId != null && !operationId.trim().isEmpty()
                && code != null && !code.trim().isEmpty();
    }
}