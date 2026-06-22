package server.handler.work;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import common.dto.work.DeleteWorkRequest;
import common.utils.JsonUtil;
import server.database.dao.AuthTokenDao;
import server.database.dao.SavedWorkDao;
import server.database.dao.UserDao;
import server.database.model.SavedWork;
import server.database.model.User;
import server.handler.BaseHandler;
import server.network.ResponseDispatcher;

@Singleton
public class DeleteWorkHandler extends BaseHandler {

    private final SavedWorkDao savedWorkDao;
    private final AuthTokenDao authTokenDao;
    private final UserDao userDao;

    @Inject
    public DeleteWorkHandler(SavedWorkDao savedWorkDao,
                             AuthTokenDao authTokenDao,
                             UserDao userDao,
                             ResponseDispatcher dispatcher) {
        super(dispatcher);
        this.savedWorkDao = savedWorkDao;
        this.authTokenDao = authTokenDao;
        this.userDao = userDao;
    }

    @Override
    public void handle(common.model.Packet packet) {
        long sessionId = packet.sessionId();
        DeleteWorkRequest request = JsonUtil.fromBytes(packet.bMsg().payload(), DeleteWorkRequest.class);

        var userIdOpt = authTokenDao.findUserIdByToken(request.token());
        if (userIdOpt.isEmpty()) {
            sendError(sessionId, packet.bPktId(), "Unauthorized");
            return;
        }

        var workOpt = savedWorkDao.findById(request.workId());
        if (workOpt.isEmpty()) {
            sendError(sessionId, packet.bPktId(), "Work not found");
            return;
        }

        int userId = userIdOpt.get();
        SavedWork work = workOpt.get();

        if (work.ownerId() != userId) {
            User user = userDao.findById(userId).orElse(null);
            if (user == null || !"ADMIN".equals(user.role())) {
                sendError(sessionId, packet.bPktId(), "Forbidden");
                return;
            }
        }

        savedWorkDao.deleteById(request.workId());
        sendOk(sessionId, packet.bPktId());
    }
}