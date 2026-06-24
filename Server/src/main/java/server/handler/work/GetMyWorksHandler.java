package server.handler.work;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import common.dto.work.GalleryItem;
import common.dto.work.GetMyWorksRequest;
import common.utils.JsonUtil;
import server.database.dao.AuthTokenDao;
import server.database.dao.SavedWorkDao;
import server.handler.BaseHandler;
import server.network.ResponseDispatcher;
import common.model.Message;
import common.model.Packet;
import common.protocol.CommandType;

import java.util.List;

@Singleton
public class GetMyWorksHandler extends BaseHandler {

    private final SavedWorkDao savedWorkDao;
    private final AuthTokenDao authTokenDao;

    @Inject
    public GetMyWorksHandler(SavedWorkDao savedWorkDao,
                             AuthTokenDao authTokenDao,
                             ResponseDispatcher dispatcher) {
        super(dispatcher);
        this.savedWorkDao = savedWorkDao;
        this.authTokenDao = authTokenDao;
    }

    @Override
    public void handle(Packet packet) {
        long sessionId = packet.sessionId();
        GetMyWorksRequest request = JsonUtil.fromBytes(packet.bMsg().payload(), GetMyWorksRequest.class);

        var userIdOpt = authTokenDao.findUserIdByToken(request.token());
        if (userIdOpt.isEmpty()) {
            sendError(sessionId, packet.bPktId(), "Unauthorized");
            return;
        }

        List<GalleryItem> items = savedWorkDao.findAllByOwner(userIdOpt.get())                .stream()
                .map(w -> new GalleryItem(
                        w.id(),
                        w.ownerUsername(),
                        w.title(),
                        w.canvasW(),
                        w.canvasH(),
                        w.savedAt().toString()
                ))
                .toList();

        dispatcher.sendToClient(sessionId, Packet.builder()
                .sessionId(sessionId)
                .bPktId(packet.bPktId())
                .bMsg(Message.builder()
                        .cType(CommandType.MY_WORKS.getCode())
                        .roomId(0)
                        .payload(JsonUtil.toBytes(items))
                        .build())
                .build());
    }
}