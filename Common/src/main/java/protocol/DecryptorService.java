package protocol;

import com.google.inject.Inject;
import model.Packet;

public class DecryptorService implements Decryptor {

    private final PacketDecoder decoder;

    @Inject
    public DecryptorService(PacketDecoder decoder) {
        this.decoder = decoder;
    }

    @Override
    public Packet decrypt(byte[] encryptedPacket) {
        try {
            return decoder.decode(encryptedPacket);
        } catch (Exception e) {
            throw new RuntimeException("Failed to decrypt packet", e);
        }
    }
}