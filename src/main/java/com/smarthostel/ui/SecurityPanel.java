package com.smarthostel.ui;

import com.smarthostel.controller.SecurityController;
import com.smarthostel.controller.StudentController;
import com.smarthostel.model.SecurityLog;
import com.smarthostel.model.Student;
import com.smarthostel.service.FaceDetectionService;
import com.smarthostel.util.AlertUtil;
import com.smarthostel.util.StyleConstants;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.util.List;

/**
 * Security & Gate Entry Management Panel with OpenCV Face Verification integration.
 */
public class SecurityPanel extends VBox {

    private final SecurityController securityController = new SecurityController();
    private final StudentController studentController = new StudentController();

    private final TableView<SecurityLog> table = new TableView<>();
    private final ObservableList<SecurityLog> logList = FXCollections.observableArrayList();

    public SecurityPanel() {
        setSpacing(18);
        setPadding(new Insets(24));
        setStyle("-fx-background-color: " + StyleConstants.COLOR_BG_APP + ";");

        // Header
        Label title = new Label("Security & Gate Access Management");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: " + StyleConstants.COLOR_TEXT_MAIN + ";");

        Label subtitle = new Label("Campus perimeter tracking, multi-factor verification (Card, Face Detection, Manual), and curfew security alerts.");
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: " + StyleConstants.COLOR_TEXT_MUTED + ";");

        VBox titleBox = new VBox(4, title, subtitle);

        // Alert stats summary
        HBox statsBox = new HBox(16);
        try {
            int alerts = securityController.getSecurityAlertsCount();
            statsBox.getChildren().addAll(
                    createStatCard("🚨 Curfew / Night Alerts", String.valueOf(alerts), alerts > 0 ? StyleConstants.COLOR_DANGER : StyleConstants.COLOR_SUCCESS),
                    createStatCard("📷 OpenCV Engine Status", FaceDetectionService.isOpenCvAvailable() ? "ACTIVE (Native)" : "SIMULATED (Fallback)", StyleConstants.COLOR_PRIMARY)
            );
        } catch (Exception ignored) {}

        // Toolbar
        HBox toolbar = new HBox(12);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        Button manualLogBtn = new Button("+ Log Manual Event");
        manualLogBtn.setStyle(StyleConstants.STYLE_SECONDARY_BTN);
        manualLogBtn.setOnAction(e -> showManualLogDialog());

        Button faceScanBtn = new Button("📷 OpenCV Face Verification");
        faceScanBtn.setStyle(StyleConstants.STYLE_PRIMARY_BTN);
        faceScanBtn.setOnAction(e -> showFaceVerificationDialog());

        Button refreshBtn = new Button("⟳ Refresh");
        refreshBtn.setStyle(StyleConstants.STYLE_SECONDARY_BTN);
        refreshBtn.setOnAction(e -> refreshTable());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        toolbar.getChildren().addAll(spacer, manualLogBtn, faceScanBtn, refreshBtn);

        // Table
        table.setItems(logList);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<SecurityLog, Integer> colId = new TableColumn<>("Log #");
        colId.setCellValueFactory(new PropertyValueFactory<>("logId"));
        colId.setMaxWidth(70);

        TableColumn<SecurityLog, String> colStudent = new TableColumn<>("Student");
        colStudent.setCellValueFactory(new PropertyValueFactory<>("studentName"));

        TableColumn<SecurityLog, String> colGate = new TableColumn<>("Gate");
        colGate.setCellValueFactory(new PropertyValueFactory<>("gate"));

        TableColumn<SecurityLog, String> colEvent = new TableColumn<>("Event Type");
        colEvent.setCellValueFactory(new PropertyValueFactory<>("eventType"));

        TableColumn<SecurityLog, Object> colTime = new TableColumn<>("Timestamp");
        colTime.setCellValueFactory(new PropertyValueFactory<>("eventTime"));

        TableColumn<SecurityLog, String> colMethod = new TableColumn<>("Verification Method");
        colMethod.setCellValueFactory(new PropertyValueFactory<>("verificationMethod"));

        TableColumn<SecurityLog, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        table.getColumns().addAll(colId, colStudent, colGate, colEvent, colTime, colMethod, colStatus);

        refreshTable();
        getChildren().addAll(titleBox, statsBox, toolbar, table);
    }

    private VBox createStatCard(String label, String value, String color) {
        VBox c = new VBox(4);
        c.setStyle(StyleConstants.STYLE_CARD + " -fx-border-left-color: " + color + "; -fx-border-left-width: 4;");
        c.setPadding(new Insets(10, 16, 10, 16));
        c.setPrefWidth(240);

        Label l = new Label(label);
        l.setStyle("-fx-font-size: 12px; -fx-text-fill: " + StyleConstants.COLOR_TEXT_MUTED + ";");

        Label v = new Label(value);
        v.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " + StyleConstants.COLOR_TEXT_MAIN + ";");

        c.getChildren().addAll(l, v);
        return c;
    }

    private void refreshTable() {
        try {
            logList.setAll(securityController.getAllLogs());
        } catch (Exception e) {
            AlertUtil.showError("Error", "Failed to load security logs: " + e.getMessage());
        }
    }

    private void showManualLogDialog() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Log Manual Gate Movement");
        dialog.setHeaderText("Record physical gate entry or departure");

        ButtonType saveBtn = new ButtonType("Save Log", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(12);
        grid.setPadding(new Insets(20, 30, 10, 10));

        ComboBox<Student> studentCombo = new ComboBox<>();
        try {
            studentCombo.getItems().addAll(studentController.getAllStudents());
        } catch (Exception ignored) {}

        ComboBox<String> gateCombo = new ComboBox<>();
        gateCombo.getItems().addAll("Main Gate", "Gate 1 (North)", "Gate 2 (South)", "Hostel Rear Gate");
        gateCombo.setValue("Main Gate");

        ComboBox<String> eventCombo = new ComboBox<>();
        eventCombo.getItems().addAll("ENTRY", "EXIT");
        eventCombo.setValue("ENTRY");

        ComboBox<String> methodCombo = new ComboBox<>();
        methodCombo.getItems().addAll("MANUAL", "CARD", "FACE");
        methodCombo.setValue("CARD");

        grid.add(new Label("Student:"), 0, 0);
        grid.add(studentCombo, 1, 0);
        grid.add(new Label("Campus Gate:"), 0, 1);
        grid.add(gateCombo, 1, 1);
        grid.add(new Label("Movement Event:"), 0, 2);
        grid.add(eventCombo, 1, 2);
        grid.add(new Label("Verification:"), 0, 3);
        grid.add(methodCombo, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == saveBtn) {
                Student s = studentCombo.getValue();
                Integer sId = s != null ? s.getStudentId() : null;
                try {
                    securityController.logManualEvent(sId, eventCombo.getValue(), gateCombo.getValue(), methodCombo.getValue());
                    refreshTable();
                    AlertUtil.showInfo("Logged", "Security gate movement recorded.");
                } catch (Exception ex) {
                    AlertUtil.showError("Logging Error", ex.getMessage());
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    /**
     * OpenCV Face Verification dialog with simulated / camera detection scan.
     */
    private void showFaceVerificationDialog() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("OpenCV Biometric Face Scanner");
        dialog.setHeaderText("Camera Feed & Face Verification Terminal");

        ButtonType scanBtn = new ButtonType("Scan & Verify", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(scanBtn, ButtonType.CANCEL);

        VBox layout = new VBox(16);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20));

        // Simulated Camera Viewfinder
        StackPane cameraFrame = new StackPane();
        cameraFrame.setPrefSize(340, 220);
        cameraFrame.setStyle("-fx-background-color: #0F172A; -fx-background-radius: 8;");

        Rectangle boundingBox = new Rectangle(140, 160);
        boundingBox.setFill(Color.TRANSPARENT);
        boundingBox.setStroke(Color.web(StyleConstants.COLOR_PRIMARY));
        boundingBox.setStrokeWidth(2);
        boundingBox.getStrokeDashArray().addAll(10d, 5d);

        Label scanStatus = new Label("READY FOR FACE RECOGNITION");
        scanStatus.setStyle("-fx-text-fill: #38BDF8; -fx-font-weight: bold; -fx-font-size: 12px;");

        Label engineMode = new Label(FaceDetectionService.isOpenCvAvailable()
                ? "Engine: OpenCV 4.9 (Active)"
                : "Engine: Fallback Biometric Scanner");
        engineMode.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 11px;");

        VBox overlay = new VBox(8, boundingBox, scanStatus, engineMode);
        overlay.setAlignment(Pos.CENTER);

        cameraFrame.getChildren().add(overlay);

        // Inputs
        GridPane inputs = new GridPane();
        inputs.setHgap(10);
        inputs.setVgap(10);
        inputs.setAlignment(Pos.CENTER);

        ComboBox<Student> studentCombo = new ComboBox<>();
        try {
            studentCombo.getItems().addAll(studentController.getAllStudents());
            if (!studentCombo.getItems().isEmpty()) {
                studentCombo.setValue(studentCombo.getItems().get(0));
            }
        } catch (Exception ignored) {}

        ComboBox<String> gateCombo = new ComboBox<>();
        gateCombo.getItems().addAll("Main Gate", "Gate 1", "Gate 2");
        gateCombo.setValue("Gate 1");

        ComboBox<String> eventCombo = new ComboBox<>();
        eventCombo.getItems().addAll("ENTRY", "EXIT");
        eventCombo.setValue("ENTRY");

        inputs.add(new Label("Select Resident:"), 0, 0);
        inputs.add(studentCombo, 1, 0);
        inputs.add(new Label("Security Gate:"), 0, 1);
        inputs.add(gateCombo, 1, 1);
        inputs.add(new Label("Event:"), 0, 2);
        inputs.add(eventCombo, 1, 2);

        layout.getChildren().addAll(cameraFrame, inputs);
        dialog.getDialogPane().setContent(layout);

        dialog.setResultConverter(btn -> {
            if (btn == scanBtn) {
                Student s = studentCombo.getValue();
                if (s == null) {
                    AlertUtil.showWarning("Missing Student", "Please select a student.");
                    return null;
                }
                try {
                    var res = securityController.performFaceVerification(s.getStudentId(), gateCombo.getValue(), eventCombo.getValue());
                    refreshTable();
                    AlertUtil.showInfo("Face Verified Successfully",
                            "Student: " + res.getStudentName() + "\n" +
                            "Match Confidence: " + res.getConfidence() + "%\n" +
                            "Result: " + res.getMessage() + "\n" +
                            "Event: " + eventCombo.getValue() + " logged to database.");
                } catch (Exception ex) {
                    AlertUtil.showError("Scan Failed", ex.getMessage());
                }
            }
            return null;
        });

        dialog.showAndWait();
    }
}
