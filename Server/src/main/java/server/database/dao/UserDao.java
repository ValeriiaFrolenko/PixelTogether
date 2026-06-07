package server.database.dao;

import com.google.inject.Inject;
import server.database.JdbcTemplate;
import server.database.model.User;

import java.util.Optional;

public class UserDao {

    private final JdbcTemplate jdbc;

    @Inject
    public UserDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public long save(User user) {
        return jdbc.insert(
                "INSERT INTO users (username, password, role) VALUES (?, ?, ?)",
                user.username(), user.password(), user.role()
        );
    }

    public int deleteById(int id) {
        return jdbc.update(
                "DELETE FROM users WHERE id = ?",
                id
        );
    }

    public int updateUsername(int id, String username) {
        return jdbc.update(
                "UPDATE users SET username = ? WHERE id = ?",
                username, id
        );
    }

    public int updatePassword(int id, String password) {
        return jdbc.update(
                "UPDATE users SET password = ? WHERE id = ?",
                password, id
        );
    }

    public Optional<User> findByUsername(String username) {
        return jdbc.queryOne(
                "SELECT * FROM users WHERE username = ?",
                rs -> User.builder()
                        .id(rs.getInt("id"))
                        .username(rs.getString("username"))
                        .password(rs.getString("password"))
                        .role(rs.getString("role"))
                        .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                        .build(),
                username
        );
    }

    public Optional<User> findById(int id) {
        return jdbc.queryOne(
                "SELECT * FROM users WHERE id = ?",
                rs -> User.builder()
                        .id(rs.getInt("id"))
                        .username(rs.getString("username"))
                        .password(rs.getString("password"))
                        .role(rs.getString("role"))
                        .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                        .build(),
                id
        );
    }
}