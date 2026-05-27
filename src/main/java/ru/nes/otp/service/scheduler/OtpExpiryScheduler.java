package ru.nes.otp.service.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.nes.otp.dao.OtpCodeDAO;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Планировщик для автоматического перевода просроченных OTP-кодов в статус EXPIRED.
 */
public class OtpExpiryScheduler {

    private static final Logger logger = LoggerFactory.getLogger(OtpExpiryScheduler.class);
    private static final int INITIAL_DELAY_SECONDS = 30;
    private static final int INTERVAL_SECONDS = 30;

    private static ScheduledExecutorService scheduler;
    private static OtpCodeDAO otpCodeDAO;

    public static void start() {
        if (scheduler != null && !scheduler.isShutdown()) {
            logger.warn("OtpExpiryScheduler is already running");
            return;
        }

        otpCodeDAO = new OtpCodeDAO();
        scheduler = Executors.newSingleThreadScheduledExecutor();

        scheduler.scheduleAtFixedRate(() -> {
            try {
                int expiredCount = otpCodeDAO.expireOldCodes();
                if (expiredCount > 0) {
                    logger.info("Expired {} OTP codes", expiredCount);
                }
            } catch (Exception e) {
                logger.error("Error while expiring OTP codes", e);
            }
        }, INITIAL_DELAY_SECONDS, INTERVAL_SECONDS, TimeUnit.SECONDS);

        logger.info("OtpExpiryScheduler started. Interval: {} seconds", INTERVAL_SECONDS);
    }

    public static void stop() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
                logger.info("OtpExpiryScheduler stopped");
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}