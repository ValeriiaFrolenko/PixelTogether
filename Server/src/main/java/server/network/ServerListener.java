package server.network;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.function.Consumer;

@Singleton
public class ServerListener implements Runnable {

    private static final int PORT = 8080;

    private final Consumer<Socket> socketPipeline;

    @Inject
    public ServerListener(Consumer<Socket> socketPipeline) {
        this.socketPipeline = socketPipeline;
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server listening on port " + PORT);
            while (!Thread.currentThread().isInterrupted()) {
                Socket socket = serverSocket.accept();
                socketPipeline.accept(socket);
            }
        } catch (IOException e) {
            System.err.println("ServerListener error: " + e.getMessage());
        }
    }
}