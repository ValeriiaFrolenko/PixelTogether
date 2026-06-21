package server.database.dao;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import server.database.JdbcTemplate;
import server.database.QueryBuilder;
import server.database.model.SavedWork;

import java.util.List;
import java.util.Optional;

@Singleton
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
                "SELECT sw.*, u.username AS owner_username FROM saved_works sw JOIN users u ON sw.owner_id = u.id WHERE sw.id = ?",
                rs -> SavedWork.builder()
                        .id(rs.getInt("id"))
                        .ownerId(rs.getInt("owner_id"))
                        .ownerUsername(rs.getString("owner_username"))
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

    public List<SavedWork> findPublicFiltered(String title, String ownerUsername) {
        QueryBuilder qb = new QueryBuilder(
                "SELECT sw.*, u.username AS owner_username FROM saved_works sw JOIN users u ON sw.owner_id = u.id WHERE sw.is_public = TRUE"
        );
        if (title != null) {
            qb.and("LOWER(sw.title) LIKE LOWER(?)", "%" + title + "%");
        }
        if (ownerUsername != null) {
            qb.and("LOWER(u.username) LIKE LOWER(?)", "%" + ownerUsername + "%");
        }
        qb.orderBy("saved_at", QueryBuilder.Direction.DESC);

        return jdbc.query(
                qb.sql(),
                rs -> SavedWork.builder()
                        .id(rs.getInt("id"))
                        .ownerId(rs.getInt("owner_id"))
                        .ownerUsername(rs.getString("owner_username"))
                        .title(rs.getString("title"))
                        .isPublic(true)
                        .imageData(rs.getBytes("image_data"))
                        .canvasW(rs.getInt("canvas_w"))
                        .canvasH(rs.getInt("canvas_h"))
                        .savedAt(rs.getTimestamp("saved_at").toLocalDateTime())
                        .build(),
                qb.params()
        );
    }

    public List<SavedWork> findAllByOwner(int ownerId) {
        return jdbc.query(
                "SELECT sw.*, u.username AS owner_username FROM saved_works sw JOIN users u ON sw.owner_id = u.id WHERE sw.owner_id = ? ORDER BY sw.saved_at DESC",
                rs -> SavedWork.builder()
                        .id(rs.getInt("id"))
                        .ownerId(rs.getInt("owner_id"))
                        .ownerUsername(rs.getString("owner_username"))
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
