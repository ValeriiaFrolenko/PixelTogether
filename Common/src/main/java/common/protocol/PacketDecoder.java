package common.protocol;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import common.model.Message;
import common.model.Packet;
import common.utils.AesUtil;
import common.utils.Crc16;

import java.nio.ByteBuffer;

public class PacketDecoder {

    private final byte[] key;

    @Inject
    public PacketDecoder(@Named("aesKey") byte[] key) {
        this.key = key;
    }

    public Packet decode(byte[] data) throws Exception {
        if (data == null || data.length < PacketStructure.MIN_PACKET_SIZE) {
            throw new IllegalArgumentException("Invalid packet data");
        }

        ByteBuffer buffer = ByteBuffer.wrap(data);

        validateMagic(buffer);
        long sessionId = buffer.getLong();
        long bPktId = buffer.getLong();
        int wLen = buffer.getInt();

        validateLength(data, wLen);
        validateHeaderCrc(buffer, data);

        int cType = buffer.getInt();
        int roomId = buffer.getInt();

        byte[] encryptedPayload = readPayload(buffer, wLen);
        validateMessageCrc(buffer, data, wLen);

        byte[] payload = AesUtil.decrypt(encryptedPayload, key);

        return Packet.builder()
                .sessionId(sessionId)
                .bPktId(bPktId)
                .bMsg(Message.builder()
                        .cType(cType)
                        .roomId(roomId)
                        .payload(payload)
                        .build())
                .build();
    }

    private void validateMagic(ByteBuffer buffer) {
        if (buffer.get() != PacketStructure.MAGIC_BYTE) {
            throw new IllegalArgumentException("Invalid magic byte");
        }
    }

    private void validateLength(byte[] data, int wLen) {
        if (data.length < PacketStructure.MIN_PACKET_SIZE + wLen) {
            throw new IllegalArgumentException("Packet too short");
        }
    }

    private void validateHeaderCrc(ByteBuffer buffer, byte[] data) {
        short headerCrc = buffer.getShort();
        if (headerCrc != Crc16.calculateCrc(data, 0, PacketStructure.HEADER_SIZE)) {
            throw new IllegalArgumentException("Header CRC mismatch");
        }
    }

    private byte[] readPayload(ByteBuffer buffer, int wLen) {
        byte[] encryptedPayload = new byte[wLen - MessageStructure.MESSAGE_HEADER_SIZE];
        buffer.get(encryptedPayload);
        return encryptedPayload;
    }

    private void validateMessageCrc(ByteBuffer buffer, byte[] data, int wLen) {
        short messageCrc = buffer.getShort();
        if (messageCrc != Crc16.calculateCrc(data, PacketStructure.OFFSET_MSG, wLen)) {
            throw new IllegalArgumentException("Message CRC mismatch");
        }
    }
}