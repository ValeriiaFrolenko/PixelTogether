package network;

import com.google.inject.Singleton;

import java.net.Socket;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class ConnectionManager {

    private final ConcurrentHashMap<Integer, Socket> sessionSockets = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, Socket>> roomSockets = new ConcurrentHashMap<>();

    public void register(int sessionId, Socket socket) {
        sessionSockets.put(sessionId, socket);
    }

    public void unregister(int sessionId) {
        sessionSockets.remove(sessionId);
    }

    public void assignRoom(int sessionId, int roomId) {
        Socket socket = sessionSockets.get(sessionId);
        if (socket != null) {
            roomSockets.computeIfAbsent(roomId, k -> new ConcurrentHashMap<>()).put(sessionId, socket);
        }
    }

    public void leaveRoom(int sessionId, int roomId) {
        ConcurrentHashMap<Integer, Socket> room = roomSockets.get(roomId);
        if (room != null) {
            room.remove(sessionId);
        }
    }

    public Socket getSocket(int sessionId) {
        return sessionSockets.get(sessionId);
    }

    public Collection<Socket> getSocketsByRoom(int roomId) {
        ConcurrentHashMap<Integer, Socket> room = roomSockets.get(roomId);
        return room != null ? room.values() : Collections.emptyList();
    }
}