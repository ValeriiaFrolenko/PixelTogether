package server.core;

import com.google.inject.Singleton;

import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class ParticipantManager {

    private final ConcurrentHashMap<Long, String> nicknames = new ConcurrentHashMap<>();

    public void assign(long sessionId, String nickname) {
        nicknames.put(sessionId, nickname);
    }

    public String get(long sessionId) {
        return nicknames.get(sessionId);
    }

    public void remove(long sessionId) {
        nicknames.remove(sessionId);
    }
}