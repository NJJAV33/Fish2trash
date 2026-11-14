package ui.render;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.AffineTransform;
import model.ObjectView;

/** Draws a big blue fish with a forked tail and stripes. Fast ones trail speed lines. */
public final class BigFishPainter implements ObjectPainter {

    private static final Color BODY = new Color(61, 126, 191);
    // Lit from above: the back is brighter than the underside.
    private static final Paint BODY_SHADED = new GradientPaint(0, -12, new Color(96, 160, 222), 0, 12, new Color(40, 92, 155));
    private static final Color BELLY = new Color(170, 205, 235);
    private static final Color SHINE = new Color(255, 255, 255, 80);
    private static final Color STRIPE = new Color(35, 80, 135);
    private static final Color SPEED_LINE = new Color(255, 255, 255, 90);
    private static final BasicStroke STRIPE_STROKE = new BasicStroke(2);
    private static final double WAG = 0.28;
    private static final double FAST = 120;   // pixels per second: from here on it shows speed lines

    private static final int[] TAIL_X = {-26, -46, -40};
    private static final int[] TAIL_UP_Y = {0, -14, 0};
    private static final int[] TAIL_DOWN_Y = {0, 14, 0};
    private static final int[] FIN_X = {-6, 6, -12};
    private static final int[] FIN_Y = {-11, -11, -20};

    @Override
    public void paint(Graphics2D g, ObjectView object, double time) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.translate(object.getX(), object.getY());
        g2.scale(object.getFacing(), 1);   // drawn facing right; flipped when it swims left

        if (Math.abs(object.getSpeedX()) > FAST) {
            g2.setColor(SPEED_LINE);
            g2.setStroke(STRIPE_STROKE);
            g2.drawLine(-52, -8, -68, -8);
            g2.drawLine(-56, 0, -76, 0);
            g2.drawLine(-52, 8, -68, 8);
        }

        // Forked tail: two triangles that swing together
        AffineTransform beforeTail = g2.getTransform();
        g2.rotate(WAG * Math.sin(14 * time + object.getPhase()), -26, 0);
        g2.setColor(BODY);
        g2.fillPolygon(TAIL_X, TAIL_UP_Y, 3);
        g2.fillPolygon(TAIL_X, TAIL_DOWN_Y, 3);
        g2.setTransform(beforeTail);

        g2.fillPolygon(FIN_X, FIN_Y, 3);   // back fin
        g2.setPaint(BODY_SHADED);
        g2.fillOval(-31, -12, 62, 24);
        g2.setColor(BELLY);
        g2.fillOval(-22, 0, 44, 10);
        g2.setColor(SHINE);
        g2.fillOval(-18, -10, 30, 4);   // light on its back

        g2.setColor(STRIPE);
        g2.setStroke(STRIPE_STROKE);
        g2.drawLine(-12, -9, -12, 8);
        g2.drawLine(-2, -11, -2, 9);
        g2.drawLine(8, -10, 8, 8);

        g2.setColor(Color.WHITE);
        g2.fillOval(17, -7, 8, 8);
        g2.setColor(Color.BLACK);
        g2.fillOval(20, -5, 4, 4);

        g2.dispose();
    }
}
