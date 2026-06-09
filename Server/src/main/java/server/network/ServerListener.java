package server.network;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import common.utils.RsaUtil;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyPair;
import java.util.function.BiConsumer;

@Singleton
public class ServerListener implements Runnable {

    private static final int PORT = 8080;

    private final BiConsumer<Socket, byte[]> socketPipeline;
    private final KeyPair rsaKeyPair;

    @Inject
    public ServerListener(BiConsumer<Socket, byte[]> socketPipeline) throws Exception {
        this.socketPipeline = socketPipeline;
        this.rsaKeyPair = RsaUtil.generateKeyPair();
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server listening on port " + PORT);
            while (!Thread.currentThread().isInterrupted()) {
                Socket socket = serverSocket.accept();
                try {
                    byte[] aesKey = performHandshake(socket);
                    System.out.println("Handshake completed for new client");
                    socketPipeline.accept(socket, aesKey);
                } catch (Exception e) {
                    System.err.println("Handshake failed: " + e.getMessage());
                    socket.close();
                }
            }
        } catch (IOException e) {
            System.err.println("ServerListener error: " + e.getMessage());
        }
    }

    private byte[] performHandshake(Socket socket) throws Exception {
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
        DataInputStream in = new DataInputStream(socket.getInputStream());

        byte[] publicKeyBytes = RsaUtil.publicKeyToBytes(rsaKeyPair.getPublic());
        out.writeInt(publicKeyBytes.length);
        out.write(publicKeyBytes);
        out.flush();

        int encryptedAesKeyLength = in.readInt();
        byte[] encryptedAesKey = in.readNBytes(encryptedAesKeyLength);

        return RsaUtil.decrypt(encryptedAesKey, rsaKeyPair.getPrivate());
    }
}