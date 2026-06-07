package server.handler;

import common.model.Packet;

public interface CommandHandler {
    void handle(Packet packet);
}