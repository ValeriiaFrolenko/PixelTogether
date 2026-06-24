package frolenko.client.util;

import javafx.scene.Node;
import javafx.stage.Stage;

public final class UiUtil {

    private UiUtil() {}

    public static Runnable withLoading(Node node) {
        node.setDisable(true);
        return () -> javafx.application.Platform.runLater(() -> node.setDisable(false));
    }

    public static Runnable withLoading(Stage stage) {
        stage.getScene().getRoot().setDisable(true);
        return () -> javafx.application.Platform.runLater(() -> stage.getScene().getRoot().setDisable(false));
    }
}