package frolenko.client.network;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import common.network.KeyStore;
import common.utils.RsaUtil;

import javax.crypto.KeyGenerator;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

@Singleton
public class ClientConnection {

    private static final String HOST = "localhost";
    private static final int PORT = 8080;
    private static final int RECONNECT_DELAY_MS = 3000;

    private final ClientReceiverService receiverService;
    private final ClientSenderService senderService;
    private final KeyStore keyStore;

    @Inject
    public ClientConnection(ClientReceiverService receiverService,
                            ClientSenderService senderService,
                            KeyStore keyStore) {
        this.receiverService = receiverService;
        this.senderService = senderService;
        this.keyStore = keyStore;
    }

    public void start() {
        while (!Thread.currentThread().isInterrupted()) {
            try (Socket socket = new Socket(HOST, PORT)) {
                System.out.println("Connected to server");
                performHandshake(socket);
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
            } catch (Exception e) {
                System.err.println("Handshake failed: " + e.getMessage());
            }
        }
    }

    private void performHandshake(Socket socket) throws Exception {
        DataInputStream in = new DataInputStream(socket.getInputStream());
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());

        int publicKeyLength = in.readInt();
        byte[] publicKeyBytes = in.readNBytes(publicKeyLength);
        var publicKey = RsaUtil.publicKeyFromBytes(publicKeyBytes);

        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(128);
        byte[] aesKey = keyGen.generateKey().getEncoded();

        byte[] encryptedAesKey = RsaUtil.encrypt(aesKey, publicKey);
        out.writeInt(encryptedAesKey.length);
        out.write(encryptedAesKey);
        out.flush();

        keyStore.register(0, aesKey);
        System.out.println("Handshake completed, AES key length: " + aesKey.length);
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