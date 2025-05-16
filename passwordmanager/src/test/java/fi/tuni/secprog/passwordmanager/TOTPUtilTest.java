package fi.tuni.secprog.passwordmanager;


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.security.NoSuchAlgorithmException;
import com.eatthepath.otp.TimeBasedOneTimePasswordGenerator;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Base32;
import org.junit.Test;

import java.time.Instant;

/*
 * Test class for TOTPUtil class
 */
public class TOTPUtilTest {

    /*
     * Test for generateSecretKey method
     * This test checks that the generated secret key is not null, matches
     * the expected format.
     */
    @Test
    public void testGenerateSecretKey() throws Exception {
        String secret1 = TOTPUtil.generateSecretKey();
        assertNotNull(secret1);
        // Base32 should only contain A-Z, 2-7, and possibly '=' for padding
        assertTrue(secret1.matches("^[A-Z2-7]+=*$"));

    }

    /*
     * Test for generateSecretKey method
     * This test checks that the generated secret keys are unique.
     */
    @Test
    public void testGenerateSecretKey_Unique() throws NoSuchAlgorithmException {
        String secret1 = TOTPUtil.generateSecretKey();
        String secret2 = TOTPUtil.generateSecretKey();
        assertNotEquals(secret1, secret2);
    }

    /*
     * Test for getTOTPAuthURL method
     */
    @Test
    public void testGetTOTPAuthURL_Format() {
        String username1 = "user1";
        String issuer = "TestIssuer";
        String secret = "JBSWY3DPEHPK3PXP";
        String url1 = TOTPUtil.getTOTPAuthURL(username1, issuer, secret);
        assertTrue(url1.startsWith("otpauth://totp/"));
        assertTrue(url1.contains("issuer=" + issuer));
        assertTrue(url1.contains("secret=" + secret));

        String username2 = "user2";
        String url2 = TOTPUtil.getTOTPAuthURL(username2, issuer, secret);
        assertNotEquals(url1, url2);
    }

    /*
     * Test for verifyTOTP method
     * This test checks that the TOTP code is verified correctly.
     */
    @Test
    public void testVerifyTOTP_ValidCode() throws Exception {
        // Generate a secret and use the same algorithm as in TOTPUtil
        String secret = TOTPUtil.generateSecretKey();
        Base32 base32 = new Base32();
        byte[] decodedKey = base32.decode(secret);
        TimeBasedOneTimePasswordGenerator totp = new TimeBasedOneTimePasswordGenerator();
        SecretKeySpec key = new SecretKeySpec(decodedKey, totp.getAlgorithm());
        int code = totp.generateOneTimePassword(key, Instant.now());

        // Should verify successfully
        assertTrue(TOTPUtil.verifyTOTP(String.format("%06d", code), secret));
    }

    /*
     * Test for verifyTOTP method
     * This test checks that the TOTP code is not verified if it is invalid.
     */
    @Test
    public void testVerifyTOTP_InvalidCode() throws Exception {
        String secret = TOTPUtil.generateSecretKey();
        // Use an obviously invalid code
        assertFalse(TOTPUtil.verifyTOTP("000000", secret));
        assertFalse(TOTPUtil.verifyTOTP("abcdef", secret));
    }

    /*
     * Test for verifyTOTP method
     * This test checks that the TOTP code is not verified if the time frame of the code
     * has expired.
     */
    @Test
    public void testVerifyTOTP_invalidTime() throws Exception {
        // Generate a secret and use the same algorithm as in TOTPUtil
        String secret = TOTPUtil.generateSecretKey();
        Base32 base32 = new Base32();
        byte[] decodedKey = base32.decode(secret);
        TimeBasedOneTimePasswordGenerator totp = new TimeBasedOneTimePasswordGenerator();
        SecretKeySpec key = new SecretKeySpec(decodedKey, totp.getAlgorithm());

        Instant shiftedTime = Instant.now().plusSeconds(totp.getTimeStep().getSeconds());
        int shiftedCode = totp.generateOneTimePassword(key, shiftedTime);
        assertFalse(TOTPUtil.verifyTOTP(String.format("%06d", shiftedCode), secret));
    }
}
