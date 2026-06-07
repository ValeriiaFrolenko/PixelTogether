package frolenko.client.network;

import com.google.inject.Inject;
import common.model.Packet;
import common.protocol.CommandType;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class ResponseManager {

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
        long pktId = packet.bPktId();
        Consumer<Packet> callback = callbacks.remove(pktId);
        if (callback != null) {
            callback.accept(packet);
            return;
        }
        CommandType type = CommandType.fromCode(packet.bMsg().cType());
        Consumer<Packet> pushHandler = pushHandlers.get(type);
        if (pushHandler != null) {
            pushHandler.accept(packet);
        } else {
            System.err.println("No server.handler for push command: " + type);
        }
    }
}