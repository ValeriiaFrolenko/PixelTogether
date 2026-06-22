package frolenko.client;

import com.google.inject.Guice;
import com.google.inject.Injector;
import frolenko.client.core.ClientModule;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
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

        FXMLLoader fxmlLoader = new FXMLLoader(PixelTogetherApp.class.getResource("view/main-view.fxml"));
        fxmlLoader.setControllerFactory(injector::getInstance);
        Scene scene = new Scene(fxmlLoader.load(), 800, 600);
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