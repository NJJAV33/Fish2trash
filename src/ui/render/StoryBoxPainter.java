package ui.render;

import java.awt.Graphics2D;

/**
 * Draws the box at the bottom of story screens: a portrait on the left and
 * up to five lines of text beside it.
 */
public final class StoryBoxPainter {

    private static final int BOX_X = 40;
    private static final int BOX_Y = 380;
    private static final int BOX_WIDTH = 720;
    private static final int BOX_HEIGHT = 180;
    private static final int TEXT_X = 205;
    private static final int FIRST_LINE_Y = 428;
    private static final int LINE_HEIGHT = 26;
    private static final double BLINK_SECONDS = 0.5;

    private final PortraitPainter portraits = new PortraitPainter();

    /**
     * @param visibleText the part of the page that has appeared so far
     * @param fullText    the whole page (used to pick the portrait)
     * @param promptText  shown in the corner once the page is complete, or null to hide it
     * @param time        seconds, makes the prompt blink
     */
    public void paint(Graphics2D g, String visibleText, String fullText, String promptText, double time) {
        g.setColor(UiPalette.PANEL_SOLID);
        g.fillRoundRect(BOX_X, BOX_Y, BOX_WIDTH, BOX_HEIGHT, 18, 18);
        g.setColor(UiPalette.PANEL_EDGE);
        g.drawRoundRect(BOX_X, BOX_Y, BOX_WIDTH, BOX_HEIGHT, 18, 18);

        portraits.paint(g, fullText, BOX_X + 22, BOX_Y + 22);

        g.setColor(UiPalette.TEXT);
        g.setFont(Fonts.BODY);
        TextDraw.lines(g, visibleText, TEXT_X, FIRST_LINE_Y, LINE_HEIGHT);

        boolean blinkOn = (int) (time / BLINK_SECONDS) % 2 == 0;
        if (promptText != null && blinkOn) {
            g.setColor(UiPalette.ACCENT);
            g.setFont(Fonts.LABEL);
            TextDraw.rightAligned(g, promptText, BOX_X + BOX_WIDTH - 20, BOX_Y + BOX_HEIGHT - 14);
        }
    }
}
