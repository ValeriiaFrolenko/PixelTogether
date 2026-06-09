package common.network;

public interface KeyStore {
    void register(long sessionId, byte[] aesKey);
    byte[] get(long sessionId);
    void remove(long sessionId);
}