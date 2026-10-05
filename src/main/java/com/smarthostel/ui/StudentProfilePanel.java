package com.smarthostel.ui;

import com.smarthostel.controller.StudentController;
import com.smarthostel.model.Student;
import com.smarthostel.util.AlertUtil;
import com.smarthostel.util.SessionManager;
import com.smarthostel.util.StyleConstants;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Student's personal profile view and contact information management panel.
 */
public class StudentProfilePanel extends ScrollPane {

    private final StudentController studentController = new StudentController();

    public StudentProfilePanel() {
        setFitToWidth(true);
        setStyle("-fx-background: " + StyleConstants.COLOR_BG_APP + "; -fx-background-color: transparent;");

        VBox content = new VBox(24);
        content.setPadding(new Insets(24));
        content.setStyle("-fx-background-color: " + StyleConstants.COLOR_BG_APP + ";");

        Student s = SessionManager.getCurrentStudent();

        // Header
        Label title = new Label("Resident Profile & Records");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: " + StyleConstants.COLOR_TEXT_MAIN + ";");

        Label subtitle = new Label("Review your registered academic, residential, and emergency contact details.");
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: " + StyleConstants.COLOR_TEXT_MUTED + ";");

        VBox titleBox = new VBox(4, title, subtitle);

        if (s == null) {
            content.getChildren().addAll(titleBox, new Label("No student profile linked to this session."));
            setContent(content);
            return;
        }

        // Profile Card
        VBox profileCard = new VBox(16);
        profileCard.setStyle(StyleConstants.STYLE_CARD);
        profileCard.setPadding(new Insets(24));
        profileCard.setMaxWidth(600);

        Label profileHeader = new Label("🎓 Academic & Personal Information");
        profileHeader.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: " + StyleConstants.COLOR_PRIMARY_DARK + ";");

        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(14);

        TextField nameField = new TextField(s.getName());
        nameField.setEditable(false);
        nameField.setStyle(StyleConstants.STYLE_TEXT_FIELD);

        TextField deptField = new TextField(s.getDepartment());
        deptField.setEditable(false);
        deptField.setStyle(StyleConstants.STYLE_TEXT_FIELD);

        TextField yearField = new TextField("Year " + s.getYear());
        yearField.setEditable(false);
        yearField.setStyle(StyleConstants.STYLE_TEXT_FIELD);

        TextField phoneField = new TextField(s.getPhone() != null ? s.getPhone() : "");
        phoneField.setStyle(StyleConstants.STYLE_TEXT_FIELD);

        TextField emailField = new TextField(s.getEmail() != null ? s.getEmail() : "");
        emailField.setStyle(StyleConstants.STYLE_TEXT_FIELD);

        TextField roomField = new TextField(s.getRoomNumber() != null ? s.getRoomNumber() + " (" + s.getBedNumber() + ")" : "Unassigned");
        roomField.setEditable(false);
        roomField.setStyle(StyleConstants.STYLE_TEXT_FIELD);

        grid.add(new Label("Full Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Department:"), 0, 1);
        grid.add(deptField, 1, 1);
        grid.add(new Label("Academic Year:"), 0, 2);
        grid.add(yearField, 1, 2);
        grid.add(new Label("Contact Phone:"), 0, 3);
        grid.add(phoneField, 1, 3);
        grid.add(new Label("Email Address:"), 0, 4);
        grid.add(emailField, 1, 4);
        grid.add(new Label("Assigned Room:"), 0, 5);
        grid.add(roomField, 1, 5);

        Button updateBtn = new Button("💾 Update Contact Details");
        updateBtn.setStyle(StyleConstants.STYLE_PRIMARY_BTN);
        updateBtn.setOnAction(e -> {
            try {
                s.setPhone(phoneField.getText().trim());
                s.setEmail(emailField.getText().trim());
                studentController.updateStudent(s);
                AlertUtil.showInfo("Saved", "Contact information updated successfully.");
            } catch (Exception ex) {
                AlertUtil.showError("Save Error", ex.getMessage());
            }
        });

        HBox btnBox = new HBox(updateBtn);
        btnBox.setAlignment(Pos.CENTER_RIGHT);

        profileCard.getChildren().addAll(profileHeader, grid, btnBox);

        content.getChildren().addAll(titleBox, profileCard);
        setContent(content);
    }
}
