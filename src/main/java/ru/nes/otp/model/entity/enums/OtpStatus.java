package ru.nes.otp.model.entity.enums;

/**
 * Статусы OTP-кода.
 */
public enum OtpStatus {
    ACTIVE,   // Код активен
    EXPIRED,  // Код просрочен
    USED;     // Код использован

    public static OtpStatus fromString(String status) {
        try {
            return OtpStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ACTIVE;
        }
    }
}