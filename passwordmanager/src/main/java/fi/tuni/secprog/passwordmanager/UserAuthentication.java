package fi.tuni.secprog.passwordmanager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.mindrot.jbcrypt.BCrypt;

/*
 * A class to handle user authentication and registration.
 */
public class UserAuthentication {

    private static int user_id;

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
     * A function to authenticate a user.
     */
    public static boolean authenticateUser(String username, String password) {
        String sql = "SELECT id, password_hash, salt " +
                     "FROM users " +
                     "WHERE username = ?";

        // Connect to the database and check if the user exists
        try (Connection conn = DatabaseHelper.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // Set the username parameter like this to prevent SQL injection
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            
            // If the query is empty, user doesn't exist, return false
            if (!rs.next()) {
                return false;
            }
            String hashedPassword = rs.getString("password_hash");
            // If the password is correct, set the user_id and save the encryption key
            if (BCrypt.checkpw(password, hashedPassword)) {
                user_id = rs.getInt("id");
                System.out.println("User_id: " + user_id);
                AESKeyHolder.storeKey(EncryptionKeyDerivationUtil.deriveKey(password, rs.getString("salt")));
                return true;
            }
            return false;
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
    public static boolean registerUser(String username, String password) {
        // THIS MIGHT BE WRONG
        String sql = "INSERT INTO users (username, password_hash, salt) VALUES (?, ?, ?)";
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt(12));

        // Connect to the database and insert the new user
        try (Connection conn = DatabaseHelper.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, hashedPassword);
            pstmt.setString(3, EncryptionKeyDerivationUtil.generateSalt());

            // Execute the query and return true if the query was successful
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error during registration: " + e.getMessage());
        }
        return false;
    }
}