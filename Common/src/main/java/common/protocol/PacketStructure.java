package common.protocol;

/**
 * Packet structure:
 *
 * Offset  Length  Mnemonic    Notes
 * 00      1       bMagic      Start of packet marker, value 0x13
 * 01      8       sessionId   Unique client session identifier, big-endian
 * 09      8       bPktId      Packet sequence number, big-endian, always increasing
 * 17      4       wLen        Length of Message in bytes, big-endian
 * 21      2       wCrc16      CRC16 of bytes (00-20), big-endian
 * 23      wLen    bMsg        Message - useful payload
 * 23+wLen 2       wCrc16      CRC16 of bytes (23 to 23+wLen-1), big-endian
 */
public final class PacketStructure {

    private PacketStructure() {}

    public static final byte MAGIC_BYTE = 0x13;

    public static final int LEN_MAGIC      = 1;
    public static final int LEN_SESSION_ID = 8;
    public static final int LEN_PKT_ID     = 8;
    public static final int LEN_W_LEN      = 4;
    public static final int LEN_CRC16      = 2;

    public static final int OFFSET_MAGIC      = 0;
    public static final int OFFSET_SESSION_ID = OFFSET_MAGIC + LEN_MAGIC;
    public static final int OFFSET_PKT_ID     = OFFSET_SESSION_ID + LEN_SESSION_ID;
    public static final int OFFSET_W_LEN      = OFFSET_PKT_ID + LEN_PKT_ID;
    public static final int OFFSET_HDR_CRC16  = OFFSET_W_LEN + LEN_W_LEN;
    public static final int OFFSET_MSG        = OFFSET_HDR_CRC16 + LEN_CRC16;

    public static final int HEADER_SIZE      = OFFSET_HDR_CRC16;
    public static final int MIN_PACKET_SIZE  = OFFSET_MSG + LEN_CRC16;
}