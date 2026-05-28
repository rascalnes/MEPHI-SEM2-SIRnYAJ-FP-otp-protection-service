package ru.nes.otp.service.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Сервис сохранения OTP-кодов в файл в корне проекта.
 */
public class FileNotificationService implements NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(FileNotificationService.class);
    private static final String OTP_DIRECTORY = "otp-files";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private final Path otpDirectory;

    public FileNotificationService() {
        // Создаем директорию в корне проекта
        this.otpDirectory = Paths.get(System.getProperty("user.dir"), OTP_DIRECTORY);
        try {
            if (!Files.exists(otpDirectory)) {
                Files.createDirectories(otpDirectory);
                logger.info("Created OTP files directory: {}", otpDirectory.toAbsolutePath());
            }
        } catch (IOException e) {
            logger.error("Failed to create OTP directory: {}", otpDirectory, e);
        }
        logger.info("FileNotificationService initialized with directory: {}", otpDirectory.toAbsolutePath());
    }

    @Override
    public boolean sendCode(String destination, String code) {
        if (destination == null || destination.trim().isEmpty()) {
            destination = "unknown";
        }

        String timestamp = LocalDateTime.now().format(DATE_FORMATTER);
        String filename = String.format("otp_%s_%s.txt", sanitizeFilename(destination), timestamp);
        Path filePath = otpDirectory.resolve(filename);

        String content = String.format(
                "====================================\n" +
                        "OTP CODE GENERATED\n" +
                        "====================================\n" +
                        "User: %s\n" +
                        "Code: %s\n" +
                        "Generated: %s\n" +
                        "Valid for: 5 minutes\n" +
                        "====================================\n",
                destination,
                code,
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        );

        try {
            Files.writeString(filePath, content, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
            logger.info("OTP code saved to file: {}", filePath.toAbsolutePath());
            return true;
        } catch (IOException e) {
            logger.error("Failed to save OTP code to file: {}", filePath, e);
            return false;
        }
    }

    private String sanitizeFilename(String name) {
        // Замена недопустимых символов в имени файла
        return name.replaceAll("[^a-zA-Z0-9.-]", "_");
    }

    @Override
    public String getChannelType() {
        return "FILE";
    }
}