package protocol;

/**
 * Command types for the PixelTogether protocol.
 *
 * Client → Server:
 *   AUTH group    - authentication and session management
 *   ROOM group    - room management
 *   DRAW group    - drawing actions
 *   WORK group    - saving and gallery
 *
 * Server → Client:
 *   RESPONSE group - server responses and broadcasts
 */
public enum CommandType {

    // --- AUTH ---
    REGISTER(1),
    LOGIN(2),
    LOGOUT(3),

    // --- ROOM ---
    CREATE_ROOM(10),
    JOIN_ROOM_PUBLIC(11),
    JOIN_ROOM_PRIVATE(12),
    LEAVE_ROOM(13),
    CLOSE_ROOM(14),
    GET_ROOMS(15),

    // --- DRAW ---
    DRAW(20),

    // --- WORK ---
    SAVE_WORK(30),
    GET_GALLERY(31),

    // --- SERVER RESPONSES ---
    OK(100),
    ERROR(101),
    ROOM_LIST(102),
    CANVAS_STATE(103),
    GALLERY(104),
    ROOM_UPDATE(105),
    PARTICIPANT_JOINED(106),
    PARTICIPANT_LEFT(107);

    private final int code;

    CommandType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static CommandType fromCode(int code) {
        for (CommandType type : values()) {
            if (type.code == code) return type;
        }
        throw new IllegalArgumentException("Unknown command type: " + code);
    }
}