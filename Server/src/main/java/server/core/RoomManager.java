package server.core;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import common.dto.room.CanvasStateResponse;
import server.database.dao.RoomDao;
import server.database.model.Room;

import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Singleton
public class RoomManager {

    private record ActiveRoom(
            Room room,
            int[][] canvas
    ) {}

    private final ConcurrentHashMap<Integer, ActiveRoom> rooms = new ConcurrentHashMap<>();
    private final RoomDao roomDao;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @Inject
    public RoomManager(RoomDao roomDao) {
        this.roomDao = roomDao;
        scheduler.scheduleAtFixedRate(this::flushAll, 1, 1, TimeUnit.SECONDS);
        scheduler.scheduleAtFixedRate(this::deleteExpired, 60, 60, TimeUnit.SECONDS);
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
    }

    public long createRoom(Room room) {
        long roomId = roomDao.save(room);
        Room saved = roomDao.findById((int) roomId).orElseThrow();
        int[][] canvas = new int[saved.canvasH()][saved.canvasW()];
        rooms.put(saved.id(), new ActiveRoom(saved, canvas));
        return roomId;
    }

    public void deleteRoom(int roomId) {
        rooms.remove(roomId);
        roomDao.deleteById(roomId);
    }

    public void deleteExpired() {
        rooms.entrySet().removeIf(entry -> {
            if (entry.getValue().room().expiresAt().isBefore(LocalDateTime.now())) {
                roomDao.deleteById(entry.getKey());
                return true;
            }
            return false;
        });
    }

    public int[][] getCanvas(int roomId) {
        ActiveRoom active = rooms.get(roomId);
        return active != null ? active.canvas() : null;
    }

    public Room getRoom(int roomId) {
        ActiveRoom active = rooms.get(roomId);
        return active != null ? active.room() : null;
    }

    public boolean exists(int roomId) {
        return rooms.containsKey(roomId);
    }

    public void applyPixels(int roomId, int x, int y, int color) {
        ActiveRoom active = rooms.get(roomId);
        if (active != null) {
            active.canvas()[y][x] = color;
        }
    }

    public void flushAll() {
        rooms.forEach((roomId, active) -> {
            byte[] state = serializeCanvas(active.canvas());
            roomDao.updateCanvasState(roomId, state);
        });
    }

    private void shutdown() {
        flushAll();
        scheduler.shutdown();
    }

    private byte[] serializeCanvas(int[][] canvas) {
        int h = canvas.length;
        int w = canvas[0].length;
        ByteBuffer buf = ByteBuffer.allocate(w * h * 4);
        for (int[] row : canvas)
            for (int color : row)
                buf.putInt(color);
        return buf.array();
    }

    public List<Room> getPublicRooms() {
        return rooms.values().stream()
                .map(ActiveRoom::room)
                .filter(room -> !room.isPrivate())
                .toList();
    }

    public CanvasStateResponse getCanvasState(int roomId) {
        ActiveRoom active = rooms.get(roomId);
        if (active == null) return null;
        int h = active.canvas().length;
        int w = active.canvas()[0].length;
        int[] pixels = new int[w * h];
        for (int y = 0; y < h; y++)
            for (int x = 0; x < w; x++)
                pixels[y * w + x] = active.canvas()[y][x];
        return new CanvasStateResponse(w, h, pixels);
    }

    public Optional<Room> findByCode(String code) {
        return rooms.values().stream()
                .map(ActiveRoom::room)
                .filter(room -> code.equals(room.code()))
                .findFirst();
    }
}