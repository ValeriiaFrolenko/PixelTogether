package server.network;

import com.google.inject.Inject;

import java.io.IOException;
import java.net.Socket;
import java.util.Collection;

public class ServerSender implements Sender {

    private final ConnectionManager connectionManager;

    @Inject
    public ServerSender(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public void sendToClient(long sessionId, byte[] packet) {
        Socket socket = connectionManager.getSocket(sessionId);
        if (socket == null) return;
        send(socket, packet);
    }

    @Override
    public void sendToRoom(Collection<Socket> sockets, byte[] packet) {
        for (Socket socket : sockets) {
            send(socket, packet);
        }
    }

    private void send(Socket socket, byte[] packet) {
        try {
            socket.getOutputStream().write(packet);
        } catch (IOException e) {
            System.err.println("Failed to send: " + e.getMessage());
        }
    }
}