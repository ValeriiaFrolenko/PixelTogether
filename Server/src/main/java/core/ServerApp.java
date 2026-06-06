package core;

import com.google.inject.Guice;
import com.google.inject.Injector;
import database.DatabaseInitializer;
import network.ServerListener;

public class ServerApp {

    public static void main(String[] args) {
        Injector injector = Guice.createInjector(new ServerModule());

        injector.getInstance(DatabaseInitializer.class).initialize();

        new Thread(injector.getInstance(ServerListener.class)).start();

        System.out.println("Server started");
    }
}