package ru.nes.otp.handler;

import com.sun.net.httpserver.HttpExchange;
import ru.nes.otp.model.dto.request.LoginRequest;
import ru.nes.otp.model.dto.request.RegisterRequest;
import ru.nes.otp.model.dto.response.LoginResponse;
import ru.nes.otp.service.AuthService;

import java.io.IOException;

/**
 * Обработчик аутентификации.
 * POST /api/register - регистрация
 * POST /api/login - логин
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

        logger.info("Auth request: {} {}", method, path);

        try {
            if ("POST".equals(method)) {
                if (path.endsWith("/register")) {
                    handleRegister(exchange);
                } else if (path.endsWith("/login")) {
                    handleLogin(exchange);
                } else {
                    sendErrorResponse(exchange, 404, "Endpoint not found");
                }
            } else {
                sendErrorResponse(exchange, 405, "Method not allowed");
            }
        } catch (Exception e) {
            logger.error("Error handling auth request", e);
            sendErrorResponse(exchange, 500, "Internal server error");
        }
    }

    private void handleRegister(HttpExchange exchange) throws IOException {
        RegisterRequest request = parseBody(exchange, RegisterRequest.class);

        if (request == null || !request.isValid()) {
            sendErrorResponse(exchange, 400, "Invalid request: login (3-50 chars) and password (min 6 chars) required");
            return;
        }

        boolean success = authService.register(request);

        if (success) {
            sendSuccessResponse(exchange, "User registered successfully");
        } else {
            sendErrorResponse(exchange, 409, "User already exists or invalid data");
        }
    }

    private void handleLogin(HttpExchange exchange) throws IOException {
        LoginRequest request = parseBody(exchange, LoginRequest.class);

        if (request == null || !request.isValid()) {
            sendErrorResponse(exchange, 400, "Invalid request: login and password required");
            return;
        }

        LoginResponse response = authService.login(request);

        if (response != null) {
            sendSuccessResponse(exchange, response);
        } else {
            sendErrorResponse(exchange, 401, "Invalid login or password");
        }
    }
}