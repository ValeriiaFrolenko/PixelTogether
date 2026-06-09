package server.network;

import com.google.inject.Singleton;
import common.network.KeyStore;

import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class ServerKeyStore implements KeyStore {

    private final ConcurrentHashMap<Long, byte[]> keys = new ConcurrentHashMap<>();

    @Override
    public void register(long sessionId, byte[] aesKey) {
        keys.put(sessionId, aesKey);
    }

    @Override
    public byte[] get(long sessionId) {
        return keys.get(sessionId);
    }

    @Override
    public void remove(long sessionId) {
        keys.remove(sessionId);
    }
}