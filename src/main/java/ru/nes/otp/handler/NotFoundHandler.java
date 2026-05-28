package ru.nes.otp.handler;

import com.sun.net.httpserver.HttpExchange;
import ru.nes.otp.model.dto.response.ErrorResponse;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * Обработчик для несуществующих эндпоинтов (404).
 */
public class NotFoundHandler extends BaseHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        long startTime = System.currentTimeMillis();
        exchange.setAttribute("startTime", startTime);

        logRequest(exchange, null);

        ErrorResponse errorResponse = new ErrorResponse(
                "Endpoint not found: " + method + " " + path,
                404,
                path
        );

        String response = gson.toJson(errorResponse);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(404, response.getBytes(StandardCharsets.UTF_8).length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes(StandardCharsets.UTF_8));
        }

        logResponse(exchange, 404, System.currentTimeMillis() - startTime);
    }
}