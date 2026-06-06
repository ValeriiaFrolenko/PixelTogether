package protocol;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import model.Message;
import model.Packet;
import utils.AesUtil;
import utils.Crc16;

import java.nio.ByteBuffer;

public class PacketEncoder {

    private final byte[] key;

    @Inject
    public PacketEncoder(@Named("aesKey") byte[] key) {
        this.key = key;
    }

    public byte[] encode(Packet packet) throws Exception {
        if (packet == null) {
            throw new IllegalArgumentException("Packet cannot be null");
        }

        byte[] encryptedPayload = AesUtil.encrypt(packet.bMsg().payload(), key);
        byte[] header = buildHeader(packet, encryptedPayload.length);
        byte[] message = buildMessage(packet.bMsg(), encryptedPayload);

        return buildResult(header, message);
    }

    private byte[] buildHeader(Packet packet, int encryptedPayloadLength) {
        int wLen = encryptedPayloadLength + MessageStructure.MESSAGE_HEADER_SIZE;

        ByteBuffer header = ByteBuffer.allocate(PacketStructure.HEADER_SIZE);
        header.put(PacketStructure.MAGIC_BYTE);
        header.put(packet.sessionId());
        header.putLong(packet.bPktId());
        header.putInt(wLen);
        return header.array();
    }

    private byte[] buildMessage(Message msg, byte[] encryptedPayload) {
        ByteBuffer message = ByteBuffer.allocate(
                MessageStructure.MESSAGE_HEADER_SIZE + encryptedPayload.length);

        message.putInt(msg.cType());
        message.putInt(msg.roomId());
        message.put(encryptedPayload);
        return message.array();
    }

    private byte[] buildResult(byte[] header, byte[] message) {
        ByteBuffer result = ByteBuffer.allocate(
                PacketStructure.MIN_PACKET_SIZE + message.length);

        result.put(header);
        result.putShort(Crc16.calculateCrc(header));
        result.put(message);
        result.putShort(Crc16.calculateCrc(message));

        return result.array();
    }
}