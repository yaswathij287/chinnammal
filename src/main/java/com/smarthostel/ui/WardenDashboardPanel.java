package com.smarthostel.ui;

import com.smarthostel.controller.AttendanceController;
import com.smarthostel.controller.ComplaintController;
import com.smarthostel.controller.LeaveController;
import com.smarthostel.controller.RoomController;
import com.smarthostel.controller.StudentController;
import com.smarthostel.controller.VisitorController;
import com.smarthostel.model.Room;
import com.smarthostel.model.Student;
import com.smarthostel.model.Warden;
import com.smarthostel.util.SessionManager;
import com.smarthostel.util.StyleConstants;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.util.List;

/**
 * Warden Dashboard displaying metrics strictly scoped to the warden's assigned block.
 */
public class WardenDashboardPanel extends ScrollPane {

    private final StudentController studentController = new StudentController();
    private final RoomController roomController = new RoomController();
    private final ComplaintController complaintController = new ComplaintController();
    private final VisitorController visitorController = new VisitorController();
    private final LeaveController leaveController = new LeaveController();
    private final AttendanceController attendanceController = new AttendanceController();

    public WardenDashboardPanel() {
        setFitToWidth(true);
        setStyle("-fx-background: " + StyleConstants.COLOR_BG_APP + "; -fx-background-color: transparent;");

        VBox content = new VBox(24);
        content.setPadding(new Insets(24));
        content.setStyle("-fx-background-color: " + StyleConstants.COLOR_BG_APP + ";");

        Warden currentWarden = SessionManager.getCurrentWarden();
        Integer blockId = (currentWarden != null) ? currentWarden.getAssignedBlock() : null;
        String blockName = (currentWarden != null && currentWarden.getBlockName() != null)
                ? currentWarden.getBlockName()
                : (blockId != null ? "Block #" + blockId : "All Blocks (Supervisory)");

        // Header
        Label title = new Label("Warden Dashboard — " + blockName);
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: " + StyleConstants.COLOR_TEXT_MAIN + ";");

        Label subtitle = new Label("Supervisory management restricted to " + blockName + ".");
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: " + StyleConstants.COLOR_TEXT_MUTED + ";");

        VBox titleBox = new VBox(4, title, subtitle);

        // Fetch Block-specific data
        int studentsCount = 0;
        int occupiedBeds = 0;
        int totalBeds = 0;
        int complaintsCount = 0;
        int visitorsCount = 0;
        int leaveCount = 0;
        int todayPresent = 0;
        int todayLate = 0;

        try {
            if (blockId != null) {
                List<Student> students = studentController.getStudentsByBlock(blockId);
                studentsCount = students.size();

                List<Room> rooms = roomController.getRoomsByBlock(blockId);
                for (Room r : rooms) {
                    totalBeds += r.getCapacity();
                    occupiedBeds += r.getOccupied();
                }

                complaintsCount = complaintController.getComplaintsByBlock(blockId).size();
                visitorsCount = visitorController.getVisitorsByBlock(blockId).size();
                leaveCount = leaveController.getLeaveRequestsByBlock(blockId).size();

                String today = LocalDate.now().toString();
                var attendanceList = attendanceController.getAttendanceByBlock(blockId, today);
                for (var a : attendanceList) {
                    if ("PRESENT".equalsIgnoreCase(a.getStatus())) todayPresent++;
                    if ("LATE".equalsIgnoreCase(a.getStatus())) todayLate++;
                }
            } else {
                studentsCount = studentController.getAllStudents().size();
            }
        } catch (Exception ignored) {}

        double occupancyPct = totalBeds > 0 ? ((double) occupiedBeds / totalBeds) * 100.0 : 0.0;

        // Metric Cards Grid
        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(16);

        grid.add(createCard("👥", "Students in Block", String.valueOf(studentsCount), StyleConstants.COLOR_PRIMARY), 0, 0);
        grid.add(createCard("🚪", "Room Occupancy", occupiedBeds + " / " + totalBeds + " (" + (int) occupancyPct + "%)", StyleConstants.COLOR_SUCCESS), 1, 0);
        grid.add(createCard("✅", "Today's Attendance", String.valueOf(todayPresent), "#0284C7"), 2, 0);
        grid.add(createCard("⏰", "Late Entries", String.valueOf(todayLate), StyleConstants.COLOR_DANGER), 3, 0);

        grid.add(createCard("⚠️", "Pending Complaints", String.valueOf(complaintsCount), StyleConstants.COLOR_WARNING), 0, 1);
        grid.add(createCard("📋", "Visitor Requests", String.valueOf(visitorsCount), StyleConstants.COLOR_PURPLE), 1, 1);
        grid.add(createCard("✈️", "Leave Requests", String.valueOf(leaveCount), "#D97706"), 2, 1);

        content.getChildren().addAll(titleBox, grid);
        setContent(content);
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
        valLbl.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: " + StyleConstants.COLOR_TEXT_MAIN + ";");

        card.getChildren().addAll(top, valLbl);
        return card;
    }
}
