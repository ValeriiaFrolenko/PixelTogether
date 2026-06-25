package server.database;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class QueryBuilderTest {

    @Test
    void baseQuery_noConditions_returnsSqlAsIs() {
        QueryBuilder qb = new QueryBuilder("SELECT * FROM saved_works WHERE is_public = TRUE");
        assertEquals("SELECT * FROM saved_works WHERE is_public = TRUE", qb.sql());
        assertEquals(0, qb.params().length);
    }

    @Test
    void and_withBaseHavingWhere_appendsAndClause() {
        QueryBuilder qb = new QueryBuilder("SELECT * FROM saved_works WHERE is_public = TRUE");
        qb.and("LOWER(title) LIKE LOWER(?)", "%art%");

        assertTrue(qb.sql().contains("AND LOWER(title) LIKE LOWER(?)"));
        assertEquals(1, qb.params().length);
        assertEquals("%art%", qb.params()[0]);
    }

    @Test
    void and_withBaseWithoutWhere_appendsWhereClause() {
        QueryBuilder qb = new QueryBuilder("SELECT * FROM saved_works");
        qb.and("is_public = TRUE");

        assertTrue(qb.sql().startsWith("SELECT * FROM saved_works WHERE"));
    }

    @Test
    void andIfNotNull_nullValue_notAdded() {
        QueryBuilder qb = new QueryBuilder("SELECT * FROM saved_works WHERE is_public = TRUE");
        qb.andIfNotNull("title = ?", null);

        assertFalse(qb.sql().contains("title"));
        assertEquals(0, qb.params().length);
    }

    @Test
    void andIfNotNull_nonNullValue_added() {
        QueryBuilder qb = new QueryBuilder("SELECT * FROM saved_works WHERE is_public = TRUE");
        qb.andIfNotNull("title = ?", "test");

        assertTrue(qb.sql().contains("title = ?"));
        assertEquals(1, qb.params().length);
    }

    @Test
    void orderBy_validColumn_appendsOrderBy() {
        QueryBuilder qb = new QueryBuilder("SELECT * FROM saved_works WHERE is_public = TRUE");
        qb.orderBy("saved_at", QueryBuilder.Direction.DESC);

        assertTrue(qb.sql().contains("ORDER BY saved_at DESC"));
    }

    @Test
    void orderBy_invalidColumn_throwsException() {
        QueryBuilder qb = new QueryBuilder("SELECT * FROM saved_works WHERE is_public = TRUE");
        assertThrows(IllegalArgumentException.class,
                () -> qb.orderBy("password", QueryBuilder.Direction.ASC));
    }

    @Test
    void limit_appendsLimitAndOffset() {
        QueryBuilder qb = new QueryBuilder("SELECT * FROM saved_works WHERE is_public = TRUE");
        qb.limit(10, 20);

        String sql = qb.sql();
        assertTrue(sql.contains("LIMIT 10"));
        assertTrue(sql.contains("OFFSET 20"));
    }

    @Test
    void multipleConditions_allAppended() {
        QueryBuilder qb = new QueryBuilder("SELECT * FROM saved_works WHERE is_public = TRUE");
        qb.and("LOWER(title) LIKE LOWER(?)", "%pixel%");
        qb.and("LOWER(username) LIKE LOWER(?)", "%alice%");

        String sql = qb.sql();
        assertTrue(sql.contains("LOWER(title)"));
        assertTrue(sql.contains("LOWER(username)"));
        assertEquals(2, qb.params().length);
    }

    @Test
    void toCountQuery_wrapsInCount() {
        QueryBuilder qb = new QueryBuilder("SELECT * FROM saved_works WHERE is_public = TRUE");
        qb.and("title = ?", "test");

        QueryBuilder countQb = qb.toCountQuery();
        assertTrue(countQb.sql().startsWith("SELECT COUNT(*)"));
        assertEquals(1, countQb.params().length);
    }
}