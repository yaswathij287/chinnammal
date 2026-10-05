package com.smarthostel.ui;

import com.smarthostel.controller.AuthController;
import com.smarthostel.model.Role;
import com.smarthostel.util.SessionManager;
import com.smarthostel.util.StyleConstants;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

/**
 * Main application window shell providing top header, role-based sidebar navigation,
 * and dynamic content swapping for Admin, Warden, and Student dashboards.
 */
public class MainAppView {

    private final Stage stage;
    private final AuthController authController = new AuthController();

    private BorderPane rootLayout;
    private StackPane contentArea;
    private final List<Button> navButtons = new ArrayList<>();
    private String currentActiveView = "Dashboard";

    public MainAppView(Stage stage) {
        this.stage = stage;
    }

    public void show() {
        stage.setTitle("Smart Hostel Management System — " + SessionManager.getDisplayName());

        rootLayout = new BorderPane();
        rootLayout.setStyle("-fx-background-color: " + StyleConstants.COLOR_BG_APP + ";");

        // 1. Build Top Header
        HBox header = buildHeader();
        rootLayout.setTop(header);

        // 2. Build Sidebar
        VBox sidebar = buildSidebar();
        rootLayout.setLeft(sidebar);

        // 3. Center Content Area
        contentArea = new StackPane();
        contentArea.setStyle("-fx-background-color: " + StyleConstants.COLOR_BG_APP + ";");
        rootLayout.setCenter(contentArea);

        // Load Default View
        navigateTo("Dashboard");

        Scene scene = new Scene(rootLayout, 1180, 740);
        stage.setScene(scene);

        stage.setWidth(1180);
        stage.setHeight(740);
        stage.setMinWidth(960);
        stage.setMinHeight(600);
        stage.setResizable(true);

        javafx.geometry.Rectangle2D screenBounds = javafx.stage.Screen.getPrimary().getVisualBounds();
        if (screenBounds != null) {
            double posX = screenBounds.getMinX() + (screenBounds.getWidth() - 1180) / 2.0;
            double posY = screenBounds.getMinY() + (screenBounds.getHeight() - 740) / 2.0;
            stage.setX(Math.max(0, posX));
            stage.setY(Math.max(0, posY));
        }

        stage.show();
        stage.centerOnScreen();
        stage.setIconified(false);
        stage.setAlwaysOnTop(true);
        stage.toFront();
        stage.requestFocus();
        javafx.application.Platform.runLater(() -> stage.setAlwaysOnTop(false));
    }

    private HBox buildHeader() {
        HBox header = new HBox(16);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(12, 24, 12, 24));
        header.setStyle("-fx-background-color: white; -fx-border-color: " + StyleConstants.COLOR_BORDER + "; -fx-border-width: 0 0 1 0;");

        Label brandIcon = new Label("🏢");
        brandIcon.setStyle("-fx-font-size: 22px;");

        Label brandTitle = new Label("Smart Hostel");
        brandTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " + StyleConstants.COLOR_BG_SIDEBAR + ";");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // User badge
        Role role = SessionManager.getCurrentRole();
        String roleStr = (role != null) ? role.name() : "GUEST";

        Label roleBadge = new Label("  " + roleStr + "  ");
        roleBadge.setStyle(
                "-fx-background-color: " + StyleConstants.COLOR_PURPLE_LIGHT + "; " +
                "-fx-text-fill: " + StyleConstants.COLOR_PRIMARY_DARK + "; " +
                "-fx-font-weight: bold; -fx-font-size: 11px; " +
                "-fx-padding: 3 8 3 8; -fx-background-radius: 12;"
        );

        Label greeting = new Label("👤 " + SessionManager.getDisplayName());
        greeting.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: " + StyleConstants.COLOR_TEXT_MAIN + ";");

        Button refreshBtn = new Button("⟳ Refresh");
        refreshBtn.setStyle(StyleConstants.STYLE_SECONDARY_BTN);
        refreshBtn.setOnAction(e -> navigateTo(currentActiveView));

        Button logoutBtn = new Button("🚪 Logout");
        logoutBtn.setStyle(StyleConstants.STYLE_DANGER_BTN);
        logoutBtn.setOnAction(e -> {
            authController.handleLogout();
            new LoginView(stage).show();
        });

        header.getChildren().addAll(brandIcon, brandTitle, spacer, greeting, roleBadge, refreshBtn, logoutBtn);
        return header;
    }

    private VBox buildSidebar() {
        VBox sidebar = new VBox(6);
        sidebar.setPrefWidth(220);
        sidebar.setPadding(new Insets(18, 12, 18, 12));
        sidebar.setStyle("-fx-background-color: " + StyleConstants.COLOR_BG_SIDEBAR + ";");

        Label navSection = new Label("MAIN MENU");
        navSection.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 0 8 8 8;");
        sidebar.getChildren().add(navSection);

        Role role = SessionManager.getCurrentRole();
        navButtons.clear();

        if (role == Role.ADMIN) {
            addNavButton(sidebar, "Dashboard", "📊 Dashboard");
            addNavButton(sidebar, "Students", "👥 Students");
            addNavButton(sidebar, "Wardens", "🛡️ Wardens");
            addNavButton(sidebar, "Rooms", "🚪 Blocks & Rooms");
            addNavButton(sidebar, "Fees", "💰 Fees & Dues");
            addNavButton(sidebar, "Complaints", "⚠️ Complaints");
            addNavButton(sidebar, "Visitors", "📋 Visitors");
            addNavButton(sidebar, "Leave Requests", "✈️ Leave Requests");
            addNavButton(sidebar, "Attendance", "✅ Attendance");
            addNavButton(sidebar, "Security", "🚨 Security");
            addNavButton(sidebar, "Reports", "📈 Reports");
        } else if (role == Role.WARDEN) {
            addNavButton(sidebar, "Dashboard", "📊 Dashboard");
            addNavButton(sidebar, "Students", "👥 Block Students");
            addNavButton(sidebar, "Rooms", "🚪 Block Rooms");
            addNavButton(sidebar, "Complaints", "⚠️ Complaints");
            addNavButton(sidebar, "Visitors", "📋 Visitor Requests");
            addNavButton(sidebar, "Leave Requests", "✈️ Leave Requests");
            addNavButton(sidebar, "Attendance", "✅ Attendance");
            addNavButton(sidebar, "Security", "🚨 Security Logs");
        } else if (role == Role.STUDENT) {
            addNavButton(sidebar, "Dashboard", "📊 Dashboard");
            addNavButton(sidebar, "My Room", "🏠 My Room");
            addNavButton(sidebar, "Fees", "💰 Fee Dues");
            addNavButton(sidebar, "Complaints", "⚠️ My Complaints");
            addNavButton(sidebar, "Visitors", "📋 Visitor Pass");
            addNavButton(sidebar, "Leave Requests", "✈️ Leave Out-Pass");
            addNavButton(sidebar, "Attendance", "✅ My Attendance");
            addNavButton(sidebar, "Profile", "🎓 My Profile");
        }

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        sidebar.getChildren().add(spacer);

        Button logoutNav = new Button("🚪 Sign Out");
        logoutNav.setMaxWidth(Double.MAX_VALUE);
        logoutNav.setStyle(StyleConstants.STYLE_SIDEBAR_BTN_NORMAL);
        logoutNav.setOnAction(e -> {
            authController.handleLogout();
            new LoginView(stage).show();
        });
        sidebar.getChildren().add(logoutNav);

        return sidebar;
    }

    private void addNavButton(VBox sidebar, String viewKey, String label) {
        Button btn = new Button(label);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setStyle(StyleConstants.STYLE_SIDEBAR_BTN_NORMAL);
        btn.setOnAction(e -> navigateTo(viewKey));
        navButtons.add(btn);
        sidebar.getChildren().add(btn);
    }

    public void navigateTo(String viewKey) {
        this.currentActiveView = viewKey;

        // Highlight active button
        for (Button b : navButtons) {
            if (b.getText().toLowerCase().contains(viewKey.toLowerCase())) {
                b.setStyle(StyleConstants.STYLE_SIDEBAR_BTN_ACTIVE);
            } else {
                b.setStyle(StyleConstants.STYLE_SIDEBAR_BTN_NORMAL);
            }
        }

        Node panel = createViewPanel(viewKey);
        contentArea.getChildren().setAll(panel);
    }

    private Node createViewPanel(String viewKey) {
        Role role = SessionManager.getCurrentRole();

        switch (viewKey) {
            case "Dashboard":
                if (role == Role.ADMIN) return new AdminDashboardPanel();
                if (role == Role.WARDEN) return new WardenDashboardPanel();
                return new StudentDashboardPanel();

            case "Students":
                return new StudentsPanel();

            case "Wardens":
                return new WardensPanel();

            case "Rooms":
                return new RoomsPanel();

            case "Fees":
                return new FeesPanel();

            case "Complaints":
                return new ComplaintsPanel();

            case "Visitors":
                return new VisitorsPanel();

            case "Leave Requests":
                return new LeaveRequestsPanel();

            case "Attendance":
                return new AttendancePanel();

            case "Security":
                return new SecurityPanel();

            case "Reports":
                return new ReportsPanel();

            case "My Room":
                return new StudentRoomPanel();

            case "Profile":
                return new StudentProfilePanel();

            default:
                return new AdminDashboardPanel();
        }
    }
}
