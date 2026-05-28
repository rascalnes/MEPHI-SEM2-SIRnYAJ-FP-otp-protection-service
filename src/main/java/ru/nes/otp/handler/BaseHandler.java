package ru.nes.otp.handler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.nes.otp.model.dto.response.ErrorResponse;
import ru.nes.otp.model.dto.response.SuccessResponse;
import ru.nes.otp.util.JsonUtil;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

/**
 * Базовый класс для всех обработчиков с улучшенным логированием.
 */
public abstract class BaseHandler implements HttpHandler {

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    protected final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();

    /**
     * Получение времени начала обработки запроса.
     */
    protected long getStartTime(HttpExchange exchange) {
        Long startTime = (Long) exchange.getAttribute("startTime");
        if (startTime == null) {
            startTime = System.currentTimeMillis();
            exchange.setAttribute("startTime", startTime);
        }
        return startTime;
    }

    /**
     * Чтение тела запроса.
     */
    protected String readBody(HttpExchange exchange) throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining());
        }
    }

    /**
     * Парсинг тела запроса с логированием.
     */
    protected <T> T parseBody(HttpExchange exchange, Class<T> clazz) throws IOException {
        String body = readBody(exchange);
        if (body == null || body.isEmpty()) {
            logRequest(exchange, null);
            return null;
        }

        // Логируем тело запроса (маскируем пароль)
        String logBody = maskSensitiveData(body);
        logRequest(exchange, logBody);

        return gson.fromJson(body, clazz);
    }

    /**
     * Маскирование чувствительных данных в логах.
     */
    private String maskSensitiveData(String body) {
        if (body == null) return null;
        // Маскируем поле password
        return body.replaceAll("\"password\"\\s*:\\s*\"[^\"]*\"", "\"password\":\"***\"");
    }

    /**
     * Логирование входящего запроса.
     */
    protected void logRequest(HttpExchange exchange, String body) {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        String query = exchange.getRequestURI().getQuery();
        String clientIp = getClientIp(exchange);

        StringBuilder logBuilder = new StringBuilder();
        logBuilder.append("→ ").append(method).append(" ").append(path);
        if (query != null && !query.isEmpty()) {
            logBuilder.append("?").append(query);
        }
        logBuilder.append(" [IP: ").append(clientIp).append("]");

        logger.info(logBuilder.toString());

        if (body != null && !body.isEmpty()) {
            logger.debug("Request body: {}", body);
        }
    }

    /**
     * Логирование ответа.
     */
    protected void logResponse(HttpExchange exchange, int statusCode, long processingTimeMs) {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        logger.info("← {} {} - {} ({} ms)", method, path, statusCode, processingTimeMs);
    }

    /**
     * Получение IP клиента.
     */
    protected String getClientIp(HttpExchange exchange) {
        String ip = exchange.getRequestHeaders().getFirst("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = exchange.getRemoteAddress().getAddress().getHostAddress();
        }
        return ip;
    }

    /**
     * Отправка успешного ответа.
     */
    protected void sendSuccess(HttpExchange exchange, Object data) throws IOException {
        long startTime = getStartTime(exchange);
        long processingTime = System.currentTimeMillis() - startTime;

        SuccessResponse<Object> response = new SuccessResponse<>(data);
        String jsonResponse = JsonUtil.toJson(response);

        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(200, jsonResponse.getBytes(StandardCharsets.UTF_8).length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(jsonResponse.getBytes(StandardCharsets.UTF_8));
        }

        logResponse(exchange, 200, processingTime);
    }

    /**
     * Отправка успешного ответа с сообщением.
     */
    protected void sendSuccessMessage(HttpExchange exchange, String message) throws IOException {
        long startTime = getStartTime(exchange);
        long processingTime = System.currentTimeMillis() - startTime;

        SuccessResponse<String> response = new SuccessResponse<>(message);
        String jsonResponse = JsonUtil.toJson(response);

        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(200, jsonResponse.getBytes(StandardCharsets.UTF_8).length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(jsonResponse.getBytes(StandardCharsets.UTF_8));
        }

        logResponse(exchange, 200, processingTime);
    }

    /**
     * Отправка ошибки с указанным статусом.
     */
    protected void sendError(HttpExchange exchange, int statusCode, String message) throws IOException {
        long startTime = getStartTime(exchange);
        long processingTime = System.currentTimeMillis() - startTime;

        ErrorResponse errorResponse = new ErrorResponse(message, statusCode, exchange.getRequestURI().getPath());
        String jsonResponse = JsonUtil.toJson(errorResponse);

        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(statusCode, jsonResponse.getBytes(StandardCharsets.UTF_8).length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(jsonResponse.getBytes(StandardCharsets.UTF_8));
        }

        logResponse(exchange, statusCode, processingTime);
        logger.warn("Error response: {} - {}", statusCode, message);
    }

    /**
     * Отправка ошибки 400 Bad Request.
     */
    protected void sendBadRequest(HttpExchange exchange, String message) throws IOException {
        sendError(exchange, 400, message);
    }

    /**
     * Отправка ошибки 401 Unauthorized.
     */
    protected void sendUnauthorized(HttpExchange exchange, String message) throws IOException {
        sendError(exchange, 401, message);
    }

    /**
     * Отправка ошибки 403 Forbidden.
     */
    protected void sendForbidden(HttpExchange exchange, String message) throws IOException {
        sendError(exchange, 403, message);
    }

    /**
     * Отправка ошибки 404 Not Found.
     */
    protected void sendNotFound(HttpExchange exchange, String message) throws IOException {
        sendError(exchange, 404, message);
    }

    /**
     * Отправка ошибки 409 Conflict.
     */
    protected void sendConflict(HttpExchange exchange, String message) throws IOException {
        sendError(exchange, 409, message);
    }

    /**
     * Отправка ошибки 500 Internal Server Error.
     */
    protected void sendInternalError(HttpExchange exchange, String message) throws IOException {
        sendError(exchange, 500, message);
    }

    protected void setCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
        exchange.getResponseHeaders().set("Access-Control-Max-Age", "86400");
    }

    protected boolean handleOptions(HttpExchange exchange) throws IOException {
        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            setCorsHeaders(exchange);
            exchange.sendResponseHeaders(204, -1);
            return true;
        }
        return false;
    }
}