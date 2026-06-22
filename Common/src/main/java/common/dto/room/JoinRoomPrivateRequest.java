package common.dto.room;

public record JoinRoomPrivateRequest(
        String code,
        String token
) {}