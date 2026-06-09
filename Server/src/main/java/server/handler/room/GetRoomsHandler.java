package server.handler.room;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import common.dto.room.RoomInfo;
import common.utils.JsonUtil;
import server.core.RoomManager;
import server.handler.BaseHandler;
import server.network.ConnectionManager;
import server.network.ResponseDispatcher;
import common.model.Message;
import common.model.Packet;
import common.protocol.CommandType;

import java.util.List;

@Singleton
public class GetRoomsHandler extends BaseHandler {

    private final RoomManager roomManager;
    private final ConnectionManager connectionManager;

    @Inject
    public GetRoomsHandler(RoomManager roomManager,
                           ConnectionManager connectionManager,
                           ResponseDispatcher dispatcher) {
        super(dispatcher);
        this.roomManager = roomManager;
        this.connectionManager = connectionManager;
    }

    @Override
    public void handle(Packet packet) {
        long sessionId = packet.sessionId();

        List<RoomInfo> rooms = roomManager.getPublicRooms().stream()
                .map(room -> new RoomInfo(
                        room.id(),
                        room.name(),
                        connectionManager.countByRoom(room.id())
                ))
                .toList();

        dispatcher.sendToClient(sessionId, Packet.builder()
                .sessionId(sessionId)
                .bPktId(0)
                .bMsg(Message.builder()
                        .cType(CommandType.ROOM_LIST.getCode())
                        .roomId(0)
                        .payload(JsonUtil.toBytes(rooms))
                        .build())
                .build());
    }
}