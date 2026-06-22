package server.core;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import server.handler.CommandHandler;
import common.model.Packet;
import common.protocol.CommandType;

import java.util.Map;
import java.util.logging.Logger;

@Singleton
public class ProcessorService {

    private static final Logger log = Logger.getLogger(ProcessorService.class.getName());
    private final Map<CommandType, CommandHandler> handlers;

    @Inject
    public ProcessorService(Map<CommandType, CommandHandler> handlers) {
        this.handlers = handlers;
    }

    public void process(Packet packet) {
        CommandType type = CommandType.fromCode(packet.bMsg().cType());

        log.info(String.format("[SERVER IN] Session: %d | PktId: %d | Cmd: %s | Room: %d | Payload: %d bytes",
                packet.sessionId(), packet.bPktId(), type.name(), packet.bMsg().roomId(), packet.bMsg().payload().length));

        CommandHandler handler = handlers.get(type);
        if (handler == null) {
            log.warning("No server.handler for command: " + type);
            return;
        }
        handler.handle(packet);
    }
}