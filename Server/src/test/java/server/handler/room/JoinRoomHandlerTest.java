package server.handler.room;

import common.dto.room.CanvasStateResponse;
import common.dto.room.JoinRoomPrivateRequest;
import common.dto.room.JoinRoomPublicRequest;
import common.model.Packet;
import common.protocol.CommandType;
import common.utils.JsonUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.core.ParticipantManager;
import server.core.RoomManager;
import server.database.model.Room;
import server.handler.BaseHandlerTest;
import server.network.ConnectionManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class JoinRoomHandlerTest extends BaseHandlerTest {

    private JoinRoomPublicHandler publicHandler;
    private JoinRoomPrivateHandler privateHandler;

    private RoomManager roomManager;
    private ParticipantManager participantManager;
    private ConnectionManager connectionManager;

    private static final long SESSION_ID = 42L;
    private static final int ROOM_ID = 10;
    private static final int CANVAS_W = 8;
    private static final int CANVAS_H = 8;

    @BeforeEach
    void setUp() {
        roomManager = mock(RoomManager.class);
        participantManager = mock(ParticipantManager.class);
        connectionManager = mock(ConnectionManager.class);

        publicHandler = new JoinRoomPublicHandler(
                roomManager, participantManager, connectionManager,
                authTokenDao, userDao, dispatcher);

        privateHandler = new JoinRoomPrivateHandler(
                roomManager, participantManager, connectionManager,
                authTokenDao, userDao, dispatcher);
    }

    // ===== JoinRoomPublicHandler =====

    @Test
    void joinPublic_roomExists_returnsCanvasState() {
        int ownerId = createUser("owner", "pass", "USER");
        Room room = buildRoom(ROOM_ID, ownerId);

        when(roomManager.exists(ROOM_ID)).thenReturn(true);
        when(roomManager.getRoom(ROOM_ID)).thenReturn(room);
        when(roomManager.getCanvasState(ROOM_ID)).thenReturn(emptyCanvas());
        when(connectionManager.getSessionSocketsByRoom(ROOM_ID)).thenReturn(Map.of());

        Packet request = buildPacket(SESSION_ID, 1L, CommandType.JOIN_ROOM_PUBLIC, ROOM_ID,
                JsonUtil.toBytes(new JoinRoomPublicRequest(null)));

        publicHandler.handle(request);

        Packet response = lastSent();
        assertCmdType(response, CommandType.CANVAS_STATE);
        assertPktIdEchoed(request, response);
        assertEquals(ROOM_ID, response.bMsg().roomId());
    }

    @Test
    void joinPublic_roomNotFound_returnsError() {
        when(roomManager.exists(ROOM_ID)).thenReturn(false);

        Packet request = buildPacket(SESSION_ID, 2L, CommandType.JOIN_ROOM_PUBLIC, ROOM_ID,
                JsonUtil.toBytes(new JoinRoomPublicRequest(null)));

        publicHandler.handle(request);

        assertErrorMessage(lastSent(), "Room not found");
        assertPktIdEchoed(request, lastSent());
    }

    @Test
    void joinPublic_withOwnerToken_isOwnerTrue() {
        int ownerId = createUser("owner", "pass", "USER");
        String ownerToken = createToken(ownerId);
        Room room = buildRoom(ROOM_ID, ownerId);

        when(roomManager.exists(ROOM_ID)).thenReturn(true);
        when(roomManager.getRoom(ROOM_ID)).thenReturn(room);
        when(roomManager.getCanvasState(ROOM_ID)).thenReturn(emptyCanvas());
        when(connectionManager.getSessionSocketsByRoom(ROOM_ID)).thenReturn(Map.of());

        publicHandler.handle(buildPacket(SESSION_ID, 3L, CommandType.JOIN_ROOM_PUBLIC, ROOM_ID,
                JsonUtil.toBytes(new JoinRoomPublicRequest(ownerToken))));

        CanvasStateResponse canvas = JsonUtil.fromBytes(
                lastSent().bMsg().payload(), CanvasStateResponse.class);
        assertTrue(canvas.isOwner());
    }

    @Test
    void joinPublic_withNonOwnerToken_isOwnerFalse() {
        int ownerId = createUser("owner", "pass", "USER");
        int otherId = createUser("other", "pass", "USER");
        String otherToken = createToken(otherId);
        Room room = buildRoom(ROOM_ID, ownerId);

        when(roomManager.exists(ROOM_ID)).thenReturn(true);
        when(roomManager.getRoom(ROOM_ID)).thenReturn(room);
        when(roomManager.getCanvasState(ROOM_ID)).thenReturn(emptyCanvas());
        when(connectionManager.getSessionSocketsByRoom(ROOM_ID)).thenReturn(Map.of());

        publicHandler.handle(buildPacket(SESSION_ID, 4L, CommandType.JOIN_ROOM_PUBLIC, ROOM_ID,
                JsonUtil.toBytes(new JoinRoomPublicRequest(otherToken))));

        CanvasStateResponse canvas = JsonUtil.fromBytes(
                lastSent().bMsg().payload(), CanvasStateResponse.class);
        assertFalse(canvas.isOwner());
    }

    @Test
    void joinPublic_withNullToken_isOwnerFalse() {
        int ownerId = createUser("owner", "pass", "USER");
        Room room = buildRoom(ROOM_ID, ownerId);

        when(roomManager.exists(ROOM_ID)).thenReturn(true);
        when(roomManager.getRoom(ROOM_ID)).thenReturn(room);
        when(roomManager.getCanvasState(ROOM_ID)).thenReturn(emptyCanvas());
        when(connectionManager.getSessionSocketsByRoom(ROOM_ID)).thenReturn(Map.of());

        publicHandler.handle(buildPacket(SESSION_ID, 5L, CommandType.JOIN_ROOM_PUBLIC, ROOM_ID,
                JsonUtil.toBytes(new JoinRoomPublicRequest(null))));

        CanvasStateResponse canvas = JsonUtil.fromBytes(
                lastSent().bMsg().payload(), CanvasStateResponse.class);
        assertFalse(canvas.isOwner());
    }

    @Test
    void joinPublic_withValidToken_assignsUsernameAsNickname() {
        int ownerId = createUser("owner", "pass", "USER");
        int userId = createUser("valeria", "pass", "USER");
        String token = createToken(userId);
        Room room = buildRoom(ROOM_ID, ownerId);

        when(roomManager.exists(ROOM_ID)).thenReturn(true);
        when(roomManager.getRoom(ROOM_ID)).thenReturn(room);
        when(roomManager.getCanvasState(ROOM_ID)).thenReturn(emptyCanvas());
        when(connectionManager.getSessionSocketsByRoom(ROOM_ID)).thenReturn(Map.of());

        publicHandler.handle(buildPacket(SESSION_ID, 6L, CommandType.JOIN_ROOM_PUBLIC, ROOM_ID,
                JsonUtil.toBytes(new JoinRoomPublicRequest(token))));

        verify(participantManager).assign(SESSION_ID, "valeria");
    }

    @Test
    void joinPublic_withNullToken_assignsGeneratedNickname() {
        int ownerId = createUser("owner", "pass", "USER");
        Room room = buildRoom(ROOM_ID, ownerId);

        when(roomManager.exists(ROOM_ID)).thenReturn(true);
        when(roomManager.getRoom(ROOM_ID)).thenReturn(room);
        when(roomManager.getCanvasState(ROOM_ID)).thenReturn(emptyCanvas());
        when(connectionManager.getSessionSocketsByRoom(ROOM_ID)).thenReturn(Map.of());

        publicHandler.handle(buildPacket(SESSION_ID, 7L, CommandType.JOIN_ROOM_PUBLIC, ROOM_ID,
                JsonUtil.toBytes(new JoinRoomPublicRequest(null))));

        // перевіряємо що assign викликався з будь-яким рядком що починається з "Unknown "
        verify(participantManager).assign(eq(SESSION_ID), argThat(n -> n.startsWith("Unknown ")));
    }

    @Test
    void joinPublic_assignsSessionToRoom() {
        int ownerId = createUser("owner", "pass", "USER");
        Room room = buildRoom(ROOM_ID, ownerId);

        when(roomManager.exists(ROOM_ID)).thenReturn(true);
        when(roomManager.getRoom(ROOM_ID)).thenReturn(room);
        when(roomManager.getCanvasState(ROOM_ID)).thenReturn(emptyCanvas());
        when(connectionManager.getSessionSocketsByRoom(ROOM_ID)).thenReturn(Map.of());

        publicHandler.handle(buildPacket(SESSION_ID, 8L, CommandType.JOIN_ROOM_PUBLIC, ROOM_ID,
                JsonUtil.toBytes(new JoinRoomPublicRequest(null))));

        verify(connectionManager).assignRoom(SESSION_ID, ROOM_ID);
    }

    @Test
    void joinPublic_broadcastsParticipantJoined() {
        int ownerId = createUser("owner", "pass", "USER");
        Room room = buildRoom(ROOM_ID, ownerId);

        when(roomManager.exists(ROOM_ID)).thenReturn(true);
        when(roomManager.getRoom(ROOM_ID)).thenReturn(room);
        when(roomManager.getCanvasState(ROOM_ID)).thenReturn(emptyCanvas());
        when(connectionManager.getSessionSocketsByRoom(ROOM_ID)).thenReturn(Map.of());

        publicHandler.handle(buildPacket(SESSION_ID, 9L, CommandType.JOIN_ROOM_PUBLIC, ROOM_ID,
                JsonUtil.toBytes(new JoinRoomPublicRequest(null))));

        // sendToClient — відповідь клієнту, sendToRoom — бродкаст
        // dispatcher.sendToRoom має бути викликаний рівно 1 раз з PARTICIPANT_JOINED
        verify(dispatcher).sendToRoom(eq(ROOM_ID), argThat(msg ->
                msg.cType() == CommandType.PARTICIPANT_JOINED.getCode()));
    }

    @Test
    void joinPublic_canvasStateContainsCurrentNicknames() {
        int ownerId = createUser("owner", "pass", "USER");
        Room room = buildRoom(ROOM_ID, ownerId);

        long alreadyInSession = 99L;
        when(roomManager.exists(ROOM_ID)).thenReturn(true);
        when(roomManager.getRoom(ROOM_ID)).thenReturn(room);
        when(roomManager.getCanvasState(ROOM_ID)).thenReturn(emptyCanvas());
        // симулюємо що в кімнаті вже є один учасник
        when(connectionManager.getSessionSocketsByRoom(ROOM_ID))
                .thenReturn(Map.of(alreadyInSession, mock(java.net.Socket.class)));
        when(participantManager.get(alreadyInSession)).thenReturn("alice");

        publicHandler.handle(buildPacket(SESSION_ID, 10L, CommandType.JOIN_ROOM_PUBLIC, ROOM_ID,
                JsonUtil.toBytes(new JoinRoomPublicRequest(null))));

        CanvasStateResponse canvas = JsonUtil.fromBytes(
                lastSent().bMsg().payload(), CanvasStateResponse.class);
        assertTrue(canvas.nicknames().contains("alice"));
    }

    @Test
    void joinPublic_pktIdEchoed() {
        int ownerId = createUser("owner", "pass", "USER");
        Room room = buildRoom(ROOM_ID, ownerId);
        long pktId = 321L;

        when(roomManager.exists(ROOM_ID)).thenReturn(true);
        when(roomManager.getRoom(ROOM_ID)).thenReturn(room);
        when(roomManager.getCanvasState(ROOM_ID)).thenReturn(emptyCanvas());
        when(connectionManager.getSessionSocketsByRoom(ROOM_ID)).thenReturn(Map.of());

        publicHandler.handle(buildPacket(SESSION_ID, pktId, CommandType.JOIN_ROOM_PUBLIC, ROOM_ID,
                JsonUtil.toBytes(new JoinRoomPublicRequest(null))));

        assertEquals(pktId, lastSent().bPktId());
    }

    // ===== JoinRoomPrivateHandler =====

    @Test
    void joinPrivate_correctCode_returnsCanvasState() {
        int ownerId = createUser("owner", "pass", "USER");
        Room room = buildRoom(ROOM_ID, ownerId);

        when(roomManager.findByCode("ABCD1234")).thenReturn(Optional.of(room));
        when(roomManager.getCanvasState(ROOM_ID)).thenReturn(emptyCanvas());
        when(connectionManager.getSessionSocketsByRoom(ROOM_ID)).thenReturn(Map.of());

        Packet request = buildPacket(SESSION_ID, 1L, CommandType.JOIN_ROOM_PRIVATE, 0,
                JsonUtil.toBytes(new JoinRoomPrivateRequest("ABCD1234", null)));

        privateHandler.handle(request);

        Packet response = lastSent();
        assertCmdType(response, CommandType.CANVAS_STATE);
        assertPktIdEchoed(request, response);
        assertEquals(ROOM_ID, response.bMsg().roomId());
    }

    @Test
    void joinPrivate_wrongCode_returnsUnauthorized() {
        when(roomManager.findByCode("BADCODE1")).thenReturn(Optional.empty());

        Packet request = buildPacket(SESSION_ID, 2L, CommandType.JOIN_ROOM_PRIVATE, 0,
                JsonUtil.toBytes(new JoinRoomPrivateRequest("BADCODE1", null)));

        privateHandler.handle(request);

        assertErrorMessage(lastSent(), "Unauthorized");
        assertPktIdEchoed(request, lastSent());
    }

    @Test
    void joinPrivate_withOwnerToken_isOwnerTrue() {
        int ownerId = createUser("owner", "pass", "USER");
        String ownerToken = createToken(ownerId);
        Room room = buildRoom(ROOM_ID, ownerId);

        when(roomManager.findByCode("OWNERCODE")).thenReturn(Optional.of(room));
        when(roomManager.getCanvasState(ROOM_ID)).thenReturn(emptyCanvas());
        when(connectionManager.getSessionSocketsByRoom(ROOM_ID)).thenReturn(Map.of());

        privateHandler.handle(buildPacket(SESSION_ID, 3L, CommandType.JOIN_ROOM_PRIVATE, 0,
                JsonUtil.toBytes(new JoinRoomPrivateRequest("OWNERCODE", ownerToken))));

        CanvasStateResponse canvas = JsonUtil.fromBytes(
                lastSent().bMsg().payload(), CanvasStateResponse.class);
        assertTrue(canvas.isOwner());
    }

    @Test
    void joinPrivate_withNonOwnerToken_isOwnerFalse() {
        int ownerId = createUser("owner", "pass", "USER");
        int otherId = createUser("other", "pass", "USER");
        String otherToken = createToken(otherId);
        Room room = buildRoom(ROOM_ID, ownerId);

        when(roomManager.findByCode("PRIVCODE1")).thenReturn(Optional.of(room));
        when(roomManager.getCanvasState(ROOM_ID)).thenReturn(emptyCanvas());
        when(connectionManager.getSessionSocketsByRoom(ROOM_ID)).thenReturn(Map.of());

        privateHandler.handle(buildPacket(SESSION_ID, 4L, CommandType.JOIN_ROOM_PRIVATE, 0,
                JsonUtil.toBytes(new JoinRoomPrivateRequest("PRIVCODE1", otherToken))));

        CanvasStateResponse canvas = JsonUtil.fromBytes(
                lastSent().bMsg().payload(), CanvasStateResponse.class);
        assertFalse(canvas.isOwner());
    }

    @Test
    void joinPrivate_withValidToken_assignsUsernameAsNickname() {
        int ownerId = createUser("owner", "pass", "USER");
        int userId = createUser("valeria", "pass", "USER");
        String token = createToken(userId);
        Room room = buildRoom(ROOM_ID, ownerId);

        when(roomManager.findByCode("NICKCODE1")).thenReturn(Optional.of(room));
        when(roomManager.getCanvasState(ROOM_ID)).thenReturn(emptyCanvas());
        when(connectionManager.getSessionSocketsByRoom(ROOM_ID)).thenReturn(Map.of());

        privateHandler.handle(buildPacket(SESSION_ID, 5L, CommandType.JOIN_ROOM_PRIVATE, 0,
                JsonUtil.toBytes(new JoinRoomPrivateRequest("NICKCODE1", token))));

        verify(participantManager).assign(SESSION_ID, "valeria");
    }

    @Test
    void joinPrivate_withNullToken_assignsGeneratedNickname() {
        int ownerId = createUser("owner", "pass", "USER");
        Room room = buildRoom(ROOM_ID, ownerId);

        when(roomManager.findByCode("GUESTCOD1")).thenReturn(Optional.of(room));
        when(roomManager.getCanvasState(ROOM_ID)).thenReturn(emptyCanvas());
        when(connectionManager.getSessionSocketsByRoom(ROOM_ID)).thenReturn(Map.of());

        privateHandler.handle(buildPacket(SESSION_ID, 6L, CommandType.JOIN_ROOM_PRIVATE, 0,
                JsonUtil.toBytes(new JoinRoomPrivateRequest("GUESTCOD1", null))));

        verify(participantManager).assign(eq(SESSION_ID), argThat(n -> n.startsWith("Unknown ")));
    }

    @Test
    void joinPrivate_assignsSessionToRoom() {
        int ownerId = createUser("owner", "pass", "USER");
        Room room = buildRoom(ROOM_ID, ownerId);

        when(roomManager.findByCode("ASSIGNCD")).thenReturn(Optional.of(room));
        when(roomManager.getCanvasState(ROOM_ID)).thenReturn(emptyCanvas());
        when(connectionManager.getSessionSocketsByRoom(ROOM_ID)).thenReturn(Map.of());

        privateHandler.handle(buildPacket(SESSION_ID, 7L, CommandType.JOIN_ROOM_PRIVATE, 0,
                JsonUtil.toBytes(new JoinRoomPrivateRequest("ASSIGNCD", null))));

        verify(connectionManager).assignRoom(SESSION_ID, ROOM_ID);
    }

    @Test
    void joinPrivate_broadcastsParticipantJoined() {
        int ownerId = createUser("owner", "pass", "USER");
        Room room = buildRoom(ROOM_ID, ownerId);

        when(roomManager.findByCode("BRDCSTCD")).thenReturn(Optional.of(room));
        when(roomManager.getCanvasState(ROOM_ID)).thenReturn(emptyCanvas());
        when(connectionManager.getSessionSocketsByRoom(ROOM_ID)).thenReturn(Map.of());

        privateHandler.handle(buildPacket(SESSION_ID, 8L, CommandType.JOIN_ROOM_PRIVATE, 0,
                JsonUtil.toBytes(new JoinRoomPrivateRequest("BRDCSTCD", null))));

        verify(dispatcher).sendToRoom(eq(ROOM_ID), argThat(msg ->
                msg.cType() == CommandType.PARTICIPANT_JOINED.getCode()));
    }

    @Test
    void joinPrivate_pktIdEchoed() {
        int ownerId = createUser("owner", "pass", "USER");
        Room room = buildRoom(ROOM_ID, ownerId);
        long pktId = 456L;

        when(roomManager.findByCode("PKTIDCOD")).thenReturn(Optional.of(room));
        when(roomManager.getCanvasState(ROOM_ID)).thenReturn(emptyCanvas());
        when(connectionManager.getSessionSocketsByRoom(ROOM_ID)).thenReturn(Map.of());

        privateHandler.handle(buildPacket(SESSION_ID, pktId, CommandType.JOIN_ROOM_PRIVATE, 0,
                JsonUtil.toBytes(new JoinRoomPrivateRequest("PKTIDCOD", null))));

        assertEquals(pktId, lastSent().bPktId());
    }

    // ---- Хелпери ----

    private Room buildRoom(int roomId, int ownerId) {
        return Room.builder()
                .id(roomId)
                .name("Test Room")
                .code("TESTCODE")
                .ownerId(ownerId)
                .isPrivate(false)
                .canvasW(CANVAS_W)
                .canvasH(CANVAS_H)
                .expiresAt(LocalDateTime.now().plusHours(1))
                .build();
    }

    private CanvasStateResponse emptyCanvas() {
        return new CanvasStateResponse(
                CANVAS_W, CANVAS_H,
                new int[CANVAS_W * CANVAS_H],
                false,
                List.of()
        );
    }
}