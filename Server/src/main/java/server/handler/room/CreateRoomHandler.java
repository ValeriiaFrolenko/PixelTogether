package server.handler.room;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import common.dto.room.CreateRoomRequest;
import common.dto.room.CreateRoomResponse;
import common.model.Message;
import common.model.Packet;
import common.protocol.CommandType;
import common.utils.JsonUtil;
import server.core.ParticipantManager;
import server.core.RoomManager;
import server.database.dao.AuthTokenDao;
import server.database.dao.UserDao;
import server.database.model.Room;
import server.database.model.User;
import server.handler.BaseHandler;
import server.network.ConnectionManager;
import server.network.ResponseDispatcher;

import java.time.LocalDateTime;
import java.util.UUID;

@Singleton
public class CreateRoomHandler extends BaseHandler {

    private final RoomManager roomManager;
    private final ParticipantManager participantManager;
    private final AuthTokenDao authTokenDao;
    private final UserDao userDao;
    private final ConnectionManager connectionManager;

    @Inject
    public CreateRoomHandler(RoomManager roomManager,
                             ParticipantManager participantManager,
                             AuthTokenDao authTokenDao,
                             UserDao userDao,
                             ConnectionManager connectionManager,
                             ResponseDispatcher dispatcher) {
        super(dispatcher);
        this.roomManager = roomManager;
        this.participantManager = participantManager;
        this.authTokenDao = authTokenDao;
        this.userDao = userDao;
        this.connectionManager = connectionManager;
    }

    @Override
    public void handle(common.model.Packet packet) {
        long sessionId = packet.sessionId();
        CreateRoomRequest request = JsonUtil.fromBytes(packet.bMsg().payload(), CreateRoomRequest.class);

        var userIdOpt = authTokenDao.findUserIdByToken(request.token());
        if (userIdOpt.isEmpty()) {
            sendError(sessionId, packet.bPktId(), "Unauthorized");
            return;
        }

        int userId = userIdOpt.get();

        String username = userDao.findById(userId)
                .map(User::username)
                .orElse("Unknown");

        String code = request.isPrivate()
                ? UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase()
                : null;

        long roomId = roomManager.createRoom(Room.builder()
                .name(request.name())
                .code(code)
                .ownerId(userId)
                .isPrivate(request.isPrivate())
                .canvasW(request.canvasW())
                .canvasH(request.canvasH())
                .expiresAt(LocalDateTime.now().plusMinutes(request.durationMinutes()))
                .build());

        participantManager.assign(sessionId, username);
        connectionManager.assignRoom(sessionId, (int) roomId);

        dispatcher.sendToClient(sessionId, Packet.builder()
                .sessionId(sessionId)
                .bPktId(packet.bPktId())
                .bMsg(Message.builder()
                        .cType(CommandType.OK.getCode())
                        .roomId((int) roomId)
                        .payload(JsonUtil.toBytes(new CreateRoomResponse(code)))
                        .build())
                .build());
    }
}