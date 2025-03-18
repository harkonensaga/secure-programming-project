package fi.tuni.secprog.passwordmanager;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseHelper {
    private static final String DB_URL = "jdbc:sqlite:password_manager.db";

    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    public void initializeDatabase() {
        String sql = "CREATE TABLE IF NOT EXISTS users (" +
                     "id            INTEGER PRIMARY KEY AUTOINCREMENT, " +
                     "username      TEXT UNIQUE NOT NULL, " +
                     "password_hash TEXT NOT NULL" +
                     ");";

        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("Database initialized.");
        } catch (SQLException e) {
            System.err.println("Error initializing database: " + e.getMessage());
        }
    }
}
