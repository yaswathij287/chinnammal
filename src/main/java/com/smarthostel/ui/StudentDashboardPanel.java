package com.smarthostel.ui;

import com.smarthostel.controller.AttendanceController;
import com.smarthostel.controller.ComplaintController;
import com.smarthostel.controller.FeeController;
import com.smarthostel.controller.LeaveController;
import com.smarthostel.dao.StudentDAO;
import com.smarthostel.model.Fee;
import com.smarthostel.model.LeaveRequest;
import com.smarthostel.model.Student;
import com.smarthostel.util.SessionManager;
import com.smarthostel.util.StyleConstants;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;

/**
 * Student Dashboard displaying strictly the logged-in student's personal information:
 * Student Name, ID, Department, Year, Phone, Email, Block, Room, Bed, Fee Status,
 * Complaints, Leave Requests, and Attendance.
 */
public class StudentDashboardPanel extends ScrollPane {

    private final FeeController feeController = new FeeController();
    private final ComplaintController complaintController = new ComplaintController();
    private final AttendanceController attendanceController = new AttendanceController();
    private final LeaveController leaveController = new LeaveController();

    public StudentDashboardPanel() {
        setFitToWidth(true);
        setStyle("-fx-background: " + StyleConstants.COLOR_BG_APP + "; -fx-background-color: transparent;");

        VBox content = new VBox(22);
        content.setPadding(new Insets(24));
        content.setStyle("-fx-background-color: " + StyleConstants.COLOR_BG_APP + ";");

        // Retrieve the current logged-in student from session & database
        Student s = SessionManager.getCurrentStudent();
        if (s != null) {
            try {
                Student fresh = new StudentDAO().getStudentById(s.getStudentId());
                if (fresh != null) {
                    s = fresh;
                }
            } catch (Exception ignored) {}
        }

        String studentName = (s != null) ? s.getName() : "Student";
        int studentId = (s != null) ? s.getStudentId() : 0;
        String dept = (s != null && s.getDepartment() != null) ? s.getDepartment() : "Not Specified";
        int year = (s != null) ? s.getYear() : 1;
        String phone = (s != null && s.getPhone() != null) ? s.getPhone() : "Not Provided";
        String email = (s != null && s.getEmail() != null) ? s.getEmail() : "Not Provided";
        String block = (s != null && s.getBlockName() != null) ? s.getBlockName() : "Unassigned";
        String room = (s != null && s.getRoomNumber() != null) ? s.getRoomNumber() : "Unassigned";
        String bed = (s != null && s.getBedNumber() != null) ? s.getBedNumber() : "Unassigned";

        // Welcome Header
        Label title = new Label("Welcome, " + studentName);
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: " + StyleConstants.COLOR_TEXT_MAIN + ";");

        Label subtitle = new Label(String.format("Student Resident Portal • ID #%d • %s (Year %d)", studentId, dept, year));
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: " + StyleConstants.COLOR_TEXT_MUTED + ";");

        VBox titleBox = new VBox(4, title, subtitle);

        // 1. Personal Resident Profile Summary Card
        VBox profileCard = new VBox(12);
        profileCard.setStyle(StyleConstants.STYLE_CARD);
        profileCard.setPadding(new Insets(18, 20, 18, 20));

        Label profileHeader = new Label("👤 My Resident Profile");
        profileHeader.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: " + StyleConstants.COLOR_PRIMARY_DARK + ";");

        GridPane profileGrid = new GridPane();
        profileGrid.setHgap(28);
        profileGrid.setVgap(10);

        addProfileField(profileGrid, "Student Name:", studentName, 0, 0);
        addProfileField(profileGrid, "Student ID:", "STU-" + String.format("%04d", studentId), 1, 0);
        addProfileField(profileGrid, "Department:", dept, 2, 0);
        addProfileField(profileGrid, "Academic Year:", "Year " + year, 3, 0);

        addProfileField(profileGrid, "Phone Number:", phone, 0, 1);
        addProfileField(profileGrid, "Email Address:", email, 1, 1);
        addProfileField(profileGrid, "Assigned Block:", block, 2, 1);
        addProfileField(profileGrid, "Room & Bed:", room + " (" + bed + ")", 3, 1);

        profileCard.getChildren().addAll(profileHeader, profileGrid);

        // 2. Fetch Student Specific Metrics from SQLite
        String feeStatus = "Paid";
        double feeBalance = 0.0;
        try {
            List<Fee> fees = feeController.getFeesByStudent(studentId);
            for (Fee f : fees) {
                if (f.getBalance() > 0) {
                    feeStatus = "Pending";
                    feeBalance += f.getBalance();
                }
            }
        } catch (Exception ignored) {}

        double attendancePct = 100.0;
        try {
            attendancePct = attendanceController.getStudentAttendancePercentage(studentId);
        } catch (Exception ignored) {}

        int pendingComplaints = 0;
        try {
            var complaints = complaintController.getComplaintsByStudent(studentId);
            for (var c : complaints) {
                if (!"RESOLVED".equalsIgnoreCase(c.getStatus())) pendingComplaints++;
            }
        } catch (Exception ignored) {}

        int activeLeaves = 0;
        try {
            List<LeaveRequest> leaves = leaveController.getLeaveRequestsByStudent(studentId);
            for (LeaveRequest lr : leaves) {
                if ("PENDING".equalsIgnoreCase(lr.getStatus()) || "APPROVED".equalsIgnoreCase(lr.getStatus())) {
                    activeLeaves++;
                }
            }
        } catch (Exception ignored) {}

        // 3. Metric KPI Cards Grid
        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(16);

        String roomBadge = room + " (" + bed + ")";
        grid.add(createCard("🚪", "Allocated Room", roomBadge, StyleConstants.COLOR_PRIMARY), 0, 0);
        grid.add(createCard("💰", "Fee Status", feeStatus + (feeBalance > 0 ? " (₹" + String.format("%,.0f", feeBalance) + ")" : ""),
                "Paid".equalsIgnoreCase(feeStatus) ? StyleConstants.COLOR_SUCCESS : StyleConstants.COLOR_DANGER), 1, 0);
        grid.add(createCard("📈", "My Attendance", String.format("%.1f%%", attendancePct), StyleConstants.COLOR_PURPLE), 2, 0);
        grid.add(createCard("⚠️", "Active Complaints", String.valueOf(pendingComplaints), StyleConstants.COLOR_WARNING), 3, 0);

        grid.add(createCard("✈️", "Leave Requests", String.valueOf(activeLeaves) + " Active", "#0284C7"), 0, 1);
        grid.add(createCard("🏢", "Assigned Block", block, StyleConstants.COLOR_PRIMARY_DARK), 1, 1);
        grid.add(createCard("🛏️", "Bed Allocation", bed, StyleConstants.COLOR_SUCCESS), 2, 1);
        grid.add(createCard("📞", "Contact Registered", phone, StyleConstants.COLOR_TEXT_MAIN), 3, 1);

        // 4. Quick Guidelines Card
        VBox noticeCard = new VBox(10);
        noticeCard.setStyle(StyleConstants.STYLE_CARD);
        noticeCard.setPadding(new Insets(18));

        Label noticeHeader = new Label("📌 Hostel Resident Guidelines");
        noticeHeader.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: " + StyleConstants.COLOR_TEXT_MAIN + ";");

        Label noticeBody = new Label(
                "• Night Curfew: 10:00 PM on weekdays. Late entries are logged at the security gate.\n" +
                "• Visitors must be registered at least 2 hours in advance and approved by the Warden.\n" +
                "• Leave requests must be approved before leaving the campus premises.\n" +
                "• Report any maintenance or plumbing issues immediately in the Complaints section."
        );
        noticeBody.setStyle("-fx-font-size: 13px; -fx-text-fill: " + StyleConstants.COLOR_TEXT_MUTED + "; -fx-line-spacing: 4;");
        noticeCard.getChildren().addAll(noticeHeader, noticeBody);

        content.getChildren().addAll(titleBox, profileCard, grid, noticeCard);
        setContent(content);
    }

    private void addProfileField(GridPane grid, String label, String val, int col, int row) {
        VBox box = new VBox(3);
        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: " + StyleConstants.COLOR_TEXT_MUTED + ";");
        Label valLbl = new Label(val);
        valLbl.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: " + StyleConstants.COLOR_TEXT_MAIN + ";");
        box.getChildren().addAll(lbl, valLbl);
        grid.add(box, col, row);
    }

    private VBox createCard(String icon, String title, String value, String accentColor) {
        VBox card = new VBox(8);
        card.setStyle(StyleConstants.STYLE_CARD + " -fx-border-left-color: " + accentColor + "; -fx-border-left-width: 4;");
        card.setPadding(new Insets(14, 16, 14, 16));
        card.setPrefWidth(210);

        HBox top = new HBox(8);
        top.setAlignment(Pos.CENTER_LEFT);

        Label iconLbl = new Label(icon);
        iconLbl.setStyle("-fx-font-size: 18px;");

        Label titleLbl = new Label(title);
        titleLbl.setStyle("-fx-font-size: 12px; -fx-font-weight: 600; -fx-text-fill: " + StyleConstants.COLOR_TEXT_MUTED + ";");

        top.getChildren().addAll(iconLbl, titleLbl);

        Label valLbl = new Label(value);
        valLbl.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: " + StyleConstants.COLOR_TEXT_MAIN + ";");

        card.getChildren().addAll(top, valLbl);
        return card;
    }
}
