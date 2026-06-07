package server.handler.room;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import common.dto.ErrorResponse;
import common.dto.room.CanvasStateResponse;
import common.dto.room.ParticipantEvent;
import common.model.Message;
import common.model.Packet;
import common.protocol.CommandType;
import common.utils.JsonUtil;
import server.core.RoomManager;
import server.core.SessionManager;
import server.handler.CommandHandler;
import server.network.ConnectionManager;
import server.network.ResponseDispatcher;

@Singleton
public class JoinRoomPublicHandler implements CommandHandler {

    private final RoomManager roomManager;
    private final SessionManager sessionManager;
    private final ConnectionManager connectionManager;
    private final ResponseDispatcher dispatcher;

    @Inject
    public JoinRoomPublicHandler(RoomManager roomManager,
                                 SessionManager sessionManager,
                                 ConnectionManager connectionManager,
                                 ResponseDispatcher dispatcher) {
        this.roomManager = roomManager;
        this.sessionManager = sessionManager;
        this.connectionManager = connectionManager;
        this.dispatcher = dispatcher;
    }

    @Override
    public void handle(Packet packet) {
        byte sessionId = packet.sessionId();
        int roomId = packet.bMsg().roomId();
        SessionManager.Session session = sessionManager.get(sessionId);

        if (session == null) {
            dispatcher.sendToClient(sessionId, buildError(sessionId, "Session not found"));
            return;
        }

        if (!roomManager.exists(roomId)) {
            dispatcher.sendToClient(sessionId, buildError(sessionId, "Room not found"));
            return;
        }

        sessionManager.assignRoom(sessionId, roomId);
        connectionManager.assignRoom(sessionId, roomId);

        CanvasStateResponse canvasState = roomManager.getCanvasState(roomId);
        dispatcher.sendToClient(sessionId, Packet.builder()
                .sessionId(sessionId)
                .bPktId(0)
                .bMsg(Message.builder()
                        .cType(CommandType.CANVAS_STATE.getCode())
                        .roomId(roomId)
                        .payload(JsonUtil.toBytes(canvasState))
                        .build())
                .build());

        dispatcher.sendToRoom(roomId, Packet.builder()
                .sessionId(sessionId)
                .bPktId(0)
                .bMsg(Message.builder()
                        .cType(CommandType.PARTICIPANT_JOINED.getCode())
                        .roomId(roomId)
                        .payload(JsonUtil.toBytes(new ParticipantEvent(session.nickname())))
                        .build())
                .build());
    }

    private Packet buildError(byte sessionId, String message) {
        return Packet.builder()
                .sessionId(sessionId)
                .bPktId(0)
                .bMsg(Message.builder()
                        .cType(CommandType.ERROR.getCode())
                        .roomId(0)
                        .payload(JsonUtil.toBytes(new ErrorResponse(message)))
                        .build())
                .build();
    }
}