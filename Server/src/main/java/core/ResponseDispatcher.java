package core;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import model.Packet;
import network.Sender;
import protocol.EncryptorService;

import java.util.concurrent.ExecutorService;

public class ResponseDispatcher {

    private final EncryptorService encryptorService;
    private final Sender sender;
    private final ExecutorService encryptorPool;
    private final ExecutorService senderPool;

    @Inject
    public ResponseDispatcher(EncryptorService encryptorService,
                              Sender sender,
                              @Named("encryptorPool") ExecutorService encryptorPool,
                              @Named("senderPool") ExecutorService senderPool) {
        this.encryptorService = encryptorService;
        this.sender = sender;
        this.encryptorPool = encryptorPool;
        this.senderPool = senderPool;
    }

    public void sendToClient(byte sessionId, Packet packet) {
        encryptorPool.submit(() -> {
            byte[] encrypted = encryptorService.encrypt(packet);
            senderPool.submit(() -> sender.sendToClient(sessionId, encrypted));
        });
    }

    public void sendToRoom(int roomId, Packet packet) {
        encryptorPool.submit(() -> {
            byte[] encrypted = encryptorService.encrypt(packet);
            senderPool.submit(() -> sender.sendToRoom(roomId, encrypted));
        });
    }
}