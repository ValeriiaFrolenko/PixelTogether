package frolenko.client.handler;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import common.dto.draw.DrawRequest;
import common.model.Packet;
import common.utils.JsonUtil;
import frolenko.client.core.AppState;
import frolenko.client.core.RoomState;
import javafx.application.Platform;

import java.util.function.Consumer;

@Singleton
public class DrawPushHandler implements Consumer<Packet> {

    private final AppState appState;

    @Inject
    public DrawPushHandler(AppState appState) {
        this.appState = appState;
    }

    @Override
    public void accept(Packet packet) {
        RoomState room = appState.getCurrentRoom();
        if (room == null) return;

        DrawRequest request = JsonUtil.fromBytes(packet.bMsg().payload(), DrawRequest.class);

        Platform.runLater(() ->
                request.pixels().forEach(p -> room.setPixel(p.x(), p.y(), p.color()))
        );
    }
}
