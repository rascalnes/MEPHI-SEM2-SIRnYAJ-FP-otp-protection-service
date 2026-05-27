package ru.nes.otp.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Конфигурация HTTP-сервера.
 */
public class ServerConfig {

    private static ServerConfig instance;
    private final Properties properties;

    private int port;
    private int backlog;

    private ServerConfig() {
        this.properties = new Properties();
        loadProperties();
        parseConfig();
    }

    public static synchronized ServerConfig getInstance() {
        if (instance == null) {
            instance = new ServerConfig();
        }
        return instance;
    }

    private void loadProperties() {
        try (InputStream input = getClass().getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (input == null) {
                throw new RuntimeException("application.properties not found");
            }
            properties.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load application.properties", e);
        }
    }

    private void parseConfig() {
        this.port = Integer.parseInt(properties.getProperty("server.port", "8080"));
        this.backlog = Integer.parseInt(properties.getProperty("server.backlog", "50"));
    }

    public int getPort() {
        return port;
    }

    public int getBacklog() {
        return backlog;
    }
}