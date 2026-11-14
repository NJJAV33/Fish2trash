package ui.render;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Paint;
import model.ObjectView;

/** Draws a see-through plastic bottle floating on its side, rocking gently. */
public final class BottlePainter implements ObjectPainter {

    // Round things are lighter on one side than the other. The gradients run
    // across the bottle, in its own coordinates, so they turn with it.
    private static final Paint PLASTIC = new GradientPaint(
            -7, 0, new Color(225, 245, 250, 190), 7, 0, new Color(140, 200, 222, 160));
    private static final Color OUTLINE = new Color(120, 175, 195);
    private static final Paint CAP = new GradientPaint(-4, 0, new Color(90, 165, 240), 4, 0, new Color(30, 100, 185));
    private static final Paint LABEL = new GradientPaint(-7, 0, new Color(95, 205, 140), 7, 0, new Color(40, 140, 88));
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

        g2.setPaint(PLASTIC);
        g2.fillRoundRect(-7, -9, 14, 22, 6, 6);   // body
        g2.fillRect(-3, -14, 6, 6);               // neck
        g2.setPaint(LABEL);
        g2.fillRect(-7, -1, 14, 7);
        g2.setPaint(CAP);
        g2.fillRect(-4, -17, 8, 4);
        g2.setStroke(THIN);
        g2.setColor(OUTLINE);
        g2.drawRoundRect(-7, -9, 14, 22, 6, 6);
        g2.setColor(SHINE);
        g2.drawLine(-4, -6, -4, 9);

        g2.dispose();
    }
}
