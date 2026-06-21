package server.handler.work;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import common.dto.work.GetWorkRequest;
import common.dto.work.GetWorkResponse;
import common.utils.JsonUtil;
import server.database.dao.SavedWorkDao;
import server.database.model.SavedWork;
import server.handler.BaseHandler;
import server.network.ResponseDispatcher;
import common.model.Message;
import common.model.Packet;
import common.protocol.CommandType;

import java.nio.ByteBuffer;
import java.util.Optional;

@Singleton
public class GetWorkHandler extends BaseHandler {

    private final SavedWorkDao savedWorkDao;

    @Inject
    public GetWorkHandler(SavedWorkDao savedWorkDao,
                          ResponseDispatcher dispatcher) {
        super(dispatcher);
        this.savedWorkDao = savedWorkDao;
    }

    @Override
    public void handle(Packet packet) {
        long sessionId = packet.sessionId();
        GetWorkRequest request = JsonUtil.fromBytes(packet.bMsg().payload(), GetWorkRequest.class);

        Optional<SavedWork> workOpt = savedWorkDao.findById(request.workId());
        if (workOpt.isEmpty()) {
            sendError(sessionId, "Work not found");
            return;
        }

        SavedWork work = workOpt.get();

        if (!work.isPublic()) {
            sendError(sessionId, "Forbidden");
            return;
        }

        int[] pixels = bytesToPixels(work.imageData(), work.canvasW(), work.canvasH());

        dispatcher.sendToClient(sessionId, Packet.builder()
                .sessionId(sessionId)
                .bPktId(0)
                .bMsg(Message.builder()
                        .cType(CommandType.WORK.getCode())
                        .roomId(0)
                        .payload(JsonUtil.toBytes(new GetWorkResponse(
                                work.id(),
                                work.ownerUsername(),
                                work.title(),
                                work.canvasW(),
                                work.canvasH(),
                                pixels
                        )))
                        .build())
                .build());
    }

    private int[] bytesToPixels(byte[] imageData, int w, int h) {
        ByteBuffer buf = ByteBuffer.wrap(imageData);
        int[] pixels = new int[w * h];
        for (int i = 0; i < pixels.length; i++) {
            pixels[i] = buf.getInt();
        }
        return pixels;
    }
}
