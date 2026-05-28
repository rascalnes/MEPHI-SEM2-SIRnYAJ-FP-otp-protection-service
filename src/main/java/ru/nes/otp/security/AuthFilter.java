package ru.nes.otp.security;

import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.nes.otp.model.entity.enums.UserRole;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
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
            "/api/admin/config", UserRole.ADMIN,
            "/api/otp/generate", UserRole.USER,
            "/api/otp/validate", UserRole.USER
    );

    @Override
    public void doFilter(HttpExchange exchange, Chain chain) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();
        String clientIp = exchange.getRemoteAddress().getAddress().getHostAddress();

        // Устанавливаем время начала обработки для ВСЕХ запросов
        exchange.setAttribute("startTime", System.currentTimeMillis());

        // Проверяем, требует ли эндпоинт аутентификации
        if (isPublicEndpoint(path)) {
            logger.debug("Public endpoint accessed: {} {} from {}", method, path, clientIp);
            chain.doFilter(exchange);
            return;
        }

        // Получаем токен из заголовка Authorization
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        String token = extractToken(authHeader);

        if (token == null || !JwtUtil.validateToken(token)) {
            logger.warn("Unauthorized access attempt to {} {} from {} - Invalid or missing token",
                    method, path, clientIp);
            sendErrorResponse(exchange, 401, "Unauthorized: Invalid or missing token");
            return;
        }

        if (JwtUtil.isTokenExpired(token)) {
            logger.warn("Unauthorized access attempt to {} {} from {} - Token expired", method, path, clientIp);
            sendErrorResponse(exchange, 401, "Unauthorized: Token expired");
            return;
        }

        // Извлекаем роль из токена
        String login = JwtUtil.extractLogin(token);
        String role = JwtUtil.extractRole(token);
        UserRole userRole = UserRole.fromString(role);

        // Проверяем права доступа для protected эндпоинтов
        UserRole requiredRole = getRequiredRole(path);
        if (requiredRole != null) {
            if (requiredRole == UserRole.ADMIN && userRole != UserRole.ADMIN) {
                logger.warn("Forbidden access attempt to {} {} by user: {} (role: {}) - Admin required",
                        method, path, login, role);
                sendErrorResponse(exchange, 403, "Forbidden: Admin access required");
                return;
            }
        }

        // Сохраняем информацию о пользователе в атрибуты запроса
        exchange.setAttribute("userId", login);
        exchange.setAttribute("userRole", role);

        logger.info("Authenticated request: {} {} by {} (role: {}) from {}",
                method, path, login, role, clientIp);

        chain.doFilter(exchange);
    }

    private boolean isPublicEndpoint(String path) {
        return PUBLIC_ENDPOINTS.stream().anyMatch(path::startsWith);
    }

    private UserRole getRequiredRole(String path) {
        for (Map.Entry<String, UserRole> entry : ROLE_REQUIREMENTS.entrySet()) {
            if (path.startsWith(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }

    private String extractToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        return authHeader.substring(7);
    }

    private void sendErrorResponse(HttpExchange exchange, int statusCode, String message) throws IOException {
        String response = String.format("{\"status\":\"error\",\"code\":%d,\"message\":\"%s\"}", statusCode, message);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, response.getBytes(StandardCharsets.UTF_8).length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes(StandardCharsets.UTF_8));
        }
    }

    @Override
    public String description() {
        return "Authentication and Authorization Filter";
    }
}