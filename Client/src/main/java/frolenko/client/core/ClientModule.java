package frolenko.client.core;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.name.Named;
import common.model.Packet;
import common.protocol.*;
import frolenko.client.network.ClientReceiver;
import frolenko.client.network.ClientReceiverFactory;
import frolenko.client.network.ResponseManager;

import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class ClientModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(Encryptor.class).to(EncryptorService.class).in(Singleton.class);
        bind(Decryptor.class).to(DecryptorService.class).in(Singleton.class);

        install(new FactoryModuleBuilder()
                .implement(ClientReceiver.class, ClientReceiver.class)
                .build(ClientReceiverFactory.class));

        MapBinder.newMapBinder(
                binder(),
                TypeLiteral.get(CommandType.class),
                new TypeLiteral<Consumer<Packet>>(){}
        );
    }

    @Provides
    @Singleton
    @Named("aesKey")
    byte[] provideAesKey() {
        String key = System.getProperty("PIXEL_AES_KEY");

        if (key == null) {
            key = System.getenv("PIXEL_AES_KEY");
        }

        if (key == null || key.length() != 16) {
            throw new IllegalStateException("PIXEL_AES_KEY env variable must be set and 16 chars");
        }
        return key.getBytes();
    }

    @Provides @Singleton @Named("receiverPool")
    ExecutorService provideReceiverPool() {
        return Executors.newCachedThreadPool();
    }

    @Provides @Singleton @Named("decryptorPool")
    ExecutorService provideDecryptorPool() {
        return Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }

    @Provides @Singleton @Named("encryptorPool")
    ExecutorService provideEncryptorPool() {
        return Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }

    @Provides @Singleton @Named("senderPool")
    ExecutorService provideSenderPool() {
        return Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);
    }

    @Provides
    @Singleton
    @Named("handleStep")
    Consumer<Packet> provideHandleStep(ResponseManager responseManager) {
        return responseManager::handle;
    }

    @Provides
    @Singleton
    @Named("decryptStep")
    Consumer<byte[]> provideDecryptStep(Decryptor decryptor,
                                        @Named("decryptorPool") ExecutorService decryptorPool,
                                        @Named("handleStep") Consumer<Packet> handleStep) {
        return bytes -> decryptorPool.submit(() -> decryptor.decrypt(bytes, handleStep));
    }

    @Provides
    @Singleton
    @Named("receiveStep")
    Consumer<Socket> provideReceiveStep(ClientReceiverFactory receiverFactory,
                                        @Named("receiverPool") ExecutorService receiverPool) {
        return socket -> receiverPool.submit(receiverFactory.create(socket));
    }
}