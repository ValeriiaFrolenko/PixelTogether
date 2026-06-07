package server;

import com.google.inject.Guice;
import com.google.inject.Injector;
import server.core.ServerModule;
import server.database.DatabaseInitializer;
import server.network.ServerListener;

public class ServerApp {

    public static void main(String[] args) {
        Injector injector = Guice.createInjector(new ServerModule());

        injector.getInstance(DatabaseInitializer.class).initialize();

        new Thread(injector.getInstance(ServerListener.class)).start();

        System.out.println("Server started");
    }
}