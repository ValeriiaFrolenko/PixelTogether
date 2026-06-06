package core;

import com.google.inject.Inject;
import handler.CommandHandler;
import model.Packet;
import protocol.CommandType;

import java.util.Map;

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
            System.err.println("No handler for command: " + type);
            return;
        }
        handler.handle(packet);
    }
}