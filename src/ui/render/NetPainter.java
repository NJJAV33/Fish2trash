package ui.render;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import model.ObjectView;

/** Draws a lost fishing net: a rope mesh in a loose blob, with two floats. */
public final class NetPainter implements ObjectPainter {

    private static final Color FILL = new Color(210, 225, 200, 40);
    private static final Color ROPE = new Color(205, 215, 185, 200);
    private static final Color FLOAT = new Color(240, 140, 40);
    private static final BasicStroke MESH_STROKE = new BasicStroke(1.2f);
    private static final BasicStroke EDGE_STROKE = new BasicStroke(2);
    private static final int MESH_GAP = 9;      // pixels between mesh lines
    private static final int MESH_SLANT = 14;   // how far each line leans

    @Override
    public void paint(Graphics2D g, ObjectView object) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.translate(object.getX(), object.getY());

        int r = (int) object.getRadius();
        Ellipse2D blob = new Ellipse2D.Double(-r, -r * 0.8, 2 * r, 1.6 * r);

        g2.setColor(FILL);
        g2.fill(blob);

        // Diamond mesh: two sets of slanted lines, cut off at the blob's edge.
        Graphics2D mesh = (Graphics2D) g2.create();
        mesh.clip(blob);
        mesh.setColor(ROPE);
        mesh.setStroke(MESH_STROKE);
        for (int x = -r - MESH_SLANT; x <= r + MESH_SLANT; x += MESH_GAP) {
            mesh.drawLine(x, -r, x + MESH_SLANT, r);
            mesh.drawLine(x, -r, x - MESH_SLANT, r);
        }
        mesh.dispose();

        g2.setColor(ROPE);
        g2.setStroke(EDGE_STROKE);
        g2.draw(blob);

        g2.setColor(FLOAT);
        g2.fillOval(-r / 2 - 5, (int) (-r * 0.8) - 5, 10, 10);
        g2.fillOval(r / 2 - 5, (int) (-r * 0.8) - 5, 10, 10);

        g2.dispose();
    }
}
