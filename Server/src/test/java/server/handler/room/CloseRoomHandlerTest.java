package server.handler.room;

import common.dto.room.CloseRoomRequest;
import common.model.Packet;
import common.protocol.CommandType;
import common.utils.JsonUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.core.RoomManager;
import server.handler.BaseHandlerTest;

import static org.mockito.Mockito.*;

class CloseRoomHandlerTest extends BaseHandlerTest {

    private CloseRoomHandler handler;
    private RoomManager roomManager;

    @BeforeEach
    void setUp() {
        roomManager = mock(RoomManager.class);
        handler = new CloseRoomHandler(roomManager, authTokenDao, userDao, dispatcher);
    }

    @Test
    void closeRoom_owner_returnsOkAndDeletesRoom() {
        int ownerId = createUser("owner", "pass", "USER");
        String token = createToken(ownerId);
        int roomId = 10;

        server.database.model.Room room = buildRoom(roomId, ownerId);
        when(roomManager.exists(roomId)).thenReturn(true);
        when(roomManager.getRoom(roomId)).thenReturn(room);

        Packet request = buildPacket(1L, 1L, CommandType.CLOSE_ROOM, roomId,
                JsonUtil.toBytes(new CloseRoomRequest(token)));

        handler.handle(request);

        assertOk(lastSent());
        assertPktIdEchoed(request, lastSent());
        verify(roomManager).deleteRoom(roomId);
    }

    @Test
    void closeRoom_admin_canCloseAnyRoom() {
        int ownerId = createUser("owner", "pass", "USER");
        int adminId = createUser("admin", "pass", "ADMIN");
        String adminToken = createToken(adminId);
        int roomId = 10;

        server.database.model.Room room = buildRoom(roomId, ownerId);
        when(roomManager.exists(roomId)).thenReturn(true);
        when(roomManager.getRoom(roomId)).thenReturn(room);

        handler.handle(buildPacket(2L, 2L, CommandType.CLOSE_ROOM, roomId,
                JsonUtil.toBytes(new CloseRoomRequest(adminToken))));

        assertOk(lastSent());
        verify(roomManager).deleteRoom(roomId);
    }

    @Test
    void closeRoom_notOwnerNotAdmin_returnsForbidden() {
        int ownerId = createUser("owner", "pass", "USER");
        int otherId = createUser("other", "pass", "USER");
        String otherToken = createToken(otherId);
        int roomId = 10;

        server.database.model.Room room = buildRoom(roomId, ownerId);
        when(roomManager.exists(roomId)).thenReturn(true);
        when(roomManager.getRoom(roomId)).thenReturn(room);

        handler.handle(buildPacket(2L, 3L, CommandType.CLOSE_ROOM, roomId,
                JsonUtil.toBytes(new CloseRoomRequest(otherToken))));

        assertErrorMessage(lastSent(), "Forbidden");
        verify(roomManager, never()).deleteRoom(roomId);
    }

    @Test
    void closeRoom_invalidToken_returnsUnauthorized() {
        int roomId = 10;
        when(roomManager.exists(roomId)).thenReturn(true);

        handler.handle(buildPacket(1L, 4L, CommandType.CLOSE_ROOM, roomId,
                JsonUtil.toBytes(new CloseRoomRequest("bad-token"))));

        assertErrorMessage(lastSent(), "Unauthorized");
        verify(roomManager, never()).deleteRoom(anyInt());
    }

    @Test
    void closeRoom_roomNotFound_returnsError() {
        int userId = createUser("user", "pass", "USER");
        String token = createToken(userId);
        int roomId = 999;

        when(roomManager.exists(roomId)).thenReturn(false);

        handler.handle(buildPacket(1L, 5L, CommandType.CLOSE_ROOM, roomId,
                JsonUtil.toBytes(new CloseRoomRequest(token))));

        assertErrorMessage(lastSent(), "Room not found");
        verify(roomManager, never()).deleteRoom(anyInt());
    }

    @Test
    void closeRoom_pktIdEchoed() {
        int ownerId = createUser("owner", "pass", "USER");
        String token = createToken(ownerId);
        int roomId = 10;
        long pktId = 123L;

        when(roomManager.exists(roomId)).thenReturn(true);
        when(roomManager.getRoom(roomId)).thenReturn(buildRoom(roomId, ownerId));

        handler.handle(buildPacket(1L, pktId, CommandType.CLOSE_ROOM, roomId,
                JsonUtil.toBytes(new CloseRoomRequest(token))));

        org.junit.jupiter.api.Assertions.assertEquals(pktId, lastSent().bPktId());
    }

    private server.database.model.Room buildRoom(int roomId, int ownerId) {
        return server.database.model.Room.builder()
                .id(roomId)
                .name("Test Room")
                .ownerId(ownerId)
                .isPrivate(false)
                .canvasW(16)
                .canvasH(16)
                .expiresAt(java.time.LocalDateTime.now().plusHours(1))
                .build();
    }
}