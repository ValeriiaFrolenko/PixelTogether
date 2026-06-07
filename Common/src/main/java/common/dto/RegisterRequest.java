package common.dto;

public record RegisterRequest(
        String username,
        String password
) {}