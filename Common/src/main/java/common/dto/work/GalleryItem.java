package common.dto.work;

import java.time.LocalDateTime;

public record GalleryItem(
        int id,
        String ownerUsername,
        String title,
        int canvasW,
        int canvasH,
        LocalDateTime savedAt
) {}
