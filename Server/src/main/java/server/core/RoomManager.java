package server.core;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import common.dto.PixelUpdate;
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

    private record Point(int x, int y) {}

    private record ActiveRoom(
            Room room,
            int canvasW,
            int canvasH,
            ConcurrentHashMap<Point, Integer> canvas
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
        rooms.put(saved.id(), new ActiveRoom(
                saved,
                saved.canvasW(),
                saved.canvasH(),
                new ConcurrentHashMap<>()
        ));
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

    public Room getRoom(int roomId) {
        ActiveRoom active = rooms.get(roomId);
        return active != null ? active.room() : null;
    }

    public boolean exists(int roomId) {
        return rooms.containsKey(roomId);
    }

    public void applyPixels(int roomId, List<PixelUpdate> pixels) {
        ActiveRoom active = rooms.get(roomId);
        if (active == null) return;
        for (PixelUpdate pixel : pixels) {
            active.canvas().put(new Point(pixel.x(), pixel.y()), pixel.color());
        }
    }

    public void flushAll() {
        rooms.forEach((roomId, active) -> {
            byte[] state = serializeCanvas(active);
            roomDao.updateCanvasState(roomId, state);
        });
    }

    private void shutdown() {
        flushAll();
        scheduler.shutdown();
    }

    private byte[] serializeCanvas(ActiveRoom active) {
        int w = active.canvasW();
        int h = active.canvasH();
        ByteBuffer buf = ByteBuffer.allocate(w * h * 4);
        for (int y = 0; y < h; y++)
            for (int x = 0; x < w; x++)
                buf.putInt(active.canvas().getOrDefault(new Point(x, y), 0));
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
        int w = active.canvasW();
        int h = active.canvasH();
        int[] pixels = new int[w * h];
        for (int y = 0; y < h; y++)
            for (int x = 0; x < w; x++)
                pixels[y * w + x] = active.canvas().getOrDefault(new Point(x, y), 0);
        return new CanvasStateResponse(w, h, pixels);
    }

    public Optional<Room> findByCode(String code) {
        return rooms.values().stream()
                .map(ActiveRoom::room)
                .filter(room -> code.equals(room.code()))
                .findFirst();
    }
}