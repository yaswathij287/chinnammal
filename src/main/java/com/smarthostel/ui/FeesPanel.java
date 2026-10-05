package com.smarthostel.ui;

import com.smarthostel.controller.FeeController;
import com.smarthostel.controller.StudentController;
import com.smarthostel.model.Fee;
import com.smarthostel.model.Role;
import com.smarthostel.model.Student;
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
 * Hostel fee management panel for tracking payments, balances, and recording installments.
 */
public class FeesPanel extends VBox {

    private final FeeController feeController = new FeeController();
    private final StudentController studentController = new StudentController();

    private final TableView<Fee> table = new TableView<>();
    private final ObservableList<Fee> feeList = FXCollections.observableArrayList();
    private final TextField searchField = new TextField();

    public FeesPanel() {
        setSpacing(18);
        setPadding(new Insets(24));
        setStyle("-fx-background-color: " + StyleConstants.COLOR_BG_APP + ";");

        // Header
        Label title = new Label("Fee Management");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: " + StyleConstants.COLOR_TEXT_MAIN + ";");

        Role role = SessionManager.getCurrentRole();
        Student student = SessionManager.getCurrentStudent();

        Label subtitle = new Label(role == Role.STUDENT
                ? "Track your hostel fee dues, installments, and payment receipts."
                : "Manage student fee collection, pending dues, and payment transactions.");
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: " + StyleConstants.COLOR_TEXT_MUTED + ";");

        VBox titleBox = new VBox(4, title, subtitle);

        // Summary Cards
        HBox statsBox = new HBox(16);
        if (role == Role.ADMIN) {
            try {
                double collected = feeController.getTotalCollectedFees();
                double pending = feeController.getTotalPendingFees();
                statsBox.getChildren().addAll(
                        createStatCard("💵 Total Collected", "₹ " + String.format("%,.2f", collected), StyleConstants.COLOR_SUCCESS),
                        createStatCard("⏳ Total Pending Dues", "₹ " + String.format("%,.2f", pending), StyleConstants.COLOR_DANGER)
                );
            } catch (Exception ignored) {}
        }

        // Toolbar
        HBox toolbar = new HBox(12);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        searchField.setPromptText("🔍 Search student...");
        searchField.setStyle(StyleConstants.STYLE_TEXT_FIELD);
        searchField.setPrefWidth(260);
        searchField.textProperty().addListener((obs, old, val) -> filterFees(val));

        Button payBtn = new Button(role == Role.STUDENT ? "💳 Pay Dues" : "Record Payment");
        payBtn.setStyle(StyleConstants.STYLE_PRIMARY_BTN);
        payBtn.setOnAction(e -> {
            Fee selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                AlertUtil.showWarning("No Selection", "Please select a fee record to record payment.");
                return;
            }
            if (selected.getBalance() <= 0) {
                AlertUtil.showInfo("Fully Paid", "This fee is already completely cleared.");
                return;
            }
            showPaymentDialog(selected);
        });

        Button addFeeBtn = new Button("+ New Fee Invoice");
        addFeeBtn.setStyle(StyleConstants.STYLE_SECONDARY_BTN);
        addFeeBtn.setVisible(role == Role.ADMIN);
        addFeeBtn.setOnAction(e -> showAddFeeDialog());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        if (role != Role.STUDENT) toolbar.getChildren().add(searchField);
        toolbar.getChildren().addAll(spacer);
        if (role == Role.ADMIN) toolbar.getChildren().add(addFeeBtn);
        toolbar.getChildren().add(payBtn);

        // Table
        table.setItems(feeList);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<Fee, Integer> colId = new TableColumn<>("Invoice #");
        colId.setCellValueFactory(new PropertyValueFactory<>("feeId"));
        colId.setMaxWidth(80);

        TableColumn<Fee, String> colStudent = new TableColumn<>("Student");
        colStudent.setCellValueFactory(new PropertyValueFactory<>("studentName"));

        TableColumn<Fee, Double> colTotal = new TableColumn<>("Total Amount (₹)");
        colTotal.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));

        TableColumn<Fee, Double> colPaid = new TableColumn<>("Paid (₹)");
        colPaid.setCellValueFactory(new PropertyValueFactory<>("paidAmount"));

        TableColumn<Fee, Double> colBalance = new TableColumn<>("Balance (₹)");
        colBalance.setCellValueFactory(new PropertyValueFactory<>("balance"));

        TableColumn<Fee, String> colDueDate = new TableColumn<>("Due Date");
        colDueDate.setCellValueFactory(new PropertyValueFactory<>("dueDate"));

        TableColumn<Fee, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        table.getColumns().addAll(colId, colStudent, colTotal, colPaid, colBalance, colDueDate, colStatus);

        refreshTable();
        if (role == Role.ADMIN && !statsBox.getChildren().isEmpty()) {
            getChildren().addAll(titleBox, statsBox, toolbar, table);
        } else {
            getChildren().addAll(titleBox, toolbar, table);
        }
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
            if (role == Role.STUDENT && student != null) {
                feeList.setAll(feeController.getFeesByStudent(student.getStudentId()));
            } else {
                feeList.setAll(feeController.getAllFees());
            }
        } catch (Exception e) {
            AlertUtil.showError("Error", "Failed to load fee records: " + e.getMessage());
        }
    }

    private void filterFees(String query) {
        if (query == null || query.trim().isEmpty()) {
            refreshTable();
            return;
        }
        try {
            List<Fee> all = feeController.getAllFees();
            String q = query.trim().toLowerCase();
            all.removeIf(f -> f.getStudentName() == null || !f.getStudentName().toLowerCase().contains(q));
            feeList.setAll(all);
        } catch (Exception ignored) {}
    }

    private void showPaymentDialog(Fee fee) {
        TextInputDialog dialog = new TextInputDialog(String.valueOf(fee.getBalance()));
        dialog.setTitle("Record Fee Payment");
        dialog.setHeaderText("Pay towards Invoice #" + fee.getFeeId() + " (" + fee.getStudentName() + ")");
        dialog.setContentText("Enter payment amount (Balance: ₹" + fee.getBalance() + "):");

        dialog.showAndWait().ifPresent(input -> {
            try {
                double amt = Double.parseDouble(input.trim());
                feeController.recordPayment(fee.getFeeId(), amt);
                refreshTable();
                AlertUtil.showInfo("Payment Recorded", "Successfully processed payment of ₹" + amt);
            } catch (Exception ex) {
                AlertUtil.showError("Payment Failed", ex.getMessage());
            }
        });
    }

    private void showAddFeeDialog() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Generate Fee Invoice");
        dialog.setHeaderText("Issue hostel semester fee invoice to a student");

        ButtonType saveBtn = new ButtonType("Generate", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(12);
        grid.setPadding(new Insets(20, 30, 10, 10));

        ComboBox<Student> studentCombo = new ComboBox<>();
        try {
            studentCombo.getItems().addAll(studentController.getAllStudents());
        } catch (Exception ignored) {}

        TextField totalAmountField = new TextField("60000");
        TextField paidAmountField = new TextField("0");
        DatePicker dueDatePicker = new DatePicker();

        grid.add(new Label("Student:"), 0, 0);
        grid.add(studentCombo, 1, 0);
        grid.add(new Label("Total Fee (₹):"), 0, 1);
        grid.add(totalAmountField, 1, 1);
        grid.add(new Label("Initial Paid (₹):"), 0, 2);
        grid.add(paidAmountField, 1, 2);
        grid.add(new Label("Due Date:"), 0, 3);
        grid.add(dueDatePicker, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == saveBtn) {
                Student s = studentCombo.getValue();
                if (s == null) {
                    AlertUtil.showWarning("Missing Student", "Please select a student.");
                    return null;
                }
                try {
                    double tot = Double.parseDouble(totalAmountField.getText().trim());
                    double paid = Double.parseDouble(paidAmountField.getText().trim());
                    String dueDate = dueDatePicker.getValue() != null ? dueDatePicker.getValue().toString() : "2026-11-30";

                    Fee f = new Fee(0, s.getStudentId(), tot, paid, tot - paid, dueDate, (tot - paid <= 0 ? "PAID" : "PENDING"));
                    feeController.addFee(f);
                    refreshTable();
                    AlertUtil.showInfo("Invoice Generated", "Fee invoice created successfully.");
                } catch (Exception ex) {
                    AlertUtil.showError("Generation Failed", ex.getMessage());
                }
            }
            return null;
        });

        dialog.showAndWait();
    }
}
