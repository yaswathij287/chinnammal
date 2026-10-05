package com.chinnammal.controller;

import com.chinnammal.dto.ApiResponse;
import com.smarthostel.dao.BlockDAO;
import com.smarthostel.dao.RoomDAO;
import com.smarthostel.model.Block;
import com.smarthostel.model.Room;
import com.smarthostel.service.RoomService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/common")
public class CommonRestController {

    private final BlockDAO blockDAO = new BlockDAO();
    private final RoomDAO roomDAO = new RoomDAO();
    private final RoomService roomService = new RoomService();

    @GetMapping("/blocks")
    public ResponseEntity<ApiResponse<List<Block>>> getBlocks() {
        try {
            return ResponseEntity.ok(ApiResponse.success(blockDAO.getAllBlocks()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/rooms")
    public ResponseEntity<ApiResponse<List<Room>>> getAllRooms() {
        try {
            return ResponseEntity.ok(ApiResponse.success(roomService.getAllRooms()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/available-rooms")
    public ResponseEntity<ApiResponse<List<Room>>> getAvailableRooms() {
        try {
            List<Room> available = roomService.getAllRooms().stream()
                    .filter(r -> r.getAvailableBeds() > 0)
                    .toList();
            return ResponseEntity.ok(ApiResponse.success(available));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(e.getMessage()));
        }
    }
}
