package ru.nes.otp.model.entity;

import ru.nes.otp.model.entity.enums.ChannelType;
import ru.nes.otp.model.entity.enums.OtpStatus;

import java.time.LocalDateTime;

/**
 * Сущность OTP-кода.
 */
public class OtpCode {
    private Long id;
    private Long userId;
    private String operationId;
    private String codeHash;
    private OtpStatus status;
    private ChannelType channel;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private LocalDateTime validatedAt;

    public OtpCode() {}

    public OtpCode(Long userId, String operationId, String codeHash,
                   ChannelType channel, LocalDateTime expiresAt) {
        this.userId = userId;
        this.operationId = operationId;
        this.codeHash = codeHash;
        this.status = OtpStatus.ACTIVE;
        this.channel = channel;
        this.expiresAt = expiresAt;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getOperationId() {
        return operationId;
    }

    public void setOperationId(String operationId) {
        this.operationId = operationId;
    }

    public String getCodeHash() {
        return codeHash;
    }

    public void setCodeHash(String codeHash) {
        this.codeHash = codeHash;
    }

    public OtpStatus getStatus() {
        return status;
    }

    public void setStatus(OtpStatus status) {
        this.status = status;
    }

    public ChannelType getChannel() {
        return channel;
    }

    public void setChannel(ChannelType channel) {
        this.channel = channel;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public LocalDateTime getValidatedAt() {
        return validatedAt;
    }

    public void setValidatedAt(LocalDateTime validatedAt) {
        this.validatedAt = validatedAt;
    }
}