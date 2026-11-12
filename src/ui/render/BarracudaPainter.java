package ui.render;

import java.awt.Color;
import java.awt.Graphics2D;
import model.ObjectView;

/** Draws a long, thin barracuda. It turns white-hot while it warns and charges. */
public final class BarracudaPainter implements ObjectPainter {

    private static final Color BODY = new Color(184, 196, 204);
    private static final Color BACK = new Color(84, 98, 110);
    private static final Color FLASH = new Color(255, 245, 235);
    private static final Color FLASH_BACK = new Color(255, 120, 100);
    private static final Color EYE = new Color(255, 60, 50);
    private static final Color TEETH = Color.WHITE;

    @Override
    public void paint(Graphics2D g, ObjectView object) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.translate(object.getX(), object.getY());
        g2.scale(object.getFacing(), 1);

        boolean alarmed = object.getAlert() > 0;

        g2.setColor(alarmed ? FLASH_BACK : BACK);
        g2.fillPolygon(new int[] {-30, -46, -40}, new int[] {0, -11, 0}, 3);   // forked tail
        g2.fillPolygon(new int[] {-30, -46, -40}, new int[] {0, 11, 0}, 3);
        g2.fillOval(-33, -8, 66, 14);                                           // dark back

        g2.setColor(alarmed ? FLASH : BODY);
        g2.fillOval(-33, -5, 66, 13);                                           // silver side

        // Open jaw with teeth
        g2.setColor(alarmed ? FLASH_BACK : BACK);
        g2.fillPolygon(new int[] {22, 38, 22}, new int[] {1, 4, 7}, 3);
        g2.setColor(TEETH);
        g2.fillPolygon(new int[] {25, 28, 31}, new int[] {2, 5, 3}, 3);
        g2.fillPolygon(new int[] {30, 33, 36}, new int[] {3, 6, 4}, 3);

        g2.setColor(EYE);
        g2.fillOval(18, -5, 6, 6);

        g2.dispose();
    }
}
