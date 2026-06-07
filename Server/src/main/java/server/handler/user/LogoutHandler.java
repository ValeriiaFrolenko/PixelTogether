package server.handler.user;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import common.model.Message;
import common.model.Packet;
import common.protocol.CommandType;
import server.core.SessionManager;
import server.handler.CommandHandler;
import server.network.ConnectionManager;
import server.network.ResponseDispatcher;

@Singleton
public class LogoutHandler implements CommandHandler {

    private final SessionManager sessionManager;
    private final ConnectionManager connectionManager;
    private final ResponseDispatcher dispatcher;

    @Inject
    public LogoutHandler(SessionManager sessionManager,
                         ConnectionManager connectionManager,
                         ResponseDispatcher dispatcher) {
        this.sessionManager = sessionManager;
        this.connectionManager = connectionManager;
        this.dispatcher = dispatcher;
    }

    @Override
    public void handle(Packet packet) {
        byte sessionId = packet.sessionId();

        sessionManager.unregister(sessionId);
        connectionManager.unregister(sessionId);

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
}