package common.dto.room;

public record CreateRoomRequest(
        String token,
        String name,
        int canvasW,
        int canvasH,
        boolean isPrivate,
        long durationMinutes
) {}