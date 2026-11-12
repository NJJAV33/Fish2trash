package ui.render;

import java.awt.Color;
import java.awt.Graphics2D;
import model.ObjectView;

/** Draws a soda can: a gray body with a red label, tipping slowly from side to side. */
public final class CanPainter implements ObjectPainter {

    private static final Color METAL = new Color(175, 180, 186);
    private static final Color RIM = new Color(208, 211, 216);
    private static final Color LABEL = new Color(205, 60, 60);
    private static final Color OUTLINE = new Color(90, 95, 100);
    private static final double TIP = 0.14;   // how far it tips, in radians

    @Override
    public void paint(Graphics2D g, ObjectView object, double time) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.translate(object.getX(), object.getY());
        g2.rotate(TIP * Math.sin(1.6 * time + object.getPhase()));

        g2.setColor(METAL);
        g2.fillRoundRect(-10, -14, 20, 28, 6, 6);
        g2.setColor(LABEL);
        g2.fillRect(-10, -5, 20, 10);
        g2.setColor(RIM);
        g2.fillOval(-9, -15, 18, 5);   // the top of the can
        g2.setColor(OUTLINE);
        g2.drawRoundRect(-10, -14, 20, 28, 6, 6);

        g2.dispose();
    }
}
