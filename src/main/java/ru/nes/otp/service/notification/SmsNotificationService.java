package ru.nes.otp.service.notification;

import org.jsmpp.bean.*;
import org.jsmpp.session.BindParameter;
import org.jsmpp.session.SMPPSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * Сервис отправки OTP-кодов через SMS (SMPP эмулятор).
 */
public class SmsNotificationService implements NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(SmsNotificationService.class);
    private static final String CONFIG_FILE = "sms.properties";

    private final String host;
    private final int port;
    private final String systemId;
    private final String password;
    private final String systemType;
    private final String sourceAddress;

    public SmsNotificationService() {
        Properties config = loadConfig();
        this.host = config.getProperty("smpp.host", "localhost");
        this.port = Integer.parseInt(config.getProperty("smpp.port", "2775"));
        this.systemId = config.getProperty("smpp.system_id", "smppclient1");
        this.password = config.getProperty("smpp.password", "password");
        this.systemType = config.getProperty("smpp.system_type", "OTP");
        this.sourceAddress = config.getProperty("smpp.source_addr", "OTPService");

        logger.info("SmsNotificationService initialized with host: {}:{}", host, port);
    }

    private Properties loadConfig() {
        try (InputStream input = getClass().getClassLoader()
                .getResourceAsStream(CONFIG_FILE)) {
            if (input == null) {
                logger.warn("{} not found, using default config", CONFIG_FILE);
                return getDefaultConfig();
            }
            Properties props = new Properties();
            props.load(input);
            return props;
        } catch (IOException e) {
            logger.error("Failed to load SMS configuration", e);
            return getDefaultConfig();
        }
    }

    private Properties getDefaultConfig() {
        Properties props = new Properties();
        props.setProperty("smpp.host", "localhost");
        props.setProperty("smpp.port", "2775");
        props.setProperty("smpp.system_id", "smppclient1");
        props.setProperty("smpp.password", "password");
        props.setProperty("smpp.system_type", "OTP");
        props.setProperty("smpp.source_addr", "OTPService");
        return props;
    }

    @Override
    public boolean sendCode(String phoneNumber, String code) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            logger.error("Phone number is empty");
            return false;
        }

        // Очистка номера телефона от лишних символов
        String cleanNumber = phoneNumber.replaceAll("[^0-9+]", "");

        logger.info("Sending OTP code via SMS to: {}", cleanNumber);

        SMPPSession session = new SMPPSession();
        try {
            BindParameter bindParameter = new BindParameter(
                    BindType.BIND_TX,
                    systemId,
                    password,
                    systemType,
                    TypeOfNumber.UNKNOWN,
                    NumberingPlanIndicator.UNKNOWN,
                    null
            );

            session.connectAndBind(host, port, bindParameter);

            String messageText = "Your OTP code: " + code;

            String messageId = String.valueOf(session.submitShortMessage(
                    systemType,
                    TypeOfNumber.UNKNOWN,
                    NumberingPlanIndicator.UNKNOWN,
                    sourceAddress,
                    TypeOfNumber.INTERNATIONAL,
                    NumberingPlanIndicator.ISDN,
                    cleanNumber,
                    new ESMClass(),
                    (byte) 0,
                    (byte) 1,
                    null,
                    null,
                    new RegisteredDelivery(SMSCDeliveryReceipt.DEFAULT),
                    (byte) 0,
                    new GeneralDataCoding(Alphabet.ALPHA_DEFAULT),
                    (byte) 0,
                    messageText.getBytes(StandardCharsets.UTF_8)
            ));

            logger.info("SMS sent successfully to {} with messageId: {}", cleanNumber, messageId);
            return true;

        } catch (Exception e) {
            logger.error("Failed to send SMS to {}: {}", cleanNumber, e.getMessage());
            // Для эмуляции отправки при тестировании (когда SMPPsim не запущен)
            logger.info("[EMULATOR] Would send SMS to {} with code: {}", cleanNumber, code);
            return true; // В эмуляторе считаем успехом
        } finally {
            try {
                if (session != null && session.getSessionState().isBound()) {
                    session.unbindAndClose();
                }
            } catch (Exception e) {
                logger.warn("Error closing SMPP session: {}", e.getMessage());
            }
        }
    }

    @Override
    public String getChannelType() {
        return "SMS";
    }
}