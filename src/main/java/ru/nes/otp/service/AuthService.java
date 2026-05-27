package ru.nes.otp.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.nes.otp.dao.UserDAO;
import ru.nes.otp.model.dto.request.LoginRequest;
import ru.nes.otp.model.dto.request.RegisterRequest;
import ru.nes.otp.model.dto.response.LoginResponse;
import ru.nes.otp.model.entity.User;
import ru.nes.otp.model.entity.enums.UserRole;
import ru.nes.otp.security.JwtUtil;
import ru.nes.otp.security.PasswordUtil;

import java.util.Optional;

/**
 * Сервис аутентификации.
 */
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    private final UserDAO userDAO;

    public AuthService() {
        this.userDAO = new UserDAO();
    }

    /**
     * Регистрация нового пользователя.
     */
    public boolean register(RegisterRequest request) {
        if (!request.isValid()) {
            logger.warn("Invalid registration request for login: {}", request.getLogin());
            return false;
        }

        // Проверка, существует ли уже пользователь
        Optional<User> existing = userDAO.findByLogin(request.getLogin());
        if (existing.isPresent()) {
            logger.warn("User already exists: {}", request.getLogin());
            return false;
        }

        // Определение роли
        UserRole role;
        boolean adminExists = userDAO.existsAdmin();

        if (!adminExists) {
            role = UserRole.ADMIN;
            logger.info("First user registered as ADMIN: {}", request.getLogin());
        } else {
            role = UserRole.USER;
        }

        // Хеширование пароля и сохранение
        String hashedPassword = PasswordUtil.hashPassword(request.getPassword());
        User user = new User(request.getLogin(), hashedPassword, role);

        Long userId = userDAO.save(user);
        logger.info("User registered successfully: {} with role {}", request.getLogin(), role);

        return userId != null && userId > 0;
    }

    /**
     * Логин пользователя.
     */
    public LoginResponse login(LoginRequest request) {
        if (!request.isValid()) {
            logger.warn("Invalid login request for login: {}", request.getLogin());
            return null;
        }

        Optional<User> userOpt = userDAO.findByLogin(request.getLogin());
        if (userOpt.isEmpty()) {
            logger.warn("User not found: {}", request.getLogin());
            return null;
        }

        User user = userOpt.get();

        // Проверка пароля
        if (!PasswordUtil.verifyPassword(request.getPassword(), user.getPasswordHash())) {
            logger.warn("Invalid password for user: {}", request.getLogin());
            return null;
        }

        // Генерация JWT токена
        String token = JwtUtil.generateToken(user.getLogin(), user.getRole().name());

        logger.info("User logged in successfully: {}", request.getLogin());

        return new LoginResponse(token, user.getLogin(), user.getRole().name(), 3600000);
    }
}