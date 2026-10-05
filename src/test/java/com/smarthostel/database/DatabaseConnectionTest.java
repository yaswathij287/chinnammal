package com.smarthostel.database;

import org.junit.jupiter.api.Test;
import java.sql.Connection;
import static org.junit.jupiter.api.Assertions.*;

public class DatabaseConnectionTest {

    @Test
    public void testGetConnection() throws Exception {
        Connection conn = DatabaseConnection.getConnection();
        assertNotNull(conn, "Connection should not be null");
        assertFalse(conn.isClosed(), "Connection should be open");
        DatabaseConnection.closeConnection();
        assertTrue(conn.isClosed(), "Connection should be closed after closeConnection()");
    }
}
