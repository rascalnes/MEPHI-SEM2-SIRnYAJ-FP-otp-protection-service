package ru.nes.otp.security;

import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.nes.otp.model.entity.enums.UserRole;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Фильтр для аутентификации и авторизации запросов.
 */
public class AuthFilter extends Filter {

    private static final Logger logger = LoggerFactory.getLogger(AuthFilter.class);

    // Публичные эндпоинты (не требуют токена)
    private static final Set<String> PUBLIC_ENDPOINTS = Set.of(
            "/api/register",
            "/api/login"
    );

    // Маппинг эндпоинтов к требуемым ролям
    private static final Map<String, UserRole> ROLE_REQUIREMENTS = Map.of(
            "/api/admin/users", UserRole.ADMIN,
            "/api/admin/config", UserRole.ADMIN
    );

    @Override
    public void doFilter(HttpExchange exchange, Chain chain) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        // Проверяем, требует ли эндпоинт аутентификации
        if (isPublicEndpoint(path)) {
            logger.debug("Public endpoint accessed: {} {}", method, path);
            chain.doFilter(exchange);
            return;
        }

        // Получаем токен из заголовка Authorization
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        String token = extractToken(authHeader);

        if (token == null || !JwtUtil.validateToken(token)) {
            sendErrorResponse(exchange, 401, "Unauthorized: Invalid or missing token");
            return;
        }

        if (JwtUtil.isTokenExpired(token)) {
            sendErrorResponse(exchange, 401, "Unauthorized: Token expired");
            return;
        }

        // Извлекаем роль из токена
        String role = JwtUtil.extractRole(token);
        UserRole userRole = UserRole.fromString(role);

        // Проверяем права доступа
        if (requiresAdminRole(path) && userRole != UserRole.ADMIN) {
            sendErrorResponse(exchange, 403, "Forbidden: Admin access required");
            return;
        }

        // Сохраняем информацию о пользователе в атрибуты запроса
        String login = JwtUtil.extractLogin(token);
        exchange.setAttribute("userId", login);
        exchange.setAttribute("userRole", role);

        logger.info("Authenticated request: {} {} by user: {}", method, path, login);
        chain.doFilter(exchange);
    }

    private boolean isPublicEndpoint(String path) {
        return PUBLIC_ENDPOINTS.stream().anyMatch(path::startsWith);
    }

    private boolean requiresAdminRole(String path) {
        return ROLE_REQUIREMENTS.keySet().stream().anyMatch(path::startsWith);
    }

    private String extractToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        return authHeader.substring(7);
    }

    private void sendErrorResponse(HttpExchange exchange, int statusCode, String message) throws IOException {
        String response = String.format("{\"status\":\"error\",\"message\":\"%s\"}", message);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, response.getBytes().length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }

    @Override
    public String description() {
        return "Authentication and Authorization Filter";
    }
}