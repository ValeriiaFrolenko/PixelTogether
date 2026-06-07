package frolenko.client.network;

import com.google.inject.Singleton;

import java.io.IOException;
import java.net.Socket;

@Singleton
public class ClientSender {

    private volatile Socket socket;

    public void connect(Socket socket) {
        this.socket = socket;
    }

    public void send(byte[] data) {
        Socket s = socket;
        if (s == null || s.isClosed()) {
            System.err.println("ClientSender: no active connection");
            return;
        }
        try {
            s.getOutputStream().write(data);
        } catch (IOException e) {
            System.err.println("ClientSender failed: " + e.getMessage());
        }
    }
}