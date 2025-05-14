package fi.tuni.secprog.passwordmanager;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/*
 * A class to handle database connection and initialization.
 */
public class DatabaseHelper {
    private static String DB_URL;
    private static final String realDB_URL = "jdbc:sqlite:password_manager.db";
    private static Connection connection;

    /*
     * A function to connect to the database.
     */
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(DB_URL);
        }
        return connection;
    }

    public static void setRealDB() {
        DB_URL = realDB_URL;
    }

    public static void setTestDB(String testDB) {
        DB_URL = testDB;
    }
    

    /*
     * A function to initialize the user database.
     */
    public static void initializeDatabase() {
        String sql1 = "CREATE TABLE IF NOT EXISTS users (" +
                     "id                INTEGER PRIMARY KEY AUTOINCREMENT," +
                     "username          VARCHAR(255) UNIQUE NOT NULL," +
                     "password_hash     VARCHAR(255) NOT NULL," +
                     "salt              VARCHAR(255) NOT NULL," +
                     "failed_attempts   INTEGER DEFAULT 0," +
                     "last_failed_login TIMESTAMP DEFAULT NULL," +
                     "lockout_until     TIMESTAMP DEFAULT NULL" +
                     ");";

        String sql2 = "CREATE TABLE IF NOT EXISTS credentials (" +
                      "id            INTEGER PRIMARY KEY AUTOINCREMENT," +
                      "user_id       INTEGER NOT NULL," +
                      "site_name     VARCHAR(255) NOT NULL," +
                      "site_username VARCHAR(255) NOT NULL," +
                      "site_password VARCHAR(255) NOT NULL," +
                      "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE" +
                      ");";

        setRealDB();
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON;");
            stmt.execute(sql1);
            stmt.execute(sql2);
        } catch (SQLException e) {
            System.err.println("Error initializing database: " + e.getMessage());
        }
    }


}
