package network;

import java.net.Socket;

public interface ServerReceiverFactory {
    ServerReceiver create(Socket socket);
}