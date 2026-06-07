package common.dto.user;

public record RegisterRequest(
        String username,
        String password
) {}