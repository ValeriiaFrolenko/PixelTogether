package server.protocol;

import common.model.Message;
import common.model.Packet;
import common.protocol.PacketDecoder;
import common.protocol.PacketEncoder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Round-trip тести: encode → decode повертає той самий пакет.
 * Якщо хтось зламає протокол (зміщення, CRC, шифрування) — ці тести впадуть першими.
 */
class PacketProtocolTest {

    private final PacketEncoder encoder = new PacketEncoder();
    private final PacketDecoder decoder = new PacketDecoder();
    // AES-128 ключ — рівно 16 байт
    private final byte[] aesKey = "0123456789abcdef".getBytes();

    @Test
    void roundTrip_simplePacket_decodesCorrectly() throws Exception {
        Packet original = buildPacket(42L, 7L, 100, 0, "hello".getBytes());

        byte[] encoded = encoder.encode(original, aesKey);
        Packet decoded = decoder.decode(encoded, aesKey);

        assertEquals(original.sessionId(), decoded.sessionId());
        assertEquals(original.bPktId(), decoded.bPktId());
        assertEquals(original.bMsg().cType(), decoded.bMsg().cType());
        assertEquals(original.bMsg().roomId(), decoded.bMsg().roomId());
        assertArrayEquals(original.bMsg().payload(), decoded.bMsg().payload());
    }

    @Test
    void roundTrip_emptyPayload_decodesCorrectly() throws Exception {
        Packet original = buildPacket(1L, 1L, 100, 0, new byte[0]);

        byte[] encoded = encoder.encode(original, aesKey);
        Packet decoded = decoder.decode(encoded, aesKey);

        assertArrayEquals(new byte[0], decoded.bMsg().payload());
    }

    @Test
    void roundTrip_largePayload_decodesCorrectly() throws Exception {
        byte[] payload = new byte[4096];
        for (int i = 0; i < payload.length; i++) payload[i] = (byte) (i % 127);

        Packet original = buildPacket(999L, 100L, 20, 5, payload);
        byte[] encoded = encoder.encode(original, aesKey);
        Packet decoded = decoder.decode(encoded, aesKey);

        assertArrayEquals(payload, decoded.bMsg().payload());
        assertEquals(5, decoded.bMsg().roomId());
    }

    @Test
    void decode_corruptedHeaderCrc_throwsException() throws Exception {
        Packet original = buildPacket(1L, 1L, 100, 0, "data".getBytes());
        byte[] encoded = encoder.encode(original, aesKey);

        // псуємо байт в заголовку (позиція 5 — всередині sessionId)
        encoded[5] ^= 0xFF;

        assertThrows(Exception.class, () -> decoder.decode(encoded, aesKey));
    }

    @Test
    void decode_corruptedMessageCrc_throwsException() throws Exception {
        Packet original = buildPacket(1L, 1L, 100, 0, "data".getBytes());
        byte[] encoded = encoder.encode(original, aesKey);

        // псуємо останній байт — це частина message CRC або payload
        encoded[encoded.length - 1] ^= 0xFF;

        assertThrows(Exception.class, () -> decoder.decode(encoded, aesKey));
    }

    @Test
    void decode_wrongAesKey_throwsException() throws Exception {
        Packet original = buildPacket(1L, 1L, 100, 0, "secret".getBytes());
        byte[] encoded = encoder.encode(original, aesKey);

        byte[] wrongKey = "abcdef0123456789".getBytes();
        assertThrows(Exception.class, () -> decoder.decode(encoded, wrongKey));
    }

    @Test
    void roundTrip_sessionIdPreserved() throws Exception {
        long sessionId = Long.MAX_VALUE;
        Packet original = buildPacket(sessionId, 1L, 100, 0, new byte[0]);

        byte[] encoded = encoder.encode(original, aesKey);
        Packet decoded = decoder.decode(encoded, aesKey);

        assertEquals(sessionId, decoded.sessionId());
    }

    @Test
    void roundTrip_pktIdPreserved() throws Exception {
        long pktId = Long.MAX_VALUE - 1;
        Packet original = buildPacket(1L, pktId, 100, 0, new byte[0]);

        byte[] encoded = encoder.encode(original, aesKey);
        Packet decoded = decoder.decode(encoded, aesKey);

        assertEquals(pktId, decoded.bPktId());
    }

    private Packet buildPacket(long sessionId, long pktId, int cType, int roomId, byte[] payload) {
        return Packet.builder()
                .sessionId(sessionId)
                .bPktId(pktId)
                .bMsg(Message.builder()
                        .cType(cType)
                        .roomId(roomId)
                        .payload(payload)
                        .build())
                .build();
    }
}