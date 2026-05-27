package ru.nes.otp;

import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.nes.otp.config.ServerConfig;
import ru.nes.otp.handler.NotFoundHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

/**
 * Главный класс приложения.
 * Точка входа для OTP-сервиса защиты операций.
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

            // Настройка контекстов (пока только заглушка для 404)
            server.createContext("/", new NotFoundHandler());

            // Настройка executor с пулом потоков
            server.setExecutor(Executors.newCachedThreadPool());

            // Запуск сервера
            server.start();

            logger.info("OTP Service started successfully on port {}", serverConfig.getPort());
            logger.info("Server is ready to accept requests");

        } catch (IOException e) {
            logger.error("Failed to start OTP Service", e);
            System.exit(1);
        }
    }
}