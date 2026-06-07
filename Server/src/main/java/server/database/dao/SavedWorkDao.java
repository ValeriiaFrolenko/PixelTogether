package server.database.dao;

import com.google.inject.Inject;
import server.database.JdbcTemplate;
import server.database.model.SavedWork;

import java.util.List;
import java.util.Optional;

public class SavedWorkDao {

    private final JdbcTemplate jdbc;

    @Inject
    public SavedWorkDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public long save(SavedWork work) {
        return jdbc.insert(
                "INSERT INTO saved_works (owner_id, title, is_public, image_data, canvas_w, canvas_h) VALUES (?, ?, ?, ?, ?, ?)",
                work.ownerId(), work.title(), work.isPublic(), work.imageData(), work.canvasW(), work.canvasH()
        );
    }

    public Optional<SavedWork> findById(int id) {
        return jdbc.queryOne(
                "SELECT * FROM saved_works WHERE id = ?",
                rs -> SavedWork.builder()
                        .id(rs.getInt("id"))
                        .ownerId(rs.getInt("owner_id"))
                        .title(rs.getString("title"))
                        .isPublic(rs.getBoolean("is_public"))
                        .imageData(rs.getBytes("image_data"))
                        .canvasW(rs.getInt("canvas_w"))
                        .canvasH(rs.getInt("canvas_h"))
                        .savedAt(rs.getTimestamp("saved_at").toLocalDateTime())
                        .build(),
                id
        );
    }

    public List<SavedWork> findAllPublic() {
        return jdbc.query(
                "SELECT * FROM saved_works WHERE is_public = TRUE ORDER BY saved_at DESC",
                rs -> SavedWork.builder()
                        .id(rs.getInt("id"))
                        .ownerId(rs.getInt("owner_id"))
                        .title(rs.getString("title"))
                        .isPublic(true)
                        .imageData(rs.getBytes("image_data"))
                        .canvasW(rs.getInt("canvas_w"))
                        .canvasH(rs.getInt("canvas_h"))
                        .savedAt(rs.getTimestamp("saved_at").toLocalDateTime())
                        .build()
        );
    }

    public List<SavedWork> findAllByOwner(int ownerId) {
        return jdbc.query(
                "SELECT * FROM saved_works WHERE owner_id = ? ORDER BY saved_at DESC",
                rs -> SavedWork.builder()
                        .id(rs.getInt("id"))
                        .ownerId(rs.getInt("owner_id"))
                        .title(rs.getString("title"))
                        .isPublic(rs.getBoolean("is_public"))
                        .imageData(rs.getBytes("image_data"))
                        .canvasW(rs.getInt("canvas_w"))
                        .canvasH(rs.getInt("canvas_h"))
                        .savedAt(rs.getTimestamp("saved_at").toLocalDateTime())
                        .build(),
                ownerId
        );
    }

    public int updateVisibility(int id, boolean isPublic) {
        return jdbc.update(
                "UPDATE saved_works SET is_public = ? WHERE id = ?",
                isPublic, id
        );
    }

    public int deleteById(int id) {
        return jdbc.update(
                "DELETE FROM saved_works WHERE id = ?",
                id
        );
    }
}