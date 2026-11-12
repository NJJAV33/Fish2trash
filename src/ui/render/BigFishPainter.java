package ui.render;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import model.ObjectView;

/** Draws a big blue fish with a forked tail and stripes. */
public final class BigFishPainter implements ObjectPainter {

    private static final Color BODY = new Color(61, 126, 191);
    private static final Color BELLY = new Color(170, 205, 235);
    private static final Color STRIPE = new Color(35, 80, 135);
    private static final BasicStroke STRIPE_STROKE = new BasicStroke(2);

    @Override
    public void paint(Graphics2D g, ObjectView object) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.translate(object.getX(), object.getY());
        g2.scale(object.getFacing(), 1);   // drawn facing right; flipped when it swims left

        g2.setColor(BODY);
        g2.fillOval(-31, -12, 62, 24);
        // Forked tail: two triangles behind the body
        g2.fillPolygon(new int[] {-26, -46, -40}, new int[] {0, -14, 0}, 3);
        g2.fillPolygon(new int[] {-26, -46, -40}, new int[] {0, 14, 0}, 3);
        g2.fillPolygon(new int[] {-6, 6, -12}, new int[] {-11, -11, -20}, 3);   // back fin

        g2.setColor(BELLY);
        g2.fillOval(-22, 0, 44, 10);

        g2.setColor(STRIPE);
        g2.setStroke(STRIPE_STROKE);
        g2.drawLine(-12, -9, -12, 8);
        g2.drawLine(-2, -11, -2, 9);
        g2.drawLine(8, -10, 8, 8);

        g2.setColor(Color.WHITE);
        g2.fillOval(17, -7, 8, 8);
        g2.setColor(Color.BLACK);
        g2.fillOval(20, -5, 4, 4);

        g2.dispose();
    }
}
