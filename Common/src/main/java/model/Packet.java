package model;

public record Packet(
        byte bSrc,
        long bPktId,
        Message bMsg
) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private byte bSrc;
        private long bPktId;
        private Message bMsg;

        public Builder bSrc(byte bSrc) { this.bSrc = bSrc; return this; }
        public Builder bPktId(long bPktId) { this.bPktId = bPktId; return this; }
        public Builder bMsg(Message bMsg) { this.bMsg = bMsg; return this; }

        public Packet build() {
            return new Packet(bSrc, bPktId, bMsg);
        }
    }
}