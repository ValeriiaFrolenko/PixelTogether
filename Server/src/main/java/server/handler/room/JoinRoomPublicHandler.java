package server.handler.room;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import common.dto.room.CanvasStateResponse;
import common.dto.room.ParticipantEvent;
import common.utils.JsonUtil;
import server.core.ParticipantManager;
import server.core.RoomManager;
import server.handler.BaseHandler;
import server.network.ConnectionManager;
import server.network.ResponseDispatcher;
import common.model.Message;
import common.model.Packet;
import common.protocol.CommandType;

@Singleton
public class JoinRoomPublicHandler extends BaseHandler {

    private final RoomManager roomManager;
    private final ParticipantManager participantManager;
    private final ConnectionManager connectionManager;

    @Inject
    public JoinRoomPublicHandler(RoomManager roomManager,
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
        int roomId = packet.bMsg().roomId();

        if (!roomManager.exists(roomId)) {
            sendError(sessionId, packet.bPktId(),"Room not found");
            return;
        }

        String nickname = participantManager.assign(sessionId);
        connectionManager.assignRoom(sessionId, roomId);

        CanvasStateResponse canvasState = roomManager.getCanvasState(roomId);
        sendOk(sessionId, packet.bPktId(), JsonUtil.toBytes(canvasState));

        dispatcher.sendToRoom(roomId, Packet.builder()
                .sessionId(sessionId)
                .bPktId(0)
                .bMsg(Message.builder()
                        .cType(CommandType.PARTICIPANT_JOINED.getCode())
                        .roomId(roomId)
                        .payload(JsonUtil.toBytes(new ParticipantEvent(nickname)))
                        .build())
                .build());
    }
}