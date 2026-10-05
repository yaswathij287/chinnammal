package com.smarthostel.service;

import com.smarthostel.dao.BlockDAO;
import com.smarthostel.dao.RoomDAO;
import com.smarthostel.exception.DatabaseException;
import com.smarthostel.exception.ValidationException;
import com.smarthostel.model.Block;
import com.smarthostel.model.Room;
import com.smarthostel.util.Validator;

import java.util.List;

/**
 * Service managing rooms, blocks, and occupancy limits.
 */
public class RoomService {

    private final RoomDAO roomDAO;
    private final BlockDAO blockDAO;

    public RoomService() {
        this.roomDAO = new RoomDAO();
        this.blockDAO = new BlockDAO();
    }

    public RoomService(RoomDAO roomDAO, BlockDAO blockDAO) {
        this.roomDAO = roomDAO;
        this.blockDAO = blockDAO;
    }

    public Room addRoom(Room room) throws ValidationException, DatabaseException {
        Validator.requireNonEmpty(room.getRoomNumber(), "Room Number");
        Validator.validatePositiveInt(room.getCapacity(), "Room Capacity");

        Room existing = roomDAO.getRoomByBlockAndNumber(room.getBlockId(), room.getRoomNumber());
        if (existing != null) {
            throw new ValidationException("Room " + room.getRoomNumber() + " already exists in this block.");
        }

        return roomDAO.addRoom(room);
    }

    public void updateRoom(Room room) throws ValidationException, DatabaseException {
        Validator.requireNonEmpty(room.getRoomNumber(), "Room Number");
        Validator.validatePositiveInt(room.getCapacity(), "Room Capacity");

        Room existing = roomDAO.getRoomById(room.getRoomId());
        if (existing != null && room.getCapacity() < existing.getOccupied()) {
            throw new ValidationException("Capacity cannot be reduced below currently occupied beds (" + existing.getOccupied() + ").");
        }

        roomDAO.updateRoom(room);
    }

    public void deleteRoom(int roomId) throws ValidationException, DatabaseException {
        Room existing = roomDAO.getRoomById(roomId);
        if (existing != null && existing.getOccupied() > 0) {
            throw new ValidationException("Cannot delete room with " + existing.getOccupied() + " currently assigned student(s). Vacate the room first.");
        }
        roomDAO.deleteRoom(roomId);
    }

    public List<Room> getAllRooms() throws DatabaseException {
        return roomDAO.getAllRooms();
    }

    public List<Room> getRoomsByBlock(int blockId) throws DatabaseException {
        return roomDAO.getRoomsByBlock(blockId);
    }

    public Room getRoomById(int roomId) throws DatabaseException {
        return roomDAO.getRoomById(roomId);
    }

    public List<Block> getAllBlocks() throws DatabaseException {
        return blockDAO.getAllBlocks();
    }

    public Block addBlock(Block block) throws ValidationException, DatabaseException {
        Validator.requireNonEmpty(block.getBlockName(), "Block Name");
        Validator.requireNonEmpty(block.getGender(), "Block Gender");
        return blockDAO.addBlock(block);
    }
}
