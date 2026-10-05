package com.smarthostel.util;

/**
 * Styling constants adhering to the modern Purple + Indigo theme.
 */
public class StyleConstants {

    // Palette
    public static final String COLOR_PRIMARY = "#6366F1";       // Indigo
    public static final String COLOR_PRIMARY_DARK = "#4F46E5";  // Indigo Dark
    public static final String COLOR_PRIMARY_HOVER = "#4338CA";
    public static final String COLOR_PURPLE = "#8B5CF6";        // Purple
    public static final String COLOR_PURPLE_LIGHT = "#EDE9FE";
    public static final String COLOR_BG_SIDEBAR = "#1E1B4B";    // Deep Indigo Sidebar
    public static final String COLOR_BG_APP = "#F8FAFC";        // Soft Gray Background
    public static final String COLOR_CARD_BG = "#FFFFFF";
    public static final String COLOR_BORDER = "#E2E8F0";
    public static final String COLOR_TEXT_MAIN = "#0F172A";
    public static final String COLOR_TEXT_MUTED = "#64748B";

    public static final String COLOR_SUCCESS = "#10B981";       // Emerald
    public static final String COLOR_WARNING = "#F59E0B";       // Amber
    public static final String COLOR_DANGER = "#EF4444";        // Rose/Red

    // Pre-composed CSS strings
    public static final String STYLE_PRIMARY_BTN =
            "-fx-background-color: " + COLOR_PRIMARY_DARK + "; " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold; " +
            "-fx-font-size: 13px; " +
            "-fx-padding: 8 16 8 16; " +
            "-fx-background-radius: 6; " +
            "-fx-cursor: hand;";

    public static final String STYLE_SECONDARY_BTN =
            "-fx-background-color: #E2E8F0; " +
            "-fx-text-fill: #1E293B; " +
            "-fx-font-weight: bold; " +
            "-fx-font-size: 13px; " +
            "-fx-padding: 8 16 8 16; " +
            "-fx-background-radius: 6; " +
            "-fx-cursor: hand;";

    public static final String STYLE_DANGER_BTN =
            "-fx-background-color: " + COLOR_DANGER + "; " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold; " +
            "-fx-font-size: 13px; " +
            "-fx-padding: 8 16 8 16; " +
            "-fx-background-radius: 6; " +
            "-fx-cursor: hand;";

    public static final String STYLE_CARD =
            "-fx-background-color: " + COLOR_CARD_BG + "; " +
            "-fx-background-radius: 10; " +
            "-fx-border-color: " + COLOR_BORDER + "; " +
            "-fx-border-radius: 10; " +
            "-fx-border-width: 1; " +
            "-fx-padding: 16; " +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.03), 8, 0, 0, 2);";

    public static final String STYLE_TEXT_FIELD =
            "-fx-background-color: white; " +
            "-fx-border-color: " + COLOR_BORDER + "; " +
            "-fx-border-radius: 6; " +
            "-fx-background-radius: 6; " +
            "-fx-padding: 8 12 8 12; " +
            "-fx-font-size: 13px;";

    public static final String STYLE_SIDEBAR_BTN_NORMAL =
            "-fx-background-color: transparent; " +
            "-fx-text-fill: #C7D2FE; " +
            "-fx-alignment: BASELINE_LEFT; " +
            "-fx-font-size: 14px; " +
            "-fx-font-weight: 500; " +
            "-fx-padding: 10 16 10 16; " +
            "-fx-background-radius: 8; " +
            "-fx-cursor: hand;";

    public static final String STYLE_SIDEBAR_BTN_ACTIVE =
            "-fx-background-color: " + COLOR_PRIMARY_DARK + "; " +
            "-fx-text-fill: white; " +
            "-fx-alignment: BASELINE_LEFT; " +
            "-fx-font-size: 14px; " +
            "-fx-font-weight: bold; " +
            "-fx-padding: 10 16 10 16; " +
            "-fx-background-radius: 8; " +
            "-fx-cursor: hand;";
}
