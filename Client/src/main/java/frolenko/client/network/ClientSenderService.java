package frolenko.client.network;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import common.model.Packet;
import common.protocol.CommandType;
import common.protocol.Encryptor;

import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.logging.Logger;

@Singleton
public class ClientSenderService {

    private static final Logger log = Logger.getLogger(ClientSenderService.class.getName());

    @Inject private Encryptor encryptor;
    @Inject private ClientSender clientSender;
    @Inject private ResponseManager responseManager;
    @Inject @Named("senderPool") private ExecutorService senderPool;

    public void connect(Socket socket) {
        clientSender.connect(socket);
    }

    public void send(Packet packet, Consumer<Packet> callback) {
        CommandType type = CommandType.fromCode(packet.bMsg().cType());
        log.info(String.format("[CLIENT OUT] PktId: %d | Cmd: %s | Room: %d | Payload: %d bytes",
                packet.bPktId(), type.name(), packet.bMsg().roomId(), packet.bMsg().payload().length));

        responseManager.register(packet.bPktId(), callback);
        senderPool.submit(() -> encryptor.encrypt(packet, clientSender::send));
    }
}