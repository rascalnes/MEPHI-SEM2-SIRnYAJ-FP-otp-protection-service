package ru.nes.otp.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Утилита для логирования.
 */
public final class LoggerUtil {

    private LoggerUtil() {}

    public static Logger getLogger(Class<?> clazz) {
        return LoggerFactory.getLogger(clazz);
    }

    public static void logRequest(Logger logger, String method, String path, String query) {
        if (query != null && !query.isEmpty()) {
            logger.info("Incoming request: {} {}?{}", method, path, query);
        } else {
            logger.info("Incoming request: {} {}", method, path);
        }
    }

    public static void logResponse(Logger logger, int statusCode, long processingTimeMs) {
        logger.info("Response status: {} ({} ms)", statusCode, processingTimeMs);
    }
}