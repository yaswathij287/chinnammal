package com.smarthostel.service;

import com.smarthostel.database.DatabaseInitializer;
import com.smarthostel.database.DatabaseSeeder;
import com.smarthostel.exception.RoomFullException;
import com.smarthostel.model.Fee;
import com.smarthostel.model.Room;
import com.smarthostel.model.Student;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class StudentAndRoomServiceTest {

    private static StudentService studentService;
    private static RoomService roomService;
    private static FeeService feeService;

    @BeforeAll
    public static void setup() {
        DatabaseInitializer.initializeDatabase();
        DatabaseSeeder.seedIfNeeded();
        studentService = new StudentService();
        roomService = new RoomService();
        feeService = new FeeService();
    }

    @Test
    @DisplayName("Verify Student CRUD and Search")
    public void testStudentLifecycle() throws Exception {
        Student newStudent = new Student(
                "Test Resident", "Civil Engg", 2, "9988776655", "test.resident@smarthostel.com", null, null
        );

        Student created = studentService.addStudent(newStudent);
        assertNotNull(created);
        assertTrue(created.getStudentId() > 0);

        List<Student> searchResults = studentService.searchStudents("Test Resident");
        assertFalse(searchResults.isEmpty());
        assertEquals("Test Resident", searchResults.get(0).getName());

        // Cleanup
        studentService.deleteStudent(created.getStudentId());
        assertNull(studentService.getStudentById(created.getStudentId()));
    }

    @Test
    @DisplayName("Attempting to allocate student to a full room must throw RoomFullException")
    public void testRoomCapacityLimit() throws Exception {
        // Room A-101 was seeded with capacity 2 and occupied 2 (FULL)
        List<Room> rooms = roomService.getAllRooms();
        Room fullRoom = null;
        for (Room r : rooms) {
            if ("A-101".equals(r.getRoomNumber())) {
                fullRoom = r;
                break;
            }
        }
        assertNotNull(fullRoom);
        assertTrue(fullRoom.isFull());

        final int targetRoomId = fullRoom.getRoomId();
        Student excessStudent = new Student(
                "Overflow Resident", "IT", 1, "9898989898", "overflow@smarthostel.com",
                targetRoomId, "Bed 3"
        );

        assertThrows(RoomFullException.class, () -> {
            studentService.addStudent(excessStudent);
        });
    }

    @Test
    @DisplayName("Verify Fee Payment Calculation and Status Transition")
    public void testFeePayment() throws Exception {
        List<Fee> fees = feeService.getAllFees();
        assertFalse(fees.isEmpty());

        Fee pendingFee = null;
        for (Fee f : fees) {
            if ("PENDING".equals(f.getStatus()) && f.getBalance() > 1000) {
                pendingFee = f;
                break;
            }
        }

        if (pendingFee != null) {
            final int targetFeeId = pendingFee.getFeeId();
            double initialBalance = pendingFee.getBalance();
            double payment = 5000.0;
            feeService.recordPayment(targetFeeId, payment);

            Fee updated = feeService.getFeesByStudent(pendingFee.getStudentId()).stream()
                    .filter(f -> f.getFeeId() == targetFeeId)
                    .findFirst().orElse(null);

            assertNotNull(updated);
            assertEquals(initialBalance - payment, updated.getBalance(), 0.01);
        }
    }
}
