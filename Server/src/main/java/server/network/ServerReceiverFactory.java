package server.network;

import java.net.Socket;

public interface ServerReceiverFactory {
    ServerReceiver create(Socket socket, byte[] aesKey);
}