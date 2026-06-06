package model;

public record Packet(
        byte sessionId,
        long bPktId,
        Message bMsg
) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private byte sessionId;
        private long bPktId;
        private Message bMsg;

        public Builder sessionId(byte sessionId) { this.sessionId = sessionId; return this; }
        public Builder bPktId(long bPktId) { this.bPktId = bPktId; return this; }
        public Builder bMsg(Message bMsg) { this.bMsg = bMsg; return this; }

        public Packet build() {
            return new Packet(sessionId, bPktId, bMsg);
        }
    }
}