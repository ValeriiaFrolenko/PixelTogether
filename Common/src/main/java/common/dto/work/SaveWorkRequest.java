package common.dto.work;

public record SaveWorkRequest(
        String token,
        String title,
        boolean isPublic
) {}
