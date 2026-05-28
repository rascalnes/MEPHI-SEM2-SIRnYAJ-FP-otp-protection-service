package ru.nes.otp.handler;

import com.sun.net.httpserver.HttpExchange;
import ru.nes.otp.model.dto.request.ConfigUpdateRequest;
import ru.nes.otp.model.dto.response.UserResponse;
import ru.nes.otp.model.entity.OtpConfig;
import ru.nes.otp.service.AdminService;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Обработчик административных API.
 */
public class AdminHandler extends BaseHandler {

    private final AdminService adminService;

    public AdminHandler() {
        this.adminService = new AdminService();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        setCorsHeaders(exchange);

        if (handleOptions(exchange)) {
            return;
        }

        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        String currentUser = (String) exchange.getAttribute("userId");

        try {
            if ("GET".equals(method) && path.equals("/api/admin/users")) {
                handleGetUsers(exchange);
            } else if ("DELETE".equals(method) && path.matches("/api/admin/users/\\d+")) {
                handleDeleteUser(exchange, currentUser);
            } else if ("GET".equals(method) && path.equals("/api/admin/config")) {
                handleGetConfig(exchange);
            } else if ("PUT".equals(method) && path.equals("/api/admin/config")) {
                handleUpdateConfig(exchange, currentUser);
            } else {
                sendNotFound(exchange, "Endpoint not found");
            }
        } catch (Exception e) {
            logger.error("Error handling admin request", e);
            sendInternalError(exchange, "Internal server error");
        }
    }

    private void handleGetUsers(HttpExchange exchange) throws IOException {
        List<UserResponse> users = adminService.getAllNonAdminUsers();
        sendSuccess(exchange, users);
    }

    private void handleDeleteUser(HttpExchange exchange, String currentUser) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String[] parts = path.split("/");
        Long userId;

        try {
            userId = Long.parseLong(parts[parts.length - 1]);
        } catch (NumberFormatException e) {
            sendBadRequest(exchange, "Invalid user ID format");
            return;
        }

        boolean deleted = adminService.deleteUser(userId, currentUser);

        if (deleted) {
            sendSuccessMessage(exchange, "User deleted successfully");
        } else {
            sendNotFound(exchange, "User not found or cannot delete yourself");
        }
    }

    private void handleGetConfig(HttpExchange exchange) throws IOException {
        Optional<OtpConfig> config = adminService.getConfig();

        if (config.isPresent()) {
            sendSuccess(exchange, config.get());
        } else {
            sendNotFound(exchange, "Configuration not found");
        }
    }

    private void handleUpdateConfig(HttpExchange exchange, String currentUser) throws IOException {
        ConfigUpdateRequest request = parseBody(exchange, ConfigUpdateRequest.class);

        if (request == null) {
            sendBadRequest(exchange, "Invalid request body");
            return;
        }

        if (request.getLifetimeSeconds() == null ||
                request.getLifetimeSeconds() < 30 ||
                request.getLifetimeSeconds() > 3600) {
            sendBadRequest(exchange, "lifetimeSeconds must be between 30 and 3600");
            return;
        }

        if (request.getCodeLength() == null ||
                request.getCodeLength() < 4 ||
                request.getCodeLength() > 8) {
            sendBadRequest(exchange, "codeLength must be between 4 and 8");
            return;
        }

        boolean updated = adminService.updateConfig(
                request.getLifetimeSeconds(),
                request.getCodeLength(),
                currentUser
        );

        if (updated) {
            sendSuccessMessage(exchange, "Configuration updated successfully");
        } else {
            sendInternalError(exchange, "Failed to update configuration");
        }
    }
}