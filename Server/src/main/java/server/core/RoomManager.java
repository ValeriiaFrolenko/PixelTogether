package server.core;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import common.dto.draw.PixelUpdate;
import common.dto.room.CanvasStateResponse;
import server.database.dao.RoomDao;
import server.database.model.Room;
import server.network.ConnectionManager;

import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicIntegerArray;

@Singleton
public class RoomManager {

    private record ActiveRoom(
            Room room,
            int canvasW,
            int canvasH,
            AtomicIntegerArray canvas
    ) {}

    private final ConcurrentHashMap<Integer, ActiveRoom> rooms = new ConcurrentHashMap<>();
    private final RoomDao roomDao;
    private final ConnectionManager connectionManager;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @Inject
    public RoomManager(RoomDao roomDao, ConnectionManager connectionManager) {
        this.roomDao = roomDao;
        this.connectionManager = connectionManager;
        scheduler.scheduleAtFixedRate(this::flushAll, 1, 1, TimeUnit.SECONDS);
        scheduler.scheduleAtFixedRate(this::deleteExpired, 60, 60, TimeUnit.SECONDS);
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
    }

    private int index(int x, int y, int width) {
        return y * width + x;
    }

    public long createRoom(Room room) {
        long roomId = roomDao.save(room);
        Room saved = roomDao.findById((int) roomId).orElseThrow();
        rooms.put(saved.id(), new ActiveRoom(
                saved,
                saved.canvasW(),
                saved.canvasH(),
                new AtomicIntegerArray(saved.canvasW() * saved.canvasH())
        ));
        return roomId;
    }

    public void deleteRoom(int roomId) {
        rooms.remove(roomId);
        roomDao.deleteById(roomId);
        connectionManager.clearRoom(roomId);
    }

    public void deleteExpired() {
        rooms.entrySet().removeIf(entry -> {
            if (entry.getValue().room().expiresAt().isBefore(LocalDateTime.now())) {
                connectionManager.clearRoom(entry.getKey());
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
            active.canvas().set(index(pixel.x(), pixel.y(), active.canvasW()), pixel.color());
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
        for (int i = 0; i < w * h; i++)
            buf.putInt(active.canvas().get(i));
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
        for (int i = 0; i < w * h; i++)
            pixels[i] = active.canvas().get(i);
        return new CanvasStateResponse(roomId, w, h, pixels, false);
    }

    public Optional<Room> findByCode(String code) {
        return rooms.values().stream()
                .map(ActiveRoom::room)
                .filter(room -> code.equals(room.code()))
                .findFirst();
    }

    public List<Room> getRoomsByOwner(int ownerId) {
        return rooms.values().stream()
                .map(ActiveRoom::room)
                .filter(room -> room.ownerId() == ownerId)
                .toList();
    }
}