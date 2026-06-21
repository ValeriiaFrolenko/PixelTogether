package server.handler.work;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import common.dto.work.SaveWorkRequest;
import common.utils.JsonUtil;
import server.core.RoomManager;
import server.database.dao.AuthTokenDao;
import server.database.dao.SavedWorkDao;
import server.database.dao.UserDao;
import server.database.model.SavedWork;
import server.handler.BaseHandler;
import server.network.ResponseDispatcher;

import java.nio.ByteBuffer;

@Singleton
public class SaveWorkHandler extends BaseHandler {

    private final RoomManager roomManager;
    private final AuthTokenDao authTokenDao;
    private final UserDao userDao;
    private final SavedWorkDao savedWorkDao;

    @Inject
    public SaveWorkHandler(RoomManager roomManager,
                           AuthTokenDao authTokenDao,
                           UserDao userDao,
                           SavedWorkDao savedWorkDao,
                           ResponseDispatcher dispatcher) {
        super(dispatcher);
        this.roomManager = roomManager;
        this.authTokenDao = authTokenDao;
        this.userDao = userDao;
        this.savedWorkDao = savedWorkDao;
    }

    @Override
    public void handle(common.model.Packet packet) {
        long sessionId = packet.sessionId();
        int roomId = packet.bMsg().roomId();
        SaveWorkRequest request = JsonUtil.fromBytes(packet.bMsg().payload(), SaveWorkRequest.class);

        var userIdOpt = authTokenDao.findUserIdByToken(request.token());
        if (userIdOpt.isEmpty()) {
            sendError(sessionId, "Unauthorized");
            return;
        }

        if (!roomManager.exists(roomId)) {
            sendError(sessionId, "Room not found");
            return;
        }

        var canvasState = roomManager.getCanvasState(roomId);
        byte[] imageData = pixelsToBytes(canvasState.pixels());

        savedWorkDao.save(SavedWork.builder()
                .ownerId(userIdOpt.get())
                .title(request.title())
                .isPublic(request.isPublic())
                .imageData(imageData)
                .canvasW(canvasState.width())
                .canvasH(canvasState.height())
                .build());

        sendOk(sessionId);
    }

    private byte[] pixelsToBytes(int[] pixels) {
        ByteBuffer buf = ByteBuffer.allocate(pixels.length * 4);
        for (int pixel : pixels) {
            buf.putInt(pixel);
        }
        return buf.array();
    }
}
