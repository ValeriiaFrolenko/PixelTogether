package common.protocol;

import common.model.Packet;
import java.util.function.Consumer;

public interface Encryptor {
    void encrypt(Packet packet, Consumer<byte[]> next);
}