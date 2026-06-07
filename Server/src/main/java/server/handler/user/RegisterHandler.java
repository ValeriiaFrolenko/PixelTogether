package server.handler.user;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import server.handler.CommandHandler;
import server.network.ResponseDispatcher;
import server.core.SessionManager;
import server.database.dao.UserDao;
import server.database.model.User;
import common.dto.ErrorResponse;
import common.dto.user.RegisterRequest;
import common.model.Message;
import common.model.Packet;
import common.protocol.CommandType;
import common.utils.JsonUtil;

@Singleton
public class RegisterHandler implements CommandHandler {

    private final UserDao userDao;
    private final SessionManager sessionManager;
    private final ResponseDispatcher dispatcher;

    @Inject
    public RegisterHandler(UserDao userDao,
                           SessionManager sessionManager,
                           ResponseDispatcher dispatcher) {
        this.userDao = userDao;
        this.sessionManager = sessionManager;
        this.dispatcher = dispatcher;
    }

    @Override
    public void handle(Packet packet) {
        byte sessionId = packet.sessionId();
        RegisterRequest request = JsonUtil.fromBytes(packet.bMsg().payload(), RegisterRequest.class);

        if (userDao.findByUsername(request.username()).isPresent()) {
            sendError(sessionId, "Username already taken");
            return;
        }

        String hashedPassword = BCrypt.withDefaults().hashToString(12, request.password().toCharArray());

        long userId = userDao.save(User.builder()
                .username(request.username())
                .password(hashedPassword)
                .role("USER")
                .build());

        sessionManager.authenticate(sessionId, (int) userId);
        sessionManager.updateNickname(sessionId, request.username());

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