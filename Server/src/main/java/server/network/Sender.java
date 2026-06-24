package server.network;

import java.net.Socket;
import java.util.Collection;

public interface Sender {
    void sendToClient(long sessionId, byte[] packet);
    void sendToRoom(Collection<Socket> roomId, byte[] packet);
}