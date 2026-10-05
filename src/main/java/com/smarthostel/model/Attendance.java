package com.smarthostel.model;

public class Attendance {
    private int attendanceId;
    private int studentId;
    private String date;
    private String entryTime;
    private String exitTime;
    private String status; // 'PRESENT', 'ABSENT', 'LATE', 'ON_LEAVE'

    // Helper display field
    private String studentName;

    public Attendance() {}

    public Attendance(int attendanceId, int studentId, String date, String entryTime, String exitTime, String status) {
        this.attendanceId = attendanceId;
        this.studentId = studentId;
        this.date = date;
        this.entryTime = entryTime;
        this.exitTime = exitTime;
        this.status = status;
    }

    public Attendance(int studentId, String date, String entryTime, String exitTime, String status) {
        this.studentId = studentId;
        this.date = date;
        this.entryTime = entryTime;
        this.exitTime = exitTime;
        this.status = status;
    }

    public int getAttendanceId() { return attendanceId; }
    public void setAttendanceId(int attendanceId) { this.attendanceId = attendanceId; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getEntryTime() { return entryTime; }
    public void setEntryTime(String entryTime) { this.entryTime = entryTime; }

    public String getExitTime() { return exitTime; }
    public void setExitTime(String exitTime) { this.exitTime = exitTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
}
