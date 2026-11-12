package ui.render;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import model.ObjectView;

/** Draws a see-through plastic bottle floating on its side, rocking gently. */
public final class BottlePainter implements ObjectPainter {

    private static final Color PLASTIC = new Color(191, 230, 242, 170);
    private static final Color OUTLINE = new Color(120, 175, 195);
    private static final Color CAP = new Color(46, 134, 222);
    private static final Color LABEL = new Color(59, 178, 115);
    private static final Color SHINE = new Color(255, 255, 255, 190);
    private static final BasicStroke THIN = new BasicStroke(1);
    private static final double TILT = Math.toRadians(75);
    private static final double ROCK = 0.12;   // how far it rocks, in radians

    @Override
    public void paint(Graphics2D g, ObjectView object, double time) {
        // Work on a copy, so the move and tilt do not leak into other drawing.
        Graphics2D g2 = (Graphics2D) g.create();
        g2.translate(object.getX(), object.getY());
        g2.rotate(TILT + ROCK * Math.sin(2 * time + object.getPhase()));

        g2.setColor(PLASTIC);
        g2.fillRoundRect(-7, -9, 14, 22, 6, 6);   // body
        g2.fillRect(-3, -14, 6, 6);               // neck
        g2.setColor(LABEL);
        g2.fillRect(-7, -1, 14, 7);
        g2.setColor(CAP);
        g2.fillRect(-4, -17, 8, 4);
        g2.setStroke(THIN);
        g2.setColor(OUTLINE);
        g2.drawRoundRect(-7, -9, 14, 22, 6, 6);
        g2.setColor(SHINE);
        g2.drawLine(-4, -6, -4, 9);

        g2.dispose();
    }
}
