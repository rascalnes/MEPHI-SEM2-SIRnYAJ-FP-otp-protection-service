package ru.nes.otp.dao;

import ru.nes.otp.model.entity.OtpConfig;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Optional;

/**
 * DAO для работы с конфигурацией OTP.
 */
public class OtpConfigDAO extends BaseDAO<OtpConfig> {

    private OtpConfig mapResultSet(ResultSet rs) throws SQLException {
        OtpConfig config = new OtpConfig();
        config.setId(rs.getLong("id"));
        config.setLifetimeSeconds(rs.getInt("lifetime_seconds"));
        config.setCodeLength(rs.getInt("code_length"));

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        config.setUpdatedAt(updatedAt != null ? updatedAt.toLocalDateTime() : null);
        config.setUpdatedBy(rs.getString("updated_by"));
        return config;
    }

    /**
     * Получение текущей конфигурации (всегда 1 запись).
     */
    public Optional<OtpConfig> getConfig() {
        String sql = "SELECT * FROM otp_config WHERE id = 1";
        return executeQuerySingle(sql, this::mapResultSet);
    }

    /**
     * Обновление конфигурации.
     */
    public boolean updateConfig(int lifetimeSeconds, int codeLength, String updatedBy) {
        String sql = "UPDATE otp_config SET lifetime_seconds = ?, code_length = ?, updated_at = CURRENT_TIMESTAMP, updated_by = ? WHERE id = 1";
        int rowsAffected = executeUpdate(sql, lifetimeSeconds, codeLength, updatedBy);
        return rowsAffected > 0;
    }

    /**
     * Инициализация конфигурации по умолчанию (если нет).
     */
    public void initDefaultConfig() {
        Optional<OtpConfig> existing = getConfig();
        if (existing.isEmpty()) {
            String sql = "INSERT INTO otp_config (id, lifetime_seconds, code_length, updated_by) VALUES (1, 300, 6, 'system')";
            executeUpdate(sql);
            logger.info("Default OTP configuration initialized");
        }
    }
}