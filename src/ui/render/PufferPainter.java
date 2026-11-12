package ui.render;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import model.ObjectView;

/**
 * Draws a pufferfish. Its body follows the object's real radius, so it
 * visibly swells, and its spikes grow as it gets alarmed.
 */
public final class PufferPainter implements ObjectPainter {

    private static final Color BODY = new Color(232, 197, 71);
    private static final Color BELLY = new Color(255, 123, 84);
    private static final Color SPIKE = new Color(181, 101, 29);
    private static final Color DANGER = new Color(255, 90, 77);
    private static final BasicStroke SPIKE_STROKE = new BasicStroke(2);
    private static final BasicStroke DANGER_STROKE = new BasicStroke(3);
    private static final int SPIKES = 12;
    private static final double WARNING_LEVEL = 0.5;   // outline turns on above this alert

    @Override
    public void paint(Graphics2D g, ObjectView object) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.translate(object.getX(), object.getY());
        g2.scale(object.getFacing(), 1);

        double alert = object.getAlert();
        int r = (int) (object.getRadius() * 0.9);

        // Spikes all the way around; they stick out further when alarmed.
        g2.setColor(SPIKE);
        g2.setStroke(SPIKE_STROKE);
        double spikeLength = 3 + 7 * alert;
        for (int i = 0; i < SPIKES; i++) {
            double angle = 2 * Math.PI * i / SPIKES;
            double cos = Math.cos(angle);
            double sin = Math.sin(angle);
            g2.drawLine((int) (cos * r), (int) (sin * r),
                    (int) (cos * (r + spikeLength)), (int) (sin * (r + spikeLength)));
        }

        g2.setColor(BODY);
        g2.fillOval(-r, -r, 2 * r, 2 * r);
        g2.setColor(BELLY);
        g2.fillArc(-r, -r, 2 * r, 2 * r, 200, 140);   // lower part of the circle

        g2.setColor(Color.WHITE);
        g2.fillOval(r / 3, -r / 2, 9, 9);
        g2.setColor(Color.BLACK);
        g2.fillOval(r / 3 + 3, -r / 2 + 3, 4, 4);
        g2.drawOval(r - 6, 0, 5, 5);   // small round mouth

        if (alert > WARNING_LEVEL) {
            g2.setColor(DANGER);
            g2.setStroke(DANGER_STROKE);
            g2.drawOval(-r - 2, -r - 2, 2 * r + 4, 2 * r + 4);
        }

        g2.dispose();
    }
}
