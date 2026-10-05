package com.smarthostel.ui;

import com.smarthostel.controller.VisitorController;
import com.smarthostel.model.Role;
import com.smarthostel.model.Student;
import com.smarthostel.model.Visitor;
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
 * Visitor gate pass and registration panel.
 */
public class VisitorsPanel extends VBox {

    private final VisitorController visitorController = new VisitorController();

    private final TableView<Visitor> table = new TableView<>();
    private final ObservableList<Visitor> visitorList = FXCollections.observableArrayList();
    private final ComboBox<String> statusFilter = new ComboBox<>();

    public VisitorsPanel() {
        setSpacing(18);
        setPadding(new Insets(24));
        setStyle("-fx-background-color: " + StyleConstants.COLOR_BG_APP + ";");

        // Header
        Label title = new Label("Visitor Gate Pass Requests");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: " + StyleConstants.COLOR_TEXT_MAIN + ";");

        Role role = SessionManager.getCurrentRole();
        Warden warden = SessionManager.getCurrentWarden();
        boolean isWarden = (role == Role.WARDEN && warden != null && warden.getAssignedBlock() != null);

        Label subtitle = new Label(role == Role.STUDENT
                ? "Request visitor gate entry permissions and track approval status."
                : (isWarden ? "Manage visitor pass requests for " + (warden.getBlockName() != null ? warden.getBlockName() : "Block #" + warden.getAssignedBlock())
                            : "Monitor and approve campus visitor requests across all hostel blocks."));
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: " + StyleConstants.COLOR_TEXT_MUTED + ";");

        VBox titleBox = new VBox(4, title, subtitle);

        // Toolbar
        HBox toolbar = new HBox(12);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        statusFilter.getItems().addAll("All Statuses", "PENDING", "APPROVED", "REJECTED");
        statusFilter.setValue("All Statuses");
        statusFilter.setStyle(StyleConstants.STYLE_TEXT_FIELD);
        statusFilter.setOnAction(e -> applyFilter());

        Button requestBtn = new Button("+ Request Visitor Entry");
        requestBtn.setStyle(StyleConstants.STYLE_PRIMARY_BTN);
        requestBtn.setOnAction(e -> showRequestVisitorDialog());

        Button approveBtn = new Button("✓ Approve Request");
        approveBtn.setStyle(StyleConstants.STYLE_PRIMARY_BTN);
        approveBtn.setVisible(role != Role.STUDENT);
        approveBtn.setOnAction(e -> handleStatusUpdate("APPROVED"));

        Button rejectBtn = new Button("✕ Reject Request");
        rejectBtn.setStyle(StyleConstants.STYLE_DANGER_BTN);
        rejectBtn.setVisible(role != Role.STUDENT);
        rejectBtn.setOnAction(e -> handleStatusUpdate("REJECTED"));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        toolbar.getChildren().addAll(new Label("Filter:"), statusFilter, spacer);
        if (role == Role.STUDENT) {
            toolbar.getChildren().add(requestBtn);
        } else {
            toolbar.getChildren().addAll(approveBtn, rejectBtn);
        }

        // Table
        table.setItems(visitorList);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<Visitor, Integer> colId = new TableColumn<>("Request #");
        colId.setCellValueFactory(new PropertyValueFactory<>("visitorId"));
        colId.setMaxWidth(80);

        TableColumn<Visitor, String> colStudent = new TableColumn<>("Host Student");
        colStudent.setCellValueFactory(new PropertyValueFactory<>("studentName"));

        TableColumn<Visitor, String> colVisitor = new TableColumn<>("Visitor Name");
        colVisitor.setCellValueFactory(new PropertyValueFactory<>("visitorName"));

        TableColumn<Visitor, String> colRel = new TableColumn<>("Relationship");
        colRel.setCellValueFactory(new PropertyValueFactory<>("relationship"));

        TableColumn<Visitor, String> colPhone = new TableColumn<>("Phone");
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));

        TableColumn<Visitor, String> colDate = new TableColumn<>("Visit Date");
        colDate.setCellValueFactory(new PropertyValueFactory<>("visitDate"));

        TableColumn<Visitor, String> colTime = new TableColumn<>("Expected Time");
        colTime.setCellValueFactory(new PropertyValueFactory<>("visitTime"));

        TableColumn<Visitor, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        table.getColumns().addAll(colId, colStudent, colVisitor, colRel, colPhone, colDate, colTime, colStatus);

        refreshTable();
        getChildren().addAll(titleBox, toolbar, table);
    }

    private void refreshTable() {
        try {
            Role role = SessionManager.getCurrentRole();
            Student student = SessionManager.getCurrentStudent();
            Warden warden = SessionManager.getCurrentWarden();

            List<Visitor> list;
            if (role == Role.STUDENT && student != null) {
                list = visitorController.getVisitorsByStudent(student.getStudentId());
            } else if (role == Role.WARDEN && warden != null && warden.getAssignedBlock() != null) {
                list = visitorController.getVisitorsByBlock(warden.getAssignedBlock());
            } else {
                list = visitorController.getAllVisitors();
            }
            visitorList.setAll(list);
        } catch (Exception e) {
            AlertUtil.showError("Error", "Failed to load visitor requests: " + e.getMessage());
        }
    }

    private void applyFilter() {
        String filter = statusFilter.getValue();
        refreshTable();
        if (filter != null && !"All Statuses".equalsIgnoreCase(filter)) {
            visitorList.removeIf(v -> !filter.equalsIgnoreCase(v.getStatus()));
        }
    }

    private void handleStatusUpdate(String newStatus) {
        Visitor selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showWarning("No Selection", "Please select a visitor request.");
            return;
        }
        try {
            if ("APPROVED".equals(newStatus)) {
                visitorController.approveVisitor(selected.getVisitorId());
            } else {
                visitorController.rejectVisitor(selected.getVisitorId());
            }
            applyFilter();
            AlertUtil.showInfo("Request Updated", "Visitor request marked as " + newStatus);
        } catch (Exception e) {
            AlertUtil.showError("Update Error", e.getMessage());
        }
    }

    private void showRequestVisitorDialog() {
        Student s = SessionManager.getCurrentStudent();
        if (s == null) return;

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Request Visitor Pass");
        dialog.setHeaderText("Apply for campus visitor entry permission");

        ButtonType submitBtn = new ButtonType("Submit Request", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(submitBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(12);
        grid.setPadding(new Insets(20, 30, 10, 10));

        TextField nameField = new TextField();
        TextField relField = new TextField();
        relField.setPromptText("e.g. Father, Mother, Sibling, Friend");
        TextField phoneField = new TextField();
        DatePicker datePicker = new DatePicker(LocalDate.now());
        TextField timeField = new TextField("11:00 AM");

        grid.add(new Label("Visitor Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Relationship:"), 0, 1);
        grid.add(relField, 1, 1);
        grid.add(new Label("Contact Phone:"), 0, 2);
        grid.add(phoneField, 1, 2);
        grid.add(new Label("Visit Date:"), 0, 3);
        grid.add(datePicker, 1, 3);
        grid.add(new Label("Expected Time:"), 0, 4);
        grid.add(timeField, 1, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == submitBtn) {
                try {
                    String d = datePicker.getValue() != null ? datePicker.getValue().toString() : LocalDate.now().toString();
                    Visitor v = new Visitor(s.getStudentId(), nameField.getText().trim(), relField.getText().trim(),
                            phoneField.getText().trim(), d, timeField.getText().trim());
                    visitorController.requestVisitor(v);
                    refreshTable();
                    AlertUtil.showInfo("Request Submitted", "Visitor pass application submitted for Warden approval.");
                } catch (Exception ex) {
                    AlertUtil.showError("Request Failed", ex.getMessage());
                }
            }
            return null;
        });

        dialog.showAndWait();
    }
}
