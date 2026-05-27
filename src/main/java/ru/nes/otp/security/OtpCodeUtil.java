package ru.nes.otp.security;

import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import java.util.Random;

/**
 * Утилита для генерации и хеширования OTP-кодов.
 */
public class OtpCodeUtil {

    private static final Logger logger = LoggerFactory.getLogger(OtpCodeUtil.class);
    private static final Random SECURE_RANDOM = new SecureRandom();

    /**
     * Генерация случайного числового OTP-кода указанной длины.
     */
    public static String generateCode(int length) {
        if (length < 1 || length > 10) {
            throw new IllegalArgumentException("Code length must be between 1 and 10");
        }

        StringBuilder code = new StringBuilder();
        for (int i = 0; i < length; i++) {
            code.append(SECURE_RANDOM.nextInt(10));
        }

        logger.debug("Generated OTP code of length: {}", length);
        return code.toString();
    }

    /**
     * Хеширование OTP-кода для хранения в БД.
     */
    public static String hashCode(String code) {
        try {
            return BCrypt.hashpw(code, BCrypt.gensalt(8));
        } catch (Exception e) {
            logger.error("Failed to hash OTP code", e);
            throw new RuntimeException("OTP code hashing failed", e);
        }
    }

    /**
     * Проверка OTP-кода.
     */
    public static boolean verifyCode(String plainCode, String hashedCode) {
        try {
            return BCrypt.checkpw(plainCode, hashedCode);
        } catch (Exception e) {
            logger.error("Failed to verify OTP code", e);
            return false;
        }
    }

    /**
     * Генерация уникального идентификатора операции.
     */
    public static String generateOperationId() {
        return java.util.UUID.randomUUID().toString();
    }
}