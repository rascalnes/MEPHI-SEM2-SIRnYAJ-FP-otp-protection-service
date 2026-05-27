package ru.nes.otp.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.nes.otp.config.DatabaseConfig;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Базовый DAO класс с общими методами для работы с БД.
 */
public abstract class BaseDAO<T> {

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    protected final DatabaseConfig dbConfig;

    protected BaseDAO() {
        this.dbConfig = DatabaseConfig.getInstance();
    }

    /**
     * Получение соединения из пула.
     */
    protected Connection getConnection() throws SQLException {
        return dbConfig.getConnection();
    }

    /**
     * Интерфейс для маппинга ResultSet с поддержкой SQLException.
     */
    @FunctionalInterface
    protected interface ResultSetMapper<T> {
        T map(ResultSet rs) throws SQLException;
    }

    /**
     * Выполнение UPDATE/INSERT/DELETE запроса.
     */
    protected int executeUpdate(String sql, Object... params) {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            setParameters(stmt, params);
            int result = stmt.executeUpdate();
            logger.debug("Executed update: {} - rows affected: {}", sql, result);
            return result;

        } catch (SQLException e) {
            logger.error("Failed to execute update: {}", sql, e);
            throw new RuntimeException("Database operation failed", e);
        }
    }

    /**
     * Выполнение INSERT запроса с возвратом сгенерированного ключа.
     */
    protected Long executeInsert(String sql, Object... params) {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            setParameters(stmt, params);
            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Insert failed, no rows affected");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    long id = generatedKeys.getLong(1);
                    logger.debug("Executed insert: {} - generated id: {}", sql, id);
                    return id;
                } else {
                    throw new SQLException("Insert failed, no ID obtained");
                }
            }

        } catch (SQLException e) {
            logger.error("Failed to execute insert: {}", sql, e);
            throw new RuntimeException("Database operation failed", e);
        }
    }

    /**
     * Выполнение SELECT запроса с маппингом результата в список объектов.
     */
    protected List<T> executeQuery(String sql, ResultSetMapper<T> mapper, Object... params) {
        List<T> results = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            setParameters(stmt, params);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    results.add(mapper.map(rs));
                }
            }

            logger.debug("Executed query: {} - returned {} rows", sql, results.size());
            return results;

        } catch (SQLException e) {
            logger.error("Failed to execute query: {}", sql, e);
            throw new RuntimeException("Database operation failed", e);
        }
    }

    /**
     * Выполнение SELECT запроса с маппингом одного результата.
     */
    protected Optional<T> executeQuerySingle(String sql, ResultSetMapper<T> mapper, Object... params) {
        List<T> results = executeQuery(sql, mapper, params);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    /**
     * Установка параметров в PreparedStatement.
     */
    private void setParameters(PreparedStatement stmt, Object[] params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            Object param = params[i];
            int index = i + 1;

            if (param == null) {
                stmt.setNull(index, Types.NULL);
            } else if (param instanceof String) {
                stmt.setString(index, (String) param);
            } else if (param instanceof Integer) {
                stmt.setInt(index, (Integer) param);
            } else if (param instanceof Long) {
                stmt.setLong(index, (Long) param);
            } else if (param instanceof Boolean) {
                stmt.setBoolean(index, (Boolean) param);
            } else if (param instanceof Timestamp) {
                stmt.setTimestamp(index, (Timestamp) param);
            } else if (param instanceof java.time.LocalDateTime) {
                stmt.setTimestamp(index, Timestamp.valueOf((java.time.LocalDateTime) param));
            } else {
                stmt.setObject(index, param);
            }
        }
    }

    /**
     * Закрытие ресурсов.
     */
    protected void close(ResultSet rs, Statement stmt, Connection conn) {
        try {
            if (rs != null) rs.close();
        } catch (SQLException e) {
            logger.warn("Failed to close ResultSet", e);
        }
        try {
            if (stmt != null) stmt.close();
        } catch (SQLException e) {
            logger.warn("Failed to close Statement", e);
        }
        try {
            if (conn != null) conn.close();
        } catch (SQLException e) {
            logger.warn("Failed to close Connection", e);
        }
    }

    /**
     * Выполнение запроса, возвращающего одно скалярное значение.
     */
    protected long executeScalar(String sql, Object... params) {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            setParameters(stmt, params);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
                return 0;
            }

        } catch (SQLException e) {
            logger.error("Failed to execute scalar query: {}", sql, e);
            throw new RuntimeException("Database operation failed", e);
        }
    }
}