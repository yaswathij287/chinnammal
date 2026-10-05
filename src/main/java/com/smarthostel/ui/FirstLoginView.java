package com.smarthostel.ui;

import com.smarthostel.controller.AuthController;
import com.smarthostel.exception.DatabaseException;
import com.smarthostel.exception.ValidationException;
import com.smarthostel.model.User;
import com.smarthostel.util.StyleConstants;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Screen presented on first login when an account is flagged with a temporary password.
 * Prompts the student to set a permanent, secure password before accessing the dashboard.
 */
public class FirstLoginView {

    private final Stage stage;
    private final User user;
    private final AuthController authController;

    public FirstLoginView(Stage stage, User user) {
        this.stage = stage;
        this.user = user;
        this.authController = new AuthController();
    }

    public void show() {
        stage.setTitle("Smart Hostel - First Login Password Setup");

        VBox root = new VBox();
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: " + StyleConstants.COLOR_BG_APP + ";");
        root.setPadding(new Insets(30));

        // Card Container
        VBox card = new VBox(18);
        card.setMaxWidth(480);
        card.setAlignment(Pos.CENTER);
        card.setStyle(StyleConstants.STYLE_CARD);
        card.setPadding(new Insets(35, 40, 35, 40));

        // Header
        Label iconLabel = new Label("🔐");
        iconLabel.setStyle("-fx-font-size: 38px;");

        Label titleLabel = new Label("First Login");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: " + StyleConstants.COLOR_BG_SIDEBAR + ";");

        Label subtitleLabel = new Label("You are using a temporary password.\nPlease choose a new permanent password to activate your account.");
        subtitleLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: " + StyleConstants.COLOR_TEXT_MUTED + "; -fx-text-alignment: center;");
        subtitleLabel.setWrapText(true);

        VBox headerBox = new VBox(6, iconLabel, titleLabel, subtitleLabel);
        headerBox.setAlignment(Pos.CENTER);

        // Fields
        Label newPassLabel = new Label("New Password");
        newPassLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: " + StyleConstants.COLOR_TEXT_MAIN + "; -fx-font-size: 12px;");
        PasswordField newPasswordField = new PasswordField();
        newPasswordField.setPromptText("Enter new secure password");
        newPasswordField.setStyle(StyleConstants.STYLE_TEXT_FIELD);

        VBox newPassBox = new VBox(5, newPassLabel, newPasswordField);
        newPassBox.setAlignment(Pos.CENTER_LEFT);

        Label confirmPassLabel = new Label("Confirm Password");
        confirmPassLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: " + StyleConstants.COLOR_TEXT_MAIN + "; -fx-font-size: 12px;");
        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Re-enter new password");
        confirmPasswordField.setStyle(StyleConstants.STYLE_TEXT_FIELD);

        VBox confirmPassBox = new VBox(5, confirmPassLabel, confirmPasswordField);
        confirmPassBox.setAlignment(Pos.CENTER_LEFT);

        // Password requirements hint box
        VBox hintsBox = new VBox(4);
        hintsBox.setStyle("-fx-background-color: " + StyleConstants.COLOR_PURPLE_LIGHT + "; -fx-padding: 10 14 10 14; -fx-background-radius: 6;");
        Label hintsTitle = new Label("Password Requirements:");
        hintsTitle.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: " + StyleConstants.COLOR_PRIMARY_DARK + ";");
        Label hintsDesc = new Label("• At least 8 characters\n• Uppercase & lowercase letters (A-Z, a-z)\n• At least one number (0-9)\n• At least one special character (@, #, $, %, etc.)");
        hintsDesc.setStyle("-fx-font-size: 11px; -fx-text-fill: #4338CA; -fx-line-spacing: 2;");
        hintsBox.getChildren().addAll(hintsTitle, hintsDesc);

        // Error message label
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: " + StyleConstants.COLOR_DANGER + "; -fx-font-size: 12px; -fx-font-weight: bold;");
        errorLabel.setVisible(false);
        errorLabel.setWrapText(true);

        // Buttons
        Button changeBtn = new Button("Change Password");
        changeBtn.setStyle(StyleConstants.STYLE_PRIMARY_BTN);
        changeBtn.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(changeBtn, Priority.ALWAYS);

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setStyle(StyleConstants.STYLE_SECONDARY_BTN);

        HBox buttonBox = new HBox(12, changeBtn, cancelBtn);
        buttonBox.setAlignment(Pos.CENTER);

        // Action Handlers
        Runnable doChangePassword = () -> {
            String newPass = newPasswordField.getText();
            String confirmPass = confirmPasswordField.getText();

            if (newPass == null || newPass.isEmpty() || confirmPass == null || confirmPass.isEmpty()) {
                errorLabel.setText("Please enter and confirm your new password.");
                errorLabel.setVisible(true);
                return;
            }

            if (!newPass.equals(confirmPass)) {
                errorLabel.setText("Passwords do not match. Please re-enter.");
                errorLabel.setVisible(true);
                return;
            }

            try {
                authController.changePassword(user.getId(), newPass);
                errorLabel.setVisible(false);

                // Password changed successfully! Open the Student Dashboard
                MainAppView mainApp = new MainAppView(stage);
                mainApp.show();
            } catch (ValidationException | DatabaseException ex) {
                errorLabel.setText(ex.getMessage());
                errorLabel.setVisible(true);
            } catch (Exception ex) {
                errorLabel.setText("Failed to update password: " + ex.getMessage());
                errorLabel.setVisible(true);
            }
        };

        changeBtn.setOnAction(e -> doChangePassword.run());
        newPasswordField.setOnAction(e -> doChangePassword.run());
        confirmPasswordField.setOnAction(e -> doChangePassword.run());

        cancelBtn.setOnAction(e -> {
            authController.handleLogout();
            new LoginView(stage).show();
        });

        card.getChildren().addAll(headerBox, newPassBox, confirmPassBox, hintsBox, errorLabel, buttonBox);
        root.getChildren().add(card);

        Scene scene = new Scene(root, 850, 650);
        stage.setScene(scene);

        stage.setWidth(850);
        stage.setHeight(650);
        stage.setMinWidth(700);
        stage.setMinHeight(550);
        stage.setResizable(true);

        stage.show();
        stage.centerOnScreen();
        stage.setIconified(false);
        stage.setAlwaysOnTop(true);
        stage.toFront();
        stage.requestFocus();
        Platform.runLater(() -> stage.setAlwaysOnTop(false));
    }
}
