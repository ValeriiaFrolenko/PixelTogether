package common.dto.work;

public record GetWorkResponse(
        int id,
        String ownerUsername,
        String title,
        int canvasW,
        int canvasH,
        int[] pixels
) {}
