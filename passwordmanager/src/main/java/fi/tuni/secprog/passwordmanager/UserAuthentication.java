package fi.tuni.secprog.passwordmanager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.mindrot.jbcrypt.BCrypt;

public class UserAuthentication {

    public UserAuthentication() {
    }

    public boolean authenticateUser(String username, String password) {
        String sql = "SELECT password_hash FROM users WHERE username = ?";

        // Connect to the database and check if the user exists
        try (Connection conn = DatabaseHelper.connect();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // Set the username parameter like this to prevent SQL injection
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            
            // If the query is empty, user doesn't exist, return false
            if (!rs.next()) {
                return false;
            }
            String hashedPassword = rs.getString("password_hash");
            return BCrypt.checkpw(password, hashedPassword);
        } catch (SQLException e) {
            System.err.println("Error during authentication: " + e.getMessage());
        }
        return false;
    }

    public boolean registerUser(String username, String password) {
        String sql = "INSERT INTO users (username, password_hash) VALUES (?, ?)";
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt(12));

        // Connect to the database and insert the new user
        try (Connection conn = DatabaseHelper.connect();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, hashedPassword);

            // Execute the query and return true if the query was successful
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error during registration: " + e.getMessage());
        }
        return false;
    }
}