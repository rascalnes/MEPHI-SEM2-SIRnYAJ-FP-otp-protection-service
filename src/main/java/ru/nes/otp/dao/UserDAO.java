package ru.nes.otp.dao;

import ru.nes.otp.model.entity.User;
import ru.nes.otp.model.entity.enums.UserRole;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

/**
 * DAO для работы с пользователями.
 */
public class UserDAO extends BaseDAO<User> {

    private User mapResultSet(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setLogin(rs.getString("login"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setRole(UserRole.fromString(rs.getString("role")));
        Timestamp timestamp = rs.getTimestamp("created_at");
        user.setCreatedAt(timestamp != null ? timestamp.toLocalDateTime() : null);
        return user;
    }

    /**
     * Сохранение нового пользователя.
     */
    public Long save(User user) {
        String sql = "INSERT INTO users (login, password_hash, role) VALUES (?, ?, ?)";
        return executeInsert(sql, user.getLogin(), user.getPasswordHash(), user.getRole().name());
    }

    /**
     * Поиск пользователя по логину.
     */
    public Optional<User> findByLogin(String login) {
        String sql = "SELECT * FROM users WHERE login = ?";
        return executeQuerySingle(sql, this::mapResultSet, login);
    }

    /**
     * Поиск пользователя по ID.
     */
    public Optional<User> findById(Long id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        return executeQuerySingle(sql, this::mapResultSet, id);
    }

    /**
     * Проверка существования администратора.
     */
    public boolean existsAdmin() {
        String sql = "SELECT COUNT(*) FROM users WHERE role = 'ADMIN'";
        long count = executeScalar(sql);
        return count > 0;
    }

    /**
     * Получение всех пользователей (кроме администраторов).
     */
    public List<User> findAllNonAdmins() {
        String sql = "SELECT * FROM users WHERE role != 'ADMIN' ORDER BY id";
        return executeQuery(sql, this::mapResultSet);
    }

    /**
     * Удаление пользователя по ID.
     */
    public boolean deleteById(Long id) {
        String sql = "DELETE FROM users WHERE id = ?";
        int rowsAffected = executeUpdate(sql, id);
        return rowsAffected > 0;
    }

    /**
     * Получение всех пользователей.
     */
    public List<User> findAll() {
        String sql = "SELECT * FROM users ORDER BY id";
        return executeQuery(sql, this::mapResultSet);
    }
}