package ru.nes.otp.service.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * Сервис отправки OTP-кодов через Telegram Bot API.
 */
public class TelegramNotificationService implements NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(TelegramNotificationService.class);
    private static final String CONFIG_FILE = "telegram.properties";
    private static final String DEFAULT_API_URL = "https://api.telegram.org/bot";

    private final String botToken;
    private final String chatId;
    private final String apiUrl;
    private final HttpClient httpClient;

    public TelegramNotificationService() {
        Properties config = loadConfig();
        this.botToken = config.getProperty("telegram.bot.token", "");
        this.chatId = config.getProperty("telegram.chat.id", "");
        this.apiUrl = config.getProperty("telegram.api.url", DEFAULT_API_URL) + botToken;
        this.httpClient = HttpClient.newHttpClient();

        if (botToken.isEmpty() || chatId.isEmpty()) {
            logger.warn("Telegram bot token or chat ID not configured. Telegram notifications will be emulated.");
        } else {
            logger.info("TelegramNotificationService initialized with chatId: {}", chatId);
        }
    }

    private Properties loadConfig() {
        try (InputStream input = getClass().getClassLoader()
                .getResourceAsStream(CONFIG_FILE)) {
            if (input == null) {
                logger.warn("{} not found, using default empty config", CONFIG_FILE);
                return new Properties();
            }
            Properties props = new Properties();
            props.load(input);
            return props;
        } catch (IOException e) {
            logger.error("Failed to load Telegram configuration", e);
            return new Properties();
        }
    }

    @Override
    public boolean sendCode(String destination, String code) {
        // destination может быть chatId или username
        String targetChatId = destination != null && !destination.isEmpty() ? destination : chatId;

        if (botToken.isEmpty()) {
            logger.warn("Telegram bot token not configured. [EMULATOR] Would send to {} with code: {}", targetChatId, code);
            return true;
        }

        if (targetChatId == null || targetChatId.isEmpty()) {
            logger.error("Telegram chat ID is not configured and destination is empty");
            return false;
        }

        logger.info("Sending OTP code via Telegram to chatId: {}", targetChatId);

        String message = String.format(
                "🔐 *OTP Verification Code*\n\n" +
                        "Your verification code is: `%s`\n\n" +
                        "This code will expire in 5 minutes.\n\n" +
                        "If you didn't request this code, please ignore this message.",
                code
        );

        String url = String.format("%s/sendMessage?chat_id=%s&text=%s&parse_mode=Markdown",
                apiUrl,
                targetChatId,
                urlEncode(message)
        );

        return sendTelegramRequest(url);
    }

    private boolean sendTelegramRequest(String url) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            int statusCode = response.statusCode();
            if (statusCode == 200) {
                logger.info("Telegram message sent successfully");
                return true;
            } else {
                logger.error("Telegram API error. Status code: {}, Response: {}", statusCode, response.body());
                logger.info("[EMULATOR] Telegram message would be sent to URL: {}", url);
                return true; // В эмуляторе считаем успехом
            }

        } catch (InterruptedException e) {
            logger.error("Interrupted while sending Telegram message: {}", e.getMessage());
            Thread.currentThread().interrupt();
            return false;
        } catch (IOException e) {
            logger.error("IO error while sending Telegram message: {}", e.getMessage());
            logger.info("[EMULATOR] Would send Telegram message to URL: {}", url);
            return true; // В эмуляторе считаем успехом
        }
    }

    private String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    @Override
    public String getChannelType() {
        return "TELEGRAM";
    }
}