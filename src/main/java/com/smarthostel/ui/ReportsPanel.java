package com.smarthostel.ui;

import com.smarthostel.controller.ReportController;
import com.smarthostel.util.AlertUtil;
import com.smarthostel.util.StyleConstants;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.Map;

/**
 * Analytical reports and operational summary export panel.
 */
public class ReportsPanel extends ScrollPane {

    private final ReportController reportController = new ReportController();

    public ReportsPanel() {
        setFitToWidth(true);
        setStyle("-fx-background: " + StyleConstants.COLOR_BG_APP + "; -fx-background-color: transparent;");

        VBox content = new VBox(24);
        content.setPadding(new Insets(24));
        content.setStyle("-fx-background-color: " + StyleConstants.COLOR_BG_APP + ";");

        // Header
        Label title = new Label("Institutional Reports & Analytics");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: " + StyleConstants.COLOR_TEXT_MAIN + ";");

        Label subtitle = new Label("Consolidated operational statistics across occupancy, revenue, security, and attendance.");
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: " + StyleConstants.COLOR_TEXT_MUTED + ";");

        VBox titleBox = new VBox(4, title, subtitle);

        // Fetch Metrics
        Map<String, Object> m;
        String summaryText = "";
        try {
            m = reportController.getMetrics();
            summaryText = reportController.getSummaryText();
        } catch (Exception e) {
            m = Map.of();
            summaryText = "Error generating report: " + e.getMessage();
        }

        // Summary Cards Grid
        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(16);

        grid.add(createCard("👥 Total Students", String.valueOf(m.getOrDefault("totalStudents", 0))), 0, 0);
        grid.add(createCard("🚪 Total Rooms", String.valueOf(m.getOrDefault("totalRooms", 0))), 1, 0);
        grid.add(createCard("📊 Occupancy %", m.getOrDefault("occupancyPercentage", 0) + "%"), 2, 0);
        grid.add(createCard("🛡️ Wardens On Duty", String.valueOf(m.getOrDefault("totalWardens", 0))), 3, 0);

        grid.add(createCard("💵 Fees Collected", "₹ " + String.format("%,.0f", (Double) m.getOrDefault("collectedFees", 0.0))), 0, 1);
        grid.add(createCard("⏳ Pending Dues", "₹ " + String.format("%,.0f", (Double) m.getOrDefault("pendingFees", 0.0))), 1, 1);
        grid.add(createCard("⚠️ Pending Complaints", String.valueOf(m.getOrDefault("pendingComplaints", 0))), 2, 1);
        grid.add(createCard("🚨 Security Alerts", String.valueOf(m.getOrDefault("securityAlerts", 0))), 3, 1);

        // Report Text Section
        VBox reportBox = new VBox(12);
        reportBox.setStyle(StyleConstants.STYLE_CARD);
        reportBox.setPadding(new Insets(18));

        HBox reportHeader = new HBox(12);
        reportHeader.setAlignment(Pos.CENTER_LEFT);

        Label rTitle = new Label("Official Management Summary");
        rTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: " + StyleConstants.COLOR_TEXT_MAIN + ";");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button copyBtn = new Button("📋 Copy Report to Clipboard");
        copyBtn.setStyle(StyleConstants.STYLE_PRIMARY_BTN);
        final String textToCopy = summaryText;
        copyBtn.setOnAction(e -> {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent c = new ClipboardContent();
            c.putString(textToCopy);
            clipboard.setContent(c);
            AlertUtil.showInfo("Copied", "Summary report copied to clipboard.");
        });

        reportHeader.getChildren().addAll(rTitle, spacer, copyBtn);

        TextArea reportArea = new TextArea(summaryText);
        reportArea.setEditable(false);
        reportArea.setPrefRowCount(16);
        reportArea.setStyle("-fx-font-family: 'Consolas', 'Courier New', monospace; -fx-font-size: 13px;");

        reportBox.getChildren().addAll(reportHeader, reportArea);

        content.getChildren().addAll(titleBox, grid, reportBox);
        setContent(content);
    }

    private VBox createCard(String label, String value) {
        VBox c = new VBox(6);
        c.setStyle(StyleConstants.STYLE_CARD);
        c.setPadding(new Insets(12, 16, 12, 16));
        c.setPrefWidth(210);

        Label l = new Label(label);
        l.setStyle("-fx-font-size: 12px; -fx-font-weight: 600; -fx-text-fill: " + StyleConstants.COLOR_TEXT_MUTED + ";");

        Label v = new Label(value);
        v.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " + StyleConstants.COLOR_TEXT_MAIN + ";");

        c.getChildren().addAll(l, v);
        return c;
    }
}
