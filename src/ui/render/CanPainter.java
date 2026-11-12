package ui.render;

import java.awt.Color;
import java.awt.Graphics2D;
import model.ObjectView;

/** Draws a soda can: a gray rounded body with a red label. */
public final class CanPainter implements ObjectPainter {

    private static final Color METAL = new Color(175, 180, 186);
    private static final Color LABEL = new Color(205, 60, 60);
    private static final Color OUTLINE = new Color(90, 95, 100);

    @Override
    public void paint(Graphics2D g, ObjectView object) {
        int left = (int) (object.getX() - 10);
        int top = (int) (object.getY() - 14);

        g.setColor(METAL);
        g.fillRoundRect(left, top, 20, 28, 6, 6);
        g.setColor(LABEL);
        g.fillRect(left, top + 9, 20, 10);
        g.setColor(OUTLINE);
        g.drawRoundRect(left, top, 20, 28, 6, 6);
    }
}
