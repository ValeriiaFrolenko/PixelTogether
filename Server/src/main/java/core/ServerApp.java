package core;

import com.google.inject.Guice;
import com.google.inject.Injector;
import database.DatabaseInitializer;

public class ServerApp {

    public static void main(String[] args) {
        Injector injector = Guice.createInjector(new ServerModule());

        DatabaseInitializer initializer = injector.getInstance(DatabaseInitializer.class);
        initializer.initialize();

        System.out.println("Server started");
    }
}