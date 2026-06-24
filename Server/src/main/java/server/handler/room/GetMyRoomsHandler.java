package server.handler.room;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import common.dto.room.GetMyRoomsRequest;
import common.dto.room.MyRoomInfo;
import common.utils.JsonUtil;
import server.core.RoomManager;
import server.database.dao.AuthTokenDao;
import server.handler.BaseHandler;
import server.network.ResponseDispatcher;
import common.model.Message;
import common.model.Packet;
import common.protocol.CommandType;

import java.util.List;

@Singleton
public class GetMyRoomsHandler extends BaseHandler {

    private final RoomManager roomManager;
    private final AuthTokenDao authTokenDao;

    @Inject
    public GetMyRoomsHandler(RoomManager roomManager,
                             AuthTokenDao authTokenDao,
                             ResponseDispatcher dispatcher) {
        super(dispatcher);
        this.roomManager = roomManager;
        this.authTokenDao = authTokenDao;
    }

    @Override
    public void handle(Packet packet) {
        long sessionId = packet.sessionId();
        GetMyRoomsRequest request = JsonUtil.fromBytes(packet.bMsg().payload(), GetMyRoomsRequest.class);

        var userIdOpt = authTokenDao.findUserIdByToken(request.token());
        if (userIdOpt.isEmpty()) {
            sendError(sessionId, packet.bPktId(), "Unauthorized");
            return;
        }

        int userId = userIdOpt.get();

        List<MyRoomInfo> rooms = roomManager.getRoomsByOwner(userId)
                .stream()
                .map(room -> new MyRoomInfo(
                        room.id(),
                        room.name(),
                        room.code(),
                        room.isPrivate(),
                        room.expiresAt().toString()
                ))
                .toList();

        dispatcher.sendToClient(sessionId, Packet.builder()
                .sessionId(sessionId)
                .bPktId(packet.bPktId())
                .bMsg(Message.builder()
                        .cType(CommandType.MY_ROOMS.getCode())
                        .roomId(0)
                        .payload(JsonUtil.toBytes(rooms))
                        .build())
                .build());
    }
}