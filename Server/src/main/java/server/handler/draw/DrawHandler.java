package server.handler.draw;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import common.dto.draw.PixelUpdate;
import common.dto.draw.DrawRequest;
import common.utils.JsonUtil;
import server.core.RoomManager;
import server.handler.BaseHandler;
import server.network.ResponseDispatcher;
import common.model.Message;
import common.model.Packet;
import common.protocol.CommandType;

import java.util.List;

@Singleton
public class DrawHandler extends BaseHandler {

    private final RoomManager roomManager;

    @Inject
    public DrawHandler(RoomManager roomManager,
                       ResponseDispatcher dispatcher) {
        super(dispatcher);
        this.roomManager = roomManager;
    }

    @Override
    public void handle(Packet packet) {
        long sessionId = packet.sessionId();
        int roomId = packet.bMsg().roomId();
        DrawRequest request = JsonUtil.fromBytes(packet.bMsg().payload(), DrawRequest.class);

        if (!roomManager.exists(roomId)) {
            sendError(sessionId, "Room not found");
            return;
        }

        server.database.model.Room room = roomManager.getRoom(roomId);
        List<PixelUpdate> valid = request.pixels().stream()
                .filter(p -> p.x() >= 0 && p.x() < room.canvasW()
                        && p.y() >= 0 && p.y() < room.canvasH())
                .toList();

        if (valid.isEmpty()) return;

        roomManager.applyPixels(roomId, valid);

        dispatcher.sendToRoom(roomId, Packet.builder()
                .sessionId(sessionId)
                .bPktId(0)
                .bMsg(Message.builder()
                        .cType(CommandType.DRAW.getCode())
                        .roomId(roomId)
                        .payload(JsonUtil.toBytes(new DrawRequest(valid)))
                        .build())
                .build());
    }
}