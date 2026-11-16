package ui.render;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.QuadCurve2D;
import model.ObjectView;

/** Draws a big blue fish with a forked tail and stripes. Fast ones trail speed lines. */
public final class BigFishPainter implements ObjectPainter {

    private static final Color FIN = new Color(52, 108, 170);
    // Lit from above: the back is brighter than the underside.
    private static final Paint BODY_SHADED = new GradientPaint(0, -12, new Color(90, 154, 220), 0, 12, new Color(36, 84, 145));
    private static final Paint BELLY = new GradientPaint(0, 2, new Color(200, 225, 245), 0, 11, new Color(150, 190, 225));
    private static final Color SHINE = new Color(255, 255, 255, 90);
    private static final Color STRIPE = new Color(30, 70, 125, 200);
    private static final Color OUTLINE = new Color(22, 52, 92);
    private static final Color SPEED_LINE = new Color(255, 255, 255, 90);
    private static final Color PUPIL = new Color(16, 16, 20);
    private static final BasicStroke LINE = new BasicStroke(1.3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    private static final BasicStroke FINE = new BasicStroke(1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    private static final BasicStroke STRIPE_STROKE = new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    private static final double WAG = 0.28;
    private static final double FAST = 120;   // pixels per second: from here on it shows speed lines

    // Made once, facing right; flipped when it swims left.
    private static final Shape BODY = body();
    private static final Shape BELLY_SHAPE = belly();
    private static final Shape TAIL = tail();
    private static final Shape BACK_FIN = triangle(-12, -10, -1, -22, 6, -11);
    private static final Shape LOWER_FIN = triangle(-8, 9, 0, 17, 5, 10);
    private static final Shape GILL = new QuadCurve2D.Double(19, -8, 14, 0, 19, 8);
    private static final Shape SPEED_LINES = speedLines();
    private static final Shape[] STRIPES = {
        new QuadCurve2D.Double(-14, -9, -16, -3, -14, 2),
        new QuadCurve2D.Double(-4, -11, -6, -4, -4, 2),
        new QuadCurve2D.Double(6, -11, 4, -4, 6, 2),
    };

    private static final int STEPS = 12;   // tail positions from one side to the other

    private final SpriteCache sprites = new SpriteCache(2 * STEPS, 50, 26, 26, BigFishPainter::drawPose);

    @Override
    public void paint(Graphics2D g, ObjectView object, double time) {
        if (Math.abs(object.getSpeedX()) > FAST) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.translate(object.getX(), object.getY());
            g2.scale(object.getFacing(), 1);
            g2.setColor(SPEED_LINE);
            g2.setStroke(STRIPE_STROKE);
            g2.draw(SPEED_LINES);
            g2.dispose();
        }
        int step = SpriteCache.step(Math.sin(14 * time + object.getPhase()), STEPS);
        sprites.paint(g, object.getX(), object.getY(), SpriteCache.pose(step, object.getFacing()));
    }

    /** Draws the fish once, facing and wagging as the pose says. */
    private static void drawPose(Graphics2D g2, int pose) {
        g2.scale(SpriteCache.facingOf(pose), 1);   // drawn facing right; flipped when it swims left

        // Forked tail, swinging where it joins the body.
        AffineTransform still = g2.getTransform();
        g2.rotate(WAG * SpriteCache.swingOf(SpriteCache.stepOf(pose), STEPS), -26, 0);
        fill(g2, TAIL, FIN);
        g2.setTransform(still);

        fill(g2, BACK_FIN, FIN);
        fill(g2, LOWER_FIN, FIN);
        g2.setPaint(BODY_SHADED);
        g2.fill(BODY);
        g2.setPaint(BELLY);
        g2.fill(BELLY_SHAPE);
        g2.setColor(STRIPE);
        g2.setStroke(STRIPE_STROKE);
        for (Shape stripe : STRIPES) {
            g2.draw(stripe);
        }
        g2.setColor(SHINE);
        g2.fillOval(-18, -10, 30, 4);   // light on its back
        g2.setColor(OUTLINE);
        g2.setStroke(LINE);
        g2.draw(BODY);
        g2.setStroke(FINE);
        g2.draw(GILL);
        g2.drawLine(27, 4, 31, 3);   // mouth

        g2.setColor(Color.WHITE);
        g2.fillOval(19, -7, 8, 8);
        g2.setColor(PUPIL);
        g2.fillOval(22, -5, 4, 4);
        g2.setColor(Color.WHITE);
        g2.fillOval(23, -5, 2, 2);
    }

    private static void fill(Graphics2D g, Shape shape, Color colour) {
        g.setColor(colour);
        g.fill(shape);
        g.setColor(OUTLINE);
        g.setStroke(LINE);
        g.draw(shape);
    }

    /** Three short lines trailing behind a fast fish, as one shape so they cost one stroke. */
    private static Shape speedLines() {
        Path2D lines = new Path2D.Double();
        lines.moveTo(-52, -8);
        lines.lineTo(-68, -8);
        lines.moveTo(-56, 0);
        lines.lineTo(-76, 0);
        lines.moveTo(-52, 8);
        lines.lineTo(-68, 8);
        return lines;
    }

    /** A long, strong body: deep in front, tapering to a narrow tail stem. */
    private static Shape body() {
        Path2D body = new Path2D.Double();
        body.moveTo(32, 2);
        body.curveTo(29, -9, 14, -13, -2, -12);
        body.curveTo(-16, -11, -24, -5, -28, 0);
        body.curveTo(-24, 5, -16, 10, -2, 11);
        body.curveTo(14, 12, 29, 9, 32, 2);
        body.closePath();
        return body;
    }

    private static Shape belly() {
        Path2D belly = new Path2D.Double();
        belly.moveTo(30, 4);
        belly.curveTo(20, 4, 0, 4, -22, 3);
        belly.curveTo(-16, 8, -4, 11, 6, 11);
        belly.curveTo(18, 11, 28, 8, 30, 4);
        belly.closePath();
        return belly;
    }

    /** A deep, crescent-shaped forked tail. */
    private static Shape tail() {
        Path2D tail = new Path2D.Double();
        tail.moveTo(-25, 0);
        tail.curveTo(-31, -6, -38, -12, -47, -16);
        tail.quadTo(-37, 0, -47, 16);
        tail.curveTo(-38, 12, -31, 6, -25, 0);
        tail.closePath();
        return tail;
    }

    private static Shape triangle(double x1, double y1, double x2, double y2, double x3, double y3) {
        Path2D shape = new Path2D.Double();
        shape.moveTo(x1, y1);
        shape.quadTo((x1 + x2) / 2 - 1, (y1 + y2) / 2, x2, y2);
        shape.quadTo((x2 + x3) / 2 + 1, (y2 + y3) / 2, x3, y3);
        shape.closePath();
        return shape;
    }
}
