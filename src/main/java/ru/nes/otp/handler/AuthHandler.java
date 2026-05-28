package ru.nes.otp.handler;

import com.sun.net.httpserver.HttpExchange;
import ru.nes.otp.model.dto.request.LoginRequest;
import ru.nes.otp.model.dto.request.RegisterRequest;
import ru.nes.otp.model.dto.response.LoginResponse;
import ru.nes.otp.service.AuthService;
import ru.nes.otp.util.ValidatorUtil;

import java.io.IOException;

/**
 * Обработчик аутентификации.
 */
public class AuthHandler extends BaseHandler {

    private final AuthService authService;

    public AuthHandler() {
        this.authService = new AuthService();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        setCorsHeaders(exchange);

        if (handleOptions(exchange)) {
            return;
        }

        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        try {
            if ("POST".equals(method)) {
                if (path.equals("/api/register")) {
                    handleRegister(exchange);
                } else if (path.equals("/api/login")) {
                    handleLogin(exchange);
                } else {
                    sendNotFound(exchange, "Endpoint not found");
                }
            } else {
                sendError(exchange, 405, "Method not allowed");
            }
        } catch (Exception e) {
            logger.error("Error handling auth request", e);
            sendInternalError(exchange, "Internal server error");
        }
    }

    private void handleRegister(HttpExchange exchange) throws IOException {
        RegisterRequest request = parseBody(exchange, RegisterRequest.class);

        if (request == null) {
            sendBadRequest(exchange, "Invalid request body");
            return;
        }

        // Валидация логина
        if (!ValidatorUtil.isValidLogin(request.getLogin())) {
            sendBadRequest(exchange, "Invalid login: must be 3-50 characters (letters, numbers, underscore)");
            return;
        }

        // Валидация пароля
        if (!ValidatorUtil.isValidPassword(request.getPassword())) {
            sendBadRequest(exchange, "Invalid password: must be at least 6 characters");
            return;
        }

        boolean success = authService.register(request);

        if (success) {
            sendSuccessMessage(exchange, "User registered successfully");
        } else {
            sendConflict(exchange, "User already exists");
        }
    }

    private void handleLogin(HttpExchange exchange) throws IOException {
        LoginRequest request = parseBody(exchange, LoginRequest.class);

        if (request == null) {
            sendBadRequest(exchange, "Invalid request body");
            return;
        }

        if (request.getLogin() == null || request.getLogin().trim().isEmpty()) {
            sendBadRequest(exchange, "Login is required");
            return;
        }

        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            sendBadRequest(exchange, "Password is required");
            return;
        }

        LoginResponse response = authService.login(request);

        if (response != null) {
            sendSuccess(exchange, response);
        } else {
            sendError(exchange, 401, "Invalid login or password");
        }
    }
}