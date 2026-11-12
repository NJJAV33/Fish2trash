package ui.render;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

/** The magnet: a red horseshoe with grey tips, opening downwards. */
public final class MagnetPainter implements ToolPainter {

    private static final Color BODY = new Color(214, 48, 49);
    private static final Color TIP = new Color(215, 220, 226);
    private static final Color RING = new Color(200, 200, 205);
    private static final BasicStroke THICK = new BasicStroke(7, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND);
    private static final BasicStroke THIN = new BasicStroke(1);
    private static final BasicStroke FIELD = new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
            1, new float[] {3, 4}, 0);   // dashed

    @Override
    public void paint(Graphics2D g, double time, boolean working) {
        if (working) {
            // The pull of the magnet: dashed arcs under the tips that breathe in and out.
            int reach = (int) (4 + 3 * Math.sin(time * 9));
            g.setColor(new Color(255, 255, 255, 130));
            g.setStroke(FIELD);
            g.drawArc(-12, 8, 24, 10 + reach, 200, 140);
            g.drawArc(-17, 6, 34, 18 + reach, 200, 140);
        }

        g.setStroke(THICK);
        g.setColor(BODY);
        g.drawArc(-11, -13, 22, 22, 0, 180);   // the top of the horseshoe
        g.drawLine(-11, -2, -11, 8);           // left leg
        g.drawLine(11, -2, 11, 8);             // right leg
        g.setColor(TIP);
        g.drawLine(-11, 8, -11, 14);
        g.drawLine(11, 8, 11, 14);

        g.setStroke(THIN);
        g.setColor(RING);
        g.drawOval(-2, -20, 4, 4);             // the ring the line is tied to
    }
}
