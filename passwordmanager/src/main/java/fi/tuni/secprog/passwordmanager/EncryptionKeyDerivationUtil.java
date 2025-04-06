package fi.tuni.secprog.passwordmanager;

import java.security.SecureRandom;
import java.security.spec.KeySpec;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class EncryptionKeyDerivationUtil {
    private static final int KEY_LENGTH = 256; // AES-256 key
    private static final int ITERATIONS = 100_000; // High iteration count for security
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";

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
     * Generate a random salt for the user's encryption key.
     */
    public static String generateSalt() {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }
}