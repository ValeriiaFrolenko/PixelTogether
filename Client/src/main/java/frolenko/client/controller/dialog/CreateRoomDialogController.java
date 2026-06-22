package frolenko.client.controller.dialog;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import frolenko.client.core.AppState;
import frolenko.client.core.RoomState;
import frolenko.client.service.RoomService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.function.Consumer;

@Singleton
public class CreateRoomDialogController {

    private final RoomService roomService;
    private final AppState appState;

    @FXML private TextField nameField;
    @FXML private TextField canvasWField;
    @FXML private TextField canvasHField;
    @FXML private TextField durationField;
    @FXML private CheckBox privateCheckBox;
    @FXML private Label errorLabel;

    private Consumer<RoomState> onCreated;

    @Inject
    public CreateRoomDialogController(RoomService roomService, AppState appState) {
        this.roomService = roomService;
        this.appState = appState;
    }

    public void setOnCreated(Consumer<RoomState> onCreated) {
        this.onCreated = onCreated;
    }

    @FXML
    public void onCreate() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            errorLabel.setText("Enter a room name.");
            return;
        }

        int canvasW, canvasH;
        long duration;
        try {
            canvasW = Integer.parseInt(canvasWField.getText().trim());
            canvasH = Integer.parseInt(canvasHField.getText().trim());
            duration = Long.parseLong(durationField.getText().trim());
        } catch (NumberFormatException e) {
            errorLabel.setText("Canvas size and duration must be numbers.");
            return;
        }

        if (canvasW <= 0 || canvasW > 500 || canvasH <= 0 || canvasH > 500) {
            errorLabel.setText("Canvas size must be between 1 and 500.");
            return;
        }

        if (duration <= 0) {
            errorLabel.setText("Duration must be greater than 0.");
            return;
        }

        roomService.createRoom(
                appState.getToken(), name, canvasW, canvasH, privateCheckBox.isSelected(), duration,
                room -> Platform.runLater(() -> {
                    if (onCreated != null) onCreated.accept(room);
                    closeStage();
                }),
                error -> Platform.runLater(() -> errorLabel.setText(error))
        );
    }

    @FXML
    public void onCancel() {
        closeStage();
    }

    private void closeStage() {
        Stage stage = (Stage) nameField.getScene().getWindow();
        stage.close();
    }
}