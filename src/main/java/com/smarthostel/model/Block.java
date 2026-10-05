package com.smarthostel.model;

public class Block {
    private int blockId;
    private String blockName;
    private String gender;
    private int totalRooms;

    public Block() {}

    public Block(int blockId, String blockName, String gender, int totalRooms) {
        this.blockId = blockId;
        this.blockName = blockName;
        this.gender = gender;
        this.totalRooms = totalRooms;
    }

    public Block(String blockName, String gender, int totalRooms) {
        this.blockName = blockName;
        this.gender = gender;
        this.totalRooms = totalRooms;
    }

    public int getBlockId() { return blockId; }
    public void setBlockId(int blockId) { this.blockId = blockId; }

    public String getBlockName() { return blockName; }
    public void setBlockName(String blockName) { this.blockName = blockName; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public int getTotalRooms() { return totalRooms; }
    public void setTotalRooms(int totalRooms) { this.totalRooms = totalRooms; }

    @Override
    public String toString() {
        return blockName;
    }
}
