package frolenko.client.controller;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import frolenko.client.config.AppView;
import frolenko.client.core.AppState;
import frolenko.client.core.RoomState;
import frolenko.client.core.ViewManager;
import frolenko.client.service.DrawService;
import frolenko.client.service.GalleryService;
import frolenko.client.service.RoomService;
import frolenko.client.ui.CanvasPane;
import frolenko.client.util.AlertHelper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;

@Singleton
public class RoomController {

    private final AppState appState;
    private final RoomService roomService;
    private final GalleryService galleryService;
    private final DrawService drawService;
    private final ViewManager viewManager;

    @FXML private AnchorPane canvasContainer;
    @FXML private ColorPicker colorPicker;
    @FXML private ListView<String> nicknameListView;
    @FXML private Button saveButton;
    @FXML private Button closeRoomButton;

    private CanvasPane canvasPane;
    private boolean listenerRegistered = false;

    @Inject
    public RoomController(AppState appState,
                          RoomService roomService,
                          GalleryService galleryService,
                          DrawService drawService,
                          ViewManager viewManager) {
        this.appState = appState;
        this.roomService = roomService;
        this.galleryService = galleryService;
        this.drawService = drawService;
        this.viewManager = viewManager;
    }

    @FXML
    public void initialize() {
        colorPicker.setValue(Color.BLACK);

        if (!listenerRegistered) {
            appState.currentRoomProperty().addListener((obs, old, room) ->
                    Platform.runLater(() -> setupRoom(room)));
            listenerRegistered = true;
        }

        RoomState current = appState.getCurrentRoom();
        if (current != null) setupRoom(current);
    }

    private void setupRoom(RoomState room) {
        if (room == null) return;

        nicknameListView.setItems(room.getNicknames());
        nicknameListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item);
            }
        });

        saveButton.setVisible(appState.isLoggedIn());
        saveButton.setManaged(appState.isLoggedIn());
        closeRoomButton.setVisible(room.isOwner());
        closeRoomButton.setManaged(room.isOwner());

        if (canvasPane != null) {
            canvasPane.shutdown();
            canvasContainer.getChildren().clear();
        }

        canvasPane = new CanvasPane(room, drawService, false);
        canvasPane.widthProperty().bind(canvasContainer.widthProperty());
        canvasPane.heightProperty().bind(canvasContainer.heightProperty());
        canvasPane.setSelectedColor(colorPicker.getValue());
        colorPicker.setOnAction(e -> canvasPane.setSelectedColor(colorPicker.getValue()));

        canvasContainer.getChildren().add(canvasPane);
    }

    @FXML
    public void onSave() {
        viewManager.showDialog(AppView.DIALOG_SAVE_WORK);
    }

    @FXML
    public void onLeave() {
        RoomState room = appState.getCurrentRoom();
        if (room == null) return;
        roomService.leaveRoom(room.getRoomId(),
                () -> Platform.runLater(() -> {
                    if (canvasPane != null) canvasPane.shutdown();
                    viewManager.clearCache();
                    viewManager.navigateTo(AppView.MAIN);
                }),
                error -> Platform.runLater(() -> AlertHelper.showError(error))
        );
    }

    @FXML
    public void onCloseRoom() {
        RoomState room = appState.getCurrentRoom();
        if (room == null) return;
        if (!AlertHelper.confirmDelete("this room")) return;
        roomService.closeRoom(room.getRoomId(), appState.getToken(),
                () -> Platform.runLater(() -> {
                    if (canvasPane != null) canvasPane.shutdown();
                    viewManager.clearCache();
                    viewManager.navigateTo(AppView.MAIN);
                }),
                error -> Platform.runLater(() -> AlertHelper.showError(error))
        );
    }
}