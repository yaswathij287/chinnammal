package com.smarthostel.dao;

import com.smarthostel.database.DatabaseConnection;
import com.smarthostel.exception.DatabaseException;
import com.smarthostel.model.Block;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for blocks table.
 */
public class BlockDAO {

    public List<Block> getAllBlocks() throws DatabaseException {
        List<Block> list = new ArrayList<>();
        String sql = "SELECT block_id, block_name, gender, total_rooms FROM blocks ORDER BY block_name;";
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql);
                 ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToBlock(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to retrieve blocks: " + e.getMessage(), e);
        }
        return list;
    }

    public Block getBlockById(int blockId) throws DatabaseException {
        String sql = "SELECT block_id, block_name, gender, total_rooms FROM blocks WHERE block_id = ?;";
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, blockId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return mapResultSetToBlock(rs);
                    }
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to retrieve block: " + e.getMessage(), e);
        }
        return null;
    }

    public Block addBlock(Block block) throws DatabaseException {
        String sql = "INSERT INTO blocks (block_name, gender, total_rooms) VALUES (?, ?, ?);";
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, block.getBlockName());
                pstmt.setString(2, block.getGender());
                pstmt.setInt(3, block.getTotalRooms());
                pstmt.executeUpdate();

                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        block.setBlockId(generatedKeys.getInt(1));
                    }
                }
                return block;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to add block: " + e.getMessage(), e);
        }
    }

    public void updateBlock(Block block) throws DatabaseException {
        String sql = "UPDATE blocks SET block_name = ?, gender = ?, total_rooms = ? WHERE block_id = ?;";
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, block.getBlockName());
                pstmt.setString(2, block.getGender());
                pstmt.setInt(3, block.getTotalRooms());
                pstmt.setInt(4, block.getBlockId());
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update block: " + e.getMessage(), e);
        }
    }

    public void deleteBlock(int blockId) throws DatabaseException {
        String sql = "DELETE FROM blocks WHERE block_id = ?;";
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, blockId);
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to delete block: " + e.getMessage(), e);
        }
    }

    public int count() throws DatabaseException {
        String sql = "SELECT COUNT(*) FROM blocks;";
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql);
                 ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to count blocks: " + e.getMessage(), e);
        }
        return 0;
    }

    private Block mapResultSetToBlock(ResultSet rs) throws SQLException {
        return new Block(
                rs.getInt("block_id"),
                rs.getString("block_name"),
                rs.getString("gender"),
                rs.getInt("total_rooms")
        );
    }
}
