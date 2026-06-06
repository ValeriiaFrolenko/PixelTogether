package network;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

public class ServerListener implements Runnable {

    private static final int PORT = 8080;

    private final ServerReceiverFactory receiverFactory;
    private final ExecutorService clientPool;

    @Inject
    public ServerListener(ServerReceiverFactory receiverFactory,
                          @Named("clientPool") ExecutorService clientPool) {
        this.receiverFactory = receiverFactory;
        this.clientPool = clientPool;
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server listening on port " + PORT);
            while (!Thread.currentThread().isInterrupted()) {
                Socket socket = serverSocket.accept();
                clientPool.submit(receiverFactory.create(socket));
            }
        } catch (IOException e) {
            System.err.println("ServerListener error: " + e.getMessage());
        }
    }
}