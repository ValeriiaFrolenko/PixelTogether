package protocol;

import com.google.inject.Inject;
import model.Packet;

public class EncryptorService implements Encryptor {

    private final PacketEncoder encoder;

    @Inject
    public EncryptorService(PacketEncoder encoder) {
        this.encoder = encoder;
    }

    @Override
    public byte[] encrypt(Packet packet) {
        try {
            return encoder.encode(packet);
        } catch (Exception e) {
            throw new RuntimeException("Failed to encrypt packet", e);
        }
    }
}