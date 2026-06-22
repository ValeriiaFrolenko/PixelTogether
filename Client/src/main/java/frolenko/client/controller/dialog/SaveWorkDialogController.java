package frolenko.client.controller.dialog;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import frolenko.client.core.AppState;
import frolenko.client.service.GalleryService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

@Singleton
public class SaveWorkDialogController {

    private final GalleryService galleryService;
    private final AppState appState;

    @FXML private TextField titleField;
    @FXML private CheckBox publicCheckBox;
    @FXML private Label errorLabel;

    @Inject
    public SaveWorkDialogController(GalleryService galleryService, AppState appState) {
        this.galleryService = galleryService;
        this.appState = appState;
    }

    @FXML
    public void onSave() {
        String title = titleField.getText().trim();
        if (title.isEmpty()) {
            errorLabel.setText("Enter a title.");
            return;
        }

        int roomId = appState.getCurrentRoom().getRoomId();
        String token = appState.getToken();

        galleryService.saveWork(roomId, token, title, publicCheckBox.isSelected(),
                () -> Platform.runLater(this::closeStage),
                error -> Platform.runLater(() -> errorLabel.setText(error))
        );
    }

    @FXML
    public void onCancel() {
        closeStage();
    }

    private void closeStage() {
        Stage stage = (Stage) titleField.getScene().getWindow();
        stage.close();
    }
}