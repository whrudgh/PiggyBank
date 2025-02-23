package sesac.intruders.piggybank.global.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Slf4j
@Component
public class CryptoUtil {
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding";
    private static final String SECRET_KEY = "my-very-insecure-secret-key";
    private static final SecretKeySpec secretKeySpec;

    static {
        byte[] key = SECRET_KEY.getBytes(StandardCharsets.UTF_8);
        byte[] keyBytes = new byte[16];
        System.arraycopy(key, 0, keyBytes, 0, Math.min(key.length, 16));
        secretKeySpec = new SecretKeySpec(keyBytes, ALGORITHM);
    }

    public String encrypt(String data) {
        try {
            log.debug("=== Encryption Start ===");
            log.debug("Input data length: {}", data.length());
            log.debug("Input data: {}", data);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
            byte[] encryptedBytes = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
            String encryptedString = Base64.getEncoder().encodeToString(encryptedBytes);

            log.debug("Encrypted data length: {}", encryptedString.length());
            log.debug("Encrypted data: {}", encryptedString);
            log.debug("=== Encryption End ===");

            return encryptedString;
        } catch (Exception e) {
            log.error("=== Encryption Error ===");
            log.error("Error type: {}", e.getClass().getName());
            log.error("Error message: {}", e.getMessage());
            log.error("Stack trace:", e);
            throw new RuntimeException("Encryption failed", e);
        }
    }

    public String decrypt(String encryptedData) {
        try {
            log.debug("=== Decryption Start ===");
            log.debug("Input data length: {}", encryptedData.length());
            log.debug("Input data: {}", encryptedData);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedData.trim()));
            String decryptedString = new String(decryptedBytes, StandardCharsets.UTF_8);

            log.debug("Decrypted data length: {}", decryptedString.length());
            log.debug("Decrypted data: {}", decryptedString);
            log.debug("=== Decryption End ===");

            return decryptedString;
        } catch (Exception e) {
            log.error("=== Decryption Error ===");
            log.error("Error type: {}", e.getClass().getName());
            log.error("Error message: {}", e.getMessage());
            log.error("Stack trace:", e);
            throw new RuntimeException("Decryption failed", e);
        }
    }
}