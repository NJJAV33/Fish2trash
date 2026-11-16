package ui.render;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Path2D;
import java.awt.geom.QuadCurve2D;
import model.ObjectView;

/**
 * Draws a stingray: flat, rounded wings and a whip tail with a barb, flapping
 * slowly. While it feels the Magnet, jagged blue sparks flicker around it as
 * a warning.
 */
public final class StingrayPainter implements ObjectPainter {

    // Sand-coloured on top, so it blends with the sea floor until it moves.
    private static final Paint BODY_SHADED = new GradientPaint(0, -12, new Color(186, 160, 120), 0, 12, new Color(112, 92, 70));
    private static final Color RIDGE = new Color(206, 184, 146);
    private static final Color EDGE = new Color(78, 62, 48);
    private static final Color SPOT = new Color(226, 208, 170);
    private static final Color EYE = new Color(20, 20, 26);
    private static final Color SPARK = new Color(150, 220, 255);
    private static final Color SPARK_CORE = Color.WHITE;
    private static final Stroke LINE = new BasicStroke(1.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    private static final Stroke TAIL_STROKE = new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    private static final Stroke SPARK_STROKE = new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    private static final double FLAP = 0.18;              // how much the wings rise and fall
    private static final double SPARK_FLICKERS_PER_SECOND = 14;
    private static final int SPARKS = 4;

    private static final Shape BODY = body();
    private static final Shape RIDGE_SHAPE = ridge();
    // One jagged spark, pointing up from the middle of the ray; it is turned to make the others.
    private static final int[] SPARK_X = {0, 5, -3, 4, 0};
    private static final int[] SPARK_Y = {-14, -21, -27, -34, -41};

    private static final int STEPS = 12;   // wing positions from lowest to highest

    // The tail bends a little every frame; this curve is reused for it.
    private final QuadCurve2D tail = new QuadCurve2D.Double();
    private final SpriteCache sprites = new SpriteCache(2 * STEPS, 27, 18, 18, StingrayPainter::drawPose);

    @Override
    public void paint(Graphics2D g, ObjectView object, double time) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.translate(object.getX(), object.getY());
        g2.scale(object.getFacing(), 1);

        // Whip tail, trailing behind and swaying, with a small barb near its base.
        double sway = 6 * Math.sin(3 * time + object.getPhase());
        tail.setCurve(-18, 0, -34, sway * 0.3, -50, sway);
        g2.setColor(EDGE);
        g2.setStroke(TAIL_STROKE);
        g2.draw(tail);
        g2.fillPolygon(new int[] {-26, -33, -27}, new int[] {-1, -4, 2}, 3);

        g2.dispose();

        int step = SpriteCache.step(Math.sin(2.4 * time + object.getPhase()), STEPS);
        sprites.paint(g, object.getX(), object.getY(), SpriteCache.pose(step, object.getFacing()));

        if (object.getAlert() > 0) {
            Graphics2D sparks = (Graphics2D) g.create();
            sparks.translate(object.getX(), object.getY());
            sparks.scale(object.getFacing(), 1);
            paintSparks(sparks, time);
            sparks.dispose();
        }
    }

    /** Draws the body once, facing and flapping as the pose says. */
    private static void drawPose(Graphics2D body, int pose) {
        // The body is squeezed and stretched up and down to flap the wings.
        body.scale(SpriteCache.facingOf(pose), 1 + FLAP * SpriteCache.swingOf(SpriteCache.stepOf(pose), STEPS));
        body.setPaint(BODY_SHADED);
        body.fill(BODY);
        body.setColor(RIDGE);
        body.fill(RIDGE_SHAPE);
        body.setColor(SPOT);
        body.fillOval(-8, -7, 4, 4);
        body.fillOval(2, 4, 4, 4);
        body.fillOval(-12, 3, 3, 3);
        body.fillOval(4, -9, 3, 3);
        body.setColor(EDGE);
        body.setStroke(LINE);
        body.draw(BODY);
        body.setColor(EYE);
        body.fillOval(12, -5, 4, 3);
        body.fillOval(12, 2, 4, 3);
    }

    /** Jagged sparks around the ray; a different set lights up on every flicker. */
    private void paintSparks(Graphics2D g2, double time) {
        int flicker = (int) (time * SPARK_FLICKERS_PER_SECOND);
        g2.setStroke(SPARK_STROKE);
        for (int i = 0; i < SPARKS; i++) {
            if ((flicker + i) % 2 != 0) {
                continue;   // this spark is dark right now
            }
            Graphics2D spark = (Graphics2D) g2.create();
            spark.rotate(Math.toRadians(-60 + i * 40));
            spark.setColor(SPARK);
            spark.drawPolyline(SPARK_X, SPARK_Y, SPARK_X.length);
            spark.setColor(SPARK_CORE);
            spark.drawLine(SPARK_X[0], SPARK_Y[0], SPARK_X[1], SPARK_Y[1]);
            spark.dispose();
        }
    }

    /** Wide, rounded wings meeting at a blunt snout in front and the tail behind. */
    private static Shape body() {
        Path2D body = new Path2D.Double();
        body.moveTo(25, 0);
        body.curveTo(20, -5, 10, -14, -2, -14);
        body.curveTo(-12, -14, -19, -6, -21, 0);
        body.curveTo(-19, 6, -12, 14, -2, 14);
        body.curveTo(10, 14, 20, 5, 25, 0);
        body.closePath();
        return body;
    }

    /** The raised middle of the body, from the snout to the tail. */
    private static Shape ridge() {
        Path2D ridge = new Path2D.Double();
        ridge.moveTo(20, 0);
        ridge.curveTo(14, -4, -8, -4, -19, 0);
        ridge.curveTo(-8, 4, 14, 4, 20, 0);
        ridge.closePath();
        return ridge;
    }
}
