package fi.tuni.secprog.passwordmanager;

import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;

import org.mindrot.jbcrypt.BCrypt;

import javafx.scene.image.Image;

/*
 * A class to handle user authentication and registration.
 */
public class UserAuthentication {

    private static int user_id = 0;
    private static final int MAX_ATTEMPTS = 5;
    private static final int TIMEOUT = 5; // in minutes

    /*
     * A function to get the logged in user's id.
     */
    public static int getUserId() {
        return user_id;
    }

    public static void setUserId(int id) {
        user_id = id;
    }

    /*
     * A function to check the password strength.
     * Returns null if the password is strong enough, otherwise returns
     * a string with the error message.
     */
    public static String checkPasswordStrenth(String password) {
        if (password.length() < 8) {
            return "Password must be at least 8 characters long.";
        } else if (!password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$")) {
            return ("Password must include both lower and uppercase letters" +
                    "and at least one number and special character.");
        }
        return null;
    }

    /*
     * A function to check is such user exists in the database.
     */
    public static boolean userExists(String username) {
        String sql = "SELECT id FROM users WHERE username = ?";
        // Connect to the database and check if the user exists
        try (Connection conn = DatabaseHelper.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            System.err.println("Error during SQL query: " + e.getMessage());
        }
        return false;
    }

    /*
     * A function to check if the account is locked.
     */
    public static boolean isAccountLocked(String username) {
        String sql = "SELECT lockout_until FROM users WHERE username = ?";
        // Connect to the database and check if the account is locked
        try (Connection conn = DatabaseHelper.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Timestamp lockout = rs.getTimestamp("lockout_until");
                if (lockout != null && lockout.toInstant().isAfter(Instant.now())) {
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error during SQL query: " + e.getMessage());
        }
        return false;
    }

    /*
     * A function to authenticate a user.
     */
    public static boolean authenticateUser(String username, char[] password) {
        if (!userExists(username)) return false;

        String sql = "SELECT password_hash, failed_attempts, last_failed_login " +
                     "FROM users " +
                     "WHERE username = ?";

        try (Connection conn = DatabaseHelper.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            
            String hashedPassword = rs.getString("password_hash");
            Integer attempts = rs.getInt("failed_attempts");
            Timestamp lastFailedLogin = rs.getTimestamp("last_failed_login");

            // If the password is incorrect, update the failed attempts
            if (!BCrypt.checkpw(new String(password), hashedPassword)) {
                updateFailedAttempts(username, attempts, lastFailedLogin);
                return false;
            }
            // Clear the password from memory after use
            java.util.Arrays.fill(password, ' ');
            return true;
        } catch (SQLException e) {
            System.err.println("Error during authentication: " + e.getMessage());
        }
        return false;
    }

    /*
     * A function to verify the TOTP code.
     */
    public static boolean verifyTOTP(String username, char[] password, String userInput) {
        String sql = "SELECT id, totp_secret, salt, failed_attempts, last_failed_login " +
                     "FROM users " +
                     "WHERE username = ?";

        try (Connection conn = DatabaseHelper.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            
            AESKeyHolder.storeKey(AESUtil.deriveKey(password, rs.getString("salt")));
            String TOTPSecret = AESUtil.decrypt(rs.getString("totp_secret"));
            Integer attempts = rs.getInt("failed_attempts");
            Timestamp lastFailedLogin = rs.getTimestamp("last_failed_login");

            if (!TOTPUtil.verifyTOTP(userInput, TOTPSecret)) {
                updateFailedAttempts(username, attempts, lastFailedLogin);
                AESKeyHolder.clearKey();
                return false;
            }
            setUserId(rs.getInt("id"));
            resetFailedAttempts(username);
            return true;
        } catch (Exception e) {
            System.err.println("Error during TOTP verification: " + e.getMessage());
        }
        AESKeyHolder.clearKey();
        return false;
    }

    /*
     * A function log out the user.
     */
    public static void logoutUser() {
        // Clear the AES key and user id
        AESKeyHolder.clearKey();
        setUserId(0);
    }

    /*
     * A function to register a new user to the database.
     */
    public static Image registerUser(String username, char[] password) {
        if (userExists(username)) return null;

        String sql = "INSERT INTO users (username, password_hash, totp_secret, salt) VALUES (?, ?, ?, ?)";
        
        // Connect to the database and insert the new user
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            String hashedPassword = BCrypt.hashpw(new String(password), BCrypt.gensalt(12));
            String salt = AESUtil.generateSalt();
            AESKeyHolder.storeKey(AESUtil.deriveKey(password, salt));
            String TOTPSecret = TOTPUtil.generateSecretKey();
            String encryptedTOTP = AESUtil.encrypt(TOTPSecret);
            AESKeyHolder.clearKey();
            
            // Clear the password from memory after use
            java.util.Arrays.fill(password, ' ');
    
            pstmt.setString(1, username);
            pstmt.setString(2, hashedPassword);
            pstmt.setString(3, encryptedTOTP);
            pstmt.setString(4, salt);
    
            // Execute the query and return true if the query was successful
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                // Generate the TQR code for the TOTP secret key
                String URL = TOTPUtil.getTOTPAuthURL(username, "PasswordManager", TOTPSecret);
                return TOTPUtil.generateQRCode(URL);
            }
        } catch (SQLException e) {
            System.err.println("Error during registration: " + e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            System.err.println("Error generating TOTP secret: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error in encrypting the TOTP secret: " + e.getMessage());
        }
        return null;
    }
    
    /*
     * A function to update the failed attempts of a user in the database.
     */
    private static void updateFailedAttempts(String username, int attempts, Timestamp lastFailedLogin) {
        String sql = "UPDATE users " +
                     "SET failed_attempts = failed_attempts + 1, last_failed_login = ? " +
                     "WHERE username = ?";

        // If the user has failed 5 times, and the last failed login is within 5 minutes,
        // lock the account for 5 minutes
        LocalDateTime allowedTime = LocalDateTime.now().minusMinutes(TIMEOUT);
        if ((attempts+1) >= MAX_ATTEMPTS && lastFailedLogin.toLocalDateTime().isAfter(allowedTime)) {
            lockAccount(username);
        } else {
            // If the user has failed less than 5 times,
            // update the failed attempts and last failed login
            try (Connection conn = DatabaseHelper.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
                pstmt.setString(2, username);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                System.err.println("Error during SQL query: " + e.getMessage());
            }
        }
    }

    /*
     * A function to reset the failed attempts of a user in the database.
     */
    private static void resetFailedAttempts(String username) {
        String sql = "UPDATE users " +
                     "SET failed_attempts = 0, last_failed_login = NULL, lockout_until = NULL " +
                     "WHERE username = ?";
        try (Connection conn = DatabaseHelper.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error during SQL query: " + e.getMessage());
        }
    }

    /*
     * A function to lock the account for 5 minutes after 5 failed attempts.
     */
    private static void lockAccount(String username) {
        LocalDateTime lockUntil = LocalDateTime.now().plusMinutes(5);
        String sql = "UPDATE users SET lockout_until = ? WHERE username = ?";
        try (Connection conn = DatabaseHelper.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setTimestamp(1, Timestamp.valueOf(lockUntil));
            pstmt.setString(2, username);
            pstmt.executeUpdate();
        }  catch (SQLException e) {
            System.err.println("Error during SQL query: " + e.getMessage());
        }
    }
}