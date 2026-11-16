package ui.render;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.QuadCurve2D;
import java.awt.geom.RoundRectangle2D;
import model.ObjectView;

/** Draws a dented soda can: grey metal, a red label with a wave, tipping slowly from side to side. */
public final class CanPainter implements ObjectPainter {

    // A can is a cylinder: dark at both edges, with a bright streak where the light hits it.
    private static final float[] ROUND = {0f, 0.3f, 0.65f, 1f};
    private static final Paint METAL = new LinearGradientPaint(-10, 0, 10, 0, ROUND, new Color[] {
        new Color(120, 126, 134), new Color(232, 235, 238), new Color(172, 178, 185), new Color(110, 116, 124)});
    private static final Paint LABEL = new LinearGradientPaint(-10, 0, 10, 0, ROUND, new Color[] {
        new Color(150, 35, 35), new Color(240, 100, 95), new Color(200, 55, 55), new Color(135, 30, 30)});
    private static final Color WAVE = new Color(255, 245, 235, 220);
    private static final Color TOP = new Color(200, 204, 210);
    private static final Color TOP_RIM = new Color(150, 156, 164);
    private static final Color TAB = new Color(170, 176, 184);
    private static final Color DENT = new Color(80, 85, 92, 150);
    private static final Color OUTLINE = new Color(80, 85, 92);
    private static final BasicStroke LINE = new BasicStroke(1.1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    private static final BasicStroke FINE = new BasicStroke(0.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    private static final BasicStroke WAVE_STROKE = new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    private static final double TIP = 0.14;   // how far it tips, in radians

    private static final Shape BODY = new RoundRectangle2D.Double(-10, -13, 20, 27, 5, 5);
    private static final Shape LID = new Ellipse2D.Double(-10, -16, 20, 6);
    private static final Shape TAB_SHAPE = new RoundRectangle2D.Double(-3, -14.5, 6, 2.8, 2, 2);
    private static final Shape LABEL_WAVE = wave();
    private static final Shape DENT_SHAPE = new QuadCurve2D.Double(3, 7, 6, 9, 9, 6);

    private static final int STEPS = 12;   // tipping positions from one side to the other

    private final SpriteCache sprites = new SpriteCache(STEPS, 20, 20, 20, CanPainter::drawPose);

    @Override
    public void paint(Graphics2D g, ObjectView object, double time) {
        sprites.paint(g, object.getX(), object.getY(), SpriteCache.step(Math.sin(1.6 * time + object.getPhase()), STEPS));
    }

    /** Draws the can once, tipped as far as the pose says. */
    private static void drawPose(Graphics2D g2, int pose) {
        g2.rotate(TIP * SpriteCache.swingOf(pose, STEPS));

        g2.setPaint(METAL);
        g2.fill(BODY);
        g2.setPaint(LABEL);
        g2.fillRect(-10, -6, 20, 12);
        g2.setColor(WAVE);
        g2.setStroke(WAVE_STROKE);
        g2.draw(LABEL_WAVE);
        g2.setColor(DENT);
        g2.setStroke(FINE);
        g2.draw(DENT_SHAPE);
        g2.setColor(OUTLINE);
        g2.setStroke(LINE);
        g2.draw(BODY);

        // The lid with its ring pull.
        g2.setColor(TOP);
        g2.fill(LID);
        g2.setColor(TOP_RIM);
        g2.setStroke(FINE);
        g2.draw(LID);
        g2.setColor(TAB);
        g2.fill(TAB_SHAPE);
        g2.setColor(OUTLINE);
        g2.draw(TAB_SHAPE);
    }

    private static Shape wave() {
        Path2D wave = new Path2D.Double();
        wave.moveTo(-10, 1);
        wave.quadTo(-5, -4, 0, 1);
        wave.quadTo(5, 6, 10, 1);
        return wave;
    }
}
