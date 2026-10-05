package com.smarthostel.ui;

import com.smarthostel.controller.RoomController;
import com.smarthostel.controller.WardenController;
import com.smarthostel.model.Block;
import com.smarthostel.model.Warden;
import com.smarthostel.util.AlertUtil;
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
 * Warden administration panel for adding, updating, assigning blocks, and managing wardens.
 */
public class WardensPanel extends VBox {

    private final WardenController wardenController = new WardenController();
    private final RoomController roomController = new RoomController();

    private final TableView<Warden> table = new TableView<>();
    private final ObservableList<Warden> wardenList = FXCollections.observableArrayList();

    public WardensPanel() {
        setSpacing(18);
        setPadding(new Insets(24));
        setStyle("-fx-background-color: " + StyleConstants.COLOR_BG_APP + ";");

        // Header
        Label title = new Label("Warden Administration");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: " + StyleConstants.COLOR_TEXT_MAIN + ";");

        Label subtitle = new Label("Supervise hostel wardens and their assigned hostel blocks.");
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: " + StyleConstants.COLOR_TEXT_MUTED + ";");

        VBox titleBox = new VBox(4, title, subtitle);

        // Toolbar
        HBox toolbar = new HBox(12);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        Button addBtn = new Button("+ Add Warden");
        addBtn.setStyle(StyleConstants.STYLE_PRIMARY_BTN);
        addBtn.setOnAction(e -> showWardenDialog(null));

        Button editBtn = new Button("Edit Selected");
        editBtn.setStyle(StyleConstants.STYLE_SECONDARY_BTN);
        editBtn.setOnAction(e -> {
            Warden selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                AlertUtil.showWarning("No Selection", "Please select a warden to edit.");
                return;
            }
            showWardenDialog(selected);
        });

        Button deleteBtn = new Button("Delete");
        deleteBtn.setStyle(StyleConstants.STYLE_DANGER_BTN);
        deleteBtn.setOnAction(e -> {
            Warden selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                AlertUtil.showWarning("No Selection", "Please select a warden to delete.");
                return;
            }
            if (AlertUtil.showConfirmation("Confirm Delete", "Delete warden " + selected.getName() + "?")) {
                try {
                    wardenController.deleteWarden(selected.getWardenId());
                    refreshTable();
                    AlertUtil.showInfo("Success", "Warden deleted successfully.");
                } catch (Exception ex) {
                    AlertUtil.showError("Error", ex.getMessage());
                }
            }
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        toolbar.getChildren().addAll(spacer, addBtn, editBtn, deleteBtn);

        // Table
        table.setItems(wardenList);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<Warden, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("wardenId"));
        colId.setMaxWidth(60);

        TableColumn<Warden, String> colName = new TableColumn<>("Name");
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Warden, String> colPhone = new TableColumn<>("Phone");
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));

        TableColumn<Warden, String> colEmail = new TableColumn<>("Email");
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));

        TableColumn<Warden, String> colBlock = new TableColumn<>("Assigned Block");
        colBlock.setCellValueFactory(new PropertyValueFactory<>("blockName"));

        table.getColumns().addAll(colId, colName, colPhone, colEmail, colBlock);

        refreshTable();
        getChildren().addAll(titleBox, toolbar, table);
    }

    private void refreshTable() {
        try {
            wardenList.setAll(wardenController.getAllWardens());
        } catch (Exception e) {
            AlertUtil.showError("Error", "Failed to load wardens: " + e.getMessage());
        }
    }

    private void showWardenDialog(Warden warden) {
        boolean isEdit = (warden != null);
        Dialog<Warden> dialog = new Dialog<>();
        dialog.setTitle(isEdit ? "Edit Warden" : "Add New Warden");
        dialog.setHeaderText(isEdit ? "Update warden profile and block assignment" : "Register a new warden");

        ButtonType saveBtnType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtnType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(12);
        grid.setPadding(new Insets(20, 30, 10, 10));

        TextField nameField = new TextField(isEdit ? warden.getName() : "");
        TextField phoneField = new TextField(isEdit ? warden.getPhone() : "");
        TextField emailField = new TextField(isEdit ? warden.getEmail() : "");

        ComboBox<Block> blockCombo = new ComboBox<>();
        try {
            List<Block> blocks = roomController.getAllBlocks();
            blockCombo.getItems().addAll(blocks);
            if (isEdit && warden.getAssignedBlock() != null) {
                for (Block b : blocks) {
                    if (b.getBlockId() == warden.getAssignedBlock()) {
                        blockCombo.setValue(b);
                        break;
                    }
                }
            }
        } catch (Exception ignored) {}

        grid.add(new Label("Warden Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Phone:"), 0, 1);
        grid.add(phoneField, 1, 1);
        grid.add(new Label("Email:"), 0, 2);
        grid.add(emailField, 1, 2);
        grid.add(new Label("Assign Block:"), 0, 3);
        grid.add(blockCombo, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveBtnType) {
                try {
                    Block selBlock = blockCombo.getValue();
                    Integer blockId = selBlock != null ? selBlock.getBlockId() : null;

                    Warden w = isEdit ? warden : new Warden();
                    w.setName(nameField.getText().trim());
                    w.setPhone(phoneField.getText().trim());
                    w.setEmail(emailField.getText().trim());
                    w.setAssignedBlock(blockId);

                    if (isEdit) {
                        wardenController.updateWarden(w);
                    } else {
                        wardenController.addWarden(w);
                    }
                    refreshTable();
                    return w;
                } catch (Exception ex) {
                    AlertUtil.showError("Save Error", ex.getMessage());
                }
            }
            return null;
        });

        dialog.showAndWait();
    }
}
