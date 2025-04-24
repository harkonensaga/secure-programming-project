package fi.tuni.secprog.passwordmanager;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;

public class AESUtil {
    private static final int KEY_LENGTH = 256; // AES-256 key
    private static final int ITERATIONS = 100_000;
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";

    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;

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
    public static SecretKeySpec deriveKey(char[] masterPassword, String salt) throws Exception {
        KeySpec spec = new PBEKeySpec(masterPassword, salt.getBytes(), ITERATIONS, KEY_LENGTH);
        SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
        byte[] keyBytes = factory.generateSecret(spec).getEncoded();
        return new SecretKeySpec(keyBytes, "AES");
    }

    /*
     * Encrypt data with provided AES key.
     */
    public static String encrypt(String data) throws Exception {
        SecretKeySpec key = AESKeyHolder.getKey();
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");

        byte[] iv = new byte[GCM_IV_LENGTH];
        new SecureRandom().nextBytes(iv);
        GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);

        cipher.init(Cipher.ENCRYPT_MODE, key, gcmSpec);
        byte[] encryptedData = cipher.doFinal(data.getBytes());

        // Combine IV + ciphertext
        byte[] ivAndEncrypted = new byte[iv.length + encryptedData.length];
        System.arraycopy(iv, 0, ivAndEncrypted, 0, iv.length);
        System.arraycopy(encryptedData, 0, ivAndEncrypted, iv.length, encryptedData.length);

        return Base64.getEncoder().encodeToString(ivAndEncrypted);
    }

    /*
     * Decrypt data with provided AES key.
     */
    public static String decrypt(String encryptedData) throws Exception {
        SecretKeySpec key = AESKeyHolder.getKey();
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");

        byte[] ivAndEncrypted = Base64.getDecoder().decode(encryptedData);

        byte[] iv = new byte[GCM_IV_LENGTH];
        byte[] ciphertext = new byte[ivAndEncrypted.length - GCM_IV_LENGTH];
        System.arraycopy(ivAndEncrypted, 0, iv, 0, GCM_IV_LENGTH);
        System.arraycopy(ivAndEncrypted, GCM_IV_LENGTH, ciphertext, 0, ciphertext.length);

        GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.DECRYPT_MODE, key, gcmSpec);

        byte[] decryptedData = cipher.doFinal(ciphertext);
        return new String(decryptedData);
    }
}
