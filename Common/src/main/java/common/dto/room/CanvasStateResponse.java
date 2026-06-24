package common.dto.room;

import java.util.List;

public record CanvasStateResponse(
        int width,
        int height,
        int[] pixels,
        boolean isOwner,
        List<String> nicknames
) {}