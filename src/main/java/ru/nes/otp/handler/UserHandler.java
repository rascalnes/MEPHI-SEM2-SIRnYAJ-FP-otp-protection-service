package ru.nes.otp.handler;

import com.sun.net.httpserver.HttpExchange;
import ru.nes.otp.model.dto.request.OtpGenerateRequest;
import ru.nes.otp.model.dto.request.OtpValidateRequest;
import ru.nes.otp.model.dto.response.OtpGenerateResponse;
import ru.nes.otp.model.dto.response.OtpValidateResponse;
import ru.nes.otp.service.OtpService;
import ru.nes.otp.util.ValidatorUtil;

import java.io.IOException;

/**
 * Обработчик пользовательских API.
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

        String login = (String) exchange.getAttribute("userId");

        if (login == null) {
            sendUnauthorized(exchange, "Unauthorized");
            return;
        }

        try {
            if ("POST".equals(method)) {
                if (path.equals("/api/otp/generate")) {
                    handleGenerateOtp(exchange, login);
                } else if (path.equals("/api/otp/validate")) {
                    handleValidateOtp(exchange, login);
                } else {
                    sendNotFound(exchange, "Endpoint not found");
                }
            } else {
                sendError(exchange, 405, "Method not allowed");
            }
        } catch (Exception e) {
            logger.error("Error handling user request", e);
            sendInternalError(exchange, "Internal server error: " + e.getMessage());
        }
    }

    private void handleGenerateOtp(HttpExchange exchange, String login) throws IOException {
        OtpGenerateRequest request = parseBody(exchange, OtpGenerateRequest.class);

        if (request == null) {
            sendBadRequest(exchange, "Invalid request body");
            return;
        }

        // Валидация operationId
        if (!ValidatorUtil.isValidOperationId(request.getOperationId())) {
            sendBadRequest(exchange, "Invalid operationId: must be 1-100 characters (letters, numbers, underscore, hyphen)");
            return;
        }

        // Валидация канала
        String channelUpper = request.getChannel().toUpperCase();
        if (!channelUpper.equals("SMS") && !channelUpper.equals("EMAIL") &&
                !channelUpper.equals("TELEGRAM") && !channelUpper.equals("FILE")) {
            sendBadRequest(exchange, "Invalid channel: must be SMS, EMAIL, TELEGRAM, or FILE");
            return;
        }

        // Валидация destination в зависимости от канала
        String destination = request.getDestination();
        if (destination == null || destination.trim().isEmpty()) {
            sendBadRequest(exchange, "Destination is required");
            return;
        }

        if (channelUpper.equals("EMAIL") && !ValidatorUtil.isValidEmail(destination)) {
            sendBadRequest(exchange, "Invalid email format");
            return;
        }

        if (channelUpper.equals("SMS") && !ValidatorUtil.isValidPhone(destination)) {
            sendBadRequest(exchange, "Invalid phone number format");
            return;
        }

        OtpGenerateResponse response = otpService.generateOtp(login, request);

        if (response != null) {
            sendSuccess(exchange, response);
        } else {
            sendInternalError(exchange, "Failed to generate OTP code");
        }
    }

    private void handleValidateOtp(HttpExchange exchange, String login) throws IOException {
        OtpValidateRequest request = parseBody(exchange, OtpValidateRequest.class);

        if (request == null) {
            sendBadRequest(exchange, "Invalid request body");
            return;
        }

        if (!ValidatorUtil.isValidOperationId(request.getOperationId())) {
            sendBadRequest(exchange, "Invalid operationId");
            return;
        }

        if (request.getCode() == null || request.getCode().trim().isEmpty()) {
            sendBadRequest(exchange, "Code is required");
            return;
        }

        OtpValidateResponse response = otpService.validateOtp(login, request);

        if (response != null) {
            if (response.isValid()) {
                sendSuccess(exchange, response);
            } else {
                sendError(exchange, 400, response.getMessage());
            }
        } else {
            sendInternalError(exchange, "Failed to validate OTP code");
        }
    }
}