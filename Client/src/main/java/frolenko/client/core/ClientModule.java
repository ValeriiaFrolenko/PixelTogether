package frolenko.client.core;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.name.Named;
import common.model.Packet;
import common.network.KeyStore;
import common.protocol.*;
import frolenko.client.handler.DrawPushHandler;
import frolenko.client.handler.ParticipantJoinedHandler;
import frolenko.client.handler.ParticipantLeftHandler;
import frolenko.client.network.ClientKeyStore;
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
        bind(KeyStore.class).to(ClientKeyStore.class).in(Singleton.class);
        bind(AppState.class).in(Singleton.class);

        install(new FactoryModuleBuilder()
                .implement(ClientReceiver.class, ClientReceiver.class)
                .build(ClientReceiverFactory.class));

        MapBinder<CommandType, Consumer<Packet>> pushBinder = MapBinder.newMapBinder(
                binder(),
                TypeLiteral.get(CommandType.class),
                new TypeLiteral<Consumer<Packet>>(){}
        );
        pushBinder.addBinding(CommandType.DRAW).to(DrawPushHandler.class);
        pushBinder.addBinding(CommandType.PARTICIPANT_JOINED).to(ParticipantJoinedHandler.class);
        pushBinder.addBinding(CommandType.PARTICIPANT_LEFT).to(ParticipantLeftHandler.class);
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