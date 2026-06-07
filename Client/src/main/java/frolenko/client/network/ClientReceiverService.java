package frolenko.client.network;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import java.net.Socket;
import java.util.function.Consumer;

@Singleton
public class ClientReceiverService {

    private final Consumer<Socket> receiveStep;

    @Inject
    public ClientReceiverService(@Named("receiveStep") Consumer<Socket> receiveStep) {
        this.receiveStep = receiveStep;
    }

    public void connect(Socket socket) {
        receiveStep.accept(socket);
    }
}