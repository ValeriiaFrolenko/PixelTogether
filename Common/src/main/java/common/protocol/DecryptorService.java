package common.protocol;

import com.google.inject.Inject;
import common.model.Packet;
import java.util.function.Consumer;

public class DecryptorService implements Decryptor {

    private final PacketDecoder decoder;

    @Inject
    public DecryptorService(PacketDecoder decoder) {
        this.decoder = decoder;
    }

    @Override
    public void decrypt(byte[] encryptedPacket, Consumer<Packet> next) {
        try {
            Packet packet = decoder.decode(encryptedPacket);
            next.accept(packet);
        } catch (Exception e) {
            throw new RuntimeException("Failed to decrypt packet", e);
        }
    }
}