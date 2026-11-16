package ui.render;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import model.ObjectView;

/** Draws a see-through plastic bottle floating on its side, rocking gently. */
public final class BottlePainter implements ObjectPainter {

    // Round things are lighter on one side than the other. The gradients run
    // across the bottle, in its own coordinates, so they turn with it.
    private static final Paint PLASTIC = new GradientPaint(
            -7, 0, new Color(225, 245, 250, 200), 7, 0, new Color(130, 192, 218, 170));
    private static final Color OUTLINE = new Color(90, 150, 175);
    private static final Color RIB = new Color(110, 170, 195, 160);
    private static final Paint CAP = new GradientPaint(-4, 0, new Color(96, 170, 245), 4, 0, new Color(28, 96, 180));
    private static final Color CAP_LINE = new Color(20, 70, 140);
    private static final Paint LABEL = new GradientPaint(-7, 0, new Color(100, 210, 145), 7, 0, new Color(38, 136, 86));
    private static final Color LABEL_STRIPE = new Color(255, 255, 255, 170);
    private static final Color SHINE = new Color(255, 255, 255, 200);
    private static final BasicStroke LINE = new BasicStroke(1.1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    private static final BasicStroke FINE = new BasicStroke(0.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    private static final BasicStroke SHINE_STROKE = new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    private static final double TILT = Math.toRadians(75);
    private static final double ROCK = 0.12;   // how far it rocks, in radians

    // Drawn standing up with the cap at the top; the painter tips it over.
    private static final Shape BODY = body();
    private static final Shape CAP_SHAPE = new RoundRectangle2D.Double(-4, -18, 8, 5, 2, 2);

    private static final int STEPS = 12;   // rocking positions from one side to the other

    private final SpriteCache sprites = new SpriteCache(STEPS, 20, 20, 20, BottlePainter::drawPose);

    @Override
    public void paint(Graphics2D g, ObjectView object, double time) {
        sprites.paint(g, object.getX(), object.getY(), SpriteCache.step(Math.sin(2 * time + object.getPhase()), STEPS));
    }

    /** Draws the bottle once, tipped over and rocked as far as the pose says. */
    private static void drawPose(Graphics2D g2, int pose) {
        g2.rotate(TILT + ROCK * SpriteCache.swingOf(pose, STEPS));

        g2.setPaint(PLASTIC);
        g2.fill(BODY);
        g2.setPaint(LABEL);
        g2.fillRect(-7, -1, 14, 7);
        g2.setColor(LABEL_STRIPE);
        g2.setStroke(FINE);
        g2.drawLine(-7, 2, 7, 2);
        g2.setColor(RIB);
        g2.drawLine(-6, 9, 6, 9);   // ridges near the bottom
        g2.drawLine(-6, 11, 6, 11);
        g2.setColor(OUTLINE);
        g2.setStroke(LINE);
        g2.draw(BODY);

        g2.setPaint(CAP);
        g2.fill(CAP_SHAPE);
        g2.setColor(CAP_LINE);
        g2.setStroke(FINE);
        g2.draw(CAP_SHAPE);
        g2.drawLine(-2, -17, -2, -14);   // grip lines on the cap
        g2.drawLine(0, -17, 0, -14);
        g2.drawLine(2, -17, 2, -14);

        g2.setColor(SHINE);
        g2.setStroke(SHINE_STROKE);
        g2.drawLine(-4, -5, -4, 10);
    }

    /** A bottle with rounded shoulders, a neck and a flat bottom. */
    private static Shape body() {
        Path2D body = new Path2D.Double();
        body.moveTo(-3, -13);
        body.lineTo(3, -13);
        body.lineTo(3, -10);
        body.curveTo(3, -8, 7, -7, 7, -3);
        body.lineTo(7, 11);
        body.quadTo(7, 13.5, 4.5, 13.5);
        body.lineTo(-4.5, 13.5);
        body.quadTo(-7, 13.5, -7, 11);
        body.lineTo(-7, -3);
        body.curveTo(-7, -7, -3, -8, -3, -10);
        body.closePath();
        return body;
    }
}
