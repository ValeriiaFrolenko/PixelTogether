package network;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.name.Named;
import protocol.PacketStructure;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.util.function.Consumer;

public class ServerReceiver implements Receiver, Runnable {

    private final Socket socket;
    private final Consumer<byte[]> pipeline;
    private final ConnectionManager connectionManager;
    private Byte sessionId = null;
    private DataInputStream in;

    @Inject
    public ServerReceiver(@Assisted Socket socket,
                          @Named("decryptStep") Consumer<byte[]> decryptStep,
                          ConnectionManager connectionManager) {
        this.socket = socket;
        this.pipeline = decryptStep;
        this.connectionManager = connectionManager;
    }

    @Override
    public void receiveMessage() throws IOException {
        byte[] header = readBytes(PacketStructure.HEADER_SIZE);
        int wLen = ByteBuffer.wrap(header).getInt(PacketStructure.OFFSET_W_LEN);
        int restSize = PacketStructure.MIN_PACKET_SIZE + wLen - PacketStructure.HEADER_SIZE;
        byte[] rest = readBytes(restSize);
        byte[] packet = ByteBuffer.allocate(header.length + rest.length)
                .put(header)
                .put(rest)
                .array();
        registerSession(header);
        pipeline.accept(packet);
    }

    private void registerSession(byte[] header) {
        if (sessionId == null) {
            sessionId = header[PacketStructure.OFFSET_SESSION_ID];
            connectionManager.register(sessionId, socket);
        }
    }

    private byte[] readBytes(int size) throws IOException {
        byte[] bytes = in.readNBytes(size);
        if (bytes.length < size) {
            throw new IOException("Connection closed while reading packet.");
        }
        return bytes;
    }

    @Override
    public void run() {
        try (socket; DataInputStream dis = new DataInputStream(socket.getInputStream())) {
            this.in = dis;
            while (!socket.isClosed() && !Thread.currentThread().isInterrupted()) {
                try {
                    receiveMessage();
                } catch (SocketTimeoutException ignored) {
                } catch (IOException | IllegalArgumentException e) {
                    System.err.println("ServerReceiver error: " + e.getMessage());
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("ServerReceiver failed to open/close stream: " + e.getMessage());
        } finally {
            if (sessionId != null) {
                connectionManager.unregister(sessionId);
            }
        }
    }
}