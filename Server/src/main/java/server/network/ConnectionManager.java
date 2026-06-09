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
    private final ConcurrentHashMap<Long, Integer> sessionRooms = new ConcurrentHashMap<>();

    public void register(long sessionId, Socket socket) {
        sessionSockets.put(sessionId, socket);
    }

    public void unregister(long sessionId) {
        Integer roomId = sessionRooms.get(sessionId);
        if (roomId != null) {
            leaveRoom(sessionId, roomId);
        }
        sessionSockets.remove(sessionId);
    }

    public void assignRoom(long sessionId, int roomId) {
        Socket socket = sessionSockets.get(sessionId);
        if (socket != null) {
            roomSockets.computeIfAbsent(roomId, k -> new ConcurrentHashMap<>()).put(sessionId, socket);
            sessionRooms.put(sessionId, roomId);
        }
    }

    public void leaveRoom(long sessionId, int roomId) {
        ConcurrentHashMap<Long, Socket> room = roomSockets.get(roomId);
        if (room != null) {
            room.remove(sessionId);
        }
        sessionRooms.remove(sessionId);
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

    public void clearRoom(int roomId) {
        ConcurrentHashMap<Long, Socket> room = roomSockets.remove(roomId);
        if (room != null) {
            room.keySet().forEach(sessionRooms::remove);
        }
    }
}