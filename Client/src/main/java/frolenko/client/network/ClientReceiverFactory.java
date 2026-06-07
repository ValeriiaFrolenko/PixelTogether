package frolenko.client.network;

import java.net.Socket;

public interface ClientReceiverFactory {
    ClientReceiver create(Socket socket);
}