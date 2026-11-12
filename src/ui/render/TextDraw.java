package ui.render;

import java.awt.FontMetrics;
import java.awt.Graphics2D;

/** Helpers for placing text: centred, right-aligned and multi-line. */
public final class TextDraw {

    private TextDraw() {
        // only static methods
    }

    /** Draws one line with its middle at centerX. */
    public static void centered(Graphics2D g, String text, int centerX, int baselineY) {
        FontMetrics metrics = g.getFontMetrics();
        g.drawString(text, centerX - metrics.stringWidth(text) / 2, baselineY);
    }

    /** Draws one line that ends at rightX. */
    public static void rightAligned(Graphics2D g, String text, int rightX, int baselineY) {
        FontMetrics metrics = g.getFontMetrics();
        g.drawString(text, rightX - metrics.stringWidth(text), baselineY);
    }

    /** Draws text that contains "\n" line breaks, one line under the other. */
    public static void lines(Graphics2D g, String text, int x, int firstBaselineY, int lineHeight) {
        String[] lines = text.split("\n", -1);
        for (int i = 0; i < lines.length; i++) {
            g.drawString(lines[i], x, firstBaselineY + i * lineHeight);
        }
    }

    /** Like lines(), but every line is centred on centerX. */
    public static void centeredLines(Graphics2D g, String text, int centerX, int firstBaselineY, int lineHeight) {
        String[] lines = text.split("\n", -1);
        for (int i = 0; i < lines.length; i++) {
            centered(g, lines[i], centerX, firstBaselineY + i * lineHeight);
        }
    }
}
