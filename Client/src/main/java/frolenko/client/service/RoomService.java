package frolenko.client.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import common.dto.ErrorResponse;
import common.dto.room.CanvasStateResponse;
import common.dto.room.MyRoomInfo;
import common.dto.room.RoomInfo;
import common.protocol.CommandType;
import common.utils.JsonUtil;
import frolenko.client.core.AppState;
import frolenko.client.core.RoomState;
import frolenko.client.core.ServerApi;

import java.util.List;
import java.util.function.Consumer;

@Singleton
public class RoomService {

    private final ServerApi serverApi;
    private final AppState appState;

    @Inject
    public RoomService(ServerApi serverApi, AppState appState) {
        this.serverApi = serverApi;
        this.appState = appState;
    }

    public void getRooms(Consumer<List<RoomInfo>> onSuccess, Consumer<String> onError) {
        serverApi.getRooms(packet -> {
            if (packet.bMsg().cType() == CommandType.ROOM_LIST.getCode()) {
                List<RoomInfo> rooms = List.of(JsonUtil.fromBytes(packet.bMsg().payload(), RoomInfo[].class));
                onSuccess.accept(rooms);
            } else {
                onError.accept(extractError(packet));
            }
        });
    }

    public void getMyRooms(String token, Consumer<List<MyRoomInfo>> onSuccess, Consumer<String> onError) {
        serverApi.getMyRooms(token, packet -> {
            if (packet.bMsg().cType() == CommandType.MY_ROOMS.getCode()) {
                List<MyRoomInfo> rooms = List.of(JsonUtil.fromBytes(packet.bMsg().payload(), MyRoomInfo[].class));
                onSuccess.accept(rooms);
            } else {
                onError.accept(extractError(packet));
            }
        });
    }

    public void joinPublic(int roomId, Consumer<RoomState> onSuccess, Consumer<String> onError) {
        serverApi.joinPublic(roomId, packet -> {
            if (packet.bMsg().cType() == CommandType.CANVAS_STATE.getCode()) {
                CanvasStateResponse canvas = JsonUtil.fromBytes(packet.bMsg().payload(), CanvasStateResponse.class);
                RoomState room = new RoomState(roomId, canvas.width(), canvas.height(), canvas.pixels(), canvas.isOwner());
                if (canvas.nicknames() != null) {
                    room.getNicknames().addAll(canvas.nicknames());
                }
                appState.setCurrentRoom(room);
                onSuccess.accept(room);
            } else {
                onError.accept(extractError(packet));
            }
        });
    }

    public void joinPrivate(String code, Consumer<RoomState> onSuccess, Consumer<String> onError) {
        serverApi.joinPrivate(code, packet -> {
            if (packet.bMsg().cType() == CommandType.CANVAS_STATE.getCode()) {
                CanvasStateResponse canvas = JsonUtil.fromBytes(packet.bMsg().payload(), CanvasStateResponse.class);
                RoomState room = new RoomState(packet.bMsg().roomId(), canvas.width(), canvas.height(), canvas.pixels(), canvas.isOwner());
                if (canvas.nicknames() != null) {
                    room.getNicknames().addAll(canvas.nicknames());
                }
                appState.setCurrentRoom(room);
                onSuccess.accept(room);
            } else {
                onError.accept(extractError(packet));
            }
        });
    }

    public void createRoom(String token, String name, int canvasW, int canvasH,
                           boolean isPrivate, long durationMinutes,
                           Consumer<RoomState> onSuccess, Consumer<String> onError) {
        serverApi.createRoom(token, name, canvasW, canvasH, isPrivate, durationMinutes, packet -> {
            if (packet.bMsg().cType() == CommandType.OK.getCode()) {
                RoomState room = new RoomState(packet.bMsg().roomId(), canvasW, canvasH, new int[canvasW * canvasH], true);
                appState.setCurrentRoom(room);
                onSuccess.accept(room);
            } else {
                onError.accept(extractError(packet));
            }
        });
    }

    public void leaveRoom(int roomId, Runnable onSuccess, Consumer<String> onError) {
        serverApi.leaveRoom(roomId, packet -> {
            if (packet.bMsg().cType() == CommandType.OK.getCode()) {
                appState.setCurrentRoom(null);
                onSuccess.run();
            } else {
                onError.accept(extractError(packet));
            }
        });
    }

    public void closeRoom(int roomId, String token, Runnable onSuccess, Consumer<String> onError) {
        serverApi.closeRoom(roomId, token, packet -> {
            if (packet.bMsg().cType() == CommandType.OK.getCode()) {
                appState.setCurrentRoom(null);
                onSuccess.run();
            } else {
                onError.accept(extractError(packet));
            }
        });
    }

    private String extractError(common.model.Packet packet) {
        return JsonUtil.fromBytes(packet.bMsg().payload(), ErrorResponse.class).message();
    }
}