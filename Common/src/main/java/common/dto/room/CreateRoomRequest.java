package common.dto.room;

public record CreateRoomRequest(
        String name,
        int canvasW,
        int canvasH,
        boolean isPrivate,
        long durationMinutes
) {}