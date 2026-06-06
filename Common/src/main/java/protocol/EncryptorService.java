package protocol;

import com.google.inject.Inject;
import model.Packet;
import java.util.function.Consumer;

public class EncryptorService implements Encryptor {

    private final PacketEncoder encoder;

    @Inject
    public EncryptorService(PacketEncoder encoder) {
        this.encoder = encoder;
    }

    @Override
    public void encrypt(Packet packet, Consumer<byte[]> next) {
        try {
            byte[] encrypted = encoder.encode(packet);
            next.accept(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("Failed to encrypt packet", e);
        }
    }
}