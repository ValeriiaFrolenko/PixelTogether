package common.dto.room;

public record RoomInfo(
        int roomId,
        String name,
        int participantsOnline
) {}