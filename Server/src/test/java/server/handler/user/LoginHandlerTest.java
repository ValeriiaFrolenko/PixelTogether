package server.handler.user;

import common.dto.user.AuthResponse;
import common.dto.user.LoginRequest;
import common.model.Packet;
import common.protocol.CommandType;
import common.utils.JsonUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.handler.BaseHandlerTest;

import static org.junit.jupiter.api.Assertions.*;

class LoginHandlerTest extends BaseHandlerTest {

    private LoginHandler handler;

    @BeforeEach
    void setUp() {
        handler = new LoginHandler(userDao, authTokenDao, dispatcher);
    }

    @Test
    void login_correctCredentials_returnsOkWithToken() {
        createUser("valeria", "pass123", "USER");

        Packet request = buildPacket(1L, 10L, CommandType.LOGIN, 0,
                JsonUtil.toBytes(new LoginRequest("valeria", "pass123")));

        handler.handle(request);

        Packet response = lastSent();
        assertOk(response);
        assertPktIdEchoed(request, response);

        AuthResponse auth = JsonUtil.fromBytes(response.bMsg().payload(), AuthResponse.class);
        assertNotNull(auth.token());
        assertFalse(auth.token().isBlank());
    }

    @Test
    void login_wrongPassword_returnsError() {
        createUser("valeria", "pass123", "USER");

        Packet request = buildPacket(1L, 11L, CommandType.LOGIN, 0,
                JsonUtil.toBytes(new LoginRequest("valeria", "wrongpass")));

        handler.handle(request);

        Packet response = lastSent();
        assertErrorMessage(response, "Unauthorized");
        assertPktIdEchoed(request, response);
    }

    @Test
    void login_unknownUsername_returnsError() {
        Packet request = buildPacket(1L, 12L, CommandType.LOGIN, 0,
                JsonUtil.toBytes(new LoginRequest("nobody", "pass123")));

        handler.handle(request);

        assertErrorMessage(lastSent(), "Unauthorized");
    }

    @Test
    void login_createsTokenInDatabase() {
        createUser("valeria", "pass123", "USER");

        Packet request = buildPacket(1L, 13L, CommandType.LOGIN, 0,
                JsonUtil.toBytes(new LoginRequest("valeria", "pass123")));

        handler.handle(request);

        AuthResponse auth = JsonUtil.fromBytes(lastSent().bMsg().payload(), AuthResponse.class);
        assertTrue(authTokenDao.existsByToken(auth.token()));
    }

    @Test
    void login_successfulLogin_pktIdEchoed() {
        createUser("valeria", "pass123", "USER");

        long pktId = 999L;
        Packet request = buildPacket(1L, pktId, CommandType.LOGIN, 0,
                JsonUtil.toBytes(new LoginRequest("valeria", "pass123")));

        handler.handle(request);

        assertEquals(pktId, lastSent().bPktId());
    }
}