package server.handler.room;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import common.dto.ErrorResponse;
import common.dto.room.CanvasStateResponse;
import common.dto.room.JoinRoomPrivateRequest;
import common.dto.room.ParticipantEvent;
import common.model.Message;
import common.model.Packet;
import common.protocol.CommandType;
import common.utils.JsonUtil;
import server.core.RoomManager;
import server.core.SessionManager;
import server.database.model.Room;
import server.handler.CommandHandler;
import server.network.ConnectionManager;
import server.network.ResponseDispatcher;

import java.util.Optional;

@Singleton
public class JoinRoomPrivateHandler implements CommandHandler {

    private final RoomManager roomManager;
    private final SessionManager sessionManager;
    private final ConnectionManager connectionManager;
    private final ResponseDispatcher dispatcher;

    @Inject
    public JoinRoomPrivateHandler(RoomManager roomManager,
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
        SessionManager.Session session = sessionManager.get(sessionId);

        if (session == null) {
            dispatcher.sendToClient(sessionId, buildError(sessionId, "Session not found"));
            return;
        }

        JoinRoomPrivateRequest request = JsonUtil.fromBytes(packet.bMsg().payload(), JoinRoomPrivateRequest.class);

        Optional<Room> roomOpt = roomManager.findByCode(request.code());
        if (roomOpt.isEmpty()) {
            dispatcher.sendToClient(sessionId, buildError(sessionId, "Invalid room code"));
            return;
        }

        Room room = roomOpt.get();

        sessionManager.assignRoom(sessionId, room.id());
        connectionManager.assignRoom(sessionId, room.id());

        CanvasStateResponse canvasState = roomManager.getCanvasState(room.id());
        dispatcher.sendToClient(sessionId, Packet.builder()
                .sessionId(sessionId)
                .bPktId(0)
                .bMsg(Message.builder()
                        .cType(CommandType.CANVAS_STATE.getCode())
                        .roomId(room.id())
                        .payload(JsonUtil.toBytes(canvasState))
                        .build())
                .build());

        dispatcher.sendToRoom(room.id(), Packet.builder()
                .sessionId(sessionId)
                .bPktId(0)
                .bMsg(Message.builder()
                        .cType(CommandType.PARTICIPANT_JOINED.getCode())
                        .roomId(room.id())
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