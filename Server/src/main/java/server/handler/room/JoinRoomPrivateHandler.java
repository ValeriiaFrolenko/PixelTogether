package server.handler.room;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import common.dto.room.CanvasStateResponse;
import common.dto.room.JoinRoomPrivateRequest;
import common.dto.room.ParticipantEvent;
import common.utils.JsonUtil;
import server.core.ParticipantManager;
import server.core.RoomManager;
import server.database.model.Room;
import server.handler.BaseHandler;
import server.network.ConnectionManager;
import server.network.ResponseDispatcher;
import common.model.Message;
import common.model.Packet;
import common.protocol.CommandType;

import java.util.Optional;

@Singleton
public class JoinRoomPrivateHandler extends BaseHandler {

    private final RoomManager roomManager;
    private final ParticipantManager participantManager;
    private final ConnectionManager connectionManager;

    @Inject
    public JoinRoomPrivateHandler(RoomManager roomManager,
                                  ParticipantManager participantManager,
                                  ConnectionManager connectionManager,
                                  ResponseDispatcher dispatcher) {
        super(dispatcher);
        this.roomManager = roomManager;
        this.participantManager = participantManager;
        this.connectionManager = connectionManager;
    }

    @Override
    public void handle(Packet packet) {
        long sessionId = packet.sessionId();
        JoinRoomPrivateRequest request = JsonUtil.fromBytes(packet.bMsg().payload(), JoinRoomPrivateRequest.class);

        Optional<Room> roomOpt = roomManager.findByCode(request.code());
        if (roomOpt.isEmpty()) {
            sendError(sessionId, packet.bPktId(),"Unauthorized");
            return;
        }

        Room room = roomOpt.get();
        String nickname = participantManager.assign(sessionId);
        connectionManager.assignRoom(sessionId, room.id());

        CanvasStateResponse canvasState = roomManager.getCanvasState(room.id());
        sendOk(sessionId, packet.bPktId(), JsonUtil.toBytes(canvasState));

        dispatcher.sendToRoom(room.id(), Packet.builder()
                .sessionId(sessionId)
                .bPktId(0)
                .bMsg(Message.builder()
                        .cType(CommandType.PARTICIPANT_JOINED.getCode())
                        .roomId(room.id())
                        .payload(JsonUtil.toBytes(new ParticipantEvent(nickname)))
                        .build())
                .build());
    }
}