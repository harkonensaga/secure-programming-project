package fi.tuni.secprog.passwordmanager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import javax.crypto.spec.SecretKeySpec;

import org.junit.Test;

/**
 * Test class for AES encryption and decryption.
 * This class tests the AESKeyHolder and AESUtil classes.
 */
public class AESTest {
    private static char[] testPassword = "testPassword123".toCharArray();

    /**
     * Test for AESKeyHolder key management.
     */
    @Test
    public void testAESKeyHolder() {
        SecretKeySpec key = new SecretKeySpec("TestKey123".getBytes(), "AES");
        AESKeyHolder.storeKey(key);
        assertEquals(key, AESKeyHolder.getKey());
        AESKeyHolder.clearKey();
        assertEquals(null, AESKeyHolder.getKey());
    }

    /**
     * Test for salt uniqueness.
     */
    @Test
    public void testGenerateSalt() {
        String salt1 = AESUtil.generateSalt();
        String salt2 = AESUtil.generateSalt();
        assertNotEquals(salt1, salt2);
    }

    /**
     * Test for AESUtil key derivation and uniqueness.
     */
    @Test
    public void testDeriveKey() throws Exception {
        String salt = AESUtil.generateSalt();
        SecretKeySpec key1 = AESUtil.deriveKey(testPassword, salt);
        SecretKeySpec key2 = AESUtil.deriveKey(testPassword, salt);

        // Ensure the derived keys are consistent for the same input
        assertEquals(key1, key2);

        // Ensure different salts produce different keys
        String differentSalt = AESUtil.generateSalt();
        SecretKeySpec key3 = AESUtil.deriveKey(testPassword, differentSalt);
        assertNotEquals(key1, key3);
    }

    /**
     * Test for AESUtil encryption and decryption.
     */
    @Test
    public void testAESUtilEncryptionDecryption() throws Exception {
        String data = "SensitiveData";
        SecretKeySpec key = AESUtil.deriveKey("TestKey123".toCharArray(), AESUtil.generateSalt());
        AESKeyHolder.storeKey(key);
        String encryptedData = AESUtil.encrypt(data);
        String decryptedData = AESUtil.decrypt(encryptedData);
        assertEquals(data, decryptedData);
        assertNotEquals(data, encryptedData);
    }
}
