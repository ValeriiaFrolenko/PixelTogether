package server.network;

import com.google.inject.Singleton;

import java.net.Socket;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class ConnectionManager {

    private final ConcurrentHashMap<Long, Socket> sessionSockets = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, ConcurrentHashMap<Long, Socket>> roomSockets = new ConcurrentHashMap<>();

    public void register(long sessionId, Socket socket) {
        sessionSockets.put(sessionId, socket);
    }

    public void unregister(long sessionId) {
        sessionSockets.remove(sessionId);
    }

    public void assignRoom(long sessionId, int roomId) {
        Socket socket = sessionSockets.get(sessionId);
        if (socket != null) {
            roomSockets.computeIfAbsent(roomId, k -> new ConcurrentHashMap<>()).put(sessionId, socket);
        }
    }

    public void leaveRoom(long sessionId, int roomId) {
        ConcurrentHashMap<Long, Socket> room = roomSockets.get(roomId);
        if (room != null) {
            room.remove(sessionId);
        }
    }

    public Socket getSocket(long sessionId) {
        return sessionSockets.get(sessionId);
    }

    public Collection<Socket> getSocketsByRoom(int roomId) {
        ConcurrentHashMap<Long, Socket> room = roomSockets.get(roomId);
        return room != null ? room.values() : Collections.emptyList();
    }

    public int countByRoom(int roomId) {
        ConcurrentHashMap<Long, Socket> room = roomSockets.get(roomId);
        return room != null ? room.size() : 0;
    }
}