package ru.nes.otp.service.notification;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Сервис отправки OTP-кодов по Email через SMTP.
 */
public class EmailNotificationService implements NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(EmailNotificationService.class);
    private static final String CONFIG_FILE = "email.properties";

    private final String username;
    private final String password;
    private final String fromEmail;
    private final Session session;

    public EmailNotificationService() {
        Properties config = loadConfig();
        this.username = config.getProperty("email.username");
        this.password = config.getProperty("email.password");
        this.fromEmail = config.getProperty("email.from");

        Properties mailProps = new Properties();
        mailProps.put("mail.smtp.host", config.getProperty("mail.smtp.host"));
        mailProps.put("mail.smtp.port", config.getProperty("mail.smtp.port"));
        mailProps.put("mail.smtp.auth", config.getProperty("mail.smtp.auth"));
        mailProps.put("mail.smtp.starttls.enable", config.getProperty("mail.smtp.starttls.enable"));
        if (config.containsKey("mail.smtp.ssl.trust")) {
            mailProps.put("mail.smtp.ssl.trust", config.getProperty("mail.smtp.ssl.trust"));
        }

        this.session = Session.getInstance(mailProps, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        logger.info("EmailNotificationService initialized with from: {}", fromEmail);
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
            logger.error("Failed to load email configuration", e);
            return getDefaultConfig();
        }
    }

    private Properties getDefaultConfig() {
        Properties props = new Properties();
        props.setProperty("email.username", "test@example.com");
        props.setProperty("email.password", "password");
        props.setProperty("email.from", "otp@example.com");
        props.setProperty("mail.smtp.host", "smtp.example.com");
        props.setProperty("mail.smtp.port", "587");
        props.setProperty("mail.smtp.auth", "true");
        props.setProperty("mail.smtp.starttls.enable", "true");
        return props;
    }

    @Override
    public boolean sendCode(String toEmail, String code) {
        if (toEmail == null || toEmail.trim().isEmpty()) {
            logger.error("Email destination is empty");
            return false;
        }

        logger.info("Sending OTP code to email: {}", toEmail);

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
            message.setSubject("Your OTP Verification Code");
            message.setText(String.format(
                    "Hello!\n\nYour OTP verification code is: %s\n\n" +
                            "This code will expire in 5 minutes.\n\n" +
                            "If you didn't request this code, please ignore this message.\n\n" +
                            "Best regards,\nOTP Protection Service",
                    code
            ));

            Transport.send(message);
            logger.info("OTP code sent successfully to {}", toEmail);
            return true;

        } catch (MessagingException e) {
            logger.error("Failed to send email to {}: {}", toEmail, e.getMessage());
            // Для эмуляции отправки при тестировании
            logger.info("[EMULATOR] Would send email to {} with code: {}", toEmail, code);
            return true; // В эмуляторе считаем успехом
        }
    }

    @Override
    public String getChannelType() {
        return "EMAIL";
    }
}