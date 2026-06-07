package common.dto.user;

public record LoginRequest(
        String username,
        String password
) {}