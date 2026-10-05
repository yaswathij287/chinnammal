package com.smarthostel.model;

public class Room {
    private int roomId;
    private String roomNumber;
    private int blockId;
    private int capacity;
    private int occupied;
    private String status;

    // Helper display field
    private String blockName;

    public Room() {}

    public Room(int roomId, String roomNumber, int blockId, int capacity, int occupied, String status) {
        this.roomId = roomId;
        this.roomNumber = roomNumber;
        this.blockId = blockId;
        this.capacity = capacity;
        this.occupied = occupied;
        this.status = status;
    }

    public Room(String roomNumber, int blockId, int capacity) {
        this.roomNumber = roomNumber;
        this.blockId = blockId;
        this.capacity = capacity;
        this.occupied = 0;
        this.status = "AVAILABLE";
    }

    public int getRoomId() { return roomId; }
    public void setRoomId(int roomId) { this.roomId = roomId; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public int getBlockId() { return blockId; }
    public void setBlockId(int blockId) { this.blockId = blockId; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public int getOccupied() { return occupied; }
    public void setOccupied(int occupied) { this.occupied = occupied; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getBlockName() { return blockName; }
    public void setBlockName(String blockName) { this.blockName = blockName; }

    public boolean isFull() {
        return occupied >= capacity;
    }

    public int getAvailableBeds() {
        return Math.max(0, capacity - occupied);
    }

    @Override
    public String toString() {
        return roomNumber + (blockName != null ? " (" + blockName + ")" : "");
    }
}
