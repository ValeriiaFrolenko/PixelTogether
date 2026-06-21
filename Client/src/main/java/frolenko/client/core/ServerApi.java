package frolenko.client.core;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import common.dto.draw.DrawRequest;
import common.dto.draw.PixelUpdate;
import common.dto.room.*;
import common.dto.user.LoginRequest;
import common.dto.user.LogoutRequest;
import common.dto.user.RegisterRequest;
import common.dto.work.*;
import common.model.Message;
import common.model.Packet;
import common.protocol.CommandType;
import common.utils.JsonUtil;
import frolenko.client.network.ClientSenderService;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

@Singleton
public class ServerApi {

    private final ClientSenderService sender;
    private final AppState appState;
    private final AtomicLong pktIdCounter = new AtomicLong(1);

    @Inject
    public ServerApi(ClientSenderService sender, AppState appState) {
        this.sender = sender;
        this.appState = appState;
    }

    // --- AUTH ---

    public void register(String username, String password, Consumer<Packet> callback) {
        send(CommandType.REGISTER, 0,
                JsonUtil.toBytes(new RegisterRequest(username, password)),
                callback);
    }

    public void login(String username, String password, Consumer<Packet> callback) {
        send(CommandType.LOGIN, 0,
                JsonUtil.toBytes(new LoginRequest(username, password)),
                callback);
    }

    public void logout(String token, Consumer<Packet> callback) {
        send(CommandType.LOGOUT, 0,
                JsonUtil.toBytes(new LogoutRequest(token)),
                callback);
    }

    // --- ROOM ---

    public void getRooms(Consumer<Packet> callback) {
        send(CommandType.GET_ROOMS, 0, new byte[0], callback);
    }

    public void createRoom(String token, String name, int canvasW, int canvasH,
                           boolean isPrivate, long durationMinutes, Consumer<Packet> callback) {
        send(CommandType.CREATE_ROOM, 0,
                JsonUtil.toBytes(new CreateRoomRequest(token, name, canvasW, canvasH, isPrivate, durationMinutes)),
                callback);
    }

    public void joinPublic(int roomId, Consumer<Packet> callback) {
        send(CommandType.JOIN_ROOM_PUBLIC, roomId, new byte[0], callback);
    }

    public void joinPrivate(String code, Consumer<Packet> callback) {
        send(CommandType.JOIN_ROOM_PRIVATE, 0,
                JsonUtil.toBytes(new JoinRoomPrivateRequest(code)),
                callback);
    }

    public void leaveRoom(int roomId, Consumer<Packet> callback) {
        send(CommandType.LEAVE_ROOM, roomId, new byte[0], callback);
    }

    public void closeRoom(int roomId, String token, Consumer<Packet> callback) {
        send(CommandType.CLOSE_ROOM, roomId,
                JsonUtil.toBytes(new CloseRoomRequest(token)),
                callback);
    }

    // --- DRAW ---

    public void draw(int roomId, List<PixelUpdate> pixels, Consumer<Packet> callback) {
        send(CommandType.DRAW, roomId,
                JsonUtil.toBytes(new DrawRequest(pixels)),
                callback);
    }

    // --- WORK ---

    public void saveWork(int roomId, String token, String title, boolean isPublic, Consumer<Packet> callback) {
        send(CommandType.SAVE_WORK, roomId,
                JsonUtil.toBytes(new SaveWorkRequest(token, title, isPublic)),
                callback);
    }

    public void getGallery(String title, String ownerUsername, Consumer<Packet> callback) {
        send(CommandType.GET_GALLERY, 0,
                JsonUtil.toBytes(new GetGalleryRequest(title, ownerUsername)),
                callback);
    }

    public void getWork(int workId, Consumer<Packet> callback) {
        send(CommandType.GET_WORK, 0,
                JsonUtil.toBytes(new GetWorkRequest(workId)),
                callback);
    }

    public void deleteWork(String token, int workId, Consumer<Packet> callback) {
        send(CommandType.DELETE_WORK, 0,
                JsonUtil.toBytes(new DeleteWorkRequest(token, workId)),
                callback);
    }

    // --- INTERNAL ---

    private void send(CommandType commandType, int roomId, byte[] payload, Consumer<Packet> callback) {
        long pktId = pktIdCounter.getAndIncrement();
        Packet packet = Packet.builder()
                .sessionId(appState.getSessionId())
                .bPktId(pktId)
                .bMsg(Message.builder()
                        .cType(commandType.getCode())
                        .roomId(roomId)
                        .payload(payload)
                        .build())
                .build();
        sender.send(packet, callback);
    }
}