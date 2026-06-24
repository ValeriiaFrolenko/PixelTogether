package common.protocol;

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
    GET_MY_ROOMS(16),

    // --- DRAW ---
    DRAW(20),

    // --- WORK ---
    SAVE_WORK(30),
    GET_GALLERY(31),
    GET_WORK(32),
    DELETE_WORK(33),
    GET_MY_WORKS(34),

    // --- SERVER RESPONSES ---
    OK(100),
    ERROR(101),
    ROOM_LIST(102),
    CANVAS_STATE(103),
    GALLERY(104),
    WORK(105),
    ROOM_UPDATE(106),
    PARTICIPANT_JOINED(107),
    PARTICIPANT_LEFT(108),
    MY_WORKS(109),
    MY_ROOMS(110),
    ROOM_CLOSED(111);

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