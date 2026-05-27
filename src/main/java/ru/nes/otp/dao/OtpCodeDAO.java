package ru.nes.otp.dao;

import ru.nes.otp.model.entity.OtpCode;
import ru.nes.otp.model.entity.enums.ChannelType;
import ru.nes.otp.model.entity.enums.OtpStatus;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * DAO для работы с OTP-кодами.
 */
public class OtpCodeDAO extends BaseDAO<OtpCode> {

    private OtpCode mapResultSet(ResultSet rs) throws SQLException {
        OtpCode otpCode = new OtpCode();
        otpCode.setId(rs.getLong("id"));
        otpCode.setUserId(rs.getLong("user_id"));
        otpCode.setOperationId(rs.getString("operation_id"));
        otpCode.setCodeHash(rs.getString("code_hash"));
        otpCode.setStatus(OtpStatus.fromString(rs.getString("status")));
        otpCode.setChannel(ChannelType.fromString(rs.getString("channel")));

        Timestamp createdAt = rs.getTimestamp("created_at");
        otpCode.setCreatedAt(createdAt != null ? createdAt.toLocalDateTime() : null);

        Timestamp expiresAt = rs.getTimestamp("expires_at");
        otpCode.setExpiresAt(expiresAt != null ? expiresAt.toLocalDateTime() : null);

        Timestamp validatedAt = rs.getTimestamp("validated_at");
        otpCode.setValidatedAt(validatedAt != null ? validatedAt.toLocalDateTime() : null);

        return otpCode;
    }

    /**
     * Сохранение нового OTP-кода.
     */
    public Long save(OtpCode otpCode) {
        String sql = "INSERT INTO otp_codes (user_id, operation_id, code_hash, status, channel, expires_at) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        return executeInsert(sql,
                otpCode.getUserId(),
                otpCode.getOperationId(),
                otpCode.getCodeHash(),
                otpCode.getStatus().name(),
                otpCode.getChannel().name(),
                otpCode.getExpiresAt()
        );
    }

    /**
     * Поиск OTP-кода по operationId.
     */
    public Optional<OtpCode> findByOperationId(String operationId) {
        String sql = "SELECT * FROM otp_codes WHERE operation_id = ?";
        return executeQuerySingle(sql, this::mapResultSet, operationId);
    }

    /**
     * Поиск активного OTP-кода по operationId.
     */
    public Optional<OtpCode> findActiveByOperationId(String operationId) {
        String sql = "SELECT * FROM otp_codes WHERE operation_id = ? AND status = 'ACTIVE' AND expires_at > CURRENT_TIMESTAMP";
        return executeQuerySingle(sql, this::mapResultSet, operationId);
    }

    /**
     * Обновление статуса OTP-кода.
     */
    public boolean updateStatus(Long id, OtpStatus status, LocalDateTime validatedAt) {
        String sql = "UPDATE otp_codes SET status = ?, validated_at = ? WHERE id = ?";
        int rowsAffected = executeUpdate(sql, status.name(), validatedAt, id);
        return rowsAffected > 0;
    }

    /**
     * Получение всех просроченных ACTIVE кодов.
     */
    public List<OtpCode> findExpiredActiveCodes() {
        String sql = "SELECT * FROM otp_codes WHERE status = 'ACTIVE' AND expires_at < CURRENT_TIMESTAMP";
        return executeQuery(sql, this::mapResultSet);
    }

    /**
     * Массовое обновление статуса на EXPIRED для просроченных кодов.
     */
    public int expireOldCodes() {
        String sql = "UPDATE otp_codes SET status = 'EXPIRED' WHERE status = 'ACTIVE' AND expires_at < CURRENT_TIMESTAMP";
        return executeUpdate(sql);
    }

    /**
     * Удаление всех OTP-кодов пользователя.
     */
    public int deleteByUserId(Long userId) {
        String sql = "DELETE FROM otp_codes WHERE user_id = ?";
        return executeUpdate(sql, userId);
    }
}