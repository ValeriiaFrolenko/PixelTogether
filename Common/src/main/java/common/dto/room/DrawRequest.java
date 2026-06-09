package common.dto.room;

import common.dto.PixelUpdate;
import java.util.List;

public record DrawRequest(
        List<PixelUpdate> pixels
) {}