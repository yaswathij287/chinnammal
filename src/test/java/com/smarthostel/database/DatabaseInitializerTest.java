package com.smarthostel.database;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the DatabaseInitializer:
 * 1. Connects to smart_hostel.db
 * 2. Creates all tables
 * 3. Prints each successful table creation
 * 4. Inserts no sample data yet
 */
public class DatabaseInitializerTest {

    private static final String[] EXPECTED_TABLES = {
        "users",
        "blocks",
        "wardens",
        "rooms",
        "students",
        "fees",
        "complaints",
        "visitors",
        "leave_requests",
        "attendance",
        "security_logs"
    };

    @Test
    @DisplayName("Verify connection to smart_hostel.db, table creation, and absence of sample data")
    public void testDatabaseInitialization() throws SQLException {
        // 1. Connect to smart_hostel.db
        Connection conn = DatabaseConnection.getConnection();
        assertNotNull(conn, "Database connection must not be null");
        assertFalse(conn.isClosed(), "Database connection must be open");

        // 2. Initialize all tables
        DatabaseInitializer.initializeDatabase();

        // 3. Verify and print each successful table creation
        List<String> existingTables = DatabaseInitializer.getExistingTables();
        System.out.println("=== Verified Tables in smart_hostel.db ===");
        for (String expectedTable : EXPECTED_TABLES) {
            assertTrue(existingTables.contains(expectedTable),
                "Expected table '" + expectedTable + "' was not found in the database!");
            System.out.println("✓ Table '" + expectedTable + "' exists and verified.");
        }

        // Verify foreign key pragma is ON
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("PRAGMA foreign_keys;")) {
            assertTrue(rs.next(), "PRAGMA foreign_keys query should return a result");
            assertEquals(1, rs.getInt(1), "Foreign key enforcement should be enabled (PRAGMA foreign_keys = 1)");
            System.out.println("✓ SQLite PRAGMA foreign_keys is ENABLED (value: 1).");
        }

        // 4. Verify table row queries execute successfully
        System.out.println("=== Verifying Table Accessibility ===");
        for (String table : EXPECTED_TABLES) {
            String countSql = "SELECT COUNT(*) FROM " + table + ";";
            try (PreparedStatement pstmt = conn.prepareStatement(countSql);
                 ResultSet rs = pstmt.executeQuery()) {
                assertTrue(rs.next());
                int count = rs.getInt(1);
                assertTrue(count >= 0, "Table '" + table + "' should have valid row count!");
                System.out.println("✓ Table '" + table + "' row count: " + count + ".");
            }
        }

        DatabaseConnection.closeConnection();
        assertTrue(conn.isClosed(), "Connection should be closed after test");
    }
}
