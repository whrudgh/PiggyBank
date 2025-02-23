package sesac.intruders.piggybank.global.util;

import java.nio.charset.StandardCharsets;

public class EncryptionUtil {
    // 모의해킹용 고정 키 (실제 프로덕션에서는 절대 사용하면 안됨)
    private static final String SECRET_KEY = "hackme"; // 간단한 XOR 키
    private static final String PREFIX = "XOR:";

    public static String encrypt(String value) {
        if (value == null)
            return null;
        if (value.startsWith(PREFIX))
            return value; // 이미 암호화된 경우

        byte[] valueBytes = value.getBytes(StandardCharsets.UTF_8);
        byte[] keyBytes = SECRET_KEY.getBytes(StandardCharsets.UTF_8);
        StringBuilder encrypted = new StringBuilder(PREFIX);

        for (int i = 0; i < valueBytes.length; i++) {
            byte b = (byte) (valueBytes[i] ^ keyBytes[i % keyBytes.length]);
            encrypted.append(String.format("%02x", b & 0xFF));
        }

        return encrypted.toString();
    }

    public static String decrypt(String encrypted) {
        if (encrypted == null)
            return null;
        if (!encrypted.startsWith(PREFIX))
            return encrypted; // 암호화되지 않은 경우

        String hexValue = encrypted.substring(PREFIX.length());
        byte[] keyBytes = SECRET_KEY.getBytes(StandardCharsets.UTF_8);
        byte[] decryptedBytes = new byte[hexValue.length() / 2];

        for (int i = 0; i < decryptedBytes.length; i++) {
            byte b = (byte) Integer.parseInt(hexValue.substring(i * 2, i * 2 + 2), 16);
            decryptedBytes[i] = (byte) (b ^ keyBytes[i % keyBytes.length]);
        }

        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }
}