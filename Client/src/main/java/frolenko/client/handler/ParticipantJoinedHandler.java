package frolenko.client.handler;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import common.dto.room.ParticipantEvent;
import common.model.Packet;
import common.utils.JsonUtil;
import frolenko.client.core.AppState;
import frolenko.client.core.RoomState;
import javafx.application.Platform;

import java.util.function.Consumer;

@Singleton
public class ParticipantJoinedHandler implements Consumer<Packet> {

    private final AppState appState;

    @Inject
    public ParticipantJoinedHandler(AppState appState) {
        this.appState = appState;
    }

    @Override
    public void accept(Packet packet) {
        RoomState room = appState.getCurrentRoom();
        if (room == null) return;

        ParticipantEvent event = JsonUtil.fromBytes(packet.bMsg().payload(), ParticipantEvent.class);

        Platform.runLater(() -> {
            if (!room.getNicknames().contains(event.nickname())) {
                room.getNicknames().add(event.nickname());
            }
        });
    }
}