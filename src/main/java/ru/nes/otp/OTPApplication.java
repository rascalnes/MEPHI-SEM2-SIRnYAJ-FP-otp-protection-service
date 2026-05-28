package ru.nes.otp;

import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.nes.otp.config.ServerConfig;
import ru.nes.otp.handler.AdminHandler;
import ru.nes.otp.handler.AuthHandler;
import ru.nes.otp.handler.NotFoundHandler;
import ru.nes.otp.handler.UserHandler;
import ru.nes.otp.security.AuthFilter;
import ru.nes.otp.service.scheduler.OtpExpiryScheduler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

/**
 * Главный класс приложения.
 */
public class OTPApplication {

    private static final Logger logger = LoggerFactory.getLogger(OTPApplication.class);

    public static void main(String[] args) {
        try {
            logger.info("Starting OTP Protection Service...");

            // Загрузка конфигурации сервера
            ServerConfig serverConfig = ServerConfig.getInstance();

            // Создание HTTP сервера
            HttpServer server = HttpServer.create(
                    new InetSocketAddress(serverConfig.getPort()),
                    serverConfig.getBacklog()
            );

            // Создание фильтра аутентификации
            AuthFilter authFilter = new AuthFilter();

            // Публичные эндпоинты (тоже с фильтром, но фильтр пропустит их)
            var authContext = server.createContext("/api/register", new AuthHandler());
            authContext.getFilters().add(authFilter);

            var loginContext = server.createContext("/api/login", new AuthHandler());
            loginContext.getFilters().add(authFilter);

            // Пользовательские эндпоинты (с фильтром)
            var userContext = server.createContext("/api/otp", new UserHandler());
            userContext.getFilters().add(authFilter);

            // Административные эндпоинты (с фильтром)
            var adminContext = server.createContext("/api/admin", new AdminHandler());
            adminContext.getFilters().add(authFilter);

            // 404 для всех остальных путей
            server.createContext("/", new NotFoundHandler());

            // Настройка executor с пулом потоков
            server.setExecutor(Executors.newCachedThreadPool());

            // Запуск планировщика для просроченных OTP-кодов
            OtpExpiryScheduler.start();

            // Запуск сервера
            server.start();

            logger.info("OTP Service started successfully on port {}", serverConfig.getPort());
            logger.info("Server is ready to accept requests");
            logger.info("Endpoints:");
            logger.info("  POST /api/register - Register new user");
            logger.info("  POST /api/login - Login and get JWT token");
            logger.info("  POST /api/otp/generate - Generate OTP code");
            logger.info("  POST /api/otp/validate - Validate OTP code");
            logger.info("  GET /api/admin/users - List all users (Admin only)");
            logger.info("  DELETE /api/admin/users/{id} - Delete user (Admin only)");
            logger.info("  GET /api/admin/config - Get OTP config (Admin only)");
            logger.info("  PUT /api/admin/config - Update OTP config (Admin only)");
            logger.info("");
            logger.info("OTP Expiry Scheduler is running (checks every 30 seconds)");

        } catch (IOException e) {
            logger.error("Failed to start OTP Service", e);
            System.exit(1);
        }
    }
}