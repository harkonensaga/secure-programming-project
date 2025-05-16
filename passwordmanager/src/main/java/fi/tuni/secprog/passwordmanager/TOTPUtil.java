package fi.tuni.secprog.passwordmanager;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base32;

import com.eatthepath.otp.TimeBasedOneTimePasswordGenerator;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.awt.image.BufferedImage;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

/*
 * A utility class to handle TOTP generation and verification.
 */
public class TOTPUtil {
    
    /*
     * A function to generate a random secret key for TOTP.
     * The key is generated using the TimeBasedOneTimePasswordGenerator algorithm.
     */
    public static String generateSecretKey() throws NoSuchAlgorithmException {
        TimeBasedOneTimePasswordGenerator totp = new TimeBasedOneTimePasswordGenerator();
        final KeyGenerator keyGenerator = KeyGenerator.getInstance(totp.getAlgorithm());
        final int macLengthInBytes = Mac.getInstance(totp.getAlgorithm()).getMacLength();
        keyGenerator.init(macLengthInBytes * 8);
        SecretKey secretKey = keyGenerator.generateKey();

        // Encode the secret key in Base32 format
        Base32 base32 = new Base32();
        String base32EncodedKey = base32.encodeToString(secretKey.getEncoded());
        return base32EncodedKey;
    }

    /*
     * A function to generate a TOTP code using the secret key.
     */
    public static String getTOTPAuthURL(String username, String issuer, String base32Secret) {
        return "otpauth://totp/" + issuer + ":" + username +
                "?secret=" + base32Secret + "&issuer=" + issuer;
    }

    /*
     * A function to generate a QR code for the TOTP secret key.
     */
    public static Image generateQRCode(String url) throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(url, BarcodeFormat.QR_CODE, 250, 250);
        BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
        return SwingFXUtils.toFXImage(bufferedImage, null);
    }

    /*
     * A function to verify the TOTP code entered by the user.
     * The function checks if the code is valid for the current time.
     */
    public static boolean verifyTOTP(String userInput, String base32Secret) throws Exception {
        TimeBasedOneTimePasswordGenerator totp = new TimeBasedOneTimePasswordGenerator();

        // Decode the base32 secret key
        Base32 base32 = new Base32();
        Instant now = Instant.now();
        byte[] decodedKey = base32.decode(base32Secret);
        SecretKeySpec key = new SecretKeySpec(decodedKey, totp.getAlgorithm());

        try {
            int code = Integer.parseInt(userInput);
            if (totp.generateOneTimePassword(key, now) == code) return true;
        } catch (NumberFormatException e) {
            System.err.println("Invalid TOTP code format: " + e.getMessage());
        }
        return false;
    }
}
