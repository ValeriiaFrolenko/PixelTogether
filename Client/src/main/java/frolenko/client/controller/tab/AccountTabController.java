package frolenko.client.controller.tab;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import common.dto.room.MyRoomInfo;
import common.dto.work.GalleryItem;
import frolenko.client.config.AppView;
import frolenko.client.core.AppState;
import frolenko.client.core.RoomState;
import frolenko.client.core.ViewManager;
import frolenko.client.service.AuthService;
import frolenko.client.service.GalleryService;
import frolenko.client.service.RoomService;
import frolenko.client.util.AlertHelper;
import frolenko.client.util.UiUtil;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.VBox;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Singleton
public class AccountTabController {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private final AuthService authService;
    private final AppState appState;
    private final GalleryService galleryService;
    private final RoomService roomService;
    private final ViewManager viewManager;

    @FXML private VBox guestPane;
    @FXML private VBox loggedInPane;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label authErrorLabel;
    @FXML private Label usernameLabel;
    @FXML private ListView<GalleryItem> myWorksListView;
    @FXML private ListView<MyRoomInfo> myRoomsListView;
    @FXML private Button loginButton;
    @FXML private Button registerButton;

    @Inject
    public AccountTabController(AuthService authService,
                                AppState appState,
                                GalleryService galleryService,
                                RoomService roomService,
                                ViewManager viewManager) {
        this.authService = authService;
        this.appState = appState;
        this.galleryService = galleryService;
        this.roomService = roomService;
        this.viewManager = viewManager;
    }

    @FXML
    public void initialize() {
        myWorksListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(GalleryItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.title() + " (" + item.canvasW() + "x" + item.canvasH() + ") — " +
                            (item.savedAt() != null ? item.savedAt() : ""));
                }
            }
        });

        myRoomsListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(MyRoomInfo item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    String type = item.isPrivate() ? "Private" : "Public";
                    String code = item.code() != null ? " | Code: " + item.code() : "";
                    String expires = LocalDateTime.parse(item.expiresAt()).format(FORMATTER);
                    setText(item.name() + " [" + type + "]" + code + " | Expires: " + expires);
                }
            }
        });

        appState.tokenProperty().addListener((obs, old, token) -> Platform.runLater(() -> {
            boolean loggedIn = token != null;
            guestPane.setVisible(!loggedIn);
            guestPane.setManaged(!loggedIn);
            loggedInPane.setVisible(loggedIn);
            loggedInPane.setManaged(loggedIn);
            if (loggedIn) {
                loadMyWorks();
                loadMyRooms();
            } else {
                myWorksListView.getItems().clear();
                myRoomsListView.getItems().clear();
            }
        }));
    }

    private void loadMyWorks() {
        galleryService.getMyWorks(appState.getToken(),
                items -> Platform.runLater(() -> myWorksListView.getItems().setAll(items)),
                error -> Platform.runLater(() -> AlertHelper.showError(error))
        );
    }

    private void loadMyRooms() {
        roomService.getMyRooms(appState.getToken(),
                rooms -> Platform.runLater(() -> myRoomsListView.getItems().setAll(rooms)),
                error -> Platform.runLater(() -> AlertHelper.showError(error))
        );
    }

    @FXML
    public void onRefreshMyWorks() {
        loadMyWorks();
    }

    @FXML
    public void onRefreshMyRooms() {
        loadMyRooms();
    }

    @FXML
    public void onViewMyWork() {
        GalleryItem selected = myWorksListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertHelper.showError("Select a work first.");
            return;
        }
        Runnable unblock = UiUtil.withLoading(myWorksListView);
        galleryService.getWork(selected.id(),
                work -> Platform.runLater(() -> {
                    unblock.run();
                    viewManager.navigateTo(AppView.WORK_VIEW);
                    appState.setCurrentRoom(new RoomState(-1, work.canvasW(), work.canvasH(), work.pixels(), false));
                }),
                error -> Platform.runLater(() -> {
                    unblock.run();
                    AlertHelper.showError(error);
                })
        );
    }

    @FXML
    public void onDeleteMyWork() {
        GalleryItem selected = myWorksListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertHelper.showError("Select a work first.");
            return;
        }
        if (!AlertHelper.confirmDelete(selected.title())) return;
        Runnable unblock = UiUtil.withLoading(myWorksListView);
        galleryService.deleteWork(appState.getToken(), selected.id(),
                () -> Platform.runLater(() -> {
                    unblock.run();
                    loadMyWorks();
                }),
                error -> Platform.runLater(() -> {
                    unblock.run();
                    AlertHelper.showError(error);
                })
        );
    }

    @FXML
    public void onJoinMyRoom() {
        MyRoomInfo selected = myRoomsListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertHelper.showError("Select a room first.");
            return;
        }
        Runnable unblock = UiUtil.withLoading(myRoomsListView);
        if (selected.isPrivate()) {
            roomService.joinPrivate(selected.code(),
                    room -> Platform.runLater(() -> {
                        unblock.run();
                        viewManager.navigateTo(AppView.ROOM);
                    }),
                    error -> Platform.runLater(() -> {
                        unblock.run();
                        AlertHelper.showError(error);
                    })
            );
        } else {
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
    }

    @FXML
    public void onCopyCode() {
        MyRoomInfo selected = myRoomsListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertHelper.showError("Select a room first.");
            return;
        }
        if (selected.code() == null) {
            AlertHelper.showError("This room is public and has no code.");
            return;
        }
        ClipboardContent content = new ClipboardContent();
        content.putString(selected.code());
        Clipboard.getSystemClipboard().setContent(content);
        AlertHelper.showInfo("Copied", "Room code copied to clipboard: " + selected.code());
    }

    @FXML
    public void onLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        if (username.isEmpty() || password.isEmpty()) {
            authErrorLabel.setText("Enter username and password.");
            return;
        }
        Runnable unblock = UiUtil.withLoading(guestPane);
        authService.login(username, password,
                token -> Platform.runLater(() -> {
                    unblock.run();
                    appState.setToken(token);
                    usernameLabel.setText(username);
                    authErrorLabel.setText("");
                    passwordField.clear();
                }),
                error -> Platform.runLater(() -> {
                    unblock.run();
                    authErrorLabel.setText(error);
                })
        );
    }

    @FXML
    public void onRegister() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        if (username.isEmpty() || password.isEmpty()) {
            authErrorLabel.setText("Enter username and password.");
            return;
        }
        Runnable unblock = UiUtil.withLoading(guestPane);
        authService.register(username, password,
                token -> Platform.runLater(() -> {
                    unblock.run();
                    appState.setToken(token);
                    usernameLabel.setText(username);
                    authErrorLabel.setText("");
                    passwordField.clear();
                }),
                error -> Platform.runLater(() -> {
                    unblock.run();
                    authErrorLabel.setText(error);
                })
        );
    }

    @FXML
    public void onLogout() {
        Runnable unblock = UiUtil.withLoading(loggedInPane);
        authService.logout(appState.getToken(),
                () -> Platform.runLater(() -> {
                    unblock.run();
                    appState.setToken(null);
                    usernameLabel.setText("");
                }),
                error -> Platform.runLater(() -> {
                    unblock.run();
                    AlertHelper.showError(error);
                })
        );
    }
}