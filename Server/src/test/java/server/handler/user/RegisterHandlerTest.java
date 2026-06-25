package server.handler.user;

import common.dto.user.AuthResponse;
import common.dto.user.RegisterRequest;
import common.model.Packet;
import common.protocol.CommandType;
import common.utils.JsonUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.handler.BaseHandlerTest;

import static org.junit.jupiter.api.Assertions.*;

class RegisterHandlerTest extends BaseHandlerTest {

    private RegisterHandler handler;

    @BeforeEach
    void setUp() {
        handler = new RegisterHandler(userDao, authTokenDao, dispatcher);
    }

    @Test
    void register_newUser_returnsOkWithToken() {
        Packet request = buildPacket(1L, 1L, CommandType.REGISTER, 0,
                JsonUtil.toBytes(new RegisterRequest("newuser", "password")));

        handler.handle(request);

        Packet response = lastSent();
        assertOk(response);
        assertPktIdEchoed(request, response);

        AuthResponse auth = JsonUtil.fromBytes(response.bMsg().payload(), AuthResponse.class);
        assertNotNull(auth.token());
        assertFalse(auth.token().isBlank());
    }

    @Test
    void register_duplicateUsername_returnsError() {
        createUser("existing", "pass", "USER");

        Packet request = buildPacket(1L, 2L, CommandType.REGISTER, 0,
                JsonUtil.toBytes(new RegisterRequest("existing", "otherpass")));

        handler.handle(request);

        assertErrorMessage(lastSent(), "Username already taken");
    }

    @Test
    void register_createsUserInDatabase() {
        Packet request = buildPacket(1L, 3L, CommandType.REGISTER, 0,
                JsonUtil.toBytes(new RegisterRequest("brandnew", "password")));

        handler.handle(request);

        assertTrue(userDao.findByUsername("brandnew").isPresent());
    }

    @Test
    void register_createsTokenInDatabase() {
        Packet request = buildPacket(1L, 4L, CommandType.REGISTER, 0,
                JsonUtil.toBytes(new RegisterRequest("tokenuser", "password")));

        handler.handle(request);

        AuthResponse auth = JsonUtil.fromBytes(lastSent().bMsg().payload(), AuthResponse.class);
        assertTrue(authTokenDao.existsByToken(auth.token()));
    }

    @Test
    void register_passwordIsHashed() {
        Packet request = buildPacket(1L, 5L, CommandType.REGISTER, 0,
                JsonUtil.toBytes(new RegisterRequest("hashtest", "plaintext")));

        handler.handle(request);

        String storedPassword = userDao.findByUsername("hashtest").get().password();
        // bcrypt хеш завжди починається з $2
        assertTrue(storedPassword.startsWith("$2"));
        assertNotEquals("plaintext", storedPassword);
    }

    @Test
    void register_pktIdEchoed() {
        long pktId = 777L;
        Packet request = buildPacket(1L, pktId, CommandType.REGISTER, 0,
                JsonUtil.toBytes(new RegisterRequest("pktuser", "password")));

        handler.handle(request);

        assertEquals(pktId, lastSent().bPktId());
    }
}