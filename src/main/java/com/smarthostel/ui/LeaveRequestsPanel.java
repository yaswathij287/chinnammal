package com.smarthostel.ui;

import com.smarthostel.controller.LeaveController;
import com.smarthostel.model.LeaveRequest;
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
import java.util.List;

/**
 * Leave & out-pass management panel for students and wardens.
 */
public class LeaveRequestsPanel extends VBox {

    private final LeaveController leaveController = new LeaveController();

    private final TableView<LeaveRequest> table = new TableView<>();
    private final ObservableList<LeaveRequest> leaveList = FXCollections.observableArrayList();
    private final ComboBox<String> statusFilter = new ComboBox<>();

    public LeaveRequestsPanel() {
        setSpacing(18);
        setPadding(new Insets(24));
        setStyle("-fx-background-color: " + StyleConstants.COLOR_BG_APP + ";");

        // Header
        Label title = new Label("Student Out-Pass & Leave Requests");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: " + StyleConstants.COLOR_TEXT_MAIN + ";");

        Role role = SessionManager.getCurrentRole();
        Warden warden = SessionManager.getCurrentWarden();
        boolean isWarden = (role == Role.WARDEN && warden != null && warden.getAssignedBlock() != null);

        Label subtitle = new Label(role == Role.STUDENT
                ? "Apply for official hostel out-pass/leave and view approval status."
                : (isWarden ? "Review and sanction leaves for students of " + (warden.getBlockName() != null ? warden.getBlockName() : "Block #" + warden.getAssignedBlock())
                            : "Monitor all hostel student leaves and gate out-passes."));
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: " + StyleConstants.COLOR_TEXT_MUTED + ";");

        VBox titleBox = new VBox(4, title, subtitle);

        // Toolbar
        HBox toolbar = new HBox(12);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        statusFilter.getItems().addAll("All Statuses", "PENDING", "APPROVED", "REJECTED");
        statusFilter.setValue("All Statuses");
        statusFilter.setStyle(StyleConstants.STYLE_TEXT_FIELD);
        statusFilter.setOnAction(e -> applyFilter());

        Button applyBtn = new Button("+ Apply for Leave");
        applyBtn.setStyle(StyleConstants.STYLE_PRIMARY_BTN);
        applyBtn.setOnAction(e -> showApplyLeaveDialog());

        Button approveBtn = new Button("✓ Approve Leave");
        approveBtn.setStyle(StyleConstants.STYLE_PRIMARY_BTN);
        approveBtn.setVisible(role != Role.STUDENT);
        approveBtn.setOnAction(e -> handleStatusUpdate("APPROVED"));

        Button rejectBtn = new Button("✕ Reject Leave");
        rejectBtn.setStyle(StyleConstants.STYLE_DANGER_BTN);
        rejectBtn.setVisible(role != Role.STUDENT);
        rejectBtn.setOnAction(e -> handleStatusUpdate("REJECTED"));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        toolbar.getChildren().addAll(new Label("Filter:"), statusFilter, spacer);
        if (role == Role.STUDENT) {
            toolbar.getChildren().add(applyBtn);
        } else {
            toolbar.getChildren().addAll(approveBtn, rejectBtn);
        }

        // Table
        table.setItems(leaveList);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<LeaveRequest, Integer> colId = new TableColumn<>("Leave #");
        colId.setCellValueFactory(new PropertyValueFactory<>("leaveId"));
        colId.setMaxWidth(70);

        TableColumn<LeaveRequest, String> colStudent = new TableColumn<>("Student");
        colStudent.setCellValueFactory(new PropertyValueFactory<>("studentName"));

        TableColumn<LeaveRequest, String> colFrom = new TableColumn<>("From Date");
        colFrom.setCellValueFactory(new PropertyValueFactory<>("fromDate"));

        TableColumn<LeaveRequest, String> colTo = new TableColumn<>("To Date");
        colTo.setCellValueFactory(new PropertyValueFactory<>("toDate"));

        TableColumn<LeaveRequest, String> colReason = new TableColumn<>("Reason");
        colReason.setCellValueFactory(new PropertyValueFactory<>("reason"));

        TableColumn<LeaveRequest, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        TableColumn<LeaveRequest, String> colApprover = new TableColumn<>("Sanctioned By");
        colApprover.setCellValueFactory(new PropertyValueFactory<>("approvedByName"));

        table.getColumns().addAll(colId, colStudent, colFrom, colTo, colReason, colStatus, colApprover);

        refreshTable();
        getChildren().addAll(titleBox, toolbar, table);
    }

    private void refreshTable() {
        try {
            Role role = SessionManager.getCurrentRole();
            Student student = SessionManager.getCurrentStudent();
            Warden warden = SessionManager.getCurrentWarden();

            List<LeaveRequest> list;
            if (role == Role.STUDENT && student != null) {
                list = leaveController.getLeaveRequestsByStudent(student.getStudentId());
            } else if (role == Role.WARDEN && warden != null && warden.getAssignedBlock() != null) {
                list = leaveController.getLeaveRequestsByBlock(warden.getAssignedBlock());
            } else {
                list = leaveController.getAllLeaveRequests();
            }
            leaveList.setAll(list);
        } catch (Exception e) {
            AlertUtil.showError("Error", "Failed to load leave requests: " + e.getMessage());
        }
    }

    private void applyFilter() {
        String filter = statusFilter.getValue();
        refreshTable();
        if (filter != null && !"All Statuses".equalsIgnoreCase(filter)) {
            leaveList.removeIf(l -> !filter.equalsIgnoreCase(l.getStatus()));
        }
    }

    private void handleStatusUpdate(String newStatus) {
        LeaveRequest selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showWarning("No Selection", "Please select a leave request.");
            return;
        }
        try {
            Warden warden = SessionManager.getCurrentWarden();
            int approverId = (warden != null) ? warden.getWardenId() : 1;
            if ("APPROVED".equals(newStatus)) {
                leaveController.approveLeave(selected.getLeaveId(), approverId);
            } else {
                leaveController.rejectLeave(selected.getLeaveId(), approverId);
            }
            applyFilter();
            AlertUtil.showInfo("Status Updated", "Leave request marked as " + newStatus);
        } catch (Exception e) {
            AlertUtil.showError("Update Error", e.getMessage());
        }
    }

    private void showApplyLeaveDialog() {
        Student s = SessionManager.getCurrentStudent();
        if (s == null) return;

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Apply for Out-Pass / Leave");
        dialog.setHeaderText("Specify leave travel dates and purpose");

        ButtonType submitBtn = new ButtonType("Submit Application", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(submitBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(12);
        grid.setPadding(new Insets(20, 30, 10, 10));

        DatePicker fromPicker = new DatePicker(LocalDate.now());
        DatePicker toPicker = new DatePicker(LocalDate.now().plusDays(2));
        TextArea reasonArea = new TextArea();
        reasonArea.setPromptText("Enter purpose of visit/leave...");
        reasonArea.setPrefRowCount(3);

        grid.add(new Label("From Date:"), 0, 0);
        grid.add(fromPicker, 1, 0);
        grid.add(new Label("To Date:"), 0, 1);
        grid.add(toPicker, 1, 1);
        grid.add(new Label("Reason:"), 0, 2);
        grid.add(reasonArea, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == submitBtn) {
                try {
                    String from = fromPicker.getValue() != null ? fromPicker.getValue().toString() : LocalDate.now().toString();
                    String to = toPicker.getValue() != null ? toPicker.getValue().toString() : LocalDate.now().plusDays(1).toString();
                    LeaveRequest lr = new LeaveRequest(s.getStudentId(), from, to, reasonArea.getText().trim());
                    leaveController.applyLeave(lr);
                    refreshTable();
                    AlertUtil.showInfo("Success", "Leave request submitted successfully.");
                } catch (Exception ex) {
                    AlertUtil.showError("Application Failed", ex.getMessage());
                }
            }
            return null;
        });

        dialog.showAndWait();
    }
}
