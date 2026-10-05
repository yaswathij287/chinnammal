package com.smarthostel.ui;

import com.smarthostel.controller.AuthController;
import com.smarthostel.exception.InvalidLoginException;
import com.smarthostel.model.User;
import com.smarthostel.util.AlertUtil;
import com.smarthostel.util.StyleConstants;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Login screen for the Smart Hostel Management System.
 */
public class LoginView {

    private final Stage stage;
    private final AuthController authController;

    public LoginView(Stage stage) {
        this.stage = stage;
        this.authController = new AuthController();
    }

    public void show() {

        System.out.println("[LoginView] Preparing login screen...");

        // =========================================================
        // MAIN ROOT
        // =========================================================

        VBox root = new VBox();

        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(30));

        root.setStyle(
                "-fx-background-color: "
                        + StyleConstants.COLOR_BG_APP
                        + ";"
        );

        // =========================================================
        // LOGIN CARD
        // =========================================================

        VBox card = new VBox(20);

        card.setMaxWidth(440);
        card.setAlignment(Pos.CENTER);

        card.setPadding(
                new Insets(35, 40, 35, 40)
        );

        card.setStyle(
                StyleConstants.STYLE_CARD
        );

        // =========================================================
        // HEADER
        // =========================================================

        Label iconLabel = new Label("🏢");

        iconLabel.setStyle(
                "-fx-font-size: 38px;"
        );

        Label titleLabel =
                new Label("Smart Hostel");

        titleLabel.setStyle(
                "-fx-font-size: 24px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-text-fill: "
                        + StyleConstants.COLOR_BG_SIDEBAR
                        + ";"
        );

        Label subtitleLabel =
                new Label(
                        "Personal Hostel Management Platform"
                );

        subtitleLabel.setStyle(
                "-fx-font-size: 13px;"
                        + "-fx-text-fill: "
                        + StyleConstants.COLOR_TEXT_MUTED
                        + ";"
        );

        VBox headerBox =
                new VBox(
                        6,
                        iconLabel,
                        titleLabel,
                        subtitleLabel
                );

        headerBox.setAlignment(Pos.CENTER);

        // =========================================================
        // USERNAME
        // =========================================================

        Label userLabel =
                new Label("Username");

        userLabel.setStyle(
                "-fx-font-weight: bold;"
                        + "-fx-text-fill: "
                        + StyleConstants.COLOR_TEXT_MAIN
                        + ";"
                        + "-fx-font-size: 12px;"
        );

        TextField usernameField =
                new TextField();

        usernameField.setPromptText(
                "Enter your username"
        );

        usernameField.setStyle(
                StyleConstants.STYLE_TEXT_FIELD
        );

        VBox userBox =
                new VBox(
                        5,
                        userLabel,
                        usernameField
                );

        userBox.setAlignment(
                Pos.CENTER_LEFT
        );

        // =========================================================
        // PASSWORD
        // =========================================================

        Label passLabel =
                new Label("Password");

        passLabel.setStyle(
                "-fx-font-weight: bold;"
                        + "-fx-text-fill: "
                        + StyleConstants.COLOR_TEXT_MAIN
                        + ";"
                        + "-fx-font-size: 12px;"
        );

        PasswordField passwordField =
                new PasswordField();

        passwordField.setPromptText(
                "Enter your password"
        );

        passwordField.setStyle(
                StyleConstants.STYLE_TEXT_FIELD
        );

        VBox passBox =
                new VBox(
                        5,
                        passLabel,
                        passwordField
                );

        passBox.setAlignment(
                Pos.CENTER_LEFT
        );

        // =========================================================
        // ERROR MESSAGE
        // =========================================================

        Label errorLabel =
                new Label();

        errorLabel.setStyle(
                "-fx-text-fill: "
                        + StyleConstants.COLOR_DANGER
                        + ";"
                        + "-fx-font-size: 12px;"
                        + "-fx-font-weight: bold;"
        );

        errorLabel.setVisible(false);
        errorLabel.setWrapText(true);

        // =========================================================
        // BUTTONS
        // =========================================================

        Button loginBtn =
                new Button("Sign In");

        loginBtn.setStyle(
                StyleConstants.STYLE_PRIMARY_BTN
        );

        loginBtn.setMaxWidth(
                Double.MAX_VALUE
        );

        HBox.setHgrow(
                loginBtn,
                Priority.ALWAYS
        );

        Button clearBtn =
                new Button("Clear");

        clearBtn.setStyle(
                StyleConstants.STYLE_SECONDARY_BTN
        );

        HBox buttonBox =
                new HBox(
                        12,
                        loginBtn,
                        clearBtn
                );

        buttonBox.setAlignment(
                Pos.CENTER
        );



        // =========================================================
        // CLEAR BUTTON
        // =========================================================

        clearBtn.setOnAction(event -> {

            usernameField.clear();
            passwordField.clear();

            errorLabel.setText("");
            errorLabel.setVisible(false);

            usernameField.requestFocus();
        });

        // =========================================================
        // LOGIN FUNCTION
        // =========================================================

        Runnable doLogin = () -> {

            String username =
                    usernameField.getText().trim();

            String password =
                    passwordField.getText();

            if (username.isEmpty()
                    || password.isEmpty()) {

                errorLabel.setText(
                        "Please enter username and password."
                );

                errorLabel.setVisible(true);

                return;
            }

            try {

                System.out.println(
                        "[LoginView] Attempting login for: "
                                + username
                );

                User authenticated =
                        authController.handleLogin(
                                username,
                                password
                        );

                if (authenticated == null) {

                    errorLabel.setText(
                            "Invalid username or password."
                    );

                    errorLabel.setVisible(true);

                    return;
                }

                System.out.println(
                        "[LoginView] Login successful."
                );

                errorLabel.setVisible(false);

                // If user is using a temporary password, show First Login password change view
                if (authenticated.isTempPassword()) {
                    System.out.println("[LoginView] Temporary password detected for " + username + ". Showing FirstLoginView.");
                    FirstLoginView firstLogin = new FirstLoginView(stage, authenticated);
                    firstLogin.show();
                } else {
                    // Open main dashboard
                    MainAppView mainApp = new MainAppView(stage);
                    mainApp.show();
                }

            } catch (InvalidLoginException ex) {

                errorLabel.setText(
                        ex.getMessage()
                );

                errorLabel.setVisible(true);

            } catch (Exception ex) {

                ex.printStackTrace();

                errorLabel.setText(
                        "Login error: "
                                + ex.getMessage()
                );

                errorLabel.setVisible(true);
            }
        };

        // =========================================================
        // BUTTON EVENTS
        // =========================================================

        loginBtn.setOnAction(
                event -> doLogin.run()
        );

        passwordField.setOnAction(
                event -> doLogin.run()
        );

        usernameField.setOnAction(
                event -> doLogin.run()
        );

        // =========================================================
        // BUILD CARD
        // =========================================================

        card.getChildren().addAll(
                headerBox,
                userBox,
                passBox,
                errorLabel,
                buttonBox
        );

        root.getChildren().add(card);

        // =========================================================
        // SCENE
        // =========================================================

        Scene scene =
                new Scene(
                        root,
                        850,
                        650
                );

        stage.setScene(scene);

        stage.setTitle(
                "Smart Hostel Management System - Login"
        );

        stage.setWidth(850);
        stage.setHeight(650);

        stage.setMinWidth(700);
        stage.setMinHeight(550);

        stage.setResizable(true);

        // =========================================================
        // CENTER WINDOW
        // =========================================================

        stage.centerOnScreen();

        // =========================================================
        // SHOW WINDOW
        // =========================================================

        System.out.println(
                "[LoginView] Showing login window..."
        );

        stage.show();

        stage.toFront();

        stage.requestFocus();

        System.out.println(
                String.format(
                        "[LoginView] Login window shown. "
                                + "Title=%s | X=%.1f | Y=%.1f | "
                                + "Width=%.0f | Height=%.0f | "
                                + "Showing=%s",
                        stage.getTitle(),
                        stage.getX(),
                        stage.getY(),
                        stage.getWidth(),
                        stage.getHeight(),
                        stage.isShowing()
                )
        );
    }
}