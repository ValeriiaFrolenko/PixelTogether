package common.dto.room;

public record CreateRoomResponse(
        long roomId,
        String code
) {}