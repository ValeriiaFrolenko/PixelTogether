package common.dto.room;

public record CanvasStateResponse(
        int roomId,
        int width,
        int height,
        int[] pixels,
        boolean isOwner
) {}