package server.database.dao;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import server.database.JdbcTemplate;
import server.database.model.Room;

import java.util.List;
import java.util.Optional;

@Singleton
public class RoomDao {

    private final JdbcTemplate jdbc;

    @Inject
    public RoomDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public long save(Room room) {
        return jdbc.insert(
                "INSERT INTO rooms (name, code, owner_id, is_private, canvas_w, canvas_h, expires_at) VALUES (?, ?, ?, ?, ?, ?, ?)",
                room.name(), room.code(), room.ownerId(), room.isPrivate(),
                room.canvasW(), room.canvasH(), room.expiresAt()
        );
    }

    public Optional<Room> findById(int id) {
        return jdbc.queryOne(
                "SELECT * FROM rooms WHERE id = ?",
                rs -> Room.builder()
                        .id(rs.getInt("id"))
                        .name(rs.getString("name"))
                        .code(rs.getString("code"))
                        .ownerId(rs.getInt("owner_id"))
                        .isPrivate(rs.getBoolean("is_private"))
                        .canvasW(rs.getInt("canvas_w"))
                        .canvasH(rs.getInt("canvas_h"))
                        .canvasState(rs.getBytes("canvas_state"))
                        .expiresAt(rs.getTimestamp("expires_at").toLocalDateTime())
                        .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                        .build(),
                id
        );
    }

    public Optional<Room> findByCode(String code) {
        return jdbc.queryOne(
                "SELECT * FROM rooms WHERE code = ?",
                rs -> Room.builder()
                        .id(rs.getInt("id"))
                        .name(rs.getString("name"))
                        .code(rs.getString("code"))
                        .ownerId(rs.getInt("owner_id"))
                        .isPrivate(rs.getBoolean("is_private"))
                        .canvasW(rs.getInt("canvas_w"))
                        .canvasH(rs.getInt("canvas_h"))
                        .canvasState(rs.getBytes("canvas_state"))
                        .expiresAt(rs.getTimestamp("expires_at").toLocalDateTime())
                        .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                        .build(),
                code
        );
    }

    public List<Room> findAllPublic() {
        return jdbc.query(
                "SELECT * FROM rooms WHERE is_private = FALSE AND expires_at > NOW()",
                rs -> Room.builder()
                        .id(rs.getInt("id"))
                        .name(rs.getString("name"))
                        .ownerId(rs.getInt("owner_id"))
                        .isPrivate(false)
                        .canvasW(rs.getInt("canvas_w"))
                        .canvasH(rs.getInt("canvas_h"))
                        .expiresAt(rs.getTimestamp("expires_at").toLocalDateTime())
                        .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                        .build()
        );
    }

    public int updateCanvasState(int id, byte[] canvasState) {
        return jdbc.update(
                "UPDATE rooms SET canvas_state = ? WHERE id = ?",
                canvasState, id
        );
    }

    public int deleteById(int id) {
        return jdbc.update(
                "DELETE FROM rooms WHERE id = ?",
                id
        );
    }

    public int deleteExpired() {
        return jdbc.update(
                "DELETE FROM rooms WHERE expires_at <= NOW()"
        );
    }
}