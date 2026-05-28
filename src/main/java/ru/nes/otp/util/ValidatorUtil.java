package ru.nes.otp.util;

import java.util.regex.Pattern;

/**
 * Утилита для валидации входных данных.
 */
public final class ValidatorUtil {

    private static final Pattern LOGIN_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,50}$");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^.{6,100}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[+]?[0-9]{10,15}$");
    private static final Pattern OPERATION_ID_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]{1,100}$");

    private ValidatorUtil() {}

    public static boolean isValidLogin(String login) {
        return login != null && LOGIN_PATTERN.matcher(login).matches();
    }

    public static boolean isValidPassword(String password) {
        return password != null && PASSWORD_PATTERN.matcher(password).matches();
    }

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean isValidPhone(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone).matches();
    }

    public static boolean isValidOperationId(String operationId) {
        return operationId != null && OPERATION_ID_PATTERN.matcher(operationId).matches();
    }

    public static boolean isValidCode(String code, int expectedLength) {
        if (code == null) return false;
        return code.matches("\\d{" + expectedLength + "}");
    }

    public static String sanitizeInput(String input) {
        if (input == null) return null;
        // Remove any non-printable characters
        return input.replaceAll("[\\x00-\\x1F\\x7F]", "");
    }
}