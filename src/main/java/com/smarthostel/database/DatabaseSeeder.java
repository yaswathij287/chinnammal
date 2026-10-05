package com.smarthostel.database;

import com.smarthostel.dao.BlockDAO;
import com.smarthostel.dao.RoomDAO;
import com.smarthostel.exception.DatabaseException;
import com.smarthostel.model.Block;
import com.smarthostel.model.Room;

/**
 * Seeds initial foundational infrastructure (Hostel Blocks and Rooms) ONLY.
 * NEVER seeds default user accounts or login credentials.
 * User accounts (Admin, Warden, Student) are generated dynamically.
 */
public class DatabaseSeeder {

    public static void seedIfNeeded() {
        try {
            BlockDAO blockDAO = new BlockDAO();
            if (blockDAO.count() > 0) {
                return;
            }

            System.out.println("[Seeder] Seeding foundational blocks and rooms for Chinnammal platform...");

            // 1. Seed Blocks
            Block blockA = blockDAO.addBlock(new Block("Block A", "MALE", 20));
            Block blockB = blockDAO.addBlock(new Block("Block B", "FEMALE", 20));

            // 2. Seed Initial Rooms
            RoomDAO roomDAO = new RoomDAO();
            roomDAO.addRoom(new Room(0, "A-101", blockA.getBlockId(), 2, 0, "AVAILABLE"));
            roomDAO.addRoom(new Room(0, "A-102", blockA.getBlockId(), 2, 0, "AVAILABLE"));
            roomDAO.addRoom(new Room(0, "A-201", blockA.getBlockId(), 3, 0, "AVAILABLE"));
            roomDAO.addRoom(new Room(0, "A-202", blockA.getBlockId(), 3, 0, "AVAILABLE"));

            roomDAO.addRoom(new Room(0, "B-101", blockB.getBlockId(), 2, 0, "AVAILABLE"));
            roomDAO.addRoom(new Room(0, "B-102", blockB.getBlockId(), 2, 0, "AVAILABLE"));

            System.out.println("[Seeder] Blocks and rooms seeded successfully. No user credentials seeded.");
        } catch (DatabaseException e) {
            System.err.println("[Seeder] Error during initial infrastructure seeding: " + e.getMessage());
        }
    }
}
