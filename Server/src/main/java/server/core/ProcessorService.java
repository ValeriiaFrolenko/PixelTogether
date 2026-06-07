package server.core;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import server.handler.CommandHandler;
import common.model.Packet;
import common.protocol.CommandType;

import java.util.Map;

@Singleton
public class ProcessorService {

    private final Map<CommandType, CommandHandler> handlers;

    @Inject
    public ProcessorService(Map<CommandType, CommandHandler> handlers) {
        this.handlers = handlers;
    }

    public void process(Packet packet) {
        CommandType type = CommandType.fromCode(packet.bMsg().cType());
        CommandHandler handler = handlers.get(type);
        if (handler == null) {
            System.err.println("No server.handler for command: " + type);
            return;
        }
        handler.handle(packet);
    }
}