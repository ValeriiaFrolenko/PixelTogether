package server.handler.user;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import common.dto.user.LogoutRequest;
import common.utils.JsonUtil;
import server.database.dao.AuthTokenDao;
import server.handler.BaseHandler;
import server.network.ResponseDispatcher;

@Singleton
public class LogoutHandler extends BaseHandler {

    private final AuthTokenDao authTokenDao;

    @Inject
    public LogoutHandler(AuthTokenDao authTokenDao,
                         ResponseDispatcher dispatcher) {
        super(dispatcher);
        this.authTokenDao = authTokenDao;
    }

    @Override
    public void handle(common.model.Packet packet) {
        long sessionId = packet.sessionId();
        LogoutRequest request = JsonUtil.fromBytes(packet.bMsg().payload(), LogoutRequest.class);

        if (!authTokenDao.existsByToken(request.token())) {
            sendError(sessionId, "Unauthorized");
            return;
        }

        authTokenDao.deleteByToken(request.token());
        sendOk(sessionId);
    }
}