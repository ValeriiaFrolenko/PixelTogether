package common.protocol;

/**
 * Packet structure:
 *
 * Offset  Length  Mnemonic    Notes
 * 00      1       bMagic      Start of packet marker, value 0x13
 * 01      1       sessionId   Unique client session identifier
 * 02      8       bPktId      Packet sequence number, big-endian, always increasing
 * 10      4       wLen        Length of Message in bytes, big-endian
 * 14      2       wCrc16      CRC16 of bytes (00-13), big-endian
 * 16      wLen    bMsg        Message - useful payload
 * 16+wLen 2       wCrc16      CRC16 of bytes (16 to 16+wLen-1), big-endian
 *
 * Message structure:
 *
 * Offset  Length    Mnemonic  Notes
 * 00      4         cType     Command type code, big-endian
 * 04      4         roomId    Room identifier, 0 if not applicable, big-endian
 * 08      wLen-8    payload   JSON-encoded data as byte array, big-endian
 */
public final class MessageStructure {

    private MessageStructure() {}

    public static final int LEN_CMD_TYPE = 4;
    public static final int LEN_ROOM_ID  = 4;

    public static final int OFFSET_CMD_TYPE = 0;
    public static final int OFFSET_ROOM_ID  = OFFSET_CMD_TYPE + LEN_CMD_TYPE;
    public static final int OFFSET_PAYLOAD  = OFFSET_ROOM_ID + LEN_ROOM_ID;

    public static final int MESSAGE_HEADER_SIZE = OFFSET_PAYLOAD;
}