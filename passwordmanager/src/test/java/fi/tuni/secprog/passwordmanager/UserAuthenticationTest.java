package fi.tuni.secprog.passwordmanager;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/*
 * Test class for UserAuthentication class
 * Test for user registration, login, password strength checking, account lockout,
 */
public class UserAuthenticationTest {
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
                    "totp_secret       VARCHAR(255) NOT NULL," +
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
     * It registers a user with the specified username and password.
     */
    @Before
    public void setUp() {
        assertNotNull(UserAuthentication.registerUser(username, password.toCharArray()));
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

    /**
     * Test for password strength checking.
     * This test checks if the password strength checking function works correctly.
     */
    @Test
    public void testPasswordStrength() {
        String shortPassword = "Pass123";
        String weakPassword = "password";
        String strongPassword = "P@ssw0rd123!";
        String veryStrongPassword = "P@ssw0rd123!@#";
        assertNotNull(UserAuthentication.checkPasswordStrenth(shortPassword));
        assertNotNull(UserAuthentication.checkPasswordStrenth(weakPassword));
        assertNull(UserAuthentication.checkPasswordStrenth(strongPassword));
        assertNull(UserAuthentication.checkPasswordStrenth(veryStrongPassword));
    }

    /**
     * Test for registering a new user with an existing username.
     */
    @Test
    public void registeringWithExistingUsername() throws Exception {
        String anotherPassword = "Password456";
        assertNull(UserAuthentication.registerUser(username, anotherPassword.toCharArray()));
    }

    /**
     * Test for logging in a user. Test also userId management.
     */
    @Test
    public void loginUser() throws Exception {
        assertTrue(UserAuthentication.userExists(username));
        assertFalse(UserAuthentication.isAccountLocked(username));
        assertTrue(UserAuthentication.authenticateUser(username, password.toCharArray()));

        String wrongPassword = "Password456";
        assertFalse(UserAuthentication.authenticateUser(username, wrongPassword.toCharArray()));
    }

    /**
     * Test for account lockout after multiple failed login attempts.
     */
    @Test
    public void testLockAccount() throws Exception {
        assertFalse(UserAuthentication.isAccountLocked(username));

        // Simulate failed login attempts
        for (int i = 0; i < 5; i++) {
            assertFalse(UserAuthentication.authenticateUser(username, "wrongpassword".toCharArray()));
        }
        assertTrue(UserAuthentication.isAccountLocked(username));
    }

    /**
     * Test for SQL injection vulnerability.
     */
    @Test
    public void testSQLInjection() throws Exception {
        String maliciousUsername1 = "' DROP TABLE users; --";
        UserAuthentication.registerUser(maliciousUsername1, password.toCharArray());
        // Check users table still exists
        conn = DatabaseHelper.getConnection();
        Statement stmt = conn.createStatement();
        assertTrue(stmt.execute("SELECT * FROM users;"));

        UserAuthentication.authenticateUser(maliciousUsername1, password.toCharArray());
        // Check users table still exists
        conn = DatabaseHelper.getConnection();
        stmt = conn.createStatement();
        assertTrue(stmt.execute("SELECT * FROM users;"));
        UserAuthentication.logoutUser();

        String maliciousUsername2 = "' OR '1'='1";
        assertFalse(UserAuthentication.authenticateUser(maliciousUsername2, password.toCharArray()));
        // Check if the user was logged in
        assertTrue(UserAuthentication.getUserId() == 0);
    }
}
