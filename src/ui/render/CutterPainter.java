package ui.render;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

/** The net cutter: two crossed blades on a red pivot. The blades snip on the way out. */
public final class CutterPainter implements ToolPainter {

    private static final Color BLADE = new Color(215, 220, 226);
    private static final Color BLADE_EDGE = new Color(120, 130, 140);
    private static final Color HANDLE = new Color(242, 163, 58);   // bright, so it shows in dark water
    private static final Color PIVOT = new Color(214, 48, 49);
    private static final BasicStroke THIN = new BasicStroke(1);
    private static final BasicStroke HANDLE_STROKE = new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    private static final int[] BLADE_X = {-4, 4, 0};
    private static final int[] BLADE_Y = {0, 0, 22};
    private static final double REST_ANGLE = 0.42;   // how far each blade is turned, in radians

    @Override
    public void paint(Graphics2D g, double time, boolean working) {
        double open = working ? 0.30 + 0.22 * Math.sin(time * 22) : REST_ANGLE;
        paintHalf(g, open);
        paintHalf(g, -open);
        g.setColor(PIVOT);
        g.fillOval(-3, -3, 6, 6);
    }

    /** One blade with its handle, turned around the pivot at (0, 0). */
    private void paintHalf(Graphics2D g, double angle) {
        Graphics2D half = (Graphics2D) g.create();
        half.rotate(angle);
        half.setColor(BLADE);
        half.fillPolygon(BLADE_X, BLADE_Y, 3);
        half.setStroke(THIN);
        half.setColor(BLADE_EDGE);
        half.drawPolygon(BLADE_X, BLADE_Y, 3);
        half.setStroke(HANDLE_STROKE);
        half.setColor(HANDLE);
        half.drawLine(0, 0, 0, -11);          // the handle is the blade carried on past the pivot
        half.drawOval(-4, -20, 8, 9);         // finger loop
        half.dispose();
    }
}
