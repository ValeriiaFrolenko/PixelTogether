package network;

public interface Sender {
    void sendToClient(byte sessionId, byte[] packet);
    void sendToRoom(int roomId, byte[] packet);
}