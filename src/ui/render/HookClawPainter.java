package ui.render;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

/** The plain hook: a shank, a bend with a barb, and an eye for the line. */
public final class HookClawPainter implements ToolPainter {

    private static final Color METAL = new Color(200, 200, 205);
    private static final BasicStroke THIN = new BasicStroke(1);
    private static final BasicStroke HOOK_STROKE = new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);

    @Override
    public void paint(Graphics2D g, double time, boolean working) {
        g.setColor(METAL);
        g.setStroke(HOOK_STROKE);
        g.drawLine(0, -5, 0, 6);               // shank
        g.drawArc(-10, 0, 10, 12, 180, 180);   // the bend, curving back up on the left
        g.drawLine(-10, 6, -7, 2);             // barb
        g.setStroke(THIN);
        g.drawOval(-2, -9, 4, 4);              // the eye the line is tied to
    }
}
