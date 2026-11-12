package ui.render;

import java.awt.Color;

/** The colours of menus, panels and the HUD, in one place. */
public final class UiPalette {

    private UiPalette() {
        // only constants, never created
    }

    public static final Color TEXT = Color.WHITE;
    public static final Color DIM_TEXT = new Color(184, 196, 204);
    public static final Color ACCENT = new Color(79, 209, 197);
    public static final Color GOLD = new Color(255, 215, 0);
    public static final Color GOOD = new Color(120, 230, 120);
    public static final Color DANGER = new Color(255, 90, 77);
    public static final Color HEART = new Color(255, 77, 109);
    public static final Color HEART_EMPTY = new Color(255, 255, 255, 60);
    public static final Color TIMER_WARNING = new Color(255, 64, 64);

    public static final Color PANEL = new Color(0, 0, 0, 130);
    public static final Color PANEL_SOLID = new Color(16, 24, 32, 225);
    public static final Color PANEL_EDGE = new Color(255, 255, 255, 60);
    public static final Color SHADE = new Color(0, 0, 0, 150);       // dims whatever is behind
    public static final Color LIGHT_SHADE = new Color(0, 0, 0, 90);
    public static final Color BAR_TRACK = new Color(255, 255, 255, 50);

    public static final Color PAPER = new Color(255, 217, 224);      // the pink slip
    public static final Color INK = new Color(58, 42, 48);
    public static final Color STAMP = new Color(200, 30, 40);
    public static final Color FIRED_TINT = new Color(160, 0, 0, 70);
}
