package ui.render;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import model.Hook;

/**
 * Draws the fishing line and the hook on its end. The line is drawn before
 * the ocean objects and the hook after them, so a caught object looks like it
 * is hanging on the hook.
 */
public final class HookPainter {

    private static final Color ROPE_EDGE = new Color(30, 30, 30);
    private static final Color ROPE_CORE = new Color(225, 215, 190);
    private static final Color METAL = new Color(200, 200, 205);
    private static final BasicStroke THIN = new BasicStroke(1);
    private static final BasicStroke ROPE_EDGE_STROKE = new BasicStroke(3);
    private static final BasicStroke HOOK_STROKE = new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    private static final double BEND = 0.03;   // how much a moving line bows, as a share of its length

    // One reusable curve, so no new shape object is made every frame.
    private final Path2D.Double curve = new Path2D.Double();

    /** The line from the boat to the hook. It bows from side to side while the hook is out. */
    public void paintRope(Graphics2D g, Hook hook, double time) {
        double ax = hook.getAnchorX();
        double ay = hook.getAnchorY();
        double tx = hook.getTipX();
        double ty = hook.getTipY();

        // The bend is sideways to the line: swap the direction's x and y.
        double bend = hook.isLineOut() ? BEND * hook.getLength() * Math.sin(time * 6) : 0;
        double length = Math.max(1, Math.hypot(tx - ax, ty - ay));
        double sideX = -(ty - ay) / length;
        double sideY = (tx - ax) / length;

        curve.reset();
        curve.moveTo(ax, ay);
        curve.quadTo((ax + tx) / 2 + sideX * bend, (ay + ty) / 2 + sideY * bend, tx, ty);
        // A dark line with a pale core shows up against both bright and dark water.
        g.setColor(ROPE_EDGE);
        g.setStroke(ROPE_EDGE_STROKE);
        g.draw(curve);
        g.setColor(ROPE_CORE);
        g.setStroke(THIN);
        g.draw(curve);
    }

    /** The hook itself, turned to hang in line with the rope. */
    public void paintHead(Graphics2D g, Hook hook) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.translate(hook.getTipX(), hook.getTipY());
        g2.rotate(-hook.getAngle());   // drawn hanging straight down, then swung to the rope's angle

        g2.setColor(METAL);
        g2.setStroke(HOOK_STROKE);
        g2.drawLine(0, -5, 0, 6);               // shank
        g2.drawArc(-10, 0, 10, 12, 180, 180);   // the bend, curving back up on the left
        g2.drawLine(-10, 6, -7, 2);             // barb
        g2.setStroke(THIN);
        g2.drawOval(-2, -9, 4, 4);              // the eye the line is tied to

        g2.dispose();
    }
}
