package common.dto.room;

public record CanvasStateResponse(
        int width,
        int height,
        int[] pixels
) {}