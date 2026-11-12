package ui.render;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Draws the small portrait beside a story page, built from simple shapes.
 * Which portrait to show is read from the page itself: the first line that
 * starts with a speaker's name, like "Dolor: ...".
 */
public final class PortraitPainter {

    /** Everyone who can appear in a portrait, with the name written under it. */
    private enum Speaker {
        NARRATOR("M/B Tala"),
        NICO("Nico"),
        DOLOR("Capt. Dolor"),
        SANTOS("Dr. Santos"),
        MIGO("Migo"),
        REEF("The Reef");

        private final String label;

        Speaker(String label) {
            this.label = label;
        }
    }

    private static final Color FRAME = new Color(20, 60, 85);
    private static final Color FRAME_EDGE = new Color(255, 255, 255, 90);
    private static final Color SKIN = new Color(214, 160, 120);
    private static final Color SKIN_DARK = new Color(170, 118, 84);
    private static final Color HAIR = new Color(30, 24, 22);
    private static final Color GREY_HAIR = new Color(190, 190, 195);
    private static final Color SLICKER = new Color(245, 200, 50);
    private static final Color NAVY = new Color(32, 48, 92);
    private static final Color WHITE_COAT = new Color(240, 244, 248);
    private static final Color ORANGE = new Color(240, 130, 50);
    private static final Color BANDANA = new Color(210, 50, 60);
    private static final Color CORAL_PINK = new Color(255, 111, 145);
    private static final Color CORAL_ORANGE = new Color(255, 150, 113);
    private static final Color HULL = new Color(170, 60, 45);
    private static final Color WAVE = new Color(110, 190, 230);
    private static final BasicStroke THIN = new BasicStroke(2);
    private static final BasicStroke THICK = new BasicStroke(5, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);

    // The words a line can start with, and who that is. Checked in this order.
    private final Map<String, Speaker> prefixes = new LinkedHashMap<>();

    public PortraitPainter() {
        prefixes.put("Nico:", Speaker.NICO);
        prefixes.put("Dolor:", Speaker.DOLOR);
        prefixes.put("Santos:", Speaker.SANTOS);
        prefixes.put("Migo:", Speaker.MIGO);
        prefixes.put("The reef:", Speaker.REEF);
    }

    /** Draws the portrait for the page into a square of 120 x 120 pixels at (x, y). */
    public void paint(Graphics2D g, String pageText, int x, int y) {
        Speaker speaker = speakerOf(pageText);

        Graphics2D g2 = (Graphics2D) g.create();
        g2.translate(x, y);
        g2.setColor(FRAME);
        g2.fillRoundRect(0, 0, 120, 120, 14, 14);
        g2.clipRect(0, 0, 120, 98);   // keep the figure above the name plate

        switch (speaker) {
            case NICO:
                drawPerson(g2, SKIN, HAIR, SLICKER);
                g2.setColor(SLICKER);                     // hood behind the head
                g2.setStroke(THICK);
                g2.drawArc(32, 16, 56, 56, 20, 140);
                break;
            case DOLOR:
                drawPerson(g2, SKIN_DARK, GREY_HAIR, NAVY);
                g2.setColor(Color.WHITE);                 // captain's cap
                g2.fillRoundRect(36, 18, 48, 16, 10, 10);
                g2.setColor(NAVY);
                g2.fillRect(34, 32, 52, 5);
                break;
            case SANTOS:
                drawPerson(g2, SKIN, HAIR, WHITE_COAT);
                g2.setColor(HAIR);                        // hair bun
                g2.fillOval(52, 10, 16, 14);
                g2.setColor(Color.WHITE);                 // glasses
                g2.setStroke(THIN);
                g2.drawOval(44, 44, 13, 11);
                g2.drawOval(63, 44, 13, 11);
                g2.drawLine(57, 49, 63, 49);
                break;
            case MIGO:
                drawPerson(g2, SKIN_DARK, HAIR, ORANGE);
                g2.setColor(BANDANA);                     // bandana across the forehead
                g2.fillRect(36, 30, 48, 8);
                g2.fillPolygon(new int[] {84, 96, 90}, new int[] {30, 28, 42}, 3);
                break;
            case REEF:
                drawCoral(g2);
                break;
            default:
                drawBoat(g2);
                break;
        }
        g2.dispose();

        // Name plate along the bottom of the frame.
        g.setColor(UiPalette.PANEL_SOLID);
        g.fillRoundRect(x, y + 98, 120, 22, 10, 10);
        g.setColor(FRAME_EDGE);
        g.drawRoundRect(x, y, 120, 120, 14, 14);
        g.setColor(UiPalette.TEXT);
        g.setFont(Fonts.LABEL);
        TextDraw.centered(g, speaker.label, x + 60, y + 114);
    }

    private Speaker speakerOf(String pageText) {
        for (String line : pageText.split("\n")) {
            for (Map.Entry<String, Speaker> entry : prefixes.entrySet()) {
                if (line.startsWith(entry.getKey())) {
                    return entry.getValue();
                }
            }
        }
        return Speaker.NARRATOR;
    }

    /** Head, hair, shoulders and a simple face. Each character adds details on top. */
    private void drawPerson(Graphics2D g, Color skin, Color hair, Color clothes) {
        g.setColor(clothes);
        g.fillRoundRect(22, 76, 76, 50, 30, 30);   // shoulders
        g.setColor(hair);
        g.fillOval(36, 20, 48, 46);                // hair behind the face
        g.setColor(skin);
        g.fillOval(40, 28, 40, 44);                // face
        g.fillRect(54, 68, 12, 12);                // neck
        g.setColor(Color.BLACK);
        g.fillOval(49, 47, 5, 5);                  // eyes
        g.fillOval(66, 47, 5, 5);
        g.setStroke(THIN);
        g.drawArc(51, 52, 18, 12, 200, 140);       // smile
    }

    private void drawCoral(Graphics2D g) {
        g.setStroke(THICK);
        g.setColor(CORAL_PINK);
        g.drawLine(40, 96, 40, 50);
        g.drawLine(40, 70, 24, 52);
        g.drawLine(40, 60, 54, 40);
        g.setColor(CORAL_ORANGE);
        g.drawLine(80, 96, 80, 56);
        g.drawLine(80, 74, 96, 58);
        g.drawLine(80, 66, 66, 48);
        g.setColor(SLICKER);                       // one small fish
        g.fillOval(52, 22, 22, 12);
        g.fillPolygon(new int[] {52, 44, 44}, new int[] {28, 22, 34}, 3);
    }

    private void drawBoat(Graphics2D g) {
        g.setColor(WAVE);
        g.fillRect(0, 70, 120, 30);
        g.setColor(WHITE_COAT);
        g.fillRect(48, 34, 26, 20);                // cabin
        g.setColor(HULL);
        g.fillPolygon(new int[] {22, 98, 86, 34}, new int[] {54, 54, 76, 76}, 4);
        g.setColor(Color.WHITE);
        g.setStroke(THIN);
        g.drawArc(8, 66, 30, 12, 0, 180);          // wave crests
        g.drawArc(84, 66, 30, 12, 0, 180);
    }
}
