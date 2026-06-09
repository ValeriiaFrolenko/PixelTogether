package server.handler.user;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import common.dto.user.AuthResponse;
import common.dto.user.RegisterRequest;
import common.utils.JsonUtil;
import server.database.dao.AuthTokenDao;
import server.database.dao.UserDao;
import server.database.model.User;
import server.handler.BaseHandler;
import server.network.ResponseDispatcher;

import java.util.UUID;

@Singleton
public class RegisterHandler extends BaseHandler {

    private final UserDao userDao;
    private final AuthTokenDao authTokenDao;

    @Inject
    public RegisterHandler(UserDao userDao,
                           AuthTokenDao authTokenDao,
                           ResponseDispatcher dispatcher) {
        super(dispatcher);
        this.userDao = userDao;
        this.authTokenDao = authTokenDao;
    }

    @Override
    public void handle(common.model.Packet packet) {
        long sessionId = packet.sessionId();
        RegisterRequest request = JsonUtil.fromBytes(packet.bMsg().payload(), RegisterRequest.class);

        if (userDao.findByUsername(request.username()).isPresent()) {
            sendError(sessionId, "Username already taken");
            return;
        }

        String hashedPassword = BCrypt.withDefaults().hashToString(12, request.password().toCharArray());

        int userId = (int) userDao.save(User.builder()
                .username(request.username())
                .password(hashedPassword)
                .role("USER")
                .build());

        String token = UUID.randomUUID().toString();
        authTokenDao.save(token, userId);

        sendOk(sessionId, JsonUtil.toBytes(new AuthResponse(token)));
    }
}