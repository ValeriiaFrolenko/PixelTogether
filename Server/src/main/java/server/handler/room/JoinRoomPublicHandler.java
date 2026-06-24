package server.handler.room;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import common.dto.room.CanvasStateResponse;
import common.dto.room.JoinRoomPublicRequest;
import common.dto.room.ParticipantEvent;
import common.utils.JsonUtil;
import server.core.NicknameResolver;
import server.core.ParticipantManager;
import server.core.RoomManager;
import server.database.dao.AuthTokenDao;
import server.database.dao.UserDao;
import server.database.model.Room;
import server.handler.BaseHandler;
import server.network.ConnectionManager;
import server.network.ResponseDispatcher;
import common.model.Message;
import common.model.Packet;
import common.protocol.CommandType;

import java.util.List;

@Singleton
public class JoinRoomPublicHandler extends BaseHandler {

    private final RoomManager roomManager;
    private final ParticipantManager participantManager;
    private final ConnectionManager connectionManager;
    private final AuthTokenDao authTokenDao;
    private final UserDao userDao;

    @Inject
    public JoinRoomPublicHandler(RoomManager roomManager,
                                 ParticipantManager participantManager,
                                 ConnectionManager connectionManager,
                                 AuthTokenDao authTokenDao,
                                 UserDao userDao,
                                 ResponseDispatcher dispatcher) {
        super(dispatcher);
        this.roomManager = roomManager;
        this.participantManager = participantManager;
        this.connectionManager = connectionManager;
        this.authTokenDao = authTokenDao;
        this.userDao = userDao;
    }

    @Override
    public void handle(Packet packet) {
        long sessionId = packet.sessionId();
        int roomId = packet.bMsg().roomId();
        JoinRoomPublicRequest request = JsonUtil.fromBytes(packet.bMsg().payload(), JoinRoomPublicRequest.class);

        if (!roomManager.exists(roomId)) {
            sendError(sessionId, packet.bPktId(), "Room not found");
            return;
        }

        Room room = roomManager.getRoom(roomId);

        boolean isOwner = authTokenDao.findUserIdByToken(request.token())
                .map(uid -> uid == room.ownerId())
                .orElse(false);

        String nickname = NicknameResolver.resolve(request.token(), authTokenDao, userDao);
        participantManager.assign(sessionId, nickname);
        connectionManager.assignRoom(sessionId, roomId);

        List<String> currentNicknames = connectionManager.getSessionSocketsByRoom(roomId)
                .keySet()
                .stream()
                .map(participantManager::get)
                .filter(n -> n != null && !n.equals(nickname))
                .toList();

        CanvasStateResponse canvasState = roomManager.getCanvasState(roomId);
        dispatcher.sendToClient(sessionId, Packet.builder()
                .sessionId(sessionId)
                .bPktId(packet.bPktId())
                .bMsg(Message.builder()
                        .cType(CommandType.CANVAS_STATE.getCode())
                        .roomId(room.id())
                        .payload(JsonUtil.toBytes(new CanvasStateResponse(
                                canvasState.width(),
                                canvasState.height(),
                                canvasState.pixels(),
                                isOwner,
                                currentNicknames
                        )))
                        .build())
                .build());

        dispatcher.sendToRoom(roomId, Message.builder()
                .cType(CommandType.PARTICIPANT_JOINED.getCode())
                .roomId(roomId)
                .payload(JsonUtil.toBytes(new ParticipantEvent(nickname)))
                .build());
    }
}