package ru.nes.otp.handler;

import com.sun.net.httpserver.HttpExchange;
import ru.nes.otp.model.dto.request.OtpGenerateRequest;
import ru.nes.otp.model.dto.request.OtpValidateRequest;
import ru.nes.otp.model.dto.response.OtpGenerateResponse;
import ru.nes.otp.model.dto.response.OtpValidateResponse;
import ru.nes.otp.service.OtpService;

import java.io.IOException;

/**
 * Обработчик пользовательских API.
 * POST /api/otp/generate - генерация OTP-кода
 * POST /api/otp/validate - валидация OTP-кода
 */
public class UserHandler extends BaseHandler {

    private final OtpService otpService;

    public UserHandler() {
        this.otpService = new OtpService();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        setCorsHeaders(exchange);

        if (handleOptions(exchange)) {
            return;
        }

        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        // Получаем логин пользователя из атрибутов (устанавливается AuthFilter)
        String login = (String) exchange.getAttribute("userId");

        if (login == null) {
            sendErrorResponse(exchange, 401, "Unauthorized");
            return;
        }

        logger.info("User request: {} {} by user: {}", method, path, login);

        try {
            if ("POST".equals(method)) {
                if (path.equals("/api/otp/generate")) {
                    handleGenerateOtp(exchange, login);
                } else if (path.equals("/api/otp/validate")) {
                    handleValidateOtp(exchange, login);
                } else {
                    sendErrorResponse(exchange, 404, "Endpoint not found");
                }
            } else {
                sendErrorResponse(exchange, 405, "Method not allowed");
            }
        } catch (Exception e) {
            logger.error("Error handling user request", e);
            sendErrorResponse(exchange, 500, "Internal server error: " + e.getMessage());
        }
    }

    private void handleGenerateOtp(HttpExchange exchange, String login) throws IOException {
        OtpGenerateRequest request = parseBody(exchange, OtpGenerateRequest.class);

        if (request == null || !request.isValid()) {
            sendErrorResponse(exchange, 400,
                    "Invalid request: operationId, channel (SMS/EMAIL/TELEGRAM/FILE), and destination required");
            return;
        }

        OtpGenerateResponse response = otpService.generateOtp(login, request);

        if (response != null) {
            sendSuccessResponse(exchange, response);
        } else {
            sendErrorResponse(exchange, 500, "Failed to generate OTP code");
        }
    }

    private void handleValidateOtp(HttpExchange exchange, String login) throws IOException {
        OtpValidateRequest request = parseBody(exchange, OtpValidateRequest.class);

        if (request == null || !request.isValid()) {
            sendErrorResponse(exchange, 400,
                    "Invalid request: operationId and code required");
            return;
        }

        OtpValidateResponse response = otpService.validateOtp(login, request);
        sendSuccessResponse(exchange, response);
    }
}