package common.dto.room;

public record MyRoomInfo(
        int roomId,
        String name,
        String code,
        boolean isPrivate,
        String expiresAt
) {}