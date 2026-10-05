package com.smarthostel.database;

import com.smarthostel.util.PasswordUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Initializes the SQLite database schema for the Chinnammal Smart Management Platform.
 * Enforces strict foreign keys, dynamic account links, password hashing, and absence of hardcoded demo accounts.
 */
public class DatabaseInitializer {

    private static final Map<String, String> TABLE_SCHEMAS = new LinkedHashMap<>();

    static {
        // 1. blocks table
        TABLE_SCHEMAS.put("blocks", """
            CREATE TABLE IF NOT EXISTS blocks (
                block_id INTEGER PRIMARY KEY AUTOINCREMENT,
                block_name TEXT NOT NULL UNIQUE,
                gender TEXT NOT NULL,
                total_rooms INTEGER NOT NULL DEFAULT 0
            );
        """);

        // 2. wardens table (references blocks)
        TABLE_SCHEMAS.put("wardens", """
            CREATE TABLE IF NOT EXISTS wardens (
                warden_id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                employee_id TEXT UNIQUE,
                department TEXT,
                phone TEXT NOT NULL,
                email TEXT NOT NULL,
                assigned_block INTEGER,
                address TEXT,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (assigned_block) REFERENCES blocks(block_id) ON DELETE SET NULL
            );
        """);

        // 3. rooms table (references blocks)
        TABLE_SCHEMAS.put("rooms", """
            CREATE TABLE IF NOT EXISTS rooms (
                room_id INTEGER PRIMARY KEY AUTOINCREMENT,
                room_number TEXT NOT NULL,
                block_id INTEGER NOT NULL,
                capacity INTEGER NOT NULL DEFAULT 1,
                occupied INTEGER NOT NULL DEFAULT 0,
                status TEXT NOT NULL DEFAULT 'AVAILABLE',
                FOREIGN KEY (block_id) REFERENCES blocks(block_id) ON DELETE CASCADE,
                UNIQUE (block_id, room_number)
            );
        """);

        // 4. students table (references rooms)
        TABLE_SCHEMAS.put("students", """
            CREATE TABLE IF NOT EXISTS students (
                student_id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                department TEXT,
                year INTEGER,
                phone TEXT,
                email TEXT,
                room_id INTEGER,
                bed_number TEXT,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (room_id) REFERENCES rooms(room_id) ON DELETE SET NULL
            );
        """);

        // 5. users table (references students, wardens)
        TABLE_SCHEMAS.put("users", """
            CREATE TABLE IF NOT EXISTS users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT NOT NULL UNIQUE,
                password TEXT,
                password_hash TEXT,
                role TEXT NOT NULL CHECK(role IN ('ADMIN', 'WARDEN', 'STUDENT')),
                linked_student_id INTEGER UNIQUE,
                linked_warden_id INTEGER UNIQUE,
                force_password_change INTEGER NOT NULL DEFAULT 0,
                status TEXT NOT NULL DEFAULT 'ACTIVE' CHECK(status IN ('ACTIVE', 'INACTIVE')),
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (linked_student_id) REFERENCES students(student_id) ON DELETE CASCADE,
                FOREIGN KEY (linked_warden_id) REFERENCES wardens(warden_id) ON DELETE CASCADE
            );
        """);

        // 6. fees table (references students)
        TABLE_SCHEMAS.put("fees", """
            CREATE TABLE IF NOT EXISTS fees (
                fee_id INTEGER PRIMARY KEY AUTOINCREMENT,
                student_id INTEGER NOT NULL,
                total_amount REAL NOT NULL,
                paid_amount REAL NOT NULL DEFAULT 0.0,
                balance REAL NOT NULL,
                due_date TEXT,
                status TEXT NOT NULL DEFAULT 'PENDING',
                FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE
            );
        """);

        // 7. complaints table (references students, rooms)
        TABLE_SCHEMAS.put("complaints", """
            CREATE TABLE IF NOT EXISTS complaints (
                complaint_id INTEGER PRIMARY KEY AUTOINCREMENT,
                student_id INTEGER NOT NULL,
                category TEXT NOT NULL,
                description TEXT NOT NULL,
                room_id INTEGER,
                status TEXT NOT NULL DEFAULT 'OPEN',
                priority TEXT NOT NULL DEFAULT 'MEDIUM',
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                resolved_at TIMESTAMP,
                FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE,
                FOREIGN KEY (room_id) REFERENCES rooms(room_id) ON DELETE SET NULL
            );
        """);

        // 8. visitors table (references students)
        TABLE_SCHEMAS.put("visitors", """
            CREATE TABLE IF NOT EXISTS visitors (
                visitor_id INTEGER PRIMARY KEY AUTOINCREMENT,
                student_id INTEGER NOT NULL,
                visitor_name TEXT NOT NULL,
                relationship TEXT NOT NULL,
                phone TEXT,
                visit_date TEXT NOT NULL,
                visit_time TEXT,
                status TEXT NOT NULL DEFAULT 'CHECKED_IN',
                FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE
            );
        """);

        // 9. leave_requests table (references students, wardens)
        TABLE_SCHEMAS.put("leave_requests", """
            CREATE TABLE IF NOT EXISTS leave_requests (
                leave_id INTEGER PRIMARY KEY AUTOINCREMENT,
                student_id INTEGER NOT NULL,
                from_date TEXT NOT NULL,
                to_date TEXT NOT NULL,
                reason TEXT NOT NULL,
                status TEXT NOT NULL DEFAULT 'PENDING',
                approved_by INTEGER,
                FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE,
                FOREIGN KEY (approved_by) REFERENCES wardens(warden_id) ON DELETE SET NULL
            );
        """);

        // 10. attendance table (references students)
        TABLE_SCHEMAS.put("attendance", """
            CREATE TABLE IF NOT EXISTS attendance (
                attendance_id INTEGER PRIMARY KEY AUTOINCREMENT,
                student_id INTEGER NOT NULL,
                date TEXT NOT NULL,
                entry_time TEXT,
                exit_time TEXT,
                status TEXT NOT NULL,
                FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE
            );
        """);

        // 11. security_logs table (references students)
        TABLE_SCHEMAS.put("security_logs", """
            CREATE TABLE IF NOT EXISTS security_logs (
                log_id INTEGER PRIMARY KEY AUTOINCREMENT,
                student_id INTEGER,
                event_type TEXT NOT NULL,
                event_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                gate TEXT NOT NULL,
                verification_method TEXT NOT NULL,
                FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE SET NULL
            );
        """);
    }

    /**
     * Initializes the database by creating all required tables and constraints.
     */
    public static void initializeDatabase() {
        System.out.println("Initializing database: smart_hostel.db ...");
        try {
            Connection conn = DatabaseConnection.getConnection();

            // Explicitly ensure foreign key constraints are enabled
            try (Statement pragmaStmt = conn.createStatement()) {
                pragmaStmt.execute("PRAGMA foreign_keys = ON;");
            }

            for (Map.Entry<String, String> entry : TABLE_SCHEMAS.entrySet()) {
                String tableName = entry.getKey();
                String ddl = entry.getValue();

                try (PreparedStatement pstmt = conn.prepareStatement(ddl)) {
                    pstmt.executeUpdate();
                    System.out.println("[DB] Table '" + tableName + "' created or verified successfully.");
                }
            }

            // Ensure wardens table columns exist
            ensureColumnExists(conn, "wardens", "employee_id", "TEXT");
            ensureColumnExists(conn, "wardens", "department", "TEXT");
            ensureColumnExists(conn, "wardens", "address", "TEXT");
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("CREATE UNIQUE INDEX IF NOT EXISTS idx_wardens_employee_id ON wardens(employee_id);");
            } catch (SQLException ignored) {}

            // Ensure users table supports all required attributes
            ensureColumnExists(conn, "users", "password", "TEXT");
            ensureColumnExists(conn, "users", "password_hash", "TEXT");
            ensureColumnExists(conn, "users", "linked_student_id", "INTEGER");
            ensureColumnExists(conn, "users", "linked_warden_id", "INTEGER");
            ensureColumnExists(conn, "users", "force_password_change", "INTEGER NOT NULL DEFAULT 0");
            ensureColumnExists(conn, "users", "status", "TEXT NOT NULL DEFAULT 'ACTIVE'");
            ensureColumnExists(conn, "users", "updated_at", "TIMESTAMP");

            // Migrate old data if present from previous schemas
            migrateLegacyUserData(conn);

            // STRICT COMPLIANCE: Completely purge all demo / hardcoded accounts (admin, warden, student)
            purgeDemoAccounts(conn);

            System.out.println("All 11 database tables initialized successfully.");
        } catch (SQLException e) {
            System.err.println("Database initialization failed: " + e.getMessage());
            throw new RuntimeException("Failed to initialize database tables", e);
        }
    }

    private static void migrateLegacyUserData(Connection conn) {
        try (Statement stmt = conn.createStatement()) {
            // Keep password and password_hash in sync
            if (columnExists(conn, "users", "password") && columnExists(conn, "users", "password_hash")) {
                stmt.executeUpdate("UPDATE users SET password_hash = password WHERE (password_hash IS NULL OR password_hash = '') AND password IS NOT NULL;");
                stmt.executeUpdate("UPDATE users SET password = password_hash WHERE (password IS NULL OR password = '') AND password_hash IS NOT NULL;");
            }
            // If legacy 'student_id' column existed and 'linked_student_id' is null
            if (columnExists(conn, "users", "student_id")) {
                stmt.executeUpdate("UPDATE users SET linked_student_id = student_id WHERE linked_student_id IS NULL AND student_id IS NOT NULL;");
            }
            // If legacy 'is_temp_password' column existed
            if (columnExists(conn, "users", "is_temp_password")) {
                stmt.executeUpdate("UPDATE users SET force_password_change = is_temp_password WHERE force_password_change = 0 AND is_temp_password = 1;");
            }

            // Secure any plaintext passwords in existing user records
            try (Statement queryStmt = conn.createStatement();
                 ResultSet rs = queryStmt.executeQuery("SELECT id, password_hash FROM users WHERE password_hash NOT LIKE '%:%';")) {
                List<Integer> idsToHash = new ArrayList<>();
                List<String> plaintexts = new ArrayList<>();
                while (rs.next()) {
                    idsToHash.add(rs.getInt("id"));
                    plaintexts.add(rs.getString("password_hash"));
                }
                for (int i = 0; i < idsToHash.size(); i++) {
                    String hashed = PasswordUtil.hashPassword(plaintexts.get(i));
                    try (PreparedStatement updateStmt = conn.prepareStatement("UPDATE users SET password_hash = ? WHERE id = ?;")) {
                        updateStmt.setString(1, hashed);
                        updateStmt.setInt(2, idsToHash.get(i));
                        updateStmt.executeUpdate();
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[DB] Note on legacy migration: " + e.getMessage());
        }
    }

    private static void purgeDemoAccounts(Connection conn) {
        try (PreparedStatement pstmt = conn.prepareStatement("DELETE FROM users WHERE username IN ('admin', 'warden', 'student');")) {
            int purged = pstmt.executeUpdate();
            if (purged > 0) {
                System.out.println("[DB] Purged " + purged + " hardcoded demo account(s).");
            }
        } catch (SQLException e) {
            System.err.println("[DB] Failed to purge demo accounts: " + e.getMessage());
        }
    }

    private static boolean columnExists(Connection conn, String tableName, String columnName) throws SQLException {
        String pragma = "PRAGMA table_info(" + tableName + ");";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(pragma)) {
            while (rs.next()) {
                if (columnName.equalsIgnoreCase(rs.getString("name"))) {
                    return true;
                }
            }
        }
        return false;
    }

    private static void ensureColumnExists(Connection conn, String tableName, String columnName, String columnDef) {
        try {
            if (!columnExists(conn, tableName, columnName)) {
                String alterSql = "ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " " + columnDef + ";";
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute(alterSql);
                    System.out.println("[DB] Added column '" + columnName + "' to table '" + tableName + "'.");
                }
            }
        } catch (SQLException e) {
            System.err.println("[DB] Note on column check (" + columnName + "): " + e.getMessage());
        }
    }

    public static List<String> getExistingTables() throws SQLException {
        List<String> tables = new ArrayList<>();
        String sql = "SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%' ORDER BY name;";
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                tables.add(rs.getString("name"));
            }
        }
        return tables;
    }

    public static void main(String[] args) {
        initializeDatabase();
    }
}
