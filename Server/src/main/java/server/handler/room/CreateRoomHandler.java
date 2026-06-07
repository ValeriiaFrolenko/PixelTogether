package server.handler.room;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import common.dto.room.CreateRoomRequest;
import common.dto.room.CreateRoomResponse;
import common.dto.ErrorResponse;
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

import java.time.LocalDateTime;
import java.util.UUID;

@Singleton
public class CreateRoomHandler implements CommandHandler {

    private final RoomManager roomManager;
    private final SessionManager sessionManager;
    private final ConnectionManager connectionManager;
    private final ResponseDispatcher dispatcher;

    @Inject
    public CreateRoomHandler(RoomManager roomManager,
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

        if (session == null || !sessionManager.isAuthenticated(sessionId)) {
            dispatcher.sendToClient(sessionId, buildError(sessionId, "Not authenticated"));
            return;
        }

        CreateRoomRequest request = JsonUtil.fromBytes(packet.bMsg().payload(), CreateRoomRequest.class);

        String code = request.isPrivate()
                ? UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase()
                : null;

        long roomId = roomManager.createRoom(Room.builder()
                .name(request.name())
                .code(code)
                .ownerId(session.userId())
                .isPrivate(request.isPrivate())
                .canvasW(request.canvasW())
                .canvasH(request.canvasH())
                .expiresAt(LocalDateTime.now().plusMinutes(request.durationMinutes()))
                .build());

        sessionManager.assignRoom(sessionId, (int) roomId);
        connectionManager.assignRoom(sessionId, (int) roomId);

        dispatcher.sendToClient(sessionId, Packet.builder()
                .sessionId(sessionId)
                .bPktId(0)
                .bMsg(Message.builder()
                        .cType(CommandType.OK.getCode())
                        .roomId((int) roomId)
                        .payload(JsonUtil.toBytes(new CreateRoomResponse(roomId, code)))
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