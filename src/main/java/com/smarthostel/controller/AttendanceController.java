package com.smarthostel.controller;

import com.smarthostel.exception.DatabaseException;
import com.smarthostel.exception.ValidationException;
import com.smarthostel.model.Attendance;
import com.smarthostel.service.AttendanceService;

import java.util.List;

/**
 * Controller mediating attendance logging and metrics.
 */
public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController() {
        this.attendanceService = new AttendanceService();
    }

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    public List<Attendance> getAllAttendance() throws DatabaseException {
        return attendanceService.getAllAttendance();
    }

    public List<Attendance> getAttendanceByBlock(int blockId, String date) throws DatabaseException {
        return attendanceService.getAttendanceByBlock(blockId, date);
    }

    public List<Attendance> getAttendanceByStudent(int studentId) throws DatabaseException {
        return attendanceService.getAttendanceByStudent(studentId);
    }

    public Attendance markAttendance(Attendance attendance) throws ValidationException, DatabaseException {
        return attendanceService.markAttendance(attendance);
    }

    public double getStudentAttendancePercentage(int studentId) throws DatabaseException {
        return attendanceService.getStudentAttendancePercentage(studentId);
    }
}
