package com.smarthostel.ui;

import com.smarthostel.controller.RoomController;
import com.smarthostel.controller.StudentController;
import com.smarthostel.model.Role;
import com.smarthostel.model.Room;
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
 * Student management panel supporting Add, Search, Update, Delete, Room Assignment, and Vacate.
 */
public class StudentsPanel extends VBox {

    private final StudentController studentController = new StudentController();
    private final RoomController roomController = new RoomController();

    private final TableView<Student> table = new TableView<>();
    private final ObservableList<Student> studentList = FXCollections.observableArrayList();
    private final TextField searchField = new TextField();

    public StudentsPanel() {
        setSpacing(18);
        setPadding(new Insets(24));
        setStyle("-fx-background-color: " + StyleConstants.COLOR_BG_APP + ";");

        // Header Title
        Label title = new Label("Student Management");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: " + StyleConstants.COLOR_TEXT_MAIN + ";");

        Role role = SessionManager.getCurrentRole();
        Warden warden = SessionManager.getCurrentWarden();
        boolean isWarden = (role == Role.WARDEN && warden != null && warden.getAssignedBlock() != null);

        Label subtitle = new Label(isWarden
                ? "Viewing students assigned to " + (warden.getBlockName() != null ? warden.getBlockName() : "Block #" + warden.getAssignedBlock())
                : "Manage all student residents, room assignments, and personal records.");
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: " + StyleConstants.COLOR_TEXT_MUTED + ";");

        VBox titleBox = new VBox(4, title, subtitle);

        // Action Toolbar
        HBox toolbar = new HBox(12);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        searchField.setPromptText("🔍 Search student by name, department, phone, email...");
        searchField.setStyle(StyleConstants.STYLE_TEXT_FIELD);
        searchField.setPrefWidth(380);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterStudents(newVal));

        Button addBtn = new Button("+ Add Student");
        addBtn.setStyle(StyleConstants.STYLE_PRIMARY_BTN);
        addBtn.setOnAction(e -> showStudentDialog(null));

        Button editBtn = new Button("Edit Selected");
        editBtn.setStyle(StyleConstants.STYLE_SECONDARY_BTN);
        editBtn.setOnAction(e -> {
            Student selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                AlertUtil.showWarning("No Selection", "Please select a student to edit.");
                return;
            }
            showStudentDialog(selected);
        });

        Button vacateBtn = new Button("Vacate Room");
        vacateBtn.setStyle(StyleConstants.STYLE_SECONDARY_BTN);
        vacateBtn.setOnAction(e -> {
            Student selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                AlertUtil.showWarning("No Selection", "Please select a student to vacate.");
                return;
            }
            if (selected.getRoomId() == null) {
                AlertUtil.showInfo("Already Vacated", selected.getName() + " does not have a room assigned.");
                return;
            }
            if (AlertUtil.showConfirmation("Confirm Vacate", "Vacate " + selected.getName() + " from room " + selected.getRoomNumber() + "?")) {
                try {
                    studentController.vacateStudent(selected.getStudentId());
                    refreshTable();
                    AlertUtil.showInfo("Success", "Student vacated from room.");
                } catch (Exception ex) {
                    AlertUtil.showError("Error", ex.getMessage());
                }
            }
        });

        Button deleteBtn = new Button("Delete");
        deleteBtn.setStyle(StyleConstants.STYLE_DANGER_BTN);
        // Only Admin can delete
        deleteBtn.setVisible(role == Role.ADMIN);
        deleteBtn.setOnAction(e -> {
            Student selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                AlertUtil.showWarning("No Selection", "Please select a student to delete.");
                return;
            }
            if (AlertUtil.showConfirmation("Confirm Delete", "Are you sure you want to delete student: " + selected.getName() + "?")) {
                try {
                    studentController.deleteStudent(selected.getStudentId());
                    refreshTable();
                    AlertUtil.showInfo("Success", "Student deleted successfully.");
                } catch (Exception ex) {
                    AlertUtil.showError("Error", ex.getMessage());
                }
            }
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        toolbar.getChildren().addAll(searchField, spacer);
        if (role == Role.ADMIN) toolbar.getChildren().add(addBtn);
        toolbar.getChildren().addAll(editBtn, vacateBtn);
        if (role == Role.ADMIN) toolbar.getChildren().add(deleteBtn);

        // Table Setup
        table.setItems(studentList);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<Student, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("studentId"));
        colId.setMaxWidth(60);

        TableColumn<Student, String> colName = new TableColumn<>("Name");
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Student, String> colDept = new TableColumn<>("Department");
        colDept.setCellValueFactory(new PropertyValueFactory<>("department"));

        TableColumn<Student, Integer> colYear = new TableColumn<>("Year");
        colYear.setCellValueFactory(new PropertyValueFactory<>("year"));
        colYear.setMaxWidth(60);

        TableColumn<Student, String> colPhone = new TableColumn<>("Phone");
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));

        TableColumn<Student, String> colEmail = new TableColumn<>("Email");
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));

        TableColumn<Student, String> colBlock = new TableColumn<>("Block");
        colBlock.setCellValueFactory(new PropertyValueFactory<>("blockName"));

        TableColumn<Student, String> colRoom = new TableColumn<>("Room");
        colRoom.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));

        TableColumn<Student, String> colBed = new TableColumn<>("Bed");
        colBed.setCellValueFactory(new PropertyValueFactory<>("bedNumber"));

        table.getColumns().addAll(colId, colName, colDept, colYear, colPhone, colEmail, colBlock, colRoom, colBed);

        refreshTable();
        getChildren().addAll(titleBox, toolbar, table);
    }

    private void refreshTable() {
        try {
            Role role = SessionManager.getCurrentRole();
            Warden warden = SessionManager.getCurrentWarden();
            List<Student> students;
            if (role == Role.WARDEN && warden != null && warden.getAssignedBlock() != null) {
                students = studentController.getStudentsByBlock(warden.getAssignedBlock());
            } else {
                students = studentController.getAllStudents();
            }
            studentList.setAll(students);
        } catch (Exception e) {
            AlertUtil.showError("Database Error", "Failed to load students: " + e.getMessage());
        }
    }

    private void filterStudents(String query) {
        if (query == null || query.trim().isEmpty()) {
            refreshTable();
            return;
        }
        try {
            List<Student> matched = studentController.searchStudents(query);
            Role role = SessionManager.getCurrentRole();
            Warden warden = SessionManager.getCurrentWarden();
            if (role == Role.WARDEN && warden != null && warden.getAssignedBlock() != null) {
                matched.removeIf(s -> {
                    try {
                        Room r = s.getRoomId() != null ? roomController.getRoomById(s.getRoomId()) : null;
                        return r == null || r.getBlockId() != warden.getAssignedBlock();
                    } catch (Exception ex) {
                        return true;
                    }
                });
            }
            studentList.setAll(matched);
        } catch (Exception e) {
            // fallback
        }
    }

    private void showStudentDialog(Student student) {
        boolean isEdit = (student != null);
        Dialog<Student> dialog = new Dialog<>();
        dialog.setTitle(isEdit ? "Edit Student" : "Add New Student");
        dialog.setHeaderText(isEdit ? "Update resident information" : "Register a new student resident");

        ButtonType saveBtnType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtnType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(12);
        grid.setPadding(new Insets(20, 30, 10, 10));

        TextField nameField = new TextField(isEdit ? student.getName() : "");
        TextField deptField = new TextField(isEdit ? student.getDepartment() : "");
        TextField yearField = new TextField(isEdit ? String.valueOf(student.getYear()) : "1");
        TextField phoneField = new TextField(isEdit ? student.getPhone() : "");
        TextField emailField = new TextField(isEdit ? student.getEmail() : "");
        TextField bedField = new TextField(isEdit && student.getBedNumber() != null ? student.getBedNumber() : "Bed 1");

        ComboBox<Room> roomCombo = new ComboBox<>();
        try {
            List<Room> rooms = roomController.getAllRooms();
            roomCombo.getItems().addAll(rooms);
            if (isEdit && student.getRoomId() != null) {
                for (Room r : rooms) {
                    if (r.getRoomId() == student.getRoomId()) {
                        roomCombo.setValue(r);
                        break;
                    }
                }
            }
        } catch (Exception ignored) {}

        grid.add(new Label("Full Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Department:"), 0, 1);
        grid.add(deptField, 1, 1);
        grid.add(new Label("Year (1-4):"), 0, 2);
        grid.add(yearField, 1, 2);
        grid.add(new Label("Phone:"), 0, 3);
        grid.add(phoneField, 1, 3);
        grid.add(new Label("Email:"), 0, 4);
        grid.add(emailField, 1, 4);
        grid.add(new Label("Assign Room:"), 0, 5);
        grid.add(roomCombo, 1, 5);
        grid.add(new Label("Bed Number:"), 0, 6);
        grid.add(bedField, 1, 6);

        dialog.getDialogPane().setContent(grid);

        Button saveBtn = (Button) dialog.getDialogPane().lookupButton(saveBtnType);
        if (saveBtn != null) {
            saveBtn.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
                try {
                    String name = nameField.getText().trim();
                    String dept = deptField.getText().trim();
                    String yearStr = yearField.getText().trim();
                    String phone = phoneField.getText().trim();
                    String email = emailField.getText().trim();
                    Room selectedRoom = roomCombo.getValue();
                    String bed = bedField.getText().trim();

                    if (yearStr.isEmpty()) {
                        AlertUtil.showWarning("Validation Error", "Academic Year is required (1-4).");
                        event.consume();
                        return;
                    }
                    int yr;
                    try {
                        yr = Integer.parseInt(yearStr);
                    } catch (NumberFormatException nfe) {
                        AlertUtil.showWarning("Validation Error", "Academic Year must be a number (1-4).");
                        event.consume();
                        return;
                    }

                    if (selectedRoom == null) {
                        AlertUtil.showWarning("Validation Error", "Please assign a room.");
                        event.consume();
                        return;
                    }

                    Student s = isEdit ? student : new Student();
                    s.setName(name);
                    s.setDepartment(dept);
                    s.setYear(yr);
                    s.setPhone(phone);
                    s.setEmail(email);
                    s.setRoomId(selectedRoom.getRoomId());
                    s.setBedNumber(bed);

                    if (isEdit) {
                        studentController.updateStudent(s);
                        refreshTable();
                        AlertUtil.showInfo("Student Updated", "Student information updated successfully.");
                    } else {
                        com.smarthostel.model.StudentRegistrationResult regResult = studentController.registerStudent(s);
                        refreshTable();
                        javafx.application.Platform.runLater(() -> showRegistrationSuccessDialog(regResult));
                    }
                } catch (Exception ex) {
                    event.consume();
                    AlertUtil.showError("Registration Error", ex.getMessage());
                }
            });
        }

        dialog.showAndWait();
    }

    private void showRegistrationSuccessDialog(com.smarthostel.model.StudentRegistrationResult result) {
        Dialog<Void> successDialog = new Dialog<>();
        successDialog.setTitle("Student Registered Successfully!");
        successDialog.setHeaderText(null);

        ButtonType copyBtnType = new ButtonType("Copy Credentials", ButtonBar.ButtonData.OTHER);
        ButtonType okBtnType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        successDialog.getDialogPane().getButtonTypes().addAll(copyBtnType, okBtnType);

        VBox content = new VBox(14);
        content.setPadding(new Insets(20, 24, 20, 24));
        content.setStyle("-fx-background-color: white;");

        Label iconAndTitle = new Label("🎉 Student Registered Successfully!");
        iconAndTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: " + StyleConstants.COLOR_PRIMARY_DARK + ";");

        VBox detailsBox = new VBox(10);
        detailsBox.setStyle("-fx-background-color: " + StyleConstants.COLOR_BG_APP + "; -fx-padding: 14; -fx-background-radius: 8; -fx-border-color: " + StyleConstants.COLOR_BORDER + "; -fx-border-radius: 8;");

        Label nameTitle = new Label("Name:");
        nameTitle.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: " + StyleConstants.COLOR_TEXT_MUTED + ";");
        Label nameVal = new Label(result.getStudent().getName());
        nameVal.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: " + StyleConstants.COLOR_TEXT_MAIN + ";");

        Label userTitle = new Label("Username:");
        userTitle.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: " + StyleConstants.COLOR_TEXT_MUTED + ";");
        Label userVal = new Label(result.getUser().getUsername());
        userVal.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: " + StyleConstants.COLOR_PRIMARY_DARK + ";");

        Label passTitle = new Label("Temporary Password:");
        passTitle.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: " + StyleConstants.COLOR_TEXT_MUTED + ";");
        Label passVal = new Label(result.getTemporaryPassword());
        passVal.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #DC2626; -fx-font-family: 'Consolas', monospace;");

        detailsBox.getChildren().addAll(nameTitle, nameVal, userTitle, userVal, passTitle, passVal);

        Label noteLabel = new Label("Please provide these credentials to the student.\nThe student will be prompted to change their password on first login.");
        noteLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: " + StyleConstants.COLOR_TEXT_MUTED + "; -fx-font-style: italic;");
        noteLabel.setWrapText(true);

        Label copyFeedback = new Label();
        copyFeedback.setStyle("-fx-text-fill: " + StyleConstants.COLOR_SUCCESS + "; -fx-font-weight: bold; -fx-font-size: 12px;");
        copyFeedback.setVisible(false);

        content.getChildren().addAll(iconAndTitle, detailsBox, noteLabel, copyFeedback);
        successDialog.getDialogPane().setContent(content);

        Button copyBtn = (Button) successDialog.getDialogPane().lookupButton(copyBtnType);
        if (copyBtn != null) {
            copyBtn.setStyle(StyleConstants.STYLE_SECONDARY_BTN);
            copyBtn.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
                javafx.scene.input.Clipboard clipboard = javafx.scene.input.Clipboard.getSystemClipboard();
                javafx.scene.input.ClipboardContent cc = new javafx.scene.input.ClipboardContent();
                cc.putString("Username: " + result.getUser().getUsername() + "\nTemporary Password: " + result.getTemporaryPassword());
                clipboard.setContent(cc);
                copyFeedback.setText("Credentials copied to clipboard! ✓");
                copyFeedback.setVisible(true);
                copyBtn.setText("Copied! ✓");
                event.consume();
            });
        }

        Button okBtn = (Button) successDialog.getDialogPane().lookupButton(okBtnType);
        if (okBtn != null) {
            okBtn.setStyle(StyleConstants.STYLE_PRIMARY_BTN);
        }

        successDialog.showAndWait();
    }
}
