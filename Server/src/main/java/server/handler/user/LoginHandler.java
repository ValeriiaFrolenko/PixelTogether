package server.handler.user;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import common.dto.user.AuthResponse;
import common.dto.user.LoginRequest;
import common.utils.JsonUtil;
import server.database.dao.AuthTokenDao;
import server.database.dao.UserDao;
import server.database.model.User;
import server.handler.BaseHandler;
import server.network.ResponseDispatcher;

import java.util.UUID;

@Singleton
public class LoginHandler extends BaseHandler {

    private final UserDao userDao;
    private final AuthTokenDao authTokenDao;

    @Inject
    public LoginHandler(UserDao userDao,
                        AuthTokenDao authTokenDao,
                        ResponseDispatcher dispatcher) {
        super(dispatcher);
        this.userDao = userDao;
        this.authTokenDao = authTokenDao;
    }

    @Override
    public void handle(common.model.Packet packet) {
        long sessionId = packet.sessionId();
        LoginRequest request = JsonUtil.fromBytes(packet.bMsg().payload(), LoginRequest.class);

        var userOpt = userDao.findByUsername(request.username());
        if (userOpt.isEmpty()) {
            sendError(sessionId, packet.bPktId(), "Unauthorized");
            return;
        }

        User user = userOpt.get();
        BCrypt.Result result = BCrypt.verifyer().verify(
                request.password().toCharArray(),
                user.password()
        );

        if (!result.verified) {
            sendError(sessionId, packet.bPktId(), "Unauthorized");
            return;
        }

        String token = UUID.randomUUID().toString();
        authTokenDao.save(token, user.id());

        sendOk(sessionId, packet.bPktId(), JsonUtil.toBytes(new AuthResponse(token)));
    }
}