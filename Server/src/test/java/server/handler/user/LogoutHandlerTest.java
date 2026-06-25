package server.handler.user;

import common.dto.user.LogoutRequest;
import common.model.Packet;
import common.protocol.CommandType;
import common.utils.JsonUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.handler.BaseHandlerTest;

import static org.junit.jupiter.api.Assertions.*;

class LogoutHandlerTest extends BaseHandlerTest {

    private LogoutHandler handler;

    @BeforeEach
    void setUp() {
        handler = new LogoutHandler(authTokenDao, dispatcher);
    }

    @Test
    void logout_validToken_returnsOk() {
        int userId = createUser("user", "pass", "USER");
        String token = createToken(userId);

        Packet request = buildPacket(1L, 1L, CommandType.LOGOUT, 0,
                JsonUtil.toBytes(new LogoutRequest(token)));

        handler.handle(request);

        assertOk(lastSent());
        assertPktIdEchoed(request, lastSent());
    }

    @Test
    void logout_validToken_deletesTokenFromDatabase() {
        int userId = createUser("user", "pass", "USER");
        String token = createToken(userId);

        handler.handle(buildPacket(1L, 1L, CommandType.LOGOUT, 0,
                JsonUtil.toBytes(new LogoutRequest(token))));

        assertFalse(authTokenDao.existsByToken(token));
    }

    @Test
    void logout_invalidToken_returnsError() {
        Packet request = buildPacket(1L, 2L, CommandType.LOGOUT, 0,
                JsonUtil.toBytes(new LogoutRequest("nonexistent-token")));

        handler.handle(request);

        assertErrorMessage(lastSent(), "Unauthorized");
    }

    @Test
    void logout_pktIdEchoed() {
        int userId = createUser("user", "pass", "USER");
        String token = createToken(userId);

        long pktId = 42L;
        handler.handle(buildPacket(1L, pktId, CommandType.LOGOUT, 0,
                JsonUtil.toBytes(new LogoutRequest(token))));

        assertEquals(pktId, lastSent().bPktId());
    }
}