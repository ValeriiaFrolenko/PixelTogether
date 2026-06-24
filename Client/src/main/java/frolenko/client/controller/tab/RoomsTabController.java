package frolenko.client.controller.tab;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import common.dto.room.RoomInfo;
import frolenko.client.config.AppView;
import frolenko.client.core.AppState;
import frolenko.client.core.ViewManager;
import frolenko.client.controller.dialog.CreateRoomDialogController;
import frolenko.client.service.RoomService;
import frolenko.client.util.AlertHelper;
import frolenko.client.util.UiUtil;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

@Singleton
public class RoomsTabController {

    private final RoomService roomService;
    private final AppState appState;
    private final ViewManager viewManager;

    @FXML private ListView<RoomInfo> roomListView;
    @FXML private Button createRoomButton;
    @FXML private Button joinButton;
    @FXML private Button refreshButton;
    @FXML private Button joinPrivateButton;
    @FXML private TextField privateCodeField;

    @Inject
    public RoomsTabController(RoomService roomService, AppState appState, ViewManager viewManager) {
        this.roomService = roomService;
        this.appState = appState;
        this.viewManager = viewManager;
    }

    @FXML
    public void initialize() {
        roomListView.setItems(appState.getRooms());
        roomListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(RoomInfo item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null
                        : item.name() + " (" + item.participantsOnline() + " online)");
            }
        });

        appState.tokenProperty().addListener((obs, old, token) -> Platform.runLater(() -> {
            boolean loggedIn = token != null;
            createRoomButton.setVisible(loggedIn);
            createRoomButton.setManaged(loggedIn);
        }));

        onRefreshRooms();
    }

    @FXML
    public void onRefreshRooms() {
        roomService.getRooms(
                rooms -> Platform.runLater(() -> appState.getRooms().setAll(rooms)),
                error -> Platform.runLater(() -> AlertHelper.showError(error))
        );
    }

    @FXML
    public void onJoinPublic() {
        RoomInfo selected = roomListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertHelper.showError("Select a room first.");
            return;
        }
        Runnable unblock = UiUtil.withLoading(joinButton);
        roomService.joinPublic(selected.roomId(),
                room -> Platform.runLater(() -> {
                    unblock.run();
                    viewManager.navigateTo(AppView.ROOM);
                }),
                error -> Platform.runLater(() -> {
                    unblock.run();
                    AlertHelper.showError(error);
                })
        );
    }

    @FXML
    public void onJoinPrivate() {
        String code = privateCodeField.getText().trim();
        if (code.isEmpty()) {
            AlertHelper.showError("Enter a room code.");
            return;
        }
        Runnable unblock = UiUtil.withLoading(joinPrivateButton);
        roomService.joinPrivate(code,
                room -> Platform.runLater(() -> {
                    unblock.run();
                    viewManager.navigateTo(AppView.ROOM);
                }),
                error -> Platform.runLater(() -> {
                    unblock.run();
                    AlertHelper.showError(error);
                })
        );
    }

    @FXML
    public void onCreateRoom() {
        viewManager.showDialog(AppView.DIALOG_CREATE_ROOM,
                (CreateRoomDialogController c) -> c.setOnCreated(
                        room -> Platform.runLater(() -> viewManager.navigateTo(AppView.ROOM))
                ));
    }
}