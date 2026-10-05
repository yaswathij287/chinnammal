package com.smarthostel.ui;

import com.smarthostel.controller.ComplaintController;
import com.smarthostel.model.Complaint;
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

import java.util.List;

/**
 * Grievance handling panel supporting complaint filing, category tagging, and resolution workflows.
 */
public class ComplaintsPanel extends VBox {

    private final ComplaintController complaintController = new ComplaintController();

    private final TableView<Complaint> table = new TableView<>();
    private final ObservableList<Complaint> complaintList = FXCollections.observableArrayList();
    private final ComboBox<String> statusFilter = new ComboBox<>();

    public ComplaintsPanel() {
        setSpacing(18);
        setPadding(new Insets(24));
        setStyle("-fx-background-color: " + StyleConstants.COLOR_BG_APP + ";");

        // Header
        Label title = new Label("Grievance & Complaints Management");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: " + StyleConstants.COLOR_TEXT_MAIN + ";");

        Role role = SessionManager.getCurrentRole();
        Warden warden = SessionManager.getCurrentWarden();
        boolean isWarden = (role == Role.WARDEN && warden != null && warden.getAssignedBlock() != null);

        Label subtitle = new Label(role == Role.STUDENT
                ? "File maintenance or facility complaints and monitor real-time resolution status."
                : (isWarden ? "Manage complaints from residents in " + (warden.getBlockName() != null ? warden.getBlockName() : "Block #" + warden.getAssignedBlock())
                            : "Monitor and resolve all hostel grievances across all blocks."));
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: " + StyleConstants.COLOR_TEXT_MUTED + ";");

        VBox titleBox = new VBox(4, title, subtitle);

        // Toolbar
        HBox toolbar = new HBox(12);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        statusFilter.getItems().addAll("All Statuses", "OPEN", "IN_PROGRESS", "RESOLVED");
        statusFilter.setValue("All Statuses");
        statusFilter.setStyle(StyleConstants.STYLE_TEXT_FIELD);
        statusFilter.setOnAction(e -> applyFilter());

        Button submitBtn = new Button("+ Submit Complaint");
        submitBtn.setStyle(StyleConstants.STYLE_PRIMARY_BTN);
        submitBtn.setOnAction(e -> showSubmitComplaintDialog());

        Button inProgressBtn = new Button("Mark In-Progress");
        inProgressBtn.setStyle(StyleConstants.STYLE_SECONDARY_BTN);
        inProgressBtn.setVisible(role != Role.STUDENT);
        inProgressBtn.setOnAction(e -> updateSelectedStatus("IN_PROGRESS"));

        Button resolveBtn = new Button("✓ Resolve Complaint");
        resolveBtn.setStyle(StyleConstants.STYLE_PRIMARY_BTN);
        resolveBtn.setVisible(role != Role.STUDENT);
        resolveBtn.setOnAction(e -> updateSelectedStatus("RESOLVED"));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        toolbar.getChildren().addAll(new Label("Filter:"), statusFilter, spacer);
        if (role == Role.STUDENT) {
            toolbar.getChildren().add(submitBtn);
        } else {
            toolbar.getChildren().addAll(inProgressBtn, resolveBtn);
        }

        // Table
        table.setItems(complaintList);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<Complaint, Integer> colId = new TableColumn<>("Ticket #");
        colId.setCellValueFactory(new PropertyValueFactory<>("complaintId"));
        colId.setMaxWidth(70);

        TableColumn<Complaint, String> colStudent = new TableColumn<>("Student");
        colStudent.setCellValueFactory(new PropertyValueFactory<>("studentName"));

        TableColumn<Complaint, String> colRoom = new TableColumn<>("Room");
        colRoom.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));

        TableColumn<Complaint, String> colCategory = new TableColumn<>("Category");
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));

        TableColumn<Complaint, String> colDesc = new TableColumn<>("Description");
        colDesc.setCellValueFactory(new PropertyValueFactory<>("description"));

        TableColumn<Complaint, String> colPriority = new TableColumn<>("Priority");
        colPriority.setCellValueFactory(new PropertyValueFactory<>("priority"));

        TableColumn<Complaint, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        table.getColumns().addAll(colId, colStudent, colRoom, colCategory, colDesc, colPriority, colStatus);

        refreshTable();
        getChildren().addAll(titleBox, toolbar, table);
    }

    private void refreshTable() {
        try {
            Role role = SessionManager.getCurrentRole();
            Student student = SessionManager.getCurrentStudent();
            Warden warden = SessionManager.getCurrentWarden();

            List<Complaint> list;
            if (role == Role.STUDENT && student != null) {
                list = complaintController.getComplaintsByStudent(student.getStudentId());
            } else if (role == Role.WARDEN && warden != null && warden.getAssignedBlock() != null) {
                list = complaintController.getComplaintsByBlock(warden.getAssignedBlock());
            } else {
                list = complaintController.getAllComplaints();
            }
            complaintList.setAll(list);
        } catch (Exception e) {
            AlertUtil.showError("Error", "Failed to load complaints: " + e.getMessage());
        }
    }

    private void applyFilter() {
        String filter = statusFilter.getValue();
        refreshTable();
        if (filter != null && !"All Statuses".equalsIgnoreCase(filter)) {
            complaintList.removeIf(c -> !filter.equalsIgnoreCase(c.getStatus()));
        }
    }

    private void updateSelectedStatus(String newStatus) {
        Complaint selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showWarning("No Selection", "Please select a complaint ticket.");
            return;
        }
        try {
            complaintController.updateStatus(selected.getComplaintId(), newStatus);
            applyFilter();
            AlertUtil.showInfo("Status Updated", "Complaint #" + selected.getComplaintId() + " marked as " + newStatus);
        } catch (Exception e) {
            AlertUtil.showError("Update Error", e.getMessage());
        }
    }

    private void showSubmitComplaintDialog() {
        Student s = SessionManager.getCurrentStudent();
        if (s == null) return;

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Submit New Complaint");
        dialog.setHeaderText("Log an issue regarding hostel maintenance, electrical, WiFi, or cleaning");

        ButtonType submitBtn = new ButtonType("Submit", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(submitBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(12);
        grid.setPadding(new Insets(20, 30, 10, 10));

        ComboBox<String> catCombo = new ComboBox<>();
        catCombo.getItems().addAll("Maintenance", "Electrical", "Plumbing", "WiFi / Internet", "Housekeeping", "Mess / Food", "Security");
        catCombo.setValue("Maintenance");

        ComboBox<String> priorityCombo = new ComboBox<>();
        priorityCombo.getItems().addAll("LOW", "MEDIUM", "HIGH");
        priorityCombo.setValue("MEDIUM");

        TextArea descArea = new TextArea();
        descArea.setPromptText("Describe the issue in detail...");
        descArea.setPrefRowCount(4);

        grid.add(new Label("Category:"), 0, 0);
        grid.add(catCombo, 1, 0);
        grid.add(new Label("Priority:"), 0, 1);
        grid.add(priorityCombo, 1, 1);
        grid.add(new Label("Description:"), 0, 2);
        grid.add(descArea, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == submitBtn) {
                try {
                    Complaint c = new Complaint(s.getStudentId(), catCombo.getValue(), descArea.getText().trim(),
                            s.getRoomId(), priorityCombo.getValue());
                    complaintController.submitComplaint(c);
                    refreshTable();
                    AlertUtil.showInfo("Submitted", "Complaint registered successfully.");
                } catch (Exception ex) {
                    AlertUtil.showError("Submission Failed", ex.getMessage());
                }
            }
            return null;
        });

        dialog.showAndWait();
    }
}
