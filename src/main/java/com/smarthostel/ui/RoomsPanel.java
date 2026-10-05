package com.smarthostel.ui;

import com.smarthostel.controller.RoomController;
import com.smarthostel.controller.StudentController;
import com.smarthostel.model.Block;
import com.smarthostel.model.Role;
import com.smarthostel.model.Room;
import com.smarthostel.model.Student;
import com.smarthostel.model.Warden;
import com.smarthostel.util.AlertUtil;
import com.smarthostel.util.SessionManager;
import com.smarthostel.util.StyleConstants;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

import java.util.List;

/**
 * Blocks & Rooms management panel showing room numbers, occupancy, capacities, and assignment actions.
 */
public class RoomsPanel extends VBox {

    private final RoomController roomController = new RoomController();
    private final StudentController studentController = new StudentController();

    private final TableView<Room> table = new TableView<>();
    private final ObservableList<Room> roomList = FXCollections.observableArrayList();
    private final ComboBox<Block> blockFilter = new ComboBox<>();

    public RoomsPanel() {
        setSpacing(18);
        setPadding(new Insets(24));
        setStyle("-fx-background-color: " + StyleConstants.COLOR_BG_APP + ";");

        // Header
        Label title = new Label("Blocks & Rooms Management");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: " + StyleConstants.COLOR_TEXT_MAIN + ";");

        Role role = SessionManager.getCurrentRole();
        Warden warden = SessionManager.getCurrentWarden();
        boolean isWarden = (role == Role.WARDEN && warden != null && warden.getAssignedBlock() != null);

        Label subtitle = new Label(isWarden
                ? "Rooms in " + (warden.getBlockName() != null ? warden.getBlockName() : "Block #" + warden.getAssignedBlock())
                : "Manage hostel accommodation, bed capacities, and room allocations.");
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: " + StyleConstants.COLOR_TEXT_MUTED + ";");

        VBox titleBox = new VBox(4, title, subtitle);

        // Toolbar
        HBox toolbar = new HBox(12);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        blockFilter.setPromptText("Filter by Block");
        blockFilter.setStyle(StyleConstants.STYLE_TEXT_FIELD);
        blockFilter.setOnAction(e -> applyFilter());

        Button addRoomBtn = new Button("+ Add Room");
        addRoomBtn.setStyle(StyleConstants.STYLE_PRIMARY_BTN);
        addRoomBtn.setOnAction(e -> showRoomDialog(null));

        Button editRoomBtn = new Button("Edit Room");
        editRoomBtn.setStyle(StyleConstants.STYLE_SECONDARY_BTN);
        editRoomBtn.setOnAction(e -> {
            Room selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                AlertUtil.showWarning("No Selection", "Please select a room to edit.");
                return;
            }
            showRoomDialog(selected);
        });

        Button assignStudentBtn = new Button("Assign Student");
        assignStudentBtn.setStyle(StyleConstants.STYLE_PRIMARY_BTN);
        assignStudentBtn.setOnAction(e -> {
            Room selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                AlertUtil.showWarning("No Selection", "Please select a room to assign a student.");
                return;
            }
            if (selected.isFull()) {
                AlertUtil.showWarning("Room Full", "Selected room " + selected.getRoomNumber() + " is already at maximum capacity!");
                return;
            }
            showAssignStudentDialog(selected);
        });

        Button deleteRoomBtn = new Button("Delete Room");
        deleteRoomBtn.setStyle(StyleConstants.STYLE_DANGER_BTN);
        deleteRoomBtn.setVisible(role == Role.ADMIN);
        deleteRoomBtn.setOnAction(e -> {
            Room selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                AlertUtil.showWarning("No Selection", "Please select a room to delete.");
                return;
            }
            if (AlertUtil.showConfirmation("Confirm Delete", "Delete room " + selected.getRoomNumber() + "?")) {
                try {
                    roomController.deleteRoom(selected.getRoomId());
                    refreshTable();
                    AlertUtil.showInfo("Success", "Room deleted successfully.");
                } catch (Exception ex) {
                    AlertUtil.showError("Error", ex.getMessage());
                }
            }
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        if (!isWarden) {
            toolbar.getChildren().addAll(new Label("Block:"), blockFilter);
        }
        toolbar.getChildren().addAll(spacer);
        if (role == Role.ADMIN) toolbar.getChildren().add(addRoomBtn);
        toolbar.getChildren().addAll(editRoomBtn, assignStudentBtn);
        if (role == Role.ADMIN) toolbar.getChildren().add(deleteRoomBtn);

        // Table
        table.setItems(roomList);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<Room, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("roomId"));
        colId.setMaxWidth(60);

        TableColumn<Room, String> colNumber = new TableColumn<>("Room No.");
        colNumber.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));

        TableColumn<Room, String> colBlock = new TableColumn<>("Block");
        colBlock.setCellValueFactory(new PropertyValueFactory<>("blockName"));

        TableColumn<Room, Integer> colCapacity = new TableColumn<>("Capacity");
        colCapacity.setCellValueFactory(new PropertyValueFactory<>("capacity"));

        TableColumn<Room, Integer> colOccupied = new TableColumn<>("Occupied");
        colOccupied.setCellValueFactory(new PropertyValueFactory<>("occupied"));

        TableColumn<Room, String> colAvailable = new TableColumn<>("Available Beds");
        colAvailable.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getAvailableBeds())));

        TableColumn<Room, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        table.getColumns().addAll(colId, colNumber, colBlock, colCapacity, colOccupied, colAvailable, colStatus);

        loadBlocks();
        refreshTable();
        getChildren().addAll(titleBox, toolbar, table);
    }

    private void loadBlocks() {
        try {
            List<Block> blocks = roomController.getAllBlocks();
            blockFilter.getItems().clear();
            blockFilter.getItems().add(null); // Option for "All"
            blockFilter.getItems().addAll(blocks);
        } catch (Exception ignored) {}
    }

    private void applyFilter() {
        Block selected = blockFilter.getValue();
        if (selected == null) {
            refreshTable();
        } else {
            try {
                roomList.setAll(roomController.getRoomsByBlock(selected.getBlockId()));
            } catch (Exception e) {
                AlertUtil.showError("Error", e.getMessage());
            }
        }
    }

    private void refreshTable() {
        try {
            Role role = SessionManager.getCurrentRole();
            Warden warden = SessionManager.getCurrentWarden();
            if (role == Role.WARDEN && warden != null && warden.getAssignedBlock() != null) {
                roomList.setAll(roomController.getRoomsByBlock(warden.getAssignedBlock()));
            } else {
                roomList.setAll(roomController.getAllRooms());
            }
        } catch (Exception e) {
            AlertUtil.showError("Error", "Failed to load rooms: " + e.getMessage());
        }
    }

    private void showRoomDialog(Room room) {
        boolean isEdit = (room != null);
        Dialog<Room> dialog = new Dialog<>();
        dialog.setTitle(isEdit ? "Edit Room" : "Add Room");
        dialog.setHeaderText(isEdit ? "Update room capacity and status" : "Create a new hostel room");

        ButtonType saveBtnType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtnType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(12);
        grid.setPadding(new Insets(20, 30, 10, 10));

        TextField roomNumField = new TextField(isEdit ? room.getRoomNumber() : "");
        TextField capacityField = new TextField(isEdit ? String.valueOf(room.getCapacity()) : "2");

        ComboBox<Block> blockCombo = new ComboBox<>();
        try {
            List<Block> blocks = roomController.getAllBlocks();
            blockCombo.getItems().addAll(blocks);
            if (isEdit) {
                for (Block b : blocks) {
                    if (b.getBlockId() == room.getBlockId()) {
                        blockCombo.setValue(b);
                        break;
                    }
                }
            } else if (!blocks.isEmpty()) {
                blockCombo.setValue(blocks.get(0));
            }
        } catch (Exception ignored) {}

        grid.add(new Label("Room Number:"), 0, 0);
        grid.add(roomNumField, 1, 0);
        grid.add(new Label("Block:"), 0, 1);
        grid.add(blockCombo, 1, 1);
        grid.add(new Label("Bed Capacity:"), 0, 2);
        grid.add(capacityField, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveBtnType) {
                try {
                    int cap = Integer.parseInt(capacityField.getText().trim());
                    Block b = blockCombo.getValue();
                    if (b == null) throw new IllegalArgumentException("Please select a block.");

                    Room r = isEdit ? room : new Room();
                    r.setRoomNumber(roomNumField.getText().trim());
                    r.setBlockId(b.getBlockId());
                    r.setCapacity(cap);

                    if (isEdit) {
                        roomController.updateRoom(r);
                    } else {
                        roomController.addRoom(r);
                    }
                    refreshTable();
                    return r;
                } catch (Exception ex) {
                    AlertUtil.showError("Save Error", ex.getMessage());
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    private void showAssignStudentDialog(Room room) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Assign Student to Room " + room.getRoomNumber());
        dialog.setHeaderText("Select an unassigned student and assign a bed number");

        ButtonType assignBtnType = new ButtonType("Assign", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(assignBtnType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(12);
        grid.setPadding(new Insets(20, 30, 10, 10));

        ComboBox<Student> studentCombo = new ComboBox<>();
        try {
            List<Student> students = studentController.getAllStudents();
            for (Student s : students) {
                if (s.getRoomId() == null) {
                    studentCombo.getItems().add(s);
                }
            }
        } catch (Exception ignored) {}

        TextField bedNumField = new TextField("Bed " + (room.getOccupied() + 1));

        grid.add(new Label("Student:"), 0, 0);
        grid.add(studentCombo, 1, 0);
        grid.add(new Label("Bed Number:"), 0, 1);
        grid.add(bedNumField, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == assignBtnType) {
                Student selStudent = studentCombo.getValue();
                if (selStudent == null) {
                    AlertUtil.showWarning("Missing Selection", "Please select a student.");
                    return null;
                }
                try {
                    selStudent.setRoomId(room.getRoomId());
                    selStudent.setBedNumber(bedNumField.getText().trim());
                    studentController.updateStudent(selStudent);
                    refreshTable();
                    AlertUtil.showInfo("Success", selStudent.getName() + " assigned to room " + room.getRoomNumber());
                } catch (Exception ex) {
                    AlertUtil.showError("Assignment Error", ex.getMessage());
                }
            }
            return null;
        });

        dialog.showAndWait();
    }
}
