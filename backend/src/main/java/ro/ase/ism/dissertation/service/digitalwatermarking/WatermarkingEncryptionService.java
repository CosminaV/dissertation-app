package ro.ase.ism.dissertation.service.digitalwatermarking;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class WatermarkingEncryptionService {

    @Value("${watermarking.enc.secret}")
    private String secret;

    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        byte[] keyBytes = hexStringToByteArray(secret);
        if (keyBytes.length != 32) {
            throw new IllegalArgumentException("Secret key must be 32 bytes for AES-256");
        }
        secretKey = new SecretKeySpec(keyBytes, "AES");
    }

    public String encrypt(String plainText) {
        try {
            // Generate random IV
            byte[] ivBytes = new byte[16];
            SecureRandom secureRandom = new SecureRandom();
            secureRandom.nextBytes(ivBytes);
            IvParameterSpec iv = new IvParameterSpec(ivBytes);

            // Initialize cipher
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv);
            byte[] encrypted = cipher.doFinal(plainText.getBytes());

            // Prepend IV to encrypted data
            byte[] combined = new byte[ivBytes.length + encrypted.length];
            System.arraycopy(ivBytes, 0, combined, 0, ivBytes.length);
            System.arraycopy(encrypted, 0, combined, ivBytes.length, encrypted.length);

            return Base64.getEncoder().encodeToString(combined);

        } catch (NoSuchPaddingException | IllegalBlockSizeException | NoSuchAlgorithmException |
                 InvalidAlgorithmParameterException | BadPaddingException | InvalidKeyException e) {
            throw new RuntimeException("Encryption failed", e);
        }
    }

    public String decrypt(String encryptedText) {
        try {
            byte[] combined = Base64.getDecoder().decode(encryptedText);

            // Extract IV
            byte[] ivBytes = new byte[16];
            System.arraycopy(combined, 0, ivBytes, 0, 16);
            IvParameterSpec iv = new IvParameterSpec(ivBytes);

            // Extract encrypted data
            byte[] encrypted = new byte[combined.length - 16];
            System.arraycopy(combined, 16, encrypted, 0, encrypted.length);

            // Decrypt
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, iv);
            byte[] decrypted = cipher.doFinal(encrypted);

            return new String(decrypted);

        } catch (NoSuchPaddingException | IllegalBlockSizeException | NoSuchAlgorithmException |
                 InvalidAlgorithmParameterException | BadPaddingException | InvalidKeyException e) {
            throw new RuntimeException("Decryption failed", e);
        }
    }

    private byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }
}
