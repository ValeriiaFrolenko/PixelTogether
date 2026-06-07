package server.network;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import common.model.Packet;
import common.protocol.Encryptor;

import java.util.concurrent.ExecutorService;

@Singleton
public class ResponseDispatcher {

    private final Encryptor encryptor;
    private final server.network.Sender sender;
    private final ExecutorService encryptorPool;
    private final ExecutorService senderPool;

    @Inject
    public ResponseDispatcher(Encryptor encryptor,
                              Sender sender,
                              @Named("encryptorPool") ExecutorService encryptorPool,
                              @Named("senderPool") ExecutorService senderPool) {
        this.encryptor = encryptor;
        this.sender = sender;
        this.encryptorPool = encryptorPool;
        this.senderPool = senderPool;
    }

    public void sendToClient(byte sessionId, Packet packet) {
        encryptorPool.submit(() ->
                encryptor.encrypt(packet, encrypted ->
                        senderPool.submit(() -> sender.sendToClient(sessionId, encrypted))
                )
        );
    }

    public void sendToRoom(int roomId, Packet packet) {
        encryptorPool.submit(() ->
                encryptor.encrypt(packet, encrypted ->
                        senderPool.submit(() -> sender.sendToRoom(roomId, encrypted))
                )
        );
    }
}