package ui.render;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import model.ObjectView;

/** Draws a long, thin barracuda with a jutting jaw. It flashes while it warns and charges. */
public final class BarracudaPainter implements ObjectPainter {

    // Dark on the back, silver on the sides, pale underneath.
    private static final Paint BODY_SHADED = new GradientPaint(0, -7, new Color(92, 108, 122), 0, 7, new Color(226, 234, 240));
    private static final Paint FLASH_SHADED = new GradientPaint(0, -7, new Color(255, 110, 90), 0, 7, new Color(255, 245, 235));
    private static final Color FIN = new Color(84, 98, 110);
    private static final Color FLASH_FIN = new Color(255, 120, 100);
    private static final Color BAR = new Color(50, 62, 74, 150);
    private static final Color OUTLINE = new Color(40, 50, 60);
    private static final Color EYE = new Color(255, 60, 50);
    private static final Color TEETH = Color.WHITE;
    private static final BasicStroke LINE = new BasicStroke(1.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    private static final BasicStroke BAR_STROKE = new BasicStroke(2.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    private static final double WAG = 0.3;
    private static final double BLINKS_PER_SECOND = 8;

    // Made once, facing right; flipped when it swims left.
    private static final Shape BODY = body();
    private static final Shape JAW = jaw();
    private static final Shape TEETH_SHAPE = teeth();
    private static final Shape TAIL = tail();
    private static final Shape BACK_FIN_1 = fin(-4, -6, 2, -14, 6, -6);
    private static final Shape BACK_FIN_2 = fin(-22, -4, -18, -10, -15, -4);
    private static final Shape LOWER_FIN = fin(-20, 4, -16, 10, -13, 4);

    private static final int STEPS = 12;   // tail positions from one side to the other

    // One set of pictures in the normal colours, one in the warning colours.
    private final SpriteCache calm = new SpriteCache(2 * STEPS, 48, 18, 18, (g2, pose) -> drawPose(g2, pose, false));
    private final SpriteCache flashing = new SpriteCache(2 * STEPS, 48, 18, 18, (g2, pose) -> drawPose(g2, pose, true));

    @Override
    public void paint(Graphics2D g, ObjectView object, double time) {
        // While alarmed it blinks between its warning colours and its normal ones.
        boolean lit = object.getAlert() > 0 && (int) (time * BLINKS_PER_SECOND * 2) % 2 == 0;
        int step = SpriteCache.step(Math.sin(16 * time + object.getPhase()), STEPS);
        (lit ? flashing : calm).paint(g, object.getX(), object.getY(), SpriteCache.pose(step, object.getFacing()));
    }

    /** Draws the barracuda once, facing, wagging and coloured as asked. */
    private static void drawPose(Graphics2D g2, int pose, boolean lit) {
        g2.scale(SpriteCache.facingOf(pose), 1);
        Color fin = lit ? FLASH_FIN : FIN;

        AffineTransform still = g2.getTransform();
        g2.rotate(WAG * SpriteCache.swingOf(SpriteCache.stepOf(pose), STEPS), -30, 0);
        fill(g2, TAIL, fin);
        g2.setTransform(still);

        fill(g2, BACK_FIN_1, fin);
        fill(g2, BACK_FIN_2, fin);
        fill(g2, LOWER_FIN, fin);
        fill(g2, JAW, fin);
        g2.setPaint(lit ? FLASH_SHADED : BODY_SHADED);
        g2.fill(BODY);
        if (!lit) {
            // Dark bars across the back.
            g2.setColor(BAR);
            g2.setStroke(BAR_STROKE);
            for (int x = -22; x <= 14; x += 9) {
                g2.drawLine(x, -5, x - 2, 0);
            }
        }
        g2.setColor(OUTLINE);
        g2.setStroke(LINE);
        g2.draw(BODY);

        g2.setColor(TEETH);
        g2.fill(TEETH_SHAPE);

        g2.setColor(EYE);
        g2.fillOval(17, -4, 6, 6);
        g2.setColor(Color.WHITE);
        g2.fillOval(20, -3, 2, 2);
    }

    private static void fill(Graphics2D g, Shape shape, Color colour) {
        g.setColor(colour);
        g.fill(shape);
        g.setColor(OUTLINE);
        g.setStroke(LINE);
        g.draw(shape);
    }

    /** A long torpedo with a pointed snout. */
    private static Shape body() {
        Path2D body = new Path2D.Double();
        body.moveTo(36, -1);
        body.curveTo(28, -6, 10, -8, -10, -7);
        body.curveTo(-24, -6, -31, -3, -33, 0);
        body.curveTo(-31, 3, -24, 6, -10, 7);
        body.curveTo(8, 7, 22, 5, 30, 2);
        body.closePath();
        return body;
    }

    /** The lower jaw, jutting out past the snout. */
    private static Shape jaw() {
        Path2D jaw = new Path2D.Double();
        jaw.moveTo(20, 2);
        jaw.lineTo(40, 1);
        jaw.quadTo(36, 6, 24, 6);
        jaw.closePath();
        return jaw;
    }

    private static Shape teeth() {
        Path2D teeth = new Path2D.Double();
        teeth.moveTo(23, 2);
        for (int x = 23; x < 38; x += 3) {
            teeth.lineTo(x + 1.5, -0.5);
            teeth.lineTo(x + 3, 1.6);
        }
        teeth.lineTo(23, 2);
        teeth.closePath();
        return teeth;
    }

    private static Shape tail() {
        Path2D tail = new Path2D.Double();
        tail.moveTo(-30, 0);
        tail.curveTo(-35, -4, -40, -9, -46, -12);
        tail.quadTo(-39, 0, -46, 12);
        tail.curveTo(-40, 9, -35, 4, -30, 0);
        tail.closePath();
        return tail;
    }

    private static Shape fin(double x1, double y1, double x2, double y2, double x3, double y3) {
        Path2D fin = new Path2D.Double();
        fin.moveTo(x1, y1);
        fin.lineTo(x2, y2);
        fin.quadTo((x2 + x3) / 2, (y2 + y3) / 2, x3, y3);
        fin.closePath();
        return fin;
    }
}
