package server.handler.work;

import common.dto.work.DeleteWorkRequest;
import common.dto.work.SaveWorkRequest;
import common.model.Packet;
import common.protocol.CommandType;
import common.utils.JsonUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.core.RoomManager;
import server.database.model.SavedWork;
import server.handler.BaseHandlerTest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WorkHandlerTest extends BaseHandlerTest {

    private SaveWorkHandler saveWorkHandler;
    private DeleteWorkHandler deleteWorkHandler;
    private RoomManager roomManager;

    // 4x4 пікселі — 16 int, достатньо для тесту
    private static final int CANVAS_W = 4;
    private static final int CANVAS_H = 4;

    @BeforeEach
    void setUp() {
        // RoomManager мокаємо — він потребує шедулера, dispatcher тощо
        // Нам потрібна лише поведінка exists() і getCanvasState()
        roomManager = mock(RoomManager.class);

        saveWorkHandler = new SaveWorkHandler(roomManager, authTokenDao, userDao, savedWorkDao, dispatcher);
        deleteWorkHandler = new DeleteWorkHandler(savedWorkDao, authTokenDao, userDao, dispatcher);
    }

    // ===== SaveWorkHandler =====

    @Test
    void saveWork_authorized_roomExists_returnsOk() {
        int userId = createUser("artist", "pass", "USER");
        String token = createToken(userId);
        int roomId = 1;

        when(roomManager.exists(roomId)).thenReturn(true);
        when(roomManager.getCanvasState(roomId)).thenReturn(
                new common.dto.room.CanvasStateResponse(CANVAS_W, CANVAS_H, new int[CANVAS_W * CANVAS_H], false, java.util.List.of())
        );

        Packet request = buildPacket(1L, 1L, CommandType.SAVE_WORK, roomId,
                JsonUtil.toBytes(new SaveWorkRequest(token, "My Art", true)));

        saveWorkHandler.handle(request);

        assertOk(lastSent());
        assertPktIdEchoed(request, lastSent());
    }

    @Test
    void saveWork_savesWorkToDatabase() {
        int userId = createUser("artist", "pass", "USER");
        String token = createToken(userId);
        int roomId = 1;

        when(roomManager.exists(roomId)).thenReturn(true);
        when(roomManager.getCanvasState(roomId)).thenReturn(
                new common.dto.room.CanvasStateResponse(CANVAS_W, CANVAS_H, new int[CANVAS_W * CANVAS_H], false, java.util.List.of())
        );

        saveWorkHandler.handle(buildPacket(1L, 1L, CommandType.SAVE_WORK, roomId,
                JsonUtil.toBytes(new SaveWorkRequest(token, "Saved Title", false))));

        var works = savedWorkDao.findAllByOwner(userId);
        assertEquals(1, works.size());
        assertEquals("Saved Title", works.get(0).title());
        assertFalse(works.get(0).isPublic());
    }

    @Test
    void saveWork_unauthorizedToken_returnsError() {
        int roomId = 1;
        when(roomManager.exists(roomId)).thenReturn(true);

        Packet request = buildPacket(1L, 2L, CommandType.SAVE_WORK, roomId,
                JsonUtil.toBytes(new SaveWorkRequest("bad-token", "title", true)));

        saveWorkHandler.handle(request);

        assertErrorMessage(lastSent(), "Unauthorized");
    }

    @Test
    void saveWork_roomNotFound_returnsError() {
        int userId = createUser("artist", "pass", "USER");
        String token = createToken(userId);

        when(roomManager.exists(999)).thenReturn(false);

        Packet request = buildPacket(1L, 3L, CommandType.SAVE_WORK, 999,
                JsonUtil.toBytes(new SaveWorkRequest(token, "title", true)));

        saveWorkHandler.handle(request);

        assertErrorMessage(lastSent(), "Room not found");
    }

    @Test
    void saveWork_pktIdEchoed() {
        int userId = createUser("artist", "pass", "USER");
        String token = createToken(userId);
        int roomId = 1;
        long pktId = 55L;

        when(roomManager.exists(roomId)).thenReturn(true);
        when(roomManager.getCanvasState(roomId)).thenReturn(
                new common.dto.room.CanvasStateResponse(CANVAS_W, CANVAS_H, new int[CANVAS_W * CANVAS_H], false, java.util.List.of())
        );

        saveWorkHandler.handle(buildPacket(1L, pktId, CommandType.SAVE_WORK, roomId,
                JsonUtil.toBytes(new SaveWorkRequest(token, "title", true))));

        assertEquals(pktId, lastSent().bPktId());
    }

    // ===== DeleteWorkHandler =====

    @Test
    void deleteWork_owner_returnsOk() {
        int userId = createUser("owner", "pass", "USER");
        String token = createToken(userId);
        int workId = saveWork(userId, "My Work", true);

        Packet request = buildPacket(1L, 1L, CommandType.DELETE_WORK, 0,
                JsonUtil.toBytes(new DeleteWorkRequest(token, workId)));

        deleteWorkHandler.handle(request);

        assertOk(lastSent());
        assertPktIdEchoed(request, lastSent());
    }

    @Test
    void deleteWork_owner_workRemovedFromDatabase() {
        int userId = createUser("owner", "pass", "USER");
        String token = createToken(userId);
        int workId = saveWork(userId, "To Delete", true);

        deleteWorkHandler.handle(buildPacket(1L, 1L, CommandType.DELETE_WORK, 0,
                JsonUtil.toBytes(new DeleteWorkRequest(token, workId))));

        assertTrue(savedWorkDao.findById(workId).isEmpty());
    }

    @Test
    void deleteWork_notOwner_notAdmin_returnsForbidden() {
        int ownerId = createUser("owner", "pass", "USER");
        int otherId = createUser("other", "pass", "USER");
        String otherToken = createToken(otherId);
        int workId = saveWork(ownerId, "Owner's Work", true);

        Packet request = buildPacket(2L, 2L, CommandType.DELETE_WORK, 0,
                JsonUtil.toBytes(new DeleteWorkRequest(otherToken, workId)));

        deleteWorkHandler.handle(request);

        assertErrorMessage(lastSent(), "Forbidden");
    }

    @Test
    void deleteWork_admin_canDeleteAnyWork() {
        int ownerId = createUser("owner", "pass", "USER");
        int adminId = createUser("admin", "pass", "ADMIN");
        String adminToken = createToken(adminId);
        int workId = saveWork(ownerId, "Owner's Work", true);

        deleteWorkHandler.handle(buildPacket(2L, 3L, CommandType.DELETE_WORK, 0,
                JsonUtil.toBytes(new DeleteWorkRequest(adminToken, workId))));

        assertOk(lastSent());
        assertTrue(savedWorkDao.findById(workId).isEmpty());
    }

    @Test
    void deleteWork_invalidToken_returnsError() {
        Packet request = buildPacket(1L, 4L, CommandType.DELETE_WORK, 0,
                JsonUtil.toBytes(new DeleteWorkRequest("bad-token", 1)));

        deleteWorkHandler.handle(request);

        assertErrorMessage(lastSent(), "Unauthorized");
    }

    @Test
    void deleteWork_workNotFound_returnsError() {
        int userId = createUser("user", "pass", "USER");
        String token = createToken(userId);

        Packet request = buildPacket(1L, 5L, CommandType.DELETE_WORK, 0,
                JsonUtil.toBytes(new DeleteWorkRequest(token, 9999)));

        deleteWorkHandler.handle(request);

        assertErrorMessage(lastSent(), "Work not found");
    }

    // ---- Хелпер ----

    private int saveWork(int ownerId, String title, boolean isPublic) {
        byte[] imageData = new byte[CANVAS_W * CANVAS_H * 4];
        return (int) savedWorkDao.save(SavedWork.builder()
                .ownerId(ownerId)
                .title(title)
                .isPublic(isPublic)
                .imageData(imageData)
                .canvasW(CANVAS_W)
                .canvasH(CANVAS_H)
                .build());
    }
}