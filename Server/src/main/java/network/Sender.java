package network;

public interface Sender {
    void sendToClient(int sessionId, byte[] packet);
    void sendToRoom(int roomId, byte[] packet);
}