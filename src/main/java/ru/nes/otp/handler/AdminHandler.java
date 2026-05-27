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
 * GET /api/admin/users - список пользователей
 * DELETE /api/admin/users/{id} - удаление пользователя
 * GET /api/admin/config - получение конфигурации
 * PUT /api/admin/config - обновление конфигурации
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

        // Извлекаем логин текущего пользователя из атрибутов
        String currentUser = (String) exchange.getAttribute("userId");

        logger.info("Admin request: {} {} by user: {}", method, path, currentUser);

        try {
            if ("GET".equals(method) && path.equals("/api/admin/users")) {
                handleGetUsers(exchange);
            } else if ("DELETE".equals(method) && path.startsWith("/api/admin/users/")) {
                handleDeleteUser(exchange, currentUser);
            } else if ("GET".equals(method) && path.equals("/api/admin/config")) {
                handleGetConfig(exchange);
            } else if ("PUT".equals(method) && path.equals("/api/admin/config")) {
                handleUpdateConfig(exchange, currentUser);
            } else {
                sendErrorResponse(exchange, 404, "Endpoint not found");
            }
        } catch (Exception e) {
            logger.error("Error handling admin request", e);
            sendErrorResponse(exchange, 500, "Internal server error: " + e.getMessage());
        }
    }

    private void handleGetUsers(HttpExchange exchange) throws IOException {
        List<UserResponse> users = adminService.getAllNonAdminUsers();
        sendSuccessResponse(exchange, users);
    }

    private void handleDeleteUser(HttpExchange exchange, String currentUser) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String[] parts = path.split("/");

        if (parts.length < 5) {
            sendErrorResponse(exchange, 400, "Invalid user ID");
            return;
        }

        try {
            Long userId = Long.parseLong(parts[4]);
            boolean deleted = adminService.deleteUser(userId, currentUser);

            if (deleted) {
                sendSuccessResponse(exchange, "User deleted successfully");
            } else {
                sendErrorResponse(exchange, 404, "User not found or cannot delete yourself");
            }
        } catch (NumberFormatException e) {
            sendErrorResponse(exchange, 400, "Invalid user ID format");
        }
    }

    private void handleGetConfig(HttpExchange exchange) throws IOException {
        Optional<OtpConfig> config = adminService.getConfig();

        if (config.isPresent()) {
            sendSuccessResponse(exchange, config.get());
        } else {
            sendErrorResponse(exchange, 404, "Configuration not found");
        }
    }

    private void handleUpdateConfig(HttpExchange exchange, String currentUser) throws IOException {
        ConfigUpdateRequest request = parseBody(exchange, ConfigUpdateRequest.class);

        if (request == null || !request.isValid()) {
            sendErrorResponse(exchange, 400,
                    "Invalid request: lifetimeSeconds (30-3600) and codeLength (4-8) required");
            return;
        }

        boolean updated = adminService.updateConfig(
                request.getLifetimeSeconds(),
                request.getCodeLength(),
                currentUser
        );

        if (updated) {
            sendSuccessResponse(exchange, "Configuration updated successfully");
        } else {
            sendErrorResponse(exchange, 400, "Failed to update configuration");
        }
    }
}