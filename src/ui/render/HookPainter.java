package ui.render;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import model.Hook;

/**
 * Draws the fishing line and the tool on its end. The line is drawn before
 * the ocean objects and the tool after them, so a caught object looks like it
 * is hanging on the tool.
 */
public final class HookPainter {

    private static final Color ROPE_EDGE = new Color(30, 30, 30);
    private static final Color ROPE_CORE = new Color(225, 215, 190);
    private static final BasicStroke THIN = new BasicStroke(1);
    private static final BasicStroke ROPE_EDGE_STROKE = new BasicStroke(3);
    private static final Color AIM_COLOR = new Color(255, 250, 225, 130);
    private static final BasicStroke AIM_STROKE = new BasicStroke(
            1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1f, new float[] {3f, 4f}, 0f);
    private static final double AIM_START = 14;   // start a short tick past the tool center
    private static final double AIM_END = 32;     // and stop short of giving a full laser sight
    private static final double BEND = 0.03;      // how much a moving line bows, as a share of its length

    // One reusable curve, so no new shape object is made every frame.
    private final Path2D.Double curve = new Path2D.Double();
    private final PainterRegistry painters;   // knows how to draw each tool

    public HookPainter(PainterRegistry painters) {
        this.painters = painters;
    }

    /** The line from the boat to the hook on a flat sea. */
    public void paintRope(Graphics2D g, Hook hook, double time) {
        paintRope(g, hook, time, 0);
    }

    /** The line from the boat to the hook. It follows the boat's heave and bows while out. */
    public void paintRope(Graphics2D g, Hook hook, double time, double boatBob) {
        double ax = hook.getAnchorX();
        double ay = hook.getAnchorY() + boatBob;
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

    /** The tool on the line, turned to hang in line with the rope. */
    public void paintHead(Graphics2D g, Hook hook, double time) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.translate(hook.getTipX(), hook.getTipY());
        g2.rotate(-hook.getAngle());   // drawn hanging straight down, then swung to the rope's angle

        if (hook.isSwinging()) {
            g2.setColor(AIM_COLOR);
            g2.setStroke(AIM_STROKE);
            g2.drawLine(0, (int) AIM_START, 0, (int) AIM_END);
        }
        painters.paintTool(g2, hook.getLoadout().getSelectedType(), time, hook.isExtending());
        g2.dispose();
    }
}
