package core;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.name.Named;
import database.ConnectionProvider;
import database.ConnectionManager;
import network.ServerReceiver;
import network.ServerReceiverFactory;
import network.ServerSender;
import network.Sender;
import protocol.DecryptorService;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class ServerModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(ConnectionProvider.class).to(ConnectionManager.class).in(Singleton.class);
        bind(Sender.class).to(ServerSender.class).in(Singleton.class);
        install(new FactoryModuleBuilder()
                .implement(ServerReceiver.class, ServerReceiver.class)
                .build(ServerReceiverFactory.class));
    }

    @Provides
    @Singleton
    @Named("aesKey")
    byte[] provideAesKey() {
        String key = System.getenv("PIXEL_AES_KEY");
        if (key == null || key.length() != 16) {
            throw new IllegalStateException("PIXEL_AES_KEY env variable must be set and 16 chars");
        }
        return key.getBytes();
    }
}