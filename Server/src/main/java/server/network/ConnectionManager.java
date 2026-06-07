package server.network;

import com.google.inject.Singleton;

import java.net.Socket;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class ConnectionManager {

    private final ConcurrentHashMap<Byte, Socket> sessionSockets = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, ConcurrentHashMap<Byte, Socket>> roomSockets = new ConcurrentHashMap<>();

    public void register(byte sessionId, Socket socket) {
        sessionSockets.put(sessionId, socket);
    }

    public void unregister(byte sessionId) {
        sessionSockets.remove(sessionId);
    }

    public void assignRoom(byte sessionId, int roomId) {
        Socket socket = sessionSockets.get(sessionId);
        if (socket != null) {
            roomSockets.computeIfAbsent(roomId, k -> new ConcurrentHashMap<>()).put(sessionId, socket);
        }
    }

    public void leaveRoom(byte sessionId, int roomId) {
        ConcurrentHashMap<Byte, Socket> room = roomSockets.get(roomId);
        if (room != null) {
            room.remove(sessionId);
        }
    }

    public Socket getSocket(byte sessionId) {
        return sessionSockets.get(sessionId);
    }

    public Collection<Socket> getSocketsByRoom(int roomId) {
        ConcurrentHashMap<Byte, Socket> room = roomSockets.get(roomId);
        return room != null ? room.values() : Collections.emptyList();
    }

    public int countByRoom(int roomId) {
        ConcurrentHashMap<Byte, Socket> room = roomSockets.get(roomId);
        return room != null ? room.size() : 0;
    }
}