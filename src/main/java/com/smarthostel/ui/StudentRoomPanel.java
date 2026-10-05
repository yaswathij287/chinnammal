package com.smarthostel.ui;

import com.smarthostel.controller.StudentController;
import com.smarthostel.model.Student;
import com.smarthostel.util.SessionManager;
import com.smarthostel.util.StyleConstants;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;

/**
 * Student's private view of allocated room, allocated bed, roommates, and facilities.
 */
public class StudentRoomPanel extends ScrollPane {

    private final StudentController studentController = new StudentController();

    public StudentRoomPanel() {
        setFitToWidth(true);
        setStyle("-fx-background: " + StyleConstants.COLOR_BG_APP + "; -fx-background-color: transparent;");

        VBox content = new VBox(24);
        content.setPadding(new Insets(24));
        content.setStyle("-fx-background-color: " + StyleConstants.COLOR_BG_APP + ";");

        Student s = SessionManager.getCurrentStudent();

        // Header
        Label title = new Label("My Hostel Room Details");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: " + StyleConstants.COLOR_TEXT_MAIN + ";");

        Label subtitle = new Label("Information regarding your assigned accommodation, roommates, and amenities.");
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: " + StyleConstants.COLOR_TEXT_MUTED + ";");

        VBox titleBox = new VBox(4, title, subtitle);

        if (s == null || s.getRoomId() == null) {
            Label unassigned = new Label("You have not been assigned to a hostel room yet. Please contact the Warden.");
            unassigned.setStyle("-fx-font-size: 14px; -fx-text-fill: " + StyleConstants.COLOR_DANGER + ";");
            content.getChildren().addAll(titleBox, unassigned);
            setContent(content);
            return;
        }

        // Room details card
        VBox roomCard = new VBox(14);
        roomCard.setStyle(StyleConstants.STYLE_CARD);
        roomCard.setPadding(new Insets(20));

        Label roomHeader = new Label("🏠 Allocated Accommodation");
        roomHeader.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: " + StyleConstants.COLOR_PRIMARY_DARK + ";");

        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(12);

        grid.add(createField("Room Number", s.getRoomNumber() != null ? s.getRoomNumber() : "N/A"), 0, 0);
        grid.add(createField("Hostel Block", s.getBlockName() != null ? s.getBlockName() : "Main Block"), 1, 0);
        grid.add(createField("Assigned Bed", s.getBedNumber() != null ? s.getBedNumber() : "Bed 1"), 2, 0);

        roomCard.getChildren().addAll(roomHeader, grid);

        // Roommates section
        VBox roommatesCard = new VBox(12);
        roommatesCard.setStyle(StyleConstants.STYLE_CARD);
        roommatesCard.setPadding(new Insets(20));

        Label roommatesHeader = new Label("👥 Roommates Sharing Room " + s.getRoomNumber());
        roommatesHeader.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: " + StyleConstants.COLOR_TEXT_MAIN + ";");

        VBox roommatesList = new VBox(10);
        try {
            List<Student> mates = studentController.getRoommates(s.getStudentId());
            if (mates.isEmpty()) {
                Label noMates = new Label("No other roommates currently sharing this room.");
                noMates.setStyle("-fx-text-fill: " + StyleConstants.COLOR_TEXT_MUTED + "; -fx-font-style: italic;");
                roommatesList.getChildren().add(noMates);
            } else {
                for (Student mate : mates) {
                    HBox mateRow = new HBox(12);
                    mateRow.setAlignment(Pos.CENTER_LEFT);
                    mateRow.setStyle("-fx-background-color: #F8FAFC; -fx-padding: 10 14 10 14; -fx-background-radius: 6;");

                    Label mIcon = new Label("👤");
                    Label mName = new Label(mate.getName() + " (" + mate.getDepartment() + " - Year " + mate.getYear() + ")");
                    mName.setStyle("-fx-font-weight: bold; -fx-text-fill: " + StyleConstants.COLOR_TEXT_MAIN + ";");

                    Label mBed = new Label(mate.getBedNumber() != null ? "[" + mate.getBedNumber() + "]" : "");
                    mBed.setStyle("-fx-text-fill: " + StyleConstants.COLOR_PRIMARY + "; -fx-font-weight: bold;");

                    mateRow.getChildren().addAll(mIcon, mName, mBed);
                    roommatesList.getChildren().add(mateRow);
                }
            }
        } catch (Exception ex) {
            roommatesList.getChildren().add(new Label("Failed to load roommates."));
        }

        roommatesCard.getChildren().addAll(roommatesHeader, roommatesList);

        // Facilities Card
        VBox facCard = new VBox(12);
        facCard.setStyle(StyleConstants.STYLE_CARD);
        facCard.setPadding(new Insets(20));

        Label facHeader = new Label("✨ Included Room Facilities & Services");
        facHeader.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: " + StyleConstants.COLOR_TEXT_MAIN + ";");

        Label facBody = new Label(
                "• High-Speed Wi-Fi (Hostel-LAN Gigabit connection)\n" +
                "• Personal Study Table, Chair & Bookshelf\n" +
                "• 2-Door Steel Wardrobe with security lock\n" +
                "• Ceiling Fan & LED Task Lighting\n" +
                "• Daily Corridors & Common Washrooms Housekeeping\n" +
                "• 24x7 Purified UV/RO Drinking Water Station on each floor"
        );
        facBody.setStyle("-fx-font-size: 13px; -fx-text-fill: " + StyleConstants.COLOR_TEXT_MUTED + "; -fx-line-spacing: 4;");
        facCard.getChildren().addAll(facHeader, facBody);

        content.getChildren().addAll(titleBox, roomCard, roommatesCard, facCard);
        setContent(content);
    }

    private VBox createField(String label, String val) {
        VBox box = new VBox(4);
        Label l = new Label(label);
        l.setStyle("-fx-font-size: 12px; -fx-text-fill: " + StyleConstants.COLOR_TEXT_MUTED + "; -fx-font-weight: 600;");

        Label v = new Label(val);
        v.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: " + StyleConstants.COLOR_TEXT_MAIN + ";");

        box.getChildren().addAll(l, v);
        return box;
    }
}
