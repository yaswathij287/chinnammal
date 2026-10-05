package com.smarthostel;

import com.smarthostel.database.DatabaseInitializer;
import com.smarthostel.database.DatabaseSeeder;
import com.smarthostel.ui.LoginView;
import com.smarthostel.util.StyleConstants;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Main extends Application {

    private static final String APP_TITLE = "Smart Hostel Management System";

    @Override
    public void start(Stage primaryStage) {

        System.out.println("[Main] start() invoked on thread: "
                + Thread.currentThread().getName());

        // ---------------------------------------------------------
        // 1. Configure Stage
        // ---------------------------------------------------------
        primaryStage.setTitle(APP_TITLE);
        primaryStage.setWidth(850);
        primaryStage.setHeight(650);
        primaryStage.setResizable(true);

        // ---------------------------------------------------------
        // 2. Create Startup Screen
        // ---------------------------------------------------------
        VBox splashRoot = new VBox(18);

        splashRoot.setAlignment(Pos.CENTER);
        splashRoot.setPadding(new Insets(40));

        splashRoot.setStyle(
                "-fx-background-color: "
                        + StyleConstants.COLOR_BG_APP
                        + ";"
        );

        Label iconLabel = new Label("🏢");

        iconLabel.setStyle(
                "-fx-font-size: 42px;"
        );

        Label titleLabel = new Label(APP_TITLE);

        titleLabel.setStyle(
                "-fx-font-size: 22px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-text-fill: "
                        + StyleConstants.COLOR_PRIMARY_DARK
                        + ";"
        );

        ProgressIndicator progress = new ProgressIndicator();

        progress.setPrefSize(42, 42);

        Label statusLabel = new Label(
                "Starting system & verifying database..."
        );

        statusLabel.setStyle(
                "-fx-font-size: 13px;"
                        + "-fx-text-fill: "
                        + StyleConstants.COLOR_TEXT_MUTED
                        + ";"
        );

        splashRoot.getChildren().addAll(
                iconLabel,
                titleLabel,
                progress,
                statusLabel
        );

        // ---------------------------------------------------------
        // 3. Create Scene
        // ---------------------------------------------------------
        Scene splashScene = new Scene(
                splashRoot,
                850,
                650
        );

        primaryStage.setScene(splashScene);

        primaryStage.setMinWidth(700);
        primaryStage.setMinHeight(550);

        // ---------------------------------------------------------
        // 4. Position Window
        // ---------------------------------------------------------
        javafx.geometry.Rectangle2D screenBounds =
                javafx.stage.Screen.getPrimary().getVisualBounds();

        double posX =
                screenBounds.getMinX()
                        + (screenBounds.getWidth() - 850) / 2.0;

        double posY =
                screenBounds.getMinY()
                        + (screenBounds.getHeight() - 650) / 2.0;

        primaryStage.setX(Math.max(0, posX));
        primaryStage.setY(Math.max(0, posY));

        // ---------------------------------------------------------
        // 5. SHOW WINDOW
        // ---------------------------------------------------------
        System.out.println("[Main] Showing startup window...");

        primaryStage.show();

        primaryStage.centerOnScreen();

        primaryStage.toFront();

        primaryStage.requestFocus();

        System.out.println(
                String.format(
                        "[Main] Startup Stage displayed successfully. "
                                + "X=%.1f, Y=%.1f, Size=%.0fx%.0f, Showing=%s",
                        primaryStage.getX(),
                        primaryStage.getY(),
                        primaryStage.getWidth(),
                        primaryStage.getHeight(),
                        primaryStage.isShowing()
                )
        );

        // ---------------------------------------------------------
        // 6. Database Initialization Task
        // ---------------------------------------------------------
        Task<Void> initTask = new Task<>() {

            @Override
            protected Void call() throws Exception {

                updateMessage(
                        "Initializing database schema..."
                );

                System.out.println(
                        "[Database] Initializing database..."
                );

                DatabaseInitializer.initializeDatabase();

                updateMessage(
                        "Verifying demo seed accounts & data..."
                );

                System.out.println(
                        "[Database] Checking seed data..."
                );

                DatabaseSeeder.seedIfNeeded();

                updateMessage("Ready.");

                System.out.println(
                        "[Database] Initialization completed."
                );

                return null;
            }
        };

        // Connect task message to UI
        statusLabel.textProperty()
                .bind(initTask.messageProperty());

        // ---------------------------------------------------------
        // 7. Database Success
        // ---------------------------------------------------------
        initTask.setOnSucceeded(event -> {

            System.out.println(
                    "[Main] Database initialization complete."
            );

            System.out.println(
                    "[Main] Loading login screen..."
            );

            try {

                LoginView loginView =
                        new LoginView(primaryStage);

                loginView.show();

                System.out.println(
                        "[Main] LoginView loaded and displayed."
                );

            } catch (Throwable t) {

                System.err.println(
                        "[Main] Error rendering login screen: "
                                + t.getMessage()
                );

                t.printStackTrace();

                showErrorAlert(
                        "UI Startup Error",
                        "Failed to render login screen:\n"
                                + t.getMessage()
                );
            }
        });

        // ---------------------------------------------------------
        // 8. Database Failure
        // ---------------------------------------------------------
        initTask.setOnFailed(event -> {

            Throwable t = initTask.getException();

            System.err.println(
                    "[Main] Database initialization failed."
            );

            if (t != null) {
                t.printStackTrace();
            }

            showErrorAlert(
                    "Database Initialization Error",
                    "Could not initialize SQLite database:\n"
                            + (t != null
                            ? t.getMessage()
                            : "Unknown error")
            );
        });

        // ---------------------------------------------------------
        // 9. Run Database Task in Background Thread
        // ---------------------------------------------------------
        Thread dbThread =
                new Thread(
                        initTask,
                        "DB-Initializer-Thread"
                );

        dbThread.setDaemon(true);

        dbThread.start();
    }

    // =============================================================
    // ERROR ALERT
    // =============================================================

    private void showErrorAlert(
            String title,
            String content
    ) {

        Platform.runLater(() -> {

            Alert alert =
                    new Alert(Alert.AlertType.ERROR);

            alert.setTitle(title);

            alert.setHeaderText(
                    "Smart Hostel Startup Failure"
            );

            alert.setContentText(content);

            alert.showAndWait();
        });
    }

    // =============================================================
    // MAIN METHOD
    // =============================================================

    public static void main(String[] args) {

        System.out.println(
                "[Main] Application starting..."
        );

        Thread.setDefaultUncaughtExceptionHandler(
                (thread, throwable) -> {

                    System.err.println(
                            "[CRITICAL ERROR in thread "
                                    + thread.getName()
                                    + "]: "
                                    + throwable.getMessage()
                    );

                    throwable.printStackTrace();
                }
        );

        launch(args);
    }
}