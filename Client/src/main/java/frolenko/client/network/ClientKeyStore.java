package frolenko.client.network;

import com.google.inject.Singleton;
import common.network.KeyStore;

@Singleton
public class ClientKeyStore implements KeyStore {

    private byte[] aesKey;

    @Override
    public void register(long sessionId, byte[] aesKey) {
        this.aesKey = aesKey;
    }

    @Override
    public byte[] get(long sessionId) {
        return aesKey;
    }

    @Override
    public void remove(long sessionId) {
        this.aesKey = null;
    }
}