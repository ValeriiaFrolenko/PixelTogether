package server.core;

import com.google.inject.Singleton;

import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class ParticipantManager {

    private final ConcurrentHashMap<Long, String> nicknames = new ConcurrentHashMap<>();

    public String assign(long sessionId) {
        String nickname = NicknameGenerator.generate();
        nicknames.put(sessionId, nickname);
        return nickname;
    }

    public String get(long sessionId) {
        return nicknames.get(sessionId);
    }

    public void remove(long sessionId) {
        nicknames.remove(sessionId);
    }
}