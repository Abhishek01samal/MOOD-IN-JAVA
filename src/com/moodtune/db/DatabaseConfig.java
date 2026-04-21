package com.moodtune.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DatabaseConfig — manages the single MySQL connection for MoodTune.
 *
 * HOW TO CONFIGURE:
 *   1. Make sure MySQL Server is running locally.
 *   2. Run schema.sql to create the database and seed data.
 *   3. Set your MySQL credentials in the constants below.
 */
public class DatabaseConfig {

    // ── Change these to match your MySQL setup ──────────────────────────────
    private static final String HOST     = "localhost";
    private static final String PORT     = "3306";
    private static final String DATABASE = "moodtune";
    public  static final String USER     = "root";
    public  static final String PASSWORD = "your_password_here";   // <── change this
    // ────────────────────────────────────────────────────────────────────────

    private static final String URL =
            "jdbc:mysql://" + HOST + ":" + PORT + "/" + DATABASE +
            "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

    private static Connection connection = null;

    /**
     * Returns a live MySQL connection, or null if the driver / server is unavailable.
     */
    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("[DB] Connected to MySQL ✓");
            }
            return connection;
        } catch (ClassNotFoundException e) {
            System.err.println("[DB] MySQL JDBC driver not found. Add mysql-connector-java.jar to classpath.");
        } catch (SQLException e) {
            System.err.println("[DB] Connection failed: " + e.getMessage());
        }
        return null;
    }

    /** Cleanly closes the connection when the app exits. */
    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("[DB] Connection closed.");
            }
        } catch (SQLException e) {
            System.err.println("[DB] Error closing connection: " + e.getMessage());
        }
    }

    /** Quick reachability check used by RecommendationService. */
    public static boolean isAvailable() {
        Connection c = getConnection();
        return (c != null);
    }
}
