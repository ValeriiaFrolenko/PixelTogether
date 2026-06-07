package server.database.model;

public record User(
        int id,
        String username,
        String password,
        String role,
        java.time.LocalDateTime createdAt
) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private int id;
        private String username;
        private String password;
        private String role;
        private java.time.LocalDateTime createdAt;

        public Builder id(int id) { this.id = id; return this; }
        public Builder username(String username) { this.username = username; return this; }
        public Builder password(String password) { this.password = password; return this; }
        public Builder role(String role) { this.role = role; return this; }
        public Builder createdAt(java.time.LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public User build() {
            return new User(id, username, password, role, createdAt);
        }
    }
}