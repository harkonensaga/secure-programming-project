package fi.tuni.secprog.passwordmanager;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;

public class AESUtil {
    private static final int KEY_LENGTH = 256; // AES-256 key
    private static final int ITERATIONS = 100_000;
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";


    /*
     * Generate a random salt for the user's encryption key.
     */
    public static String generateSalt() {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    /*
     * Derive an encryption key from the user's master password and salt.
     */
    public static SecretKeySpec deriveKey(String masterPassword, String salt) throws Exception {
        KeySpec spec = new PBEKeySpec(masterPassword.toCharArray(), salt.getBytes(), ITERATIONS, KEY_LENGTH);
        SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
        byte[] keyBytes = factory.generateSecret(spec).getEncoded();
        return new SecretKeySpec(keyBytes, "AES");
    }

    /*
     * Encrypt data with provided AES key.
     */
    public static String encrypt(String data) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        SecretKeySpec key = AESKeyHolder.getKey();
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encryptedData = cipher.doFinal(data.getBytes());
        return Base64.getEncoder().encodeToString(encryptedData);
    }

    /*
     * Decrypt data with provided AES key.
     */
    public static String decrypt(String encryptedData) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        SecretKeySpec key = AESKeyHolder.getKey();
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decryptedData = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
        return new String(decryptedData);
    }
}
