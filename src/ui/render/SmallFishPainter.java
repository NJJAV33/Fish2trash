package ui.render;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.AffineTransform;
import model.ObjectView;

/** Draws a small orange fish that faces the way it swims and wags its tail. */
public final class SmallFishPainter implements ObjectPainter {

    // Lit from above: the back is brighter than the underside.
    private static final Paint BODY = new GradientPaint(0, -11, new Color(255, 178, 70), 0, 11, new Color(232, 112, 22));
    private static final Color BELLY = new Color(255, 205, 150);
    private static final Color SHINE = new Color(255, 255, 255, 90);
    private static final Color FIN = new Color(230, 110, 20);
    private static final double WAG = 0.35;   // how far the tail swings, in radians

    // Shapes are drawn facing right, around (0, 0); the painter flips them for a fish swimming left.
    private static final int[] TAIL_X = {-15, -30, -30};
    private static final int[] TAIL_Y = {0, -10, 10};
    private static final int[] FIN_X = {-6, 6, -4};
    private static final int[] FIN_Y = {-9, -9, -17};

    @Override
    public void paint(Graphics2D g, ObjectView object, double time) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.translate(object.getX(), object.getY());
        g2.scale(object.getFacing(), 1);

        // The tail swings around the point where it joins the body.
        AffineTransform beforeTail = g2.getTransform();
        g2.rotate(WAG * Math.sin(10 * time + object.getPhase()), -15, 0);
        g2.setColor(FIN);
        g2.fillPolygon(TAIL_X, TAIL_Y, 3);
        g2.setTransform(beforeTail);

        g2.fillPolygon(FIN_X, FIN_Y, 3);
        g2.setPaint(BODY);
        g2.fillOval(-18, -11, 36, 22);
        g2.setColor(BELLY);
        g2.fillOval(-12, 1, 24, 8);
        g2.setColor(SHINE);
        g2.fillOval(-9, -9, 14, 4);   // light on its back

        g2.setColor(Color.WHITE);
        g2.fillOval(5, -7, 8, 8);
        g2.setColor(Color.BLACK);
        g2.fillOval(8, -5, 4, 4);

        g2.dispose();
    }
}
