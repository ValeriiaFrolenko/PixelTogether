package core;

import com.google.inject.Guice;
import com.google.inject.Injector;
import database.DatabaseInitializer;
import network.ServerListener;

public class ServerApp {

    public static void main(String[] args) {
        Injector injector = Guice.createInjector(new ServerModule());

        injector.getInstance(DatabaseInitializer.class).initialize();

        Thread listenerThread = new Thread(injector.getInstance(ServerListener.class));
        listenerThread.start();

        System.out.println("Server started");
    }
}