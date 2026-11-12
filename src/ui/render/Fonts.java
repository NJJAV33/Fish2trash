package ui.render;

import java.awt.Font;

/** The fonts of the game, created once and shared by every screen. */
public final class Fonts {

    private Fonts() {
        // only constants, never created
    }

    public static final Font TITLE = new Font(Font.SANS_SERIF, Font.BOLD, 64);
    public static final Font BANNER = new Font(Font.SANS_SERIF, Font.BOLD, 40);
    public static final Font HEADING = new Font(Font.SANS_SERIF, Font.BOLD, 26);
    public static final Font SUBHEADING = new Font(Font.SANS_SERIF, Font.BOLD, 18);
    public static final Font BODY = new Font(Font.SANS_SERIF, Font.PLAIN, 17);
    public static final Font LABEL = new Font(Font.SANS_SERIF, Font.BOLD, 13);
    public static final Font SMALL = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
    public static final Font TIMER = new Font(Font.MONOSPACED, Font.BOLD, 24);
    public static final Font MONO = new Font(Font.MONOSPACED, Font.PLAIN, 15);
    public static final Font STAMP = new Font(Font.SANS_SERIF, Font.BOLD, 54);
}
