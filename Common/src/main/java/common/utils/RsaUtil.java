package common.utils;

import javax.crypto.Cipher;
import java.security.*;

public class RsaUtil {

    private static final String ALGORITHM = "RSA";
    private static final int KEY_SIZE = 2048;

    private RsaUtil() {}

    public static KeyPair generateKeyPair() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance(ALGORITHM);
        generator.initialize(KEY_SIZE);
        return generator.generateKeyPair();
    }

    public static byte[] encrypt(byte[] data, PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return cipher.doFinal(data);
    }

    public static byte[] decrypt(byte[] data, PrivateKey privateKey) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        return cipher.doFinal(data);
    }

    public static byte[] publicKeyToBytes(PublicKey publicKey) {
        return publicKey.getEncoded();
    }

    public static PublicKey publicKeyFromBytes(byte[] bytes) throws Exception {
        return KeyFactory.getInstance(ALGORITHM)
                .generatePublic(new java.security.spec.X509EncodedKeySpec(bytes));
    }
}