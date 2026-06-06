package core;

import com.google.inject.Singleton;

import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class SessionManager {

    public record Session(
            byte sessionId,
            Integer userId,
            String nickname,
            Integer roomId
    ) {}

    private final ConcurrentHashMap<Byte, Session> sessions = new ConcurrentHashMap<>();

    public void register(byte sessionId, String nickname) {
        sessions.put(sessionId, new Session(sessionId, null, nickname, null));
    }

    public void authenticate(byte sessionId, int userId) {
        Session session = sessions.get(sessionId);
        if (session != null) {
            sessions.put(sessionId, new Session(sessionId, userId, session.nickname(), session.roomId()));
        }
    }

    public void assignRoom(byte sessionId, int roomId) {
        Session session = sessions.get(sessionId);
        if (session != null) {
            sessions.put(sessionId, new Session(sessionId, session.userId(), session.nickname(), roomId));
        }
    }

    public void leaveRoom(byte sessionId) {
        Session session = sessions.get(sessionId);
        if (session != null) {
            sessions.put(sessionId, new Session(sessionId, session.userId(), session.nickname(), null));
        }
    }

    public void unregister(byte sessionId) {
        sessions.remove(sessionId);
    }

    public Session get(byte sessionId) {
        return sessions.get(sessionId);
    }

    public boolean isAuthenticated(byte sessionId) {
        Session session = sessions.get(sessionId);
        return session != null && session.userId() != null;
    }
}