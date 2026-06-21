package server.handler.room;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import common.dto.room.ParticipantEvent;
import common.utils.JsonUtil;
import server.core.ParticipantManager;
import server.handler.BaseHandler;
import server.network.ConnectionManager;
import server.network.ResponseDispatcher;
import common.model.Message;
import common.model.Packet;
import common.protocol.CommandType;

@Singleton
public class LeaveRoomHandler extends BaseHandler {

    private final ParticipantManager participantManager;
    private final ConnectionManager connectionManager;

    @Inject
    public LeaveRoomHandler(ParticipantManager participantManager,
                            ConnectionManager connectionManager,
                            ResponseDispatcher dispatcher) {
        super(dispatcher);
        this.participantManager = participantManager;
        this.connectionManager = connectionManager;
    }

    @Override
    public void handle(Packet packet) {
        long sessionId = packet.sessionId();
        int roomId = packet.bMsg().roomId();

        String nickname = participantManager.get(sessionId);
        connectionManager.leaveRoom(sessionId, roomId);
        participantManager.remove(sessionId);

        sendOk(sessionId);

        dispatcher.sendToRoom(roomId, Packet.builder()
                .sessionId(sessionId)
                .bPktId(0)
                .bMsg(Message.builder()
                        .cType(CommandType.PARTICIPANT_LEFT.getCode())
                        .roomId(roomId)
                        .payload(JsonUtil.toBytes(new ParticipantEvent(nickname)))
                        .build())
                .build());
    }
}