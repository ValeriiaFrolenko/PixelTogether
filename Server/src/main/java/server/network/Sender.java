package server.network;

public interface Sender {
    void sendToClient(long sessionId, byte[] packet);
    void sendToRoom(int roomId, byte[] packet);
}