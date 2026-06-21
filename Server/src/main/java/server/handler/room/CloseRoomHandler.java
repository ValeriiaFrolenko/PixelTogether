package server.handler.room;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import common.dto.room.CloseRoomRequest;
import common.utils.JsonUtil;
import server.core.RoomManager;
import server.database.dao.AuthTokenDao;
import server.database.dao.UserDao;
import server.database.model.Room;
import server.database.model.User;
import server.handler.BaseHandler;
import server.network.ResponseDispatcher;

@Singleton
public class CloseRoomHandler extends BaseHandler {

    private final RoomManager roomManager;
    private final AuthTokenDao authTokenDao;
    private final UserDao userDao;

    @Inject
    public CloseRoomHandler(RoomManager roomManager,
                            AuthTokenDao authTokenDao,
                            UserDao userDao,
                            ResponseDispatcher dispatcher) {
        super(dispatcher);
        this.roomManager = roomManager;
        this.authTokenDao = authTokenDao;
        this.userDao = userDao;
    }

    @Override
    public void handle(common.model.Packet packet) {
        long sessionId = packet.sessionId();
        int roomId = packet.bMsg().roomId();
        CloseRoomRequest request = JsonUtil.fromBytes(packet.bMsg().payload(), CloseRoomRequest.class);

        var userIdOpt = authTokenDao.findUserIdByToken(request.token());
        if (userIdOpt.isEmpty()) {
            sendError(sessionId, "Unauthorized");
            return;
        }

        if (!roomManager.exists(roomId)) {
            sendError(sessionId, "Room not found");
            return;
        }

        int userId = userIdOpt.get();
        Room room = roomManager.getRoom(roomId);

        if (room.ownerId() != userId) {
            User user = userDao.findById(userId).orElse(null);
            if (user == null || !"ADMIN".equals(user.role())) {
                sendError(sessionId, "Forbidden");
                return;
            }
        }

        roomManager.deleteRoom(roomId);
        sendOk(sessionId);
    }
}