package server.handler.user;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import common.dto.ErrorResponse;
import common.dto.user.LoginRequest;
import common.model.Message;
import common.model.Packet;
import common.protocol.CommandType;
import common.utils.JsonUtil;
import server.core.SessionManager;
import server.database.dao.UserDao;
import server.database.model.User;
import server.handler.CommandHandler;
import server.network.ResponseDispatcher;

@Singleton
public class LoginHandler implements CommandHandler {

    private final UserDao userDao;
    private final SessionManager sessionManager;
    private final ResponseDispatcher dispatcher;

    @Inject
    public LoginHandler(UserDao userDao,
                        SessionManager sessionManager,
                        ResponseDispatcher dispatcher) {
        this.userDao = userDao;
        this.sessionManager = sessionManager;
        this.dispatcher = dispatcher;
    }

    @Override
    public void handle(Packet packet) {
        byte sessionId = packet.sessionId();
        LoginRequest request = JsonUtil.fromBytes(packet.bMsg().payload(), LoginRequest.class);

        var userOpt = userDao.findByUsername(request.username());
        if (userOpt.isEmpty()) {
            sendError(sessionId, "User not found");
            return;
        }

        User user = userOpt.get();
        BCrypt.Result result = BCrypt.verifyer().verify(
                request.password().toCharArray(),
                user.password()
        );

        if (!result.verified) {
            sendError(sessionId, "Invalid password");
            return;
        }

        sessionManager.authenticate(sessionId, user.id());
        sessionManager.updateNickname(sessionId, user.username());

        dispatcher.sendToClient(sessionId, buildOk(sessionId));
    }

    private Packet buildOk(byte sessionId) {
        return Packet.builder()
                .sessionId(sessionId)
                .bPktId(0)
                .bMsg(Message.builder()
                        .cType(CommandType.OK.getCode())
                        .roomId(0)
                        .payload(new byte[0])
                        .build())
                .build();
    }

    private void sendError(byte sessionId, String message) {
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
}