package ru.nes.otp.handler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

/**
 * Базовый класс для всех обработчиков.
 */
public abstract class BaseHandler implements HttpHandler {

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    protected final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    protected String readBody(HttpExchange exchange) throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining());
        }
    }

    protected <T> T parseBody(HttpExchange exchange, Class<T> clazz) throws IOException {
        String body = readBody(exchange);
        if (body == null || body.isEmpty()) {
            return null;
        }
        return gson.fromJson(body, clazz);
    }

    protected void sendResponse(HttpExchange exchange, int statusCode, Object responseBody) throws IOException {
        String response = gson.toJson(responseBody);
        sendRawResponse(exchange, statusCode, response);
    }

    protected void sendRawResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.getResponseHeaders().set("charset", "utf-8");
        exchange.sendResponseHeaders(statusCode, response.getBytes(StandardCharsets.UTF_8).length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes(StandardCharsets.UTF_8));
        }
    }

    protected void sendErrorResponse(HttpExchange exchange, int statusCode, String message) throws IOException {
        String response = String.format("{\"status\":\"error\",\"message\":\"%s\"}", message);
        sendRawResponse(exchange, statusCode, response);
    }

    protected void sendSuccessResponse(HttpExchange exchange, String message) throws IOException {
        String response = String.format("{\"status\":\"success\",\"message\":\"%s\"}", message);
        sendRawResponse(exchange, 200, response);
    }

    protected void sendSuccessResponse(HttpExchange exchange, Object data) throws IOException {
        String response = gson.toJson(data);
        sendRawResponse(exchange, 200, response);
    }

    protected void setCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
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