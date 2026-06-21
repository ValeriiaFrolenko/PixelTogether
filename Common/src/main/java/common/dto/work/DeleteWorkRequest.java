package common.dto.work;

public record DeleteWorkRequest(
        String token,
        int workId
) {}
