package ru.nes.otp.model.entity.enums;

/**
 * Каналы доставки кода.
 */
public enum ChannelType {
    SMS,
    EMAIL,
    TELEGRAM,
    FILE;

    public static ChannelType fromString(String channel) {
        try {
            return ChannelType.valueOf(channel.toUpperCase());
        } catch (IllegalArgumentException e) {
            return EMAIL;
        }
    }
}