package fi.tuni.secprog.passwordmanager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;

import org.mindrot.jbcrypt.BCrypt;

/*
 * A class to handle user authentication and registration.
 */
public class UserAuthentication {

    private static int user_id;
    private static final int MAX_ATTEMPTS = 5;
    private static final int TIMEOUT = 5; // in minutes

    /*
     * A function to get the logged in user's id.
     */
    public static int getUserId() {
        return user_id;
    }

    /*
     * A function to clear the logged out user's id.
     */
    public static void clearUserId() {
        user_id = 0;
        return;
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
        String sql = "SELECT id, password_hash, salt, failed_attempts, last_failed_login " +
                     "FROM users " +
                     "WHERE username = ?";

        try (Connection conn = DatabaseHelper.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            
            // If the query is empty, user doesn't exist, return false
            if (!rs.next()) {
                return false;
            }
            String hashedPassword = rs.getString("password_hash");
            Integer attempts = rs.getInt("failed_attempts");
            Timestamp lastFailedLogin = rs.getTimestamp("last_failed_login");

            // If the password is correct, set the user_id and save the encryption key
            if (BCrypt.checkpw(new String(password), hashedPassword)) {
                user_id = rs.getInt("id");
                AESKeyHolder.storeKey(AESUtil.deriveKey(password, rs.getString("salt")));
                resetFailedAttempts(username);
                // Clear the password from memory after use
                java.util.Arrays.fill(password, ' ');
                return true;
            } else {
                // If the password is incorrect, update the failed attempts and check
                // if the account should be locked
                updateFailedAttempts(username);
                LocalDateTime allowedTime = LocalDateTime.now().minusMinutes(TIMEOUT);
                if ((attempts+1) >= MAX_ATTEMPTS && lastFailedLogin.toLocalDateTime().isAfter(allowedTime)) {
                    lockAccount(username);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error during authentication: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error during key derivation: " + e.getMessage());
        }
        return false;
    }

    /*
     * A function to register a new user to the database.
     */
    public static boolean registerUser(String username, char[] password) {
        String sql = "INSERT INTO users (username, password_hash, salt) VALUES (?, ?, ?)";
        
        String hashedPassword = BCrypt.hashpw(new String(password), BCrypt.gensalt(12));
        String salt = AESUtil.generateSalt();
        // Clear the password from memory after use
        java.util.Arrays.fill(password, ' ');
    
        // Connect to the database and insert the new user
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
    
            pstmt.setString(1, username);
            pstmt.setString(2, hashedPassword);
            pstmt.setString(3, salt);
    
            // Execute the query and return true if the query was successful
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error during registration: " + e.getMessage());
        }
        return false;
    }
    
    /*
     * A function to update the failed attempts of a user in the database.
     */
    private static void updateFailedAttempts(String username) {
        String sql = "UPDATE users " +
                     "SET failed_attempts = failed_attempts + 1, last_failed_login = ? " +
                     "WHERE username = ?";
        try (Connection conn = DatabaseHelper.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setString(2, username);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error during SQL query: " + e.getMessage());
        }
    }

    /*
     * A function to reset the failed attempts of a user in the database.
     */
    private static void resetFailedAttempts(String username) {
        String sql = "UPDATE users SET failed_attempts = 0 WHERE username = ?";
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