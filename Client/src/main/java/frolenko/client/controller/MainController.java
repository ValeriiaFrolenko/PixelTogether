package frolenko.client.controller;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import frolenko.client.core.AppState;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

@Singleton
public class MainController {

    private final AppState appState;

    @FXML private Label serverBanner;

    @Inject
    public MainController(AppState appState) {
        this.appState = appState;
    }

    @FXML
    public void initialize() {
        appState.serverAvailableProperty().addListener((obs, old, available) -> {
            serverBanner.setVisible(!available);
            serverBanner.setManaged(!available);
            if (!available) {
                ((VBox) serverBanner.getParent()).getChildren().forEach(node -> node.setDisable(false));
            }
        });
    }
}