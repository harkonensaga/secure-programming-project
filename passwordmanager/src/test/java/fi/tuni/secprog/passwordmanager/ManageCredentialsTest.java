package fi.tuni.secprog.passwordmanager;



import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;


/*
 * Test class for ManageCredentials class
 */
public class ManageCredentialsTest {
    private static File tempDbFile;
    private static Connection conn;
    private static final String username = "userX";
    private static final String password = "Password123";

    @BeforeClass
    public static void setupClass() throws Exception {
        tempDbFile = File.createTempFile("testdb", ".sqlite");
        tempDbFile.deleteOnExit();

        DatabaseHelper.setTestDB("jdbc:sqlite:" + tempDbFile.getAbsolutePath());
        conn = DatabaseHelper.getConnection();

        Statement stmt = conn.createStatement();
        stmt.execute("DROP TABLE IF EXISTS users;");
        stmt.execute("DROP TABLE IF EXISTS credentials;");
        stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "id                INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "username          VARCHAR(255) UNIQUE NOT NULL," +
                    "password_hash     VARCHAR(255) NOT NULL," +
                    "salt              VARCHAR(255) NOT NULL," +
                    "failed_attempts   INTEGER DEFAULT 0," +
                    "last_failed_login TIMESTAMP DEFAULT NULL," +
                    "lockout_until     TIMESTAMP DEFAULT NULL" +
                    ");");
        stmt.execute("CREATE TABLE IF NOT EXISTS credentials (" +
                      "id            INTEGER PRIMARY KEY AUTOINCREMENT," +
                      "user_id       INTEGER NOT NULL," +
                      "site_name     VARCHAR(255) NOT NULL," +
                      "site_username VARCHAR(255) NOT NULL," +
                      "site_password VARCHAR(255) NOT NULL," +
                      "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE" +
                      ");");
    }

    @AfterClass
    public static void teardownClass() throws Exception {
        if (conn != null) conn.close();
        if (tempDbFile.exists()) tempDbFile.delete();
    }
    
    /**
     * This method is called before each test method.
     * It registers a user and authenticates them.
     */
    @Before
    public void setUp() {
        assertTrue(UserAuthentication.registerUser(username, password.toCharArray()));
        assertTrue(UserAuthentication.authenticateUser(username, password.toCharArray()));
    }

    /**
     * This method is called after each test method.
     * It deletes the user from the database.
     */
    @After
    public void tearDown() {
        String sql = "DELETE FROM users WHERE username = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.executeUpdate();
            assertFalse(UserAuthentication.userExists(username));
        } catch (SQLException e) {
            System.err.println("Error during SQL query: " + e.getMessage());
        }
    }

    /*
     * Test for strong password generation.
     */
    @Test
    public void testGeneratePass() {
        String generatedPass1 = ManageCredentials.generatePassword(8);
        assertNotNull(generatedPass1);
        assertEquals(generatedPass1.length(), 8);
        assertTrue(generatedPass1.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$"));

        String generatedPass2 = ManageCredentials.generatePassword(10);
        assertNotNull(generatedPass2);
        assertEquals(generatedPass2.length(), 10);
        assertTrue(generatedPass2.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$"));
    }

    /*
     * Test for storing a key correctly in the database.
     */
    @Test
    public void testStoreKey() {
        String siteName = "example.com";
        String username = "websiteUser";
        String password = "StrongPass123";
        assertTrue(ManageCredentials.storeKey(siteName, username, password));
        List<String> credentials = ManageCredentials.getCredentials(siteName);
        assertEquals(credentials.get(0), username);
        assertEquals(credentials.get(1), password);
    }

    /*
     * Test for updating a key correctly in the database.
     */
    @Test
    public void testUpdateKey() {
        String siteName = "example.com";
        String username = "websiteUser";
        String password = "StrongPass123";
        assertTrue(ManageCredentials.storeKey(siteName, username, password));
        String newUsername = "newUser";
        String newPassword = "NewStrongPass123";
        assertTrue(ManageCredentials.updateKey(siteName, newUsername, newPassword));
        List<String> credentials = ManageCredentials.getCredentials(siteName);
        assertEquals(credentials.get(0), newUsername);
        assertEquals(credentials.get(1), newPassword);
    }

    /*
     * Test for deleting a key correctly from the database.
     */
    @Test
    public void testDeleteKey() {
        String siteName = "example.com";
        String username = "websiteUser";
        String password = "StrongPass123";
        assertTrue(ManageCredentials.storeKey(siteName, username, password));
        assertTrue(ManageCredentials.deleteKey(siteName));
        List<String> credentials = ManageCredentials.getCredentials(siteName);
        assertNull(credentials);
    }

    /*
     * Test for getting all websites stored in the database for the user.
     */
    @Test
    public void testGetCWebsites() {
        assertTrue(ManageCredentials.getWebsites().isEmpty());
        String siteName1 = "example1.com";
        String username1 = "websiteUser1";
        String password1 = "StrongPass123";
        assertTrue(ManageCredentials.storeKey(siteName1, username1, password1));
        String siteName2 = "example2.com";
        String username2 = "websiteUser2";
        String password2 = "StrongPass123";
        assertTrue(ManageCredentials.storeKey(siteName2, username2, password2));
        List<String> websites = ManageCredentials.getWebsites();
        assertEquals(websites.size(), 2);
        assertTrue(websites.contains(siteName1));
        assertTrue(websites.contains(siteName2));
    }

    /*
     * Test for handling a non-existent site.
     */
    @Test
    public void testForNonexistentSite() {
        List<String> credentials = ManageCredentials.getCredentials("nonexistent.com");
        assertNull(credentials);
        assertFalse(ManageCredentials.updateKey("nonexistent.com", "user", "pass"));
        assertFalse(ManageCredentials.deleteKey("nonexistent.com"));
    }
}
