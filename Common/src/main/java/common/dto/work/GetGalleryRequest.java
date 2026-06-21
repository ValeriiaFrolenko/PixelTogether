package common.dto.work;

public record GetGalleryRequest(
        String title,
        String ownerUsername
) {}
