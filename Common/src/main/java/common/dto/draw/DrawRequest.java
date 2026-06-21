package common.dto.draw;

import java.util.List;

public record DrawRequest(
        List<PixelUpdate> pixels
) {}