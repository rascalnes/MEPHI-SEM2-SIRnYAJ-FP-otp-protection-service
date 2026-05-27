package ru.nes.otp.security;

import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Утилита для хеширования и верификации паролей.
 * Использует BCrypt алгоритм.
 */
public class PasswordUtil {

    private static final Logger logger = LoggerFactory.getLogger(PasswordUtil.class);
    private static final int BCRYPT_ROUNDS = 12;

    /**
     * Хеширование пароля.
     */
    public static String hashPassword(String plainPassword) {
        try {
            return BCrypt.hashpw(plainPassword, BCrypt.gensalt(BCRYPT_ROUNDS));
        } catch (Exception e) {
            logger.error("Failed to hash password", e);
            throw new RuntimeException("Password hashing failed", e);
        }
    }

    /**
     * Проверка пароля.
     */
    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        try {
            return BCrypt.checkpw(plainPassword, hashedPassword);
        } catch (Exception e) {
            logger.error("Failed to verify password", e);
            return false;
        }
    }
}