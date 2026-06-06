package dto;

public record RegisterRequest(
        String username,
        String password
) {}