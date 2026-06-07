package common.protocol;

import common.model.Packet;
import java.util.function.Consumer;

public interface Decryptor {
    void decrypt(byte[] encryptedPacket, Consumer<Packet> next);
}