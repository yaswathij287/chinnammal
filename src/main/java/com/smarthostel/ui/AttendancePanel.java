package com.smarthostel.ui;

import com.smarthostel.controller.AttendanceController;
import com.smarthostel.controller.StudentController;
import com.smarthostel.model.Attendance;
import com.smarthostel.model.Role;
import com.smarthostel.model.Student;
import com.smarthostel.model.Warden;
import com.smarthostel.util.AlertUtil;
import com.smarthostel.util.SessionManager;
import com.smarthostel.util.StyleConstants;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Attendance tracking panel showing daily roll-calls, curfew late entries, and student history.
 */
public class AttendancePanel extends VBox {

    private final AttendanceController attendanceController = new AttendanceController();
    private final StudentController studentController = new StudentController();

    private final TableView<Attendance> table = new TableView<>();
    private final ObservableList<Attendance> attendanceList = FXCollections.observableArrayList();
    private final DatePicker datePicker = new DatePicker(LocalDate.now());

    public AttendancePanel() {
        setSpacing(18);
        setPadding(new Insets(24));
        setStyle("-fx-background-color: " + StyleConstants.COLOR_BG_APP + ";");

        // Header
        Label title = new Label("Attendance & Curfew Registry");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: " + StyleConstants.COLOR_TEXT_MAIN + ";");

        Role role = SessionManager.getCurrentRole();
        Student student = SessionManager.getCurrentStudent();
        Warden warden = SessionManager.getCurrentWarden();
        boolean isWarden = (role == Role.WARDEN && warden != null && warden.getAssignedBlock() != null);

        Label subtitle = new Label(role == Role.STUDENT
                ? "Your entry/exit log and overall attendance percentage."
                : (isWarden ? "Daily roll call and night entry verification for " + (warden.getBlockName() != null ? warden.getBlockName() : "Block #" + warden.getAssignedBlock())
                            : "Monitor hostel student night curfews and daily attendance."));
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: " + StyleConstants.COLOR_TEXT_MUTED + ";");

        VBox titleBox = new VBox(4, title, subtitle);

        // Stats summary for student
        HBox statsBox = new HBox(16);
        if (role == Role.STUDENT && student != null) {
            try {
                double pct = attendanceController.getStudentAttendancePercentage(student.getStudentId());
                statsBox.getChildren().add(createStatCard("📈 My Attendance Record", pct + "% Attended", StyleConstants.COLOR_PRIMARY));
            } catch (Exception ignored) {}
        } else {
            try {
                int present = attendanceController.getAllAttendance().size();
                statsBox.getChildren().add(createStatCard("📋 Total Recorded Entries", String.valueOf(present), StyleConstants.COLOR_PRIMARY));
            } catch (Exception ignored) {}
        }

        // Toolbar
        HBox toolbar = new HBox(12);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        datePicker.setStyle(StyleConstants.STYLE_TEXT_FIELD);
        datePicker.setOnAction(e -> filterByDate());

        Button allBtn = new Button("View All Dates");
        allBtn.setStyle(StyleConstants.STYLE_SECONDARY_BTN);
        allBtn.setOnAction(e -> refreshTable());

        Button markBtn = new Button("+ Mark Attendance");
        markBtn.setStyle(StyleConstants.STYLE_PRIMARY_BTN);
        markBtn.setVisible(role != Role.STUDENT);
        markBtn.setOnAction(e -> showMarkAttendanceDialog());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        if (role != Role.STUDENT) {
            toolbar.getChildren().addAll(new Label("Date:"), datePicker, allBtn);
        }
        toolbar.getChildren().addAll(spacer);
        if (role != Role.STUDENT) toolbar.getChildren().add(markBtn);

        // Table
        table.setItems(attendanceList);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<Attendance, Integer> colId = new TableColumn<>("Log #");
        colId.setCellValueFactory(new PropertyValueFactory<>("attendanceId"));
        colId.setMaxWidth(70);

        TableColumn<Attendance, String> colStudent = new TableColumn<>("Student");
        colStudent.setCellValueFactory(new PropertyValueFactory<>("studentName"));

        TableColumn<Attendance, String> colDate = new TableColumn<>("Date");
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));

        TableColumn<Attendance, String> colEntry = new TableColumn<>("Morning Exit");
        colEntry.setCellValueFactory(new PropertyValueFactory<>("entryTime"));

        TableColumn<Attendance, String> colExit = new TableColumn<>("Night Return");
        colExit.setCellValueFactory(new PropertyValueFactory<>("exitTime"));

        TableColumn<Attendance, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        table.getColumns().addAll(colId, colStudent, colDate, colEntry, colExit, colStatus);

        refreshTable();
        getChildren().addAll(titleBox, statsBox, toolbar, table);
    }

    private VBox createStatCard(String label, String value, String color) {
        VBox c = new VBox(4);
        c.setStyle(StyleConstants.STYLE_CARD + " -fx-border-left-color: " + color + "; -fx-border-left-width: 4;");
        c.setPadding(new Insets(10, 16, 10, 16));
        c.setPrefWidth(220);

        Label l = new Label(label);
        l.setStyle("-fx-font-size: 12px; -fx-text-fill: " + StyleConstants.COLOR_TEXT_MUTED + ";");

        Label v = new Label(value);
        v.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " + StyleConstants.COLOR_TEXT_MAIN + ";");

        c.getChildren().addAll(l, v);
        return c;
    }

    private void refreshTable() {
        try {
            Role role = SessionManager.getCurrentRole();
            Student student = SessionManager.getCurrentStudent();
            Warden warden = SessionManager.getCurrentWarden();

            List<Attendance> list;
            if (role == Role.STUDENT && student != null) {
                list = attendanceController.getAttendanceByStudent(student.getStudentId());
            } else if (role == Role.WARDEN && warden != null && warden.getAssignedBlock() != null) {
                list = attendanceController.getAttendanceByBlock(warden.getAssignedBlock(), null);
            } else {
                list = attendanceController.getAllAttendance();
            }
            attendanceList.setAll(list);
        } catch (Exception e) {
            AlertUtil.showError("Error", "Failed to load attendance logs: " + e.getMessage());
        }
    }

    private void filterByDate() {
        if (datePicker.getValue() == null) {
            refreshTable();
            return;
        }
        String d = datePicker.getValue().toString();
        refreshTable();
        attendanceList.removeIf(a -> !d.equals(a.getDate()));
    }

    private void showMarkAttendanceDialog() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Mark Student Attendance");
        dialog.setHeaderText("Log hostel check-in / curfew status for resident");

        ButtonType saveBtn = new ButtonType("Save Record", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(12);
        grid.setPadding(new Insets(20, 30, 10, 10));

        ComboBox<Student> studentCombo = new ComboBox<>();
        try {
            Role role = SessionManager.getCurrentRole();
            Warden warden = SessionManager.getCurrentWarden();
            if (role == Role.WARDEN && warden != null && warden.getAssignedBlock() != null) {
                studentCombo.getItems().addAll(studentController.getStudentsByBlock(warden.getAssignedBlock()));
            } else {
                studentCombo.getItems().addAll(studentController.getAllStudents());
            }
        } catch (Exception ignored) {}

        DatePicker dp = new DatePicker(LocalDate.now());
        TextField entryField = new TextField("08:00 AM");
        TextField exitField = new TextField("09:30 PM");

        ComboBox<String> statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll("PRESENT", "LATE", "ABSENT", "ON_LEAVE");
        statusCombo.setValue("PRESENT");

        grid.add(new Label("Student:"), 0, 0);
        grid.add(studentCombo, 1, 0);
        grid.add(new Label("Date:"), 0, 1);
        grid.add(dp, 1, 1);
        grid.add(new Label("Morning Departure:"), 0, 2);
        grid.add(entryField, 1, 2);
        grid.add(new Label("Night Return:"), 0, 3);
        grid.add(exitField, 1, 3);
        grid.add(new Label("Attendance Status:"), 0, 4);
        grid.add(statusCombo, 1, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == saveBtn) {
                Student s = studentCombo.getValue();
                if (s == null) {
                    AlertUtil.showWarning("Missing Student", "Please select a student.");
                    return null;
                }
                try {
                    String d = dp.getValue() != null ? dp.getValue().toString() : LocalDate.now().toString();
                    Attendance a = new Attendance(s.getStudentId(), d, entryField.getText().trim(),
                            exitField.getText().trim(), statusCombo.getValue());
                    attendanceController.markAttendance(a);
                    refreshTable();
                    AlertUtil.showInfo("Recorded", "Attendance logged for " + s.getName());
                } catch (Exception ex) {
                    AlertUtil.showError("Save Error", ex.getMessage());
                }
            }
            return null;
        });

        dialog.showAndWait();
    }
}
