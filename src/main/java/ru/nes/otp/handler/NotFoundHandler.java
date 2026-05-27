package ru.nes.otp.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Обработчик для несуществующих эндпоинтов (404).
 */
public class NotFoundHandler implements HttpHandler {

    private static final Logger logger = LoggerFactory.getLogger(NotFoundHandler.class);

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        logger.warn("404 Not Found: {} {}", method, path);

        String response = "{\"status\":\"error\",\"message\":\"Endpoint not found\"}";
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(404, response.getBytes().length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }
}