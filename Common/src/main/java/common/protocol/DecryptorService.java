package common.protocol;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import common.model.Packet;
import common.network.KeyStore;

import java.nio.ByteBuffer;
import java.util.function.Consumer;

@Singleton
public class DecryptorService implements Decryptor {

    private final PacketDecoder decoder;
    private final KeyStore keyStore;

    @Inject
    public DecryptorService(PacketDecoder decoder, KeyStore keyStore) {
        this.decoder = decoder;
        this.keyStore = keyStore;
    }

    @Override
    public void decrypt(byte[] encryptedPacket, Consumer<Packet> next) {
        try {
            long sessionId = ByteBuffer.wrap(encryptedPacket)
                    .getLong(PacketStructure.OFFSET_SESSION_ID);
            byte[] aesKey = keyStore.get(sessionId);
            if (aesKey == null) {
                throw new IllegalStateException("No AES key for session: " + sessionId);
            }
            Packet packet = decoder.decode(encryptedPacket, aesKey);
            next.accept(packet);
        } catch (Exception e) {
            throw new RuntimeException("Failed to decrypt packet", e);
        }
    }
}