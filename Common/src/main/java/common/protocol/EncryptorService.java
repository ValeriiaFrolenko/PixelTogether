package common.protocol;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import common.model.Packet;
import common.network.KeyStore;

import java.util.function.Consumer;

@Singleton
public class EncryptorService implements Encryptor {

    private final PacketEncoder encoder;
    private final KeyStore keyStore;

    @Inject
    public EncryptorService(PacketEncoder encoder, KeyStore keyStore) {
        this.encoder = encoder;
        this.keyStore = keyStore;
    }

    @Override
    public void encrypt(Packet packet, Consumer<byte[]> next) {
        try {
            byte[] aesKey = keyStore.get(packet.sessionId());
            if (aesKey == null) {
                throw new IllegalStateException("No AES key for session: " + packet.sessionId());
            }
            byte[] encrypted = encoder.encode(packet, aesKey);
            next.accept(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("Failed to encrypt packet", e);
        }
    }
}