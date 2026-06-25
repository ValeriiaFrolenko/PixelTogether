package server.handler;

import org.junit.jupiter.api.BeforeEach;
import server.database.ConnectionProvider;
import server.database.JdbcTemplate;
import server.database.dao.AuthTokenDao;
import server.database.dao.RoomDao;
import server.database.dao.SavedWorkDao;
import server.database.dao.UserDao;
import server.network.ResponseDispatcher;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;

/**
 * Базовий клас для інтеграційних тестів хендлерів.
 * Використовує H2 in-memory з реальною схемою.
 * Перед кожним тестом очищає всі таблиці.
 */
public abstract class BaseHandlerTest {

    protected static final String DB_URL = "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1";
    protected static final String DB_USER = "sa";
    protected static final String DB_PASS = "";

    protected ConnectionProvider connectionProvider;
    protected JdbcTemplate jdbc;
    protected UserDao userDao;
    protected AuthTokenDao authTokenDao;
    protected RoomDao roomDao;
    protected SavedWorkDao savedWorkDao;
    protected ResponseDispatcher dispatcher;

    // Список відправлених пакетів — замість реального dispatcher
    protected List<common.model.Packet> sentPackets;

    @BeforeEach
    void baseSetUp() throws Exception {
        connectionProvider = () -> DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
        initSchema();
        clearTables();

        jdbc = new JdbcTemplate(connectionProvider);
        userDao = new UserDao(jdbc);
        authTokenDao = new AuthTokenDao(jdbc);
        roomDao = new RoomDao(jdbc);
        savedWorkDao = new SavedWorkDao(jdbc);

        sentPackets = new ArrayList<>();
        dispatcher = buildCapturingDispatcher(sentPackets);
    }

    /**
     * Створює ResponseDispatcher який замість відправки по мережі
     * складає пакети в список sentPackets.
     */
    private ResponseDispatcher buildCapturingDispatcher(List<common.model.Packet> sink) {
        // ResponseDispatcher має багато залежностей — мокаємо його напряму
        // і підміняємо sendToClient через Mockito answer
        ResponseDispatcher mock = mock(ResponseDispatcher.class);
        org.mockito.Mockito.doAnswer(inv -> {
            sink.add(inv.getArgument(1));
            return null;
        }).when(mock).sendToClient(
                org.mockito.ArgumentMatchers.anyLong(),
                org.mockito.ArgumentMatchers.any()
        );
        return mock;
    }

    private void initSchema() throws SQLException {
        try (Connection conn = connectionProvider.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id         INT AUTO_INCREMENT PRIMARY KEY,
                    username   VARCHAR(50)  NOT NULL UNIQUE,
                    password   VARCHAR(255) NOT NULL,
                    role       VARCHAR(10)  NOT NULL DEFAULT 'USER',
                    created_at TIMESTAMP    NOT NULL DEFAULT NOW(),
                    CHECK (role IN ('USER', 'ADMIN'))
                )
                """);
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS rooms (
                    id           INT AUTO_INCREMENT PRIMARY KEY,
                    name         VARCHAR(100) NOT NULL,
                    code         VARCHAR(8)   UNIQUE,
                    owner_id     INT          NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                    is_private   BOOLEAN      NOT NULL DEFAULT FALSE,
                    canvas_w     INT          NOT NULL DEFAULT 100,
                    canvas_h     INT          NOT NULL DEFAULT 100,
                    canvas_state BINARY VARYING,
                    expires_at   TIMESTAMP    NOT NULL,
                    created_at   TIMESTAMP    NOT NULL DEFAULT NOW(),
                    CHECK (canvas_w > 0 AND canvas_w <= 500),
                    CHECK (canvas_h > 0 AND canvas_h <= 500)
                )
                """);
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS saved_works (
                    id         INT AUTO_INCREMENT PRIMARY KEY,
                    owner_id   INT          NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                    title      VARCHAR(100) NOT NULL,
                    is_public  BOOLEAN      NOT NULL DEFAULT FALSE,
                    image_data BINARY VARYING NOT NULL,
                    canvas_w   INT          NOT NULL,
                    canvas_h   INT          NOT NULL,
                    saved_at   TIMESTAMP    NOT NULL DEFAULT NOW()
                )
                """);
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS auth_tokens (
                    token      VARCHAR(64) PRIMARY KEY,
                    user_id    INT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                    created_at TIMESTAMP NOT NULL DEFAULT NOW()
                )
                """);
        }
    }

    private void clearTables() throws SQLException {
        try (Connection conn = connectionProvider.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM auth_tokens");
            stmt.execute("DELETE FROM saved_works");
            stmt.execute("DELETE FROM rooms");
            stmt.execute("DELETE FROM users");
        }
    }

    // ---- Хелпери для побудови пакетів ----

    protected common.model.Packet buildPacket(long sessionId, long pktId,
                                              common.protocol.CommandType cmd,
                                              int roomId, byte[] payload) {
        return common.model.Packet.builder()
                .sessionId(sessionId)
                .bPktId(pktId)
                .bMsg(common.model.Message.builder()
                        .cType(cmd.getCode())
                        .roomId(roomId)
                        .payload(payload)
                        .build())
                .build();
    }

    protected common.model.Packet lastSent() {
        if (sentPackets.isEmpty()) throw new AssertionError("No packets were sent");
        return sentPackets.get(sentPackets.size() - 1);
    }

    protected void assertOk(common.model.Packet packet) {
        org.junit.jupiter.api.Assertions.assertEquals(
                common.protocol.CommandType.OK.getCode(),
                packet.bMsg().cType(),
                "Expected OK response"
        );
    }

    protected void assertError(common.model.Packet packet) {
        org.junit.jupiter.api.Assertions.assertEquals(
                common.protocol.CommandType.ERROR.getCode(),
                packet.bMsg().cType(),
                "Expected ERROR response"
        );
    }

    protected void assertErrorMessage(common.model.Packet packet, String expected) {
        assertError(packet);
        common.dto.ErrorResponse err = common.utils.JsonUtil.fromBytes(
                packet.bMsg().payload(), common.dto.ErrorResponse.class);
        org.junit.jupiter.api.Assertions.assertEquals(expected, err.message());
    }

    protected void assertCmdType(common.model.Packet packet, common.protocol.CommandType expected) {
        org.junit.jupiter.api.Assertions.assertEquals(
                expected.getCode(),
                packet.bMsg().cType(),
                "Expected command type " + expected
        );
    }

    protected void assertPktIdEchoed(common.model.Packet request, common.model.Packet response) {
        org.junit.jupiter.api.Assertions.assertEquals(
                request.bPktId(), response.bPktId(),
                "Response must echo request bPktId"
        );
    }

    // ---- Хелпери для створення тестових даних ----

    protected int createUser(String username, String password, String role) {
        String hashed = at.favre.lib.crypto.bcrypt.BCrypt.withDefaults()
                .hashToString(4, password.toCharArray()); // cost=4 для швидкості тестів
        return (int) userDao.save(server.database.model.User.builder()
                .username(username)
                .password(hashed)
                .role(role)
                .build());
    }

    protected String createToken(int userId) {
        String token = java.util.UUID.randomUUID().toString();
        authTokenDao.save(token, userId);
        return token;
    }

    protected int createRoom(int ownerId, String name, boolean isPrivate,
                             String code, int canvasW, int canvasH, long minutesUntilExpiry) {
        return (int) roomDao.save(server.database.model.Room.builder()
                .name(name)
                .code(code)
                .ownerId(ownerId)
                .isPrivate(isPrivate)
                .canvasW(canvasW)
                .canvasH(canvasH)
                .expiresAt(java.time.LocalDateTime.now().plusMinutes(minutesUntilExpiry))
                .build());
    }
}