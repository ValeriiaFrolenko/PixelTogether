package frolenko.client.network;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import common.model.Packet;
import common.protocol.CommandType;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.logging.Logger;

@Singleton
public class ResponseManager {

    private static final Logger log = Logger.getLogger(ResponseManager.class.getName());

    private final ConcurrentHashMap<Long, Consumer<Packet>> callbacks = new ConcurrentHashMap<>();
    private final Map<CommandType, Consumer<Packet>> pushHandlers;

    @Inject
    public ResponseManager(Map<CommandType, Consumer<Packet>> pushHandlers) {
        this.pushHandlers = pushHandlers;
    }

    public void register(long pktId, Consumer<Packet> callback) {
        callbacks.put(pktId, callback);
    }

    public void handle(Packet packet) {
        CommandType type = CommandType.fromCode(packet.bMsg().cType());
        log.info(String.format("[CLIENT IN] PktId: %d | Cmd: %s | Room: %d | Payload: %d bytes",
                packet.bPktId(), type.name(), packet.bMsg().roomId(), packet.bMsg().payload().length));

        long pktId = packet.bPktId();
        Consumer<Packet> callback = callbacks.remove(pktId);
        if (callback != null) {
            callback.accept(packet);
            return;
        }

        Consumer<Packet> pushHandler = pushHandlers.get(type);
        if (pushHandler != null) {
            pushHandler.accept(packet);
        } else {
            log.warning("No server.handler for push command: " + type);
        }
    }
}