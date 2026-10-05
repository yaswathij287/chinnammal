package com.smarthostel.service;

import com.smarthostel.dao.AttendanceDAO;
import com.smarthostel.exception.DatabaseException;
import com.smarthostel.exception.ValidationException;
import com.smarthostel.model.Attendance;
import com.smarthostel.util.Validator;

import java.time.LocalDate;
import java.util.List;

/**
 * Service managing attendance tracking, curfew adherence, and percentage metrics.
 */
public class AttendanceService {

    private final AttendanceDAO attendanceDAO;

    public AttendanceService() {
        this.attendanceDAO = new AttendanceDAO();
    }

    public AttendanceService(AttendanceDAO attendanceDAO) {
        this.attendanceDAO = attendanceDAO;
    }

    public Attendance markAttendance(Attendance attendance) throws ValidationException, DatabaseException {
        Validator.requireNonEmpty(attendance.getDate(), "Attendance Date");
        Validator.requireNonEmpty(attendance.getStatus(), "Attendance Status");
        return attendanceDAO.markAttendance(attendance);
    }

    public List<Attendance> getAllAttendance() throws DatabaseException {
        return attendanceDAO.getAllAttendance();
    }

    public List<Attendance> getAttendanceByBlock(int blockId, String date) throws DatabaseException {
        return attendanceDAO.getAttendanceByBlock(blockId, date);
    }

    public List<Attendance> getAttendanceByStudent(int studentId) throws DatabaseException {
        return attendanceDAO.getAttendanceByStudent(studentId);
    }

    public double getStudentAttendancePercentage(int studentId) throws DatabaseException {
        return attendanceDAO.getStudentAttendancePercentage(studentId);
    }

    public int getTodayPresentCount() throws DatabaseException {
        return attendanceDAO.countTodayPresent(LocalDate.now().toString());
    }

    public int getTodayLateCount() throws DatabaseException {
        return attendanceDAO.countTodayLate(LocalDate.now().toString());
    }
}
