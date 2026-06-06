package database.model;

import java.time.LocalDateTime;

public record Room(
        int id,
        String name,
        String code,
        int ownerId,
        boolean isPrivate,
        int canvasW,
        int canvasH,
        byte[] canvasState,
        LocalDateTime expiresAt,
        LocalDateTime createdAt
) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private int id;
        private String name;
        private String code;
        private int ownerId;
        private boolean isPrivate;
        private int canvasW;
        private int canvasH;
        private byte[] canvasState;
        private LocalDateTime expiresAt;
        private LocalDateTime createdAt;

        public Builder id(int id) { this.id = id; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder code(String code) { this.code = code; return this; }
        public Builder ownerId(int ownerId) { this.ownerId = ownerId; return this; }
        public Builder isPrivate(boolean isPrivate) { this.isPrivate = isPrivate; return this; }
        public Builder canvasW(int canvasW) { this.canvasW = canvasW; return this; }
        public Builder canvasH(int canvasH) { this.canvasH = canvasH; return this; }
        public Builder canvasState(byte[] canvasState) { this.canvasState = canvasState; return this; }
        public Builder expiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public Room build() {
            return new Room(id, name, code, ownerId, isPrivate, canvasW, canvasH, canvasState, expiresAt, createdAt);
        }
    }
}