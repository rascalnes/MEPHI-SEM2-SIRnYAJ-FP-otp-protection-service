package ru.nes.otp.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Конфигурация подключения к базе данных.
 * Использует HikariCP для пула соединений.
 */
public class DatabaseConfig {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);
    private static DatabaseConfig instance;
    private HikariDataSource dataSource;
    private final Properties properties;

    private DatabaseConfig() {
        this.properties = new Properties();
        loadProperties();
        initDataSource();
        initDatabase();
    }

    public static synchronized DatabaseConfig getInstance() {
        if (instance == null) {
            instance = new DatabaseConfig();
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

    private void initDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(properties.getProperty("db.url"));
        config.setUsername(properties.getProperty("db.username"));
        config.setPassword(properties.getProperty("db.password"));
        config.setMaximumPoolSize(Integer.parseInt(properties.getProperty("db.maxPoolSize", "10")));
        config.setMinimumIdle(Integer.parseInt(properties.getProperty("db.minIdle", "2")));
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);

        // Настройки для PostgreSQL
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        this.dataSource = new HikariDataSource(config);
        logger.info("Database connection pool initialized");
    }

    private void initDatabase() {
        try (Connection conn = getConnection()) {
            // Выполнение init.sql
            String initSql = readInitSql();
            if (initSql != null && !initSql.isEmpty()) {
                try (var stmt = conn.createStatement()) {
                    // Разбиваем на отдельные statements
                    String[] statements = initSql.split(";");
                    for (String sql : statements) {
                        String trimmed = sql.trim();
                        if (!trimmed.isEmpty()) {
                            try {
                                stmt.execute(trimmed);
                                logger.debug("Executed SQL: {}", trimmed.substring(0, Math.min(100, trimmed.length())));
                            } catch (SQLException e) {
                                logger.warn("SQL execution warning: {}", e.getMessage());
                            }
                        }
                    }
                }
            }
            logger.info("Database initialization completed");
        } catch (SQLException e) {
            logger.error("Failed to initialize database", e);
            throw new RuntimeException("Database initialization failed", e);
        }
    }

    private String readInitSql() {
        try (InputStream input = getClass().getClassLoader()
                .getResourceAsStream("db/init.sql")) {
            if (input == null) {
                logger.warn("init.sql not found, skipping initialization");
                return null;
            }
            return new String(input.readAllBytes());
        } catch (IOException e) {
            logger.error("Failed to read init.sql", e);
            return null;
        }
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public void closePool() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            logger.info("Database connection pool closed");
        }
    }
}