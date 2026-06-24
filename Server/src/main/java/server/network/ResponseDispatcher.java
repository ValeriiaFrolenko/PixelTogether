package server.network;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import common.model.Packet;
import common.protocol.CommandType;
import common.protocol.Encryptor;

import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.logging.Logger;

@Singleton
public class ResponseDispatcher {

    private static final Logger log = Logger.getLogger(ResponseDispatcher.class.getName());

    private final Encryptor encryptor;
    private final Sender sender;
    private final ConnectionManager connectionManager;
    private final ExecutorService encryptorPool;
    private final ExecutorService senderPool;

    @Inject
    public ResponseDispatcher(Encryptor encryptor,
                              Sender sender,
                              ConnectionManager connectionManager,
                              @Named("encryptorPool") ExecutorService encryptorPool,
                              @Named("senderPool") ExecutorService senderPool) {
        this.encryptor = encryptor;
        this.sender = sender;
        this.connectionManager = connectionManager;
        this.encryptorPool = encryptorPool;
        this.senderPool = senderPool;
    }

    public void sendToClient(long sessionId, Packet packet) {
        CommandType type = CommandType.fromCode(packet.bMsg().cType());
        log.info(String.format("[SERVER OUT -> Client %d] PktId: %d | Cmd: %s | Room: %d | Payload: %d bytes",
                sessionId, packet.bPktId(), type.name(), packet.bMsg().roomId(), packet.bMsg().payload().length));

        encryptorPool.submit(() ->
                encryptor.encrypt(packet, encrypted ->
                        senderPool.submit(() -> sender.sendToClient(sessionId, encrypted))
                )
        );
    }

    public void sendToRoom(int roomId, Packet packet) {
        CommandType type = CommandType.fromCode(packet.bMsg().cType());
        log.info(String.format("[SERVER OUT -> Room %d] PktId: %d | Cmd: %s | Room: %d | Payload: %d bytes",
                roomId, packet.bPktId(), type.name(), packet.bMsg().roomId(), packet.bMsg().payload().length));

        Collection<Socket> sockets = new ArrayList<>(connectionManager.getSocketsByRoom(roomId));        encryptorPool.submit(() ->
                encryptor.encrypt(packet, encrypted ->
                        senderPool.submit(() -> sender.sendToRoom(sockets, encrypted))
                )
        );
    }
}