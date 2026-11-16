package ui.render;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import model.ObjectView;
import util.GameConstants;

/**
 * Draws a pufferfish. Its body follows the object's real radius, so it
 * visibly swells, and its spikes grow as it gets alarmed.
 */
public final class PufferPainter implements ObjectPainter {

    private static final Color FIN = new Color(232, 190, 64);
    // A ball lit from the upper front: a bright spot that darkens toward the edge.
    private static final float[] BALL = {0f, 1f};
    private static final Color[] BALL_COLORS = {new Color(255, 238, 150), new Color(204, 156, 38)};
    private static final Color BELLY = new Color(255, 236, 200);
    private static final Color SPOT = new Color(150, 106, 30, 150);
    private static final Color SPIKE = new Color(160, 92, 26);
    private static final Color OUTLINE = new Color(120, 78, 20);
    private static final Color DANGER = new Color(255, 90, 77);
    private static final Color PUPIL = new Color(20, 16, 14);
    private static final BasicStroke SPIKE_STROKE = new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    private static final BasicStroke LINE = new BasicStroke(1.3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    private static final BasicStroke DANGER_STROKE = new BasicStroke(3);
    private static final int SPIKES = 14;
    private static final double WARNING_LEVEL = 0.5;   // the outline turns on above this alert
    // Spots on its back, as fractions of the radius.
    private static final double[][] SPOTS = {{-0.45, -0.45}, {-0.1, -0.65}, {-0.6, -0.05}, {0.05, -0.3}, {-0.3, -0.2}};

    // The directions of the spikes never change, so they are worked out once.
    private static final double[] SPIKE_COS = new double[SPIKES];
    private static final double[] SPIKE_SIN = new double[SPIKES];
    static {
        for (int i = 0; i < SPIKES; i++) {
            SPIKE_COS[i] = Math.cos(2 * Math.PI * i / SPIKES);
            SPIKE_SIN[i] = Math.sin(2 * Math.PI * i / SPIKES);
        }
    }

    private static final int STEPS = 12;   // tail positions from one side to the other
    private static final double CALM_RADIUS = GameConstants.RADIUS_PUFFER * 0.9;

    // A calm pufferfish always has the same size, so it is copied from finished
    // pictures. An alarmed one keeps changing size and is drawn shape by shape.
    private final SpriteCache calm = new SpriteCache(2 * STEPS, 36, 26, 26,
            (g2, pose) -> {
                g2.scale(SpriteCache.facingOf(pose), 1);
                drawPuffer(g2, CALM_RADIUS, 0, SpriteCache.swingOf(SpriteCache.stepOf(pose), STEPS));
            });

    @Override
    public void paint(Graphics2D g, ObjectView object, double time) {
        double beat = Math.sin(12 * time + object.getPhase());
        double alert = object.getAlert();
        if (alert == 0) {
            calm.paint(g, object.getX(), object.getY(), SpriteCache.pose(SpriteCache.step(beat, STEPS), object.getFacing()));
            return;
        }
        Graphics2D g2 = (Graphics2D) g.create();
        g2.translate(object.getX(), object.getY());
        g2.scale(object.getFacing(), 1);
        drawPuffer(g2, object.getRadius() * 0.9, alert, beat);
        g2.dispose();
    }

    /** Draws the fish facing right around (0, 0), with body radius r. */
    private static void drawPuffer(Graphics2D g2, double r, double alert, double beat) {
        // A small tail that flutters.
        AffineTransform still = g2.getTransform();
        g2.rotate(0.4 * beat, -r, 0);
        Path2D tail = new Path2D.Double();
        tail.moveTo(-r + 3, 0);
        tail.quadTo(-r - 8, -9, -r - 13, -8);
        tail.quadTo(-r - 9, 0, -r - 13, 8);
        tail.quadTo(-r - 8, 9, -r + 3, 0);
        g2.setColor(FIN);
        g2.fill(tail);
        g2.setColor(OUTLINE);
        g2.setStroke(LINE);
        g2.draw(tail);
        g2.setTransform(still);

        // Spikes all the way around; they stick out further when alarmed.
        g2.setColor(SPIKE);
        g2.setStroke(SPIKE_STROKE);
        double spikeLength = 3 + 7 * alert;
        for (int i = 0; i < SPIKES; i++) {
            g2.drawLine((int) (SPIKE_COS[i] * r), (int) (SPIKE_SIN[i] * r),
                    (int) (SPIKE_COS[i] * (r + spikeLength)), (int) (SPIKE_SIN[i] * (r + spikeLength)));
        }

        // The ball changes size as the fish swells, so its shading is made for the size it has now.
        Ellipse2D ball = new Ellipse2D.Double(-r, -r, 2 * r, 2 * r);
        g2.setPaint(new RadialGradientPaint((float) (r * 0.3), (float) (-r * 0.4), (float) (r * 1.4), BALL, BALL_COLORS));
        g2.fill(ball);
        g2.setColor(BELLY);
        g2.fillArc((int) -r, (int) -r, (int) (2 * r), (int) (2 * r), 205, 130);   // pale underside
        g2.setColor(SPOT);
        double spot = Math.max(2, r * 0.16);
        for (double[] s : SPOTS) {
            g2.fillOval((int) (s[0] * r), (int) (s[1] * r), (int) spot, (int) spot);
        }
        g2.setColor(OUTLINE);
        g2.setStroke(LINE);
        g2.draw(ball);

        // A big worried eye with a brow, and a small round mouth.
        double eyeX = r * 0.3;
        double eyeY = -r * 0.45;
        g2.setColor(Color.WHITE);
        g2.fillOval((int) eyeX, (int) eyeY, 10, 10);
        g2.setColor(PUPIL);
        g2.fillOval((int) eyeX + 4, (int) eyeY + 3, 5, 5);
        g2.setColor(Color.WHITE);
        g2.fillOval((int) eyeX + 6, (int) eyeY + 3, 2, 2);
        g2.setColor(OUTLINE);
        g2.drawOval((int) eyeX, (int) eyeY, 10, 10);
        int browLift = (int) (2 * alert);
        g2.drawLine((int) eyeX - 1, (int) eyeY - 1 - browLift, (int) eyeX + 9, (int) eyeY - 3);
        g2.drawOval((int) (r - 8), 1, 5, 5);

        if (alert > WARNING_LEVEL) {
            g2.setColor(DANGER);
            g2.setStroke(DANGER_STROKE);
            g2.drawOval((int) (-r - 2), (int) (-r - 2), (int) (2 * r + 4), (int) (2 * r + 4));
        }
    }
}
