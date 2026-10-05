package com.smarthostel.ui;

import com.smarthostel.controller.ReportController;
import com.smarthostel.controller.SecurityController;
import com.smarthostel.model.SecurityLog;
import com.smarthostel.util.StyleConstants;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.Map;

/**
 * Admin Dashboard displaying 8 high-level KPI cards and recent security events.
 */
public class AdminDashboardPanel extends ScrollPane {

    private final ReportController reportController = new ReportController();
    private final SecurityController securityController = new SecurityController();

    public AdminDashboardPanel() {
        setFitToWidth(true);
        setStyle("-fx-background: " + StyleConstants.COLOR_BG_APP + "; -fx-background-color: transparent;");

        VBox content = new VBox(24);
        content.setPadding(new Insets(24));
        content.setStyle("-fx-background-color: " + StyleConstants.COLOR_BG_APP + ";");

        // Welcome Title
        Label title = new Label("Admin Overview");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: " + StyleConstants.COLOR_TEXT_MAIN + ";");

        Label subtitle = new Label("Real-time summary of hostel operations, residency, and finances.");
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: " + StyleConstants.COLOR_TEXT_MUTED + ";");

        VBox titleBox = new VBox(4, title, subtitle);

        // Fetch Metrics
        Map<String, Object> m;
        try {
            m = reportController.getMetrics();
        } catch (Exception e) {
            m = Map.of();
        }

        // 8 Metric Cards Grid
        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(16);

        grid.add(createCard("👥", "Total Students", String.valueOf(m.getOrDefault("totalStudents", 0)), StyleConstants.COLOR_PRIMARY), 0, 0);
        grid.add(createCard("🛡️", "Total Wardens", String.valueOf(m.getOrDefault("totalWardens", 0)), StyleConstants.COLOR_PURPLE), 1, 0);
        grid.add(createCard("🚪", "Total Rooms", String.valueOf(m.getOrDefault("totalRooms", 0)), "#0284C7"), 2, 0);
        grid.add(createCard("📊", "Occupancy Rate", m.getOrDefault("occupancyPercentage", 0) + "%", StyleConstants.COLOR_SUCCESS), 3, 0);

        grid.add(createCard("💰", "Pending Fees", "₹ " + String.format("%,.0f", (Double) m.getOrDefault("pendingFees", 0.0)), StyleConstants.COLOR_DANGER), 0, 1);
        grid.add(createCard("⚠️", "Pending Complaints", String.valueOf(m.getOrDefault("pendingComplaints", 0)), StyleConstants.COLOR_WARNING), 1, 1);
        grid.add(createCard("📋", "Visitor Requests", String.valueOf(m.getOrDefault("pendingVisitors", 0)), "#8B5CF6"), 2, 1);
        grid.add(createCard("🚨", "Security Alerts", String.valueOf(m.getOrDefault("securityAlerts", 0)), "#E11D48"), 3, 1);

        // Recent Security Activity Section
        VBox activityBox = new VBox(12);
        activityBox.setStyle(StyleConstants.STYLE_CARD);
        activityBox.setPadding(new Insets(16));

        Label activityTitle = new Label("Recent Gate & Security Logs");
        activityTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: " + StyleConstants.COLOR_TEXT_MAIN + ";");

        TableView<SecurityLog> table = new TableView<>();
        table.setPrefHeight(220);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<SecurityLog, String> colStudent = new TableColumn<>("Student");
        colStudent.setCellValueFactory(new PropertyValueFactory<>("studentName"));

        TableColumn<SecurityLog, String> colGate = new TableColumn<>("Gate");
        colGate.setCellValueFactory(new PropertyValueFactory<>("gate"));

        TableColumn<SecurityLog, String> colEvent = new TableColumn<>("Event");
        colEvent.setCellValueFactory(new PropertyValueFactory<>("eventType"));

        TableColumn<SecurityLog, String> colMethod = new TableColumn<>("Method");
        colMethod.setCellValueFactory(new PropertyValueFactory<>("verificationMethod"));

        TableColumn<SecurityLog, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        table.getColumns().addAll(colStudent, colGate, colEvent, colMethod, colStatus);

        try {
            table.getItems().addAll(securityController.getRecentLogs(8));
        } catch (Exception ignored) {}

        activityBox.getChildren().addAll(activityTitle, table);

        content.getChildren().addAll(titleBox, grid, activityBox);
        setContent(content);
    }

    private VBox createCard(String icon, String title, String value, String accentColor) {
        VBox card = new VBox(8);
        card.setStyle(StyleConstants.STYLE_CARD + " -fx-border-left-color: " + accentColor + "; -fx-border-left-width: 4;");
        card.setPadding(new Insets(14, 16, 14, 16));
        card.setPrefWidth(210);

        HBox top = new HBox(8);
        top.setAlignment(Pos.CENTER_LEFT);

        Label iconLbl = new Label(icon);
        iconLbl.setStyle("-fx-font-size: 18px;");

        Label titleLbl = new Label(title);
        titleLbl.setStyle("-fx-font-size: 12px; -fx-font-weight: 600; -fx-text-fill: " + StyleConstants.COLOR_TEXT_MUTED + ";");

        top.getChildren().addAll(iconLbl, titleLbl);

        Label valLbl = new Label(value);
        valLbl.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: " + StyleConstants.COLOR_TEXT_MAIN + ";");

        card.getChildren().addAll(top, valLbl);
        return card;
    }
}
