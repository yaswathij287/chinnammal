package com.smarthostel.dao;

import com.smarthostel.database.DatabaseConnection;
import com.smarthostel.exception.DatabaseException;
import com.smarthostel.model.Room;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for rooms table.
 */
public class RoomDAO {

    public List<Room> getAllRooms() throws DatabaseException {
        List<Room> list = new ArrayList<>();
        String sql = """
            SELECT r.room_id, r.room_number, r.block_id, r.capacity, r.occupied, r.status, b.block_name
            FROM rooms r
            LEFT JOIN blocks b ON r.block_id = b.block_id
            ORDER BY b.block_name, r.room_number;
        """;
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql);
                 ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToRoom(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to retrieve rooms: " + e.getMessage(), e);
        }
        return list;
    }

    public List<Room> getRoomsByBlock(int blockId) throws DatabaseException {
        List<Room> list = new ArrayList<>();
        String sql = """
            SELECT r.room_id, r.room_number, r.block_id, r.capacity, r.occupied, r.status, b.block_name
            FROM rooms r
            LEFT JOIN blocks b ON r.block_id = b.block_id
            WHERE r.block_id = ?
            ORDER BY r.room_number;
        """;
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, blockId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        list.add(mapResultSetToRoom(rs));
                    }
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to retrieve rooms for block: " + e.getMessage(), e);
        }
        return list;
    }

    public Room getRoomById(int roomId) throws DatabaseException {
        String sql = """
            SELECT r.room_id, r.room_number, r.block_id, r.capacity, r.occupied, r.status, b.block_name
            FROM rooms r
            LEFT JOIN blocks b ON r.block_id = b.block_id
            WHERE r.room_id = ?;
        """;
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, roomId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return mapResultSetToRoom(rs);
                    }
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to retrieve room by id: " + e.getMessage(), e);
        }
        return null;
    }

    public Room getRoomByBlockAndNumber(int blockId, String roomNumber) throws DatabaseException {
        String sql = """
            SELECT r.room_id, r.room_number, r.block_id, r.capacity, r.occupied, r.status, b.block_name
            FROM rooms r
            LEFT JOIN blocks b ON r.block_id = b.block_id
            WHERE r.block_id = ? AND r.room_number = ?;
        """;
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, blockId);
                pstmt.setString(2, roomNumber);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return mapResultSetToRoom(rs);
                    }
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find room by block and number: " + e.getMessage(), e);
        }
        return null;
    }

    public Room addRoom(Room room) throws DatabaseException {
        String sql = "INSERT INTO rooms (room_number, block_id, capacity, occupied, status) VALUES (?, ?, ?, ?, ?);";
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, room.getRoomNumber());
                pstmt.setInt(2, room.getBlockId());
                pstmt.setInt(3, room.getCapacity());
                pstmt.setInt(4, room.getOccupied());
                pstmt.setString(5, room.getStatus() != null ? room.getStatus() : "AVAILABLE");
                pstmt.executeUpdate();

                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        room.setRoomId(generatedKeys.getInt(1));
                    }
                }
                return room;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to add room: " + e.getMessage(), e);
        }
    }

    public void updateRoom(Room room) throws DatabaseException {
        String sql = "UPDATE rooms SET room_number = ?, block_id = ?, capacity = ?, occupied = ?, status = ? WHERE room_id = ?;";
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, room.getRoomNumber());
                pstmt.setInt(2, room.getBlockId());
                pstmt.setInt(3, room.getCapacity());
                pstmt.setInt(4, room.getOccupied());
                pstmt.setString(5, room.getStatus());
                pstmt.setInt(6, room.getRoomId());
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update room: " + e.getMessage(), e);
        }
    }

    public void deleteRoom(int roomId) throws DatabaseException {
        String sql = "DELETE FROM rooms WHERE room_id = ?;";
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, roomId);
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to delete room: " + e.getMessage(), e);
        }
    }

    public void updateOccupancy(int roomId, int delta) throws DatabaseException {
        try {
            Connection conn = DatabaseConnection.getConnection();
            updateOccupancy(conn, roomId, delta);
        } catch (SQLException e) {
            throw new DatabaseException("Failed to get database connection: " + e.getMessage(), e);
        }
    }

    public void updateOccupancy(Connection conn, int roomId, int delta) throws DatabaseException {
        String sql = """
            UPDATE rooms
            SET occupied = MAX(0, MIN(capacity, occupied + ?)),
                status = CASE
                    WHEN (occupied + ?) >= capacity THEN 'FULL'
                    ELSE 'AVAILABLE'
                END
            WHERE room_id = ?;
        """;
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, delta);
            pstmt.setInt(2, delta);
            pstmt.setInt(3, roomId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update room occupancy: " + e.getMessage(), e);
        }
    }

    public int countTotalRooms() throws DatabaseException {
        String sql = "SELECT COUNT(*) FROM rooms;";
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql);
                 ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to count rooms: " + e.getMessage(), e);
        }
        return 0;
    }

    public int countTotalBeds() throws DatabaseException {
        String sql = "SELECT COALESCE(SUM(capacity), 0) FROM rooms;";
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql);
                 ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to count total beds: " + e.getMessage(), e);
        }
        return 0;
    }

    public int countOccupiedBeds() throws DatabaseException {
        String sql = "SELECT COALESCE(SUM(occupied), 0) FROM rooms;";
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql);
                 ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to count occupied beds: " + e.getMessage(), e);
        }
        return 0;
    }

    private Room mapResultSetToRoom(ResultSet rs) throws SQLException {
        Room room = new Room(
                rs.getInt("room_id"),
                rs.getString("room_number"),
                rs.getInt("block_id"),
                rs.getInt("capacity"),
                rs.getInt("occupied"),
                rs.getString("status")
        );
        room.setBlockName(rs.getString("block_name"));
        return room;
    }
}
