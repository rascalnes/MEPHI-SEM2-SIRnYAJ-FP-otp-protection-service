package ru.nes.otp.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.nes.otp.dao.OtpCodeDAO;
import ru.nes.otp.dao.OtpConfigDAO;
import ru.nes.otp.dao.UserDAO;
import ru.nes.otp.model.dto.request.OtpGenerateRequest;
import ru.nes.otp.model.dto.request.OtpValidateRequest;
import ru.nes.otp.model.dto.response.OtpGenerateResponse;
import ru.nes.otp.model.dto.response.OtpValidateResponse;
import ru.nes.otp.model.entity.OtpCode;
import ru.nes.otp.model.entity.OtpConfig;
import ru.nes.otp.model.entity.User;
import ru.nes.otp.model.entity.enums.ChannelType;
import ru.nes.otp.model.entity.enums.OtpStatus;
import ru.nes.otp.security.OtpCodeUtil;
import ru.nes.otp.service.notification.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Сервис для работы с OTP-кодами.
 */
public class OtpService {

    private static final Logger logger = LoggerFactory.getLogger(OtpService.class);

    private final OtpCodeDAO otpCodeDAO;
    private final OtpConfigDAO otpConfigDAO;
    private final UserDAO userDAO;
    private final Map<String, NotificationService> notificationServices;

    public OtpService() {
        this.otpCodeDAO = new OtpCodeDAO();
        this.otpConfigDAO = new OtpConfigDAO();
        this.userDAO = new UserDAO();
        this.notificationServices = new HashMap<>();

        // Инициализация сервисов уведомлений
        registerNotificationService(new EmailNotificationService());
        registerNotificationService(new SmsNotificationService());
        registerNotificationService(new TelegramNotificationService());
        registerNotificationService(new FileNotificationService());

        logger.info("OtpService initialized with {} notification channels", notificationServices.size());
    }

    private void registerNotificationService(NotificationService service) {
        notificationServices.put(service.getChannelType().toUpperCase(), service);
        logger.info("Registered notification service: {}", service.getChannelType());
    }

    /**
     * Генерация OTP-кода для операции.
     */
    public OtpGenerateResponse generateOtp(String login, OtpGenerateRequest request) {
        logger.info("Generating OTP for user: {}, operation: {}, channel: {}",
                login, request.getOperationId(), request.getChannel());

        // Получаем пользователя
        Optional<User> userOpt = userDAO.findByLogin(login);
        if (userOpt.isEmpty()) {
            logger.warn("User not found: {}", login);
            return null;
        }

        User user = userOpt.get();

        // Получаем конфигурацию OTP
        Optional<OtpConfig> configOpt = otpConfigDAO.getConfig();
        if (configOpt.isEmpty()) {
            logger.error("OTP configuration not found");
            return null;
        }

        OtpConfig config = configOpt.get();

        // Проверяем, существует ли уже активный код для этой операции
        Optional<OtpCode> existingCode = otpCodeDAO.findActiveByOperationId(request.getOperationId());
        if (existingCode.isPresent()) {
            logger.warn("Active OTP already exists for operation: {}", request.getOperationId());
            return new OtpGenerateResponse(
                    request.getOperationId(),
                    request.getChannel(),
                    request.getDestination(),
                    config.getLifetimeSeconds(),
                    "Active OTP code already exists for this operation"
            );
        }

        // Генерируем код
        String plainCode = OtpCodeUtil.generateCode(config.getCodeLength());
        String hashedCode = OtpCodeUtil.hashCode(plainCode);

        // Сохраняем в БД
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(config.getLifetimeSeconds());
        ChannelType channel = ChannelType.fromString(request.getChannel());

        OtpCode otpCode = new OtpCode(
                user.getId(),
                request.getOperationId(),
                hashedCode,
                channel,
                expiresAt
        );

        Long codeId = otpCodeDAO.save(otpCode);

        if (codeId == null || codeId == 0) {
            logger.error("Failed to save OTP code for operation: {}", request.getOperationId());
            return null;
        }

        // Отправка кода через выбранный канал
        boolean sent = sendCodeViaChannel(request.getChannel(), request.getDestination(), plainCode);

        if (!sent) {
            logger.warn("Failed to send OTP code via {} to {}", request.getChannel(), request.getDestination());
        }

        logger.info("OTP code generated successfully for operation: {}, codeId: {}",
                request.getOperationId(), codeId);

        String message = sent
                ? String.format("OTP code sent successfully via %s to %s", request.getChannel(), request.getDestination())
                : String.format("OTP code generated but failed to send via %s", request.getChannel());

        return new OtpGenerateResponse(
                request.getOperationId(),
                request.getChannel(),
                request.getDestination(),
                config.getLifetimeSeconds(),
                message
        );
    }

    /**
     * Отправка кода через указанный канал.
     */
    private boolean sendCodeViaChannel(String channel, String destination, String code) {
        String channelUpper = channel.toUpperCase();
        NotificationService service = notificationServices.get(channelUpper);

        if (service == null) {
            logger.error("Unknown notification channel: {}", channel);
            return false;
        }

        logger.info("Sending OTP code via {} to {}", channelUpper, destination);
        return service.sendCode(destination, code);
    }

    /**
     * Валидация OTP-кода.
     */
    public OtpValidateResponse validateOtp(String login, OtpValidateRequest request) {
        logger.info("Validating OTP for user: {}, operation: {}", login, request.getOperationId());

        // Получаем пользователя
        Optional<User> userOpt = userDAO.findByLogin(login);
        if (userOpt.isEmpty()) {
            logger.warn("User not found: {}", login);
            return new OtpValidateResponse(false, "User not found");
        }

        User user = userOpt.get();

        // Ищем OTP код по operationId
        Optional<OtpCode> otpCodeOpt = otpCodeDAO.findByOperationId(request.getOperationId());
        if (otpCodeOpt.isEmpty()) {
            logger.warn("OTP code not found for operation: {}", request.getOperationId());
            return new OtpValidateResponse(false, "OTP code not found for this operation");
        }

        OtpCode otpCode = otpCodeOpt.get();

        // Проверяем принадлежность кода пользователю
        if (!otpCode.getUserId().equals(user.getId())) {
            logger.warn("OTP code belongs to different user. Expected: {}, Actual: {}",
                    user.getId(), otpCode.getUserId());
            return new OtpValidateResponse(false, "OTP code not associated with this user");
        }

        // Проверяем статус
        if (otpCode.getStatus() != OtpStatus.ACTIVE) {
            logger.warn("OTP code is not active. Status: {}", otpCode.getStatus());
            return new OtpValidateResponse(false, "OTP code is " + otpCode.getStatus().name().toLowerCase());
        }

        // Проверяем срок действия
        if (LocalDateTime.now().isAfter(otpCode.getExpiresAt())) {
            logger.warn("OTP code expired for operation: {}", request.getOperationId());
            otpCodeDAO.updateStatus(otpCode.getId(), OtpStatus.EXPIRED, null);
            return new OtpValidateResponse(false, "OTP code has expired");
        }

        // Проверяем код
        if (!OtpCodeUtil.verifyCode(request.getCode(), otpCode.getCodeHash())) {
            logger.warn("Invalid OTP code for operation: {}", request.getOperationId());
            return new OtpValidateResponse(false, "Invalid OTP code");
        }

        // Код верный - меняем статус на USED
        boolean updated = otpCodeDAO.updateStatus(otpCode.getId(), OtpStatus.USED, LocalDateTime.now());

        if (updated) {
            logger.info("OTP code validated successfully for operation: {}", request.getOperationId());
            return new OtpValidateResponse(true, "OTP code validated successfully");
        } else {
            logger.error("Failed to update OTP code status for operation: {}", request.getOperationId());
            return new OtpValidateResponse(false, "Failed to update OTP code status");
        }
    }

    /**
     * Получение активного OTP-кода по operationId (для внутреннего использования).
     */
    public Optional<OtpCode> getActiveOtpCode(String operationId) {
        return otpCodeDAO.findActiveByOperationId(operationId);
    }

    /**
     * Принудительный сброс статуса OTP-кода.
     */
    public boolean expireOtpCode(String operationId) {
        Optional<OtpCode> otpCodeOpt = otpCodeDAO.findByOperationId(operationId);
        if (otpCodeOpt.isEmpty()) {
            return false;
        }

        OtpCode otpCode = otpCodeOpt.get();
        if (otpCode.getStatus() == OtpStatus.ACTIVE) {
            return otpCodeDAO.updateStatus(otpCode.getId(), OtpStatus.EXPIRED, null);
        }

        return false;
    }
}