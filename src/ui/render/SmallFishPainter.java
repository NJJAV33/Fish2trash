package ui.render;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.QuadCurve2D;
import model.ObjectView;

/** Draws a small orange fish that faces the way it swims and wags its tail. */
public final class SmallFishPainter implements ObjectPainter {

    // Lit from above: the back is brighter than the underside.
    private static final Paint BODY = new GradientPaint(0, -11, new Color(255, 182, 72), 0, 11, new Color(226, 104, 18));
    private static final Color BELLY = new Color(255, 214, 160);
    private static final Color SHINE = new Color(255, 255, 255, 110);
    private static final Color FIN = new Color(236, 118, 26);
    private static final Color FIN_RAY = new Color(190, 80, 10);
    private static final Color OUTLINE = new Color(150, 60, 10);
    private static final Color PUPIL = new Color(20, 16, 14);
    private static final BasicStroke LINE = new BasicStroke(1.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    private static final BasicStroke FINE = new BasicStroke(0.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    private static final double WAG = 0.35;   // how far the tail swings, in radians

    // Shapes are made once, facing right around (0, 0); the painter flips them for a fish swimming left.
    private static final Shape BODY_SHAPE = body();
    private static final Shape TAIL = tail();
    private static final Shape BACK_FIN = backFin();
    private static final Shape BELLY_SHAPE = new Ellipse2D.Double(-11, 2, 22, 7);
    private static final Shape SIDE_FIN = new Ellipse2D.Double(0, 1, 6.5, 3.5);
    private static final Shape GILL = new QuadCurve2D.Double(8, -7, 4, 0, 8, 7);

    private static final int STEPS = 12;   // tail positions from one side to the other

    private final SpriteCache sprites = new SpriteCache(2 * STEPS, 34, 20, 20, SmallFishPainter::drawPose);

    @Override
    public void paint(Graphics2D g, ObjectView object, double time) {
        int step = SpriteCache.step(Math.sin(10 * time + object.getPhase()), STEPS);
        sprites.paint(g, object.getX(), object.getY(), SpriteCache.pose(step, object.getFacing()));
    }

    /** Draws the fish once, facing and wagging as the pose says. */
    private static void drawPose(Graphics2D g2, int pose) {
        g2.scale(SpriteCache.facingOf(pose), 1);
        double beat = SpriteCache.swingOf(SpriteCache.stepOf(pose), STEPS);

        // The tail swings around the point where it joins the body.
        AffineTransform still = g2.getTransform();
        g2.rotate(WAG * beat, -14, 0);
        fill(g2, TAIL, FIN);
        g2.setColor(FIN_RAY);
        g2.setStroke(FINE);
        g2.drawLine(-17, 0, -26, -6);
        g2.drawLine(-17, 0, -26, 6);
        g2.setTransform(still);

        fill(g2, BACK_FIN, FIN);
        g2.setPaint(BODY);
        g2.fill(BODY_SHAPE);
        g2.setColor(BELLY);
        g2.fill(BELLY_SHAPE);
        g2.setColor(SHINE);
        g2.fillOval(-9, -9, 15, 4);   // light on its back
        g2.setColor(OUTLINE);
        g2.setStroke(LINE);
        g2.draw(BODY_SHAPE);
        g2.setStroke(FINE);
        g2.draw(GILL);

        // The side fin paddles in time with the tail.
        g2.rotate(0.4 + 0.3 * beat, 0, 3);
        fill(g2, SIDE_FIN, FIN);
        g2.setTransform(still);

        g2.setColor(Color.WHITE);
        g2.fillOval(8, -7, 8, 8);
        g2.setColor(PUPIL);
        g2.fillOval(11, -5, 4, 4);
        g2.setColor(Color.WHITE);
        g2.fillOval(12, -5, 2, 2);
        g2.setColor(OUTLINE);
        g2.setStroke(FINE);
        g2.drawLine(15, 3, 18, 2);   // mouth
    }

    private static void fill(Graphics2D g, Shape shape, Color colour) {
        g.setColor(colour);
        g.fill(shape);
        g.setColor(OUTLINE);
        g.setStroke(LINE);
        g.draw(shape);
    }

    /** A rounded body with a blunt nose, narrowing toward the tail. */
    private static Shape body() {
        Path2D body = new Path2D.Double();
        body.moveTo(19, 1);
        body.curveTo(18, -8, 7, -12, -4, -10);
        body.curveTo(-12, -8.5, -16, -4, -17, 0);
        body.curveTo(-16, 4, -12, 8.5, -4, 10);
        body.curveTo(7, 12, 18, 9, 19, 1);
        body.closePath();
        return body;
    }

    /** A fan tail with a shallow notch. */
    private static Shape tail() {
        Path2D tail = new Path2D.Double();
        tail.moveTo(-14, 0);
        tail.curveTo(-20, -4, -25, -11, -30, -12);
        tail.quadTo(-25, 0, -30, 12);
        tail.curveTo(-25, 11, -20, 4, -14, 0);
        tail.closePath();
        return tail;
    }

    private static Shape backFin() {
        Path2D fin = new Path2D.Double();
        fin.moveTo(-9, -8);
        fin.quadTo(-6, -18, 6, -16);
        fin.quadTo(2, -12, 6, -9);
        fin.closePath();
        return fin;
    }
}
