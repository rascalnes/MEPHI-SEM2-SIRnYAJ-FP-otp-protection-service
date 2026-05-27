package ru.nes.otp.model.entity;

import java.time.LocalDateTime;

/**
 * Сущность конфигурации OTP-кодов.
 * В таблице всегда должна быть ровно одна запись (id = 1).
 */
public class OtpConfig {
    private Long id;
    private Integer lifetimeSeconds;
    private Integer codeLength;
    private LocalDateTime updatedAt;
    private String updatedBy;

    public OtpConfig() {}

    public OtpConfig(Integer lifetimeSeconds, Integer codeLength, String updatedBy) {
        this.id = 1L;
        this.lifetimeSeconds = lifetimeSeconds;
        this.codeLength = codeLength;
        this.updatedBy = updatedBy;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }
}