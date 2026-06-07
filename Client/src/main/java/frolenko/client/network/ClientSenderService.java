package frolenko.client.network;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import common.model.Packet;
import common.protocol.Encryptor;

import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

@Singleton
public class ClientSenderService {

    @Inject private Encryptor encryptor;
    @Inject private ClientSender clientSender;
    @Inject private ResponseManager responseManager;
    @Inject @Named("encryptorPool") private ExecutorService encryptorPool;
    @Inject @Named("senderPool") private ExecutorService senderPool;

    public void connect(Socket socket) {
        clientSender.connect(socket);
    }

    public void send(Packet packet, Consumer<Packet> callback) {
        responseManager.register(packet.bPktId(), callback);
        encryptorPool.submit(() ->
                encryptor.encrypt(packet, encrypted ->
                        senderPool.submit(() -> clientSender.send(encrypted))));
    }
}