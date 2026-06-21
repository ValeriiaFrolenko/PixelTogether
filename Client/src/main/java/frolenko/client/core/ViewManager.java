package frolenko.client.core;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import frolenko.client.PixelTogetherApp;
import frolenko.client.config.AppView;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@Singleton
public class ViewManager {

    private final Injector injector;
    private StackPane rootArea;
    private final Map<AppView, Parent> cache = new HashMap<>();

    @Inject
    public ViewManager(Injector injector) {
        this.injector = injector;
    }

    public Parent initialize() {
        rootArea = new StackPane();
        return rootArea;
    }

    public void navigateTo(AppView view) {
        if (cache.containsKey(view)) {
            rootArea.getChildren().setAll(cache.get(view));
            return;
        }
        Parent root = loadView(view);
        cache.put(view, root);
        rootArea.getChildren().setAll(root);
    }

    public <T> T showDialog(AppView view, Consumer<T> controllerConsumer) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    PixelTogetherApp.class.getResource(view.getFxmlPath())
            );
            loader.setControllerFactory(injector::getInstance);
            Parent root = loader.load();
            T controller = loader.getController();

            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            if (rootArea != null && rootArea.getScene() != null) {
                dialogStage.initOwner(rootArea.getScene().getWindow());
            }
            dialogStage.setScene(new Scene(root));

            if (controllerConsumer != null) {
                controllerConsumer.accept(controller);
            }

            dialogStage.showAndWait();
            return controller;
        } catch (IOException e) {
            throw new RuntimeException("Failed to load dialog: " + view.getFxmlPath(), e);
        }
    }

    public void showDialog(AppView view) {
        showDialog(view, null);
    }

    public void clearCache() {
        cache.clear();
    }

    private Parent loadView(AppView view) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    PixelTogetherApp.class.getResource(view.getFxmlPath())
            );
            loader.setControllerFactory(injector::getInstance);
            return loader.load();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load view: " + view.getFxmlPath(), e);
        }
    }
}