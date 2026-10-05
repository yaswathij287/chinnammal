package com.smarthostel.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Manages the SQLite database connection for smart_hostel.db.
 */
public class DatabaseConnection {

    private static final String DB_NAME = "smart_hostel.db";
    private static final String URL = "jdbc:sqlite:" + DB_NAME;

    private static Connection connection = null;

    private DatabaseConnection() {
        // Prevent direct instantiation
    }

    /**
     * Retrieves an active connection to the SQLite database.
     *
     * @return Connection object to smart_hostel.db
     * @throws SQLException if a database access error occurs
     */
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(URL);
            try (java.sql.Statement stmt = connection.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON;");
            }
        }
        return connection;
    }

    /**
     * Closes the active database connection if open.
     */
    public static void closeConnection() {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException e) {
                System.err.println("Error closing database connection: " + e.getMessage());
            } finally {
                connection = null;
            }
        }
    }
}
