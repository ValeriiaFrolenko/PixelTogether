package server.database;

import com.google.inject.Inject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {

    private final ConnectionProvider connectionProvider;

    @Inject
    public DatabaseInitializer(ConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    public void initialize() {
        String sql = loadSql();
        try (Connection conn = connectionProvider.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            throw new JdbcTemplate.DatabaseException("Failed to initialize server.database", e);
        }
    }

    private String loadSql() {
        try (InputStream is = getClass().getResourceAsStream("/init.sql")) {
            if (is == null) throw new JdbcTemplate.DatabaseException("init.sql not found");
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new JdbcTemplate.DatabaseException("Failed to load init.sql", e);
        }
    }
}