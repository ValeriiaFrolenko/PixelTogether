package frolenko.client.network;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.io.IOException;
import java.net.Socket;

@Singleton
public class ClientConnection {

    private static final String HOST = "localhost";
    private static final int PORT = 8080;
    private static final int RECONNECT_DELAY_MS = 3000;

    private final ClientReceiverService receiverService;
    private final ClientSenderService senderService;

    @Inject
    public ClientConnection(ClientReceiverService receiverService,
                            ClientSenderService senderService) {
        this.receiverService = receiverService;
        this.senderService = senderService;
    }

    public void start() {
        while (!Thread.currentThread().isInterrupted()) {
            try (Socket socket = new Socket(HOST, PORT)) {
                System.out.println("Connected to server");
                senderService.connect(socket);
                receiverService.connect(socket);
                waitUntilDisconnected(socket);
            } catch (IOException e) {
                System.err.println("Server unavailable, retrying in " + RECONNECT_DELAY_MS + "ms");
                try {
                    Thread.sleep(RECONNECT_DELAY_MS);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

    private void waitUntilDisconnected(Socket socket) {
        while (!socket.isClosed() && !Thread.currentThread().isInterrupted()) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}