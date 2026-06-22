package server.handler.work;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import common.dto.work.GalleryItem;
import common.dto.work.GetGalleryRequest;
import common.utils.JsonUtil;
import server.database.dao.SavedWorkDao;
import server.handler.BaseHandler;
import server.network.ResponseDispatcher;
import common.model.Message;
import common.model.Packet;
import common.protocol.CommandType;

import java.util.List;

@Singleton
public class GetGalleryHandler extends BaseHandler {

    private final SavedWorkDao savedWorkDao;

    @Inject
    public GetGalleryHandler(SavedWorkDao savedWorkDao,
                             ResponseDispatcher dispatcher) {
        super(dispatcher);
        this.savedWorkDao = savedWorkDao;
    }

    @Override
    public void handle(Packet packet) {
        long sessionId = packet.sessionId();
        GetGalleryRequest request = JsonUtil.fromBytes(packet.bMsg().payload(), GetGalleryRequest.class);

        List<GalleryItem> items = savedWorkDao.findPublicFiltered(request.title(), request.ownerUsername())
                .stream()
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
                        .cType(CommandType.GALLERY.getCode())
                        .roomId(0)
                        .payload(JsonUtil.toBytes(items))
                        .build())
                .build());
    }
}