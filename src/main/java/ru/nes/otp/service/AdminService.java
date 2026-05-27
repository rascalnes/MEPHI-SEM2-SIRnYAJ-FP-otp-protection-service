package ru.nes.otp.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.nes.otp.dao.OtpCodeDAO;
import ru.nes.otp.dao.OtpConfigDAO;
import ru.nes.otp.dao.UserDAO;
import ru.nes.otp.model.dto.response.UserResponse;
import ru.nes.otp.model.entity.OtpConfig;
import ru.nes.otp.model.entity.User;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Сервис для административных операций.
 */
public class AdminService {

    private static final Logger logger = LoggerFactory.getLogger(AdminService.class);
    private final UserDAO userDAO;
    private final OtpConfigDAO configDAO;
    private final OtpCodeDAO otpCodeDAO;

    public AdminService() {
        this.userDAO = new UserDAO();
        this.configDAO = new OtpConfigDAO();
        this.otpCodeDAO = new OtpCodeDAO();
    }

    /**
     * Получение списка всех пользователей (кроме администраторов).
     */
    public List<UserResponse> getAllNonAdminUsers() {
        List<User> users = userDAO.findAllNonAdmins();
        return users.stream()
                .map(UserResponse::new)
                .collect(Collectors.toList());
    }

    /**
     * Получение всех пользователей (включая администраторов).
     */
    public List<UserResponse> getAllUsers() {
        List<User> users = userDAO.findAll();
        return users.stream()
                .map(UserResponse::new)
                .collect(Collectors.toList());
    }

    /**
     * Удаление пользователя и всех его OTP-кодов.
     */
    public boolean deleteUser(Long userId, String currentUserLogin) {
        Optional<User> userOpt = userDAO.findById(userId);
        if (userOpt.isEmpty()) {
            logger.warn("User not found for deletion: {}", userId);
            return false;
        }

        User user = userOpt.get();

        // Нельзя удалить самого себя
        if (user.getLogin().equals(currentUserLogin)) {
            logger.warn("User cannot delete themselves: {}", currentUserLogin);
            return false;
        }

        // Удаляем OTP-коды пользователя
        int codesDeleted = otpCodeDAO.deleteByUserId(userId);

        // Удаляем пользователя
        boolean userDeleted = userDAO.deleteById(userId);

        if (userDeleted) {
            logger.info("User deleted: {} (login: {}) with {} OTP codes",
                    userId, user.getLogin(), codesDeleted);
        }

        return userDeleted;
    }

    /**
     * Получение текущей конфигурации OTP.
     */
    public Optional<OtpConfig> getConfig() {
        return configDAO.getConfig();
    }

    /**
     * Обновление конфигурации OTP.
     */
    public boolean updateConfig(int lifetimeSeconds, int codeLength, String updatedBy) {
        if (lifetimeSeconds < 30 || lifetimeSeconds > 3600) {
            logger.warn("Invalid lifetime seconds: {}", lifetimeSeconds);
            return false;
        }

        if (codeLength < 4 || codeLength > 8) {
            logger.warn("Invalid code length: {}", codeLength);
            return false;
        }

        boolean updated = configDAO.updateConfig(lifetimeSeconds, codeLength, updatedBy);

        if (updated) {
            logger.info("OTP config updated by {}: lifetime={}s, length={}",
                    updatedBy, lifetimeSeconds, codeLength);
        }

        return updated;
    }
}