package com.smarthostel.controller;

import com.smarthostel.exception.DatabaseException;
import com.smarthostel.exception.ValidationException;
import com.smarthostel.model.Block;
import com.smarthostel.model.Room;
import com.smarthostel.service.RoomService;

import java.util.List;

/**
 * Controller mediating rooms and blocks operations.
 */
public class RoomController {

    private final RoomService roomService;

    public RoomController() {
        this.roomService = new RoomService();
    }

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    public List<Room> getAllRooms() throws DatabaseException {
        return roomService.getAllRooms();
    }

    public List<Room> getRoomsByBlock(int blockId) throws DatabaseException {
        return roomService.getRoomsByBlock(blockId);
    }

    public Room getRoomById(Integer roomId) throws DatabaseException {
        if (roomId == null) return null;
        return roomService.getRoomById(roomId);
    }

    public Room addRoom(Room room) throws ValidationException, DatabaseException {
        return roomService.addRoom(room);
    }

    public void updateRoom(Room room) throws ValidationException, DatabaseException {
        roomService.updateRoom(room);
    }

    public void deleteRoom(int roomId) throws ValidationException, DatabaseException {
        roomService.deleteRoom(roomId);
    }

    public List<Block> getAllBlocks() throws DatabaseException {
        return roomService.getAllBlocks();
    }

    public Block addBlock(Block block) throws ValidationException, DatabaseException {
        return roomService.addBlock(block);
    }
}
