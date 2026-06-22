package frolenko.client;

import com.google.inject.Guice;
import com.google.inject.Injector;
import frolenko.client.config.AppView;
import frolenko.client.core.ClientModule;
import frolenko.client.core.ViewManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import frolenko.client.network.ClientConnection;

import java.io.IOException;

public class PixelTogetherApp extends Application {

    private static Injector injector;

    @Override
    public void start(Stage stage) throws IOException {
        injector = Guice.createInjector(new ClientModule());
        Thread connectionThread = new Thread(injector.getInstance(ClientConnection.class)::start);
        connectionThread.setDaemon(true);
        connectionThread.start();

        ViewManager viewManager = injector.getInstance(ViewManager.class);
        Parent root = viewManager.initialize();
        viewManager.navigateTo(AppView.MAIN);

        Scene scene = new Scene(root, 800, 600);
        stage.setTitle("PixelTogether");
        stage.setScene(scene);
        stage.show();
    }

    public static Injector getInjector() {
        return injector;
    }

    public static void main(String[] args) {
        launch();
    }
}