package server.database.dao;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import server.database.JdbcTemplate;

import java.util.Optional;

@Singleton
public class AuthTokenDao {

    private final JdbcTemplate jdbc;

    @Inject
    public AuthTokenDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void save(String token, int userId) {
        jdbc.update(
                "INSERT INTO auth_tokens (token, user_id) VALUES (?, ?)",
                token, userId
        );
    }

    public Optional<Integer> findUserIdByToken(String token) {
        return jdbc.queryOne(
                "SELECT user_id FROM auth_tokens WHERE token = ?",
                rs -> rs.getInt("user_id"),
                token
        );
    }

    public boolean existsByToken(String token) {
        return findUserIdByToken(token).isPresent();
    }

    public void deleteByToken(String token) {
        jdbc.update("DELETE FROM auth_tokens WHERE token = ?", token);
    }

    public void deleteByUserId(int userId) {
        jdbc.update("DELETE FROM auth_tokens WHERE user_id = ?", userId);
    }
}