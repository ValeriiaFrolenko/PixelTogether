package server.handler;

import common.dto.ErrorResponse;
import common.model.Message;
import common.model.Packet;
import common.protocol.CommandType;
import common.utils.JsonUtil;
import server.network.ResponseDispatcher;

public abstract class BaseHandler implements CommandHandler {

    protected final ResponseDispatcher dispatcher;

    protected BaseHandler(ResponseDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    protected void sendError(long sessionId, String message) {
        dispatcher.sendToClient(sessionId, Packet.builder()
                .sessionId(sessionId)
                .bPktId(0)
                .bMsg(Message.builder()
                        .cType(CommandType.ERROR.getCode())
                        .roomId(0)
                        .payload(JsonUtil.toBytes(new ErrorResponse(message)))
                        .build())
                .build());
    }

    protected void sendOk(long sessionId) {
        dispatcher.sendToClient(sessionId, Packet.builder()
                .sessionId(sessionId)
                .bPktId(0)
                .bMsg(Message.builder()
                        .cType(CommandType.OK.getCode())
                        .roomId(0)
                        .payload(new byte[0])
                        .build())
                .build());
    }

    protected void sendOk(long sessionId, byte[] payload) {
        dispatcher.sendToClient(sessionId, Packet.builder()
                .sessionId(sessionId)
                .bPktId(0)
                .bMsg(Message.builder()
                        .cType(CommandType.OK.getCode())
                        .roomId(0)
                        .payload(payload)
                        .build())
                .build());
    }
}