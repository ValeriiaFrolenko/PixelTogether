package server.core;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.name.Named;
import common.network.KeyStore;
import server.database.ConnectionProvider;
import server.database.ConnectionManager;
import server.handler.CommandHandler;
import server.handler.room.CreateRoomHandler;
import server.handler.room.GetRoomsHandler;
import server.handler.room.JoinRoomPrivateHandler;
import server.handler.room.JoinRoomPublicHandler;
import server.handler.user.LoginHandler;
import server.handler.user.LogoutHandler;
import server.handler.user.RegisterHandler;
import common.model.Packet;
import common.protocol.CommandType;
import common.protocol.Decryptor;
import common.protocol.DecryptorService;
import common.protocol.Encryptor;
import common.protocol.EncryptorService;
import server.network.Sender;
import server.network.ServerKeyStore;
import server.network.ServerReceiver;
import server.network.ServerReceiverFactory;
import server.network.ServerSender;

import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ServerModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(ConnectionProvider.class).to(ConnectionManager.class).in(Singleton.class);
        bind(Sender.class).to(ServerSender.class).in(Singleton.class);
        bind(Encryptor.class).to(EncryptorService.class).in(Singleton.class);
        bind(Decryptor.class).to(DecryptorService.class).in(Singleton.class);
        bind(KeyStore.class).to(ServerKeyStore.class).in(Singleton.class);

        install(new FactoryModuleBuilder()
                .implement(ServerReceiver.class, ServerReceiver.class)
                .build(ServerReceiverFactory.class));

        MapBinder<CommandType, CommandHandler> handlerBinder =
                MapBinder.newMapBinder(binder(), CommandType.class, CommandHandler.class);
        handlerBinder.addBinding(CommandType.REGISTER).to(RegisterHandler.class);
        handlerBinder.addBinding(CommandType.LOGIN).to(LoginHandler.class);
        handlerBinder.addBinding(CommandType.LOGOUT).to(LogoutHandler.class);
        handlerBinder.addBinding(CommandType.CREATE_ROOM).to(CreateRoomHandler.class);
        handlerBinder.addBinding(CommandType.JOIN_ROOM_PUBLIC).to(JoinRoomPublicHandler.class);
        handlerBinder.addBinding(CommandType.JOIN_ROOM_PRIVATE).to(JoinRoomPrivateHandler.class);
        handlerBinder.addBinding(CommandType.GET_ROOMS).to(GetRoomsHandler.class);
    }

    @Provides
    @Singleton
    @Named("receiverPool")
    ExecutorService provideReceiverPool() {
        return Executors.newCachedThreadPool();
    }

    @Provides
    @Singleton
    @Named("decryptorPool")
    ExecutorService provideDecryptorPool() {
        return Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }

    @Provides
    @Singleton
    @Named("processorPool")
    ExecutorService provideProcessorPool() {
        return Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }

    @Provides
    @Singleton
    @Named("encryptorPool")
    ExecutorService provideEncryptorPool() {
        return Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }

    @Provides
    @Singleton
    @Named("senderPool")
    ExecutorService provideSenderPool() {
        return Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);
    }

    @Provides
    @Singleton
    @Named("processStep")
    Consumer<Packet> provideProcessStep(ProcessorService processorService,
                                        @Named("processorPool") ExecutorService pool) {
        return packet -> pool.submit(() -> processorService.process(packet));
    }

    @Provides
    @Singleton
    @Named("decryptStep")
    Consumer<byte[]> provideDecryptStep(Decryptor decryptor,
                                        @Named("decryptorPool") ExecutorService pool,
                                        @Named("processStep") Consumer<Packet> processStep) {
        return bytes -> pool.submit(() -> decryptor.decrypt(bytes, processStep));
    }

    @Provides
    @Singleton
    BiConsumer<Socket, byte[]> provideSocketStep(ServerReceiverFactory receiverFactory,
                                                 @Named("receiverPool") ExecutorService pool) {
        return (socket, aesKey) -> pool.submit(receiverFactory.create(socket, aesKey));
    }
}