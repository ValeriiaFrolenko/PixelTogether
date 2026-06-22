package common.dto.work;

public record GalleryItem(
        int id,
        String ownerUsername,
        String title,
        int canvasW,
        int canvasH,
        String savedAt
) {}