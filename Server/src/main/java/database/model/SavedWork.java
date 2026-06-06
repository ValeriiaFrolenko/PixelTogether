package database.model;

import java.time.LocalDateTime;

public record SavedWork(
        int id,
        int ownerId,
        String title,
        boolean isPublic,
        byte[] imageData,
        int canvasW,
        int canvasH,
        LocalDateTime savedAt
) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private int id;
        private int ownerId;
        private String title;
        private boolean isPublic;
        private byte[] imageData;
        private int canvasW;
        private int canvasH;
        private LocalDateTime savedAt;

        public Builder id(int id) { this.id = id; return this; }
        public Builder ownerId(int ownerId) { this.ownerId = ownerId; return this; }
        public Builder title(String title) { this.title = title; return this; }
        public Builder isPublic(boolean isPublic) { this.isPublic = isPublic; return this; }
        public Builder imageData(byte[] imageData) { this.imageData = imageData; return this; }
        public Builder canvasW(int canvasW) { this.canvasW = canvasW; return this; }
        public Builder canvasH(int canvasH) { this.canvasH = canvasH; return this; }
        public Builder savedAt(LocalDateTime savedAt) { this.savedAt = savedAt; return this; }

        public SavedWork build() {
            return new SavedWork(id, ownerId, title, isPublic, imageData, canvasW, canvasH, savedAt);
        }
    }
}