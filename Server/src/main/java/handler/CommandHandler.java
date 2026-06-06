package handler;

import model.Packet;

public interface CommandHandler {
    void handle(Packet packet);
}