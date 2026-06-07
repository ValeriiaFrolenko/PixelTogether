package common.model;

public record Message(
        int cType,
        int roomId,
        byte[] payload
) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private int cType;
        private int roomId;
        private byte[] payload;

        public Builder cType(int cType) { this.cType = cType; return this; }
        public Builder roomId(int roomId) { this.roomId = roomId; return this; }
        public Builder payload(byte[] payload) { this.payload = payload; return this; }

        public Message build() {
            return new Message(cType, roomId, payload);
        }
    }
}