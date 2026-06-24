package frolenko.client.handler;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import common.model.Packet;
import frolenko.client.config.AppView;
import frolenko.client.core.AppState;
import frolenko.client.core.ViewManager;
import frolenko.client.util.AlertHelper;
import javafx.application.Platform;

import java.util.function.Consumer;

@Singleton
public class RoomClosedHandler implements Consumer<Packet> {

    private final AppState appState;
    private final ViewManager viewManager;

    @Inject
    public RoomClosedHandler(AppState appState, ViewManager viewManager) {
        this.appState = appState;
        this.viewManager = viewManager;
    }

    @Override
    public void accept(Packet packet) {
        Platform.runLater(() -> {
            appState.setCurrentRoom(null);
            viewManager.clearView(AppView.ROOM);
            viewManager.navigateTo(AppView.MAIN);
            AlertHelper.showInfo("Room closed", "This room has expired.");
        });
    }
}