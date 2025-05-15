package fi.tuni.secprog.passwordmanager;

import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/*
 * This class contains methods to manage the credentials of the user.
 * It includes methods to generate a secure password, store, retrieve, update, and delete credentials.
 */
public class ManageCredentials {

    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS = "0123456789";
    private static final String SYMBOLS = "!@#$%^&*()_-+=[]{};:,.?";

    private static final String ALL_CHARS = UPPER + LOWER + DIGITS + SYMBOLS;

    private static final SecureRandom random = new SecureRandom();

    /*
     * A function to generate a cyber secure random password of a given length.
     */
    public static String generatePassword(int length) {
        StringBuilder password = new StringBuilder(length);

        // Ensure at least one char from each category
        password.append(UPPER.charAt(random.nextInt(UPPER.length())));
        password.append(LOWER.charAt(random.nextInt(LOWER.length())));
        password.append(DIGITS.charAt(random.nextInt(DIGITS.length())));
        password.append(SYMBOLS.charAt(random.nextInt(SYMBOLS.length())));

        // Fill the rest with random characters from all categories
        for (int i = 4; i < length; i++) {
            password.append(ALL_CHARS.charAt(random.nextInt(ALL_CHARS.length())));
        }
        return shuffleString(password.toString());
    }

    /*
     * A function to shuffle a string.
     */
    private static String shuffleString(String input) {
        char[] a = input.toCharArray();
        for (int i = a.length - 1; i > 0; i--) {
            int index = random.nextInt(i + 1);
            // Swap
            char temp = a[i];
            a[i] = a[index];
            a[index] = temp;
        }
        return new String(a);
    }

    /*
     * A function to get logged in user's all websites from the database.
     */
    public static List<String> getWebsites() {
        String sql = "SELECT site_name " +
                     "FROM credentials " +
                     "WHERE user_id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, UserAuthentication.getUserId());
            ResultSet rs = pstmt.executeQuery();

            // Return the websites
            ArrayList<String> websites = new ArrayList<>();
            while (rs.next()) {
                websites.add(rs.getString("site_name"));
            }
            return websites;
        } catch (SQLException e) {
            System.err.println("Error in getting the websites: " + e.getMessage());
            return null;
        }
    }

    /*
     * A function to get username and password for a certain website from the database.
     */
    public static List<String> getCredentials(String siteName) {
        String sql = "SELECT site_username, site_password " +
                     "FROM credentials " +
                     "WHERE user_id = ? AND site_name = ?";
        try (Connection conn = DatabaseHelper.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, UserAuthentication.getUserId());
            pstmt.setString(2, siteName);
            ResultSet rs = pstmt.executeQuery();
            
            if (!rs.next()) return null;
            String username = AESUtil.decrypt(rs.getString("site_username"));
            String password = AESUtil.decrypt(rs.getString("site_password"));
            return List.of(username, password);
        } catch (SQLException e) {
            System.err.println("Error in retrieveing the password: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error in decrypting the password: " + e.getMessage());
        }
        return null;
    }

    /*
     * A function to check if the user already has credentials for the site.
     */
    private static boolean doCredentialsExist(String siteName) {
        String sql1 = "SELECT site_name " +
                      "FROM credentials " +
                      "WHERE user_id = ? AND site_name = ?";
        try (Connection conn = DatabaseHelper.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql1)) {

            pstmt.setInt(1, UserAuthentication.getUserId());
            pstmt.setString(2, siteName);

            // Execute the query and return false if user already has credentials for the site
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            System.err.println("Error in checking the credentials: " + e.getMessage());
            return true;
        }
    }

    /*
     * A function to save a new password to the database.
     */
    public static boolean storeKey(String siteName, String username, String password) {
        // Check if the user already has credentials for the site
        if (doCredentialsExist(siteName)) return false;

        String sql2 = "INSERT INTO credentials " +
                      "(user_id, site_name, site_username, site_password) " +
                      "VALUES (?, ?, ?, ?)";
    
        try (Connection conn = DatabaseHelper.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql2)) {
                System.out.println("Stage 1");

            // Encrypt the credentials before storing them
            String encryptedUsername = AESUtil.encrypt(username);
            String encryptedPass = AESUtil.encrypt(password);
                System.out.println("Stage 2");
            pstmt.setInt(1, UserAuthentication.getUserId());
            pstmt.setString(2, siteName);
            pstmt.setString(3, encryptedUsername);
            pstmt.setString(4, encryptedPass);

            // Execute the query and return true if the update was successful
            int affectedRows = pstmt.executeUpdate();
                System.out.println(affectedRows > 0);
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error in storing the key: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error in encrypting the password: " + e.getMessage());
        }
        return false;
    }

    /*
     * A function to update the credentials for a certain website in the database.
     */
    public static boolean updateKey(String siteName, String username, String password) {
        String sql = "UPDATE credentials " +
                     "SET site_username = ?, site_password = ? " +
                     "WHERE user_id = ? AND site_name = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Encrypt the credentials before storing them
            String encryptedUsername = AESUtil.encrypt(username);
            String encryptedPass = AESUtil.encrypt(password);
            pstmt.setString(1, encryptedUsername);
            pstmt.setString(2, encryptedPass);
            pstmt.setInt(3, UserAuthentication.getUserId());
            pstmt.setString(4, siteName);

            // Execute the query and return true if the update was successful
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error in updating the key: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error in encrypting the password: " + e.getMessage());
        }
        return false;
    }

    /*
     * A function to delete the credentials for a certain website from the database.
     */
    public static boolean deleteKey(String siteName) {
        String sql = "DELETE FROM credentials " +
                     "WHERE user_id = ? AND site_name = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, UserAuthentication.getUserId());
            pstmt.setString(2, siteName);

            // Execute the query and return true if the deletion was successful
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error in deleting the key: " + e.getMessage());
        }
        return false;
    }
}
