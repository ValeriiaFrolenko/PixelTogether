package frolenko.client.controller;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import frolenko.client.config.AppView;
import frolenko.client.core.AppState;
import frolenko.client.core.RoomState;
import frolenko.client.core.ViewManager;
import frolenko.client.service.DrawService;
import frolenko.client.ui.CanvasPane;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;

@Singleton
public class WorkController {

    private final AppState appState;
    private final ViewManager viewManager;
    private final DrawService drawService;

    @FXML private AnchorPane canvasContainer;
    @FXML private Label titleLabel;

    private CanvasPane canvasPane;
    private boolean listenerRegistered = false;

    @Inject
    public WorkController(AppState appState, ViewManager viewManager, DrawService drawService) {
        this.appState = appState;
        this.viewManager = viewManager;
        this.drawService = drawService;
    }

    @FXML
    public void initialize() {
        if (!listenerRegistered) {
            appState.currentRoomProperty().addListener((obs, old, room) -> {
                if (room != null) render(room);
            });
            listenerRegistered = true;
        }
    }

    private void render(RoomState room) {
        canvasContainer.getChildren().clear();

        canvasPane = new CanvasPane(room, drawService, true);
        canvasPane.widthProperty().bind(canvasContainer.widthProperty());
        canvasPane.heightProperty().bind(canvasContainer.heightProperty());

        AnchorPane.setTopAnchor(canvasPane, 0.0);
        AnchorPane.setBottomAnchor(canvasPane, 0.0);
        AnchorPane.setLeftAnchor(canvasPane, 0.0);
        AnchorPane.setRightAnchor(canvasPane, 0.0);

        canvasContainer.getChildren().add(canvasPane);
    }

    @FXML
    public void onBack() {
        appState.setCurrentRoom(null);
        viewManager.clearCache();
        viewManager.navigateTo(AppView.MAIN);
    }
}