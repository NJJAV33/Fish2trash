package ui.render;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import model.ObjectView;
import util.GameConstants;

/**
 * Draws a lost fishing net: a rope mesh in a loose blob, with two floats and
 * frayed ends, breathing slowly in the current.
 */
public final class NetPainter implements ObjectPainter {

    private static final Color FILL = new Color(210, 225, 200, 40);
    private static final Color ROPE = new Color(205, 215, 185, 200);
    private static final Paint FLOAT = new GradientPaint(0, -31, new Color(255, 170, 70), 0, -20, new Color(215, 105, 20));
    private static final Color FLOAT_SHINE = new Color(255, 255, 255, 150);
    private static final BasicStroke MESH_STROKE = new BasicStroke(1.2f);
    private static final BasicStroke EDGE_STROKE = new BasicStroke(2);
    private static final int MESH_GAP = 9;      // pixels between mesh lines
    private static final int MESH_SLANT = 14;   // how far each line leans
    private static final double SWAY = 0.05;    // it stretches and squeezes by 5%

    // Every net is the same size, so its outline is made once and reused.
    private static final int R = (int) GameConstants.RADIUS_NET;
    private static final Ellipse2D BLOB = new Ellipse2D.Double(-R, -R * 0.8, 2 * R, 1.6 * R);
    private static final int TOP = (int) (-R * 0.8);
    private static final int BOTTOM = (int) (R * 0.8);

    // The mesh is slow to draw, and every net looks the same apart from its
    // sway. So each sway step is drawn once into a small picture and copied
    // from then on.
    private static final int SWAY_STEPS = 9;                  // from fully squeezed to fully stretched
    private static final int HALF_WIDTH = R + 8;              // the picture's size around the net's middle
    private static final int ABOVE = R + 2;
    private static final int BELOW = R + 12;

    private final BufferedImage[] pictures = new BufferedImage[SWAY_STEPS];
    private double pictureScale;   // the size the pictures were made for

    @Override
    public void paint(Graphics2D g, ObjectView object, double time) {
        double scale = PixelImages.scaleOf(g);
        if (scale != pictureScale) {
            Arrays.fill(pictures, null);   // the window shows the game at another size now
            pictureScale = scale;
        }
        double sway = Math.sin(1.5 * time + object.getPhase());   // -1 to 1
        int step = (int) Math.round((sway + 1) / 2 * (SWAY_STEPS - 1));
        if (pictures[step] == null) {
            pictures[step] = render(SWAY * (2.0 * step / (SWAY_STEPS - 1) - 1), scale);
        }
        PixelImages.paint(g, pictures[step], object.getX() - HALF_WIDTH, object.getY() - ABOVE, scale);
    }

    /** Draws the net once, stretched by the given sway, into a picture of its own. */
    private static BufferedImage render(double sway, double scale) {
        BufferedImage picture = PixelImages.create(2 * HALF_WIDTH, ABOVE + BELOW, scale, true);
        Graphics2D g2 = PixelImages.draw(picture, scale);
        g2.translate(HALF_WIDTH, ABOVE);
        g2.scale(1 + sway, 1 - sway);   // wider when flatter, like something soft

        // Frayed ends hanging below
        g2.setColor(ROPE);
        g2.setStroke(MESH_STROKE);
        g2.drawLine(-12, BOTTOM - 4, -16, BOTTOM + 9);
        g2.drawLine(2, BOTTOM - 1, 0, BOTTOM + 12);
        g2.drawLine(14, BOTTOM - 4, 19, BOTTOM + 8);

        g2.setColor(FILL);
        g2.fill(BLOB);

        // Diamond mesh: two sets of slanted lines, cut off at the blob's edge.
        Graphics2D mesh = (Graphics2D) g2.create();
        mesh.clip(BLOB);
        mesh.setColor(ROPE);
        for (int x = -R - MESH_SLANT; x <= R + MESH_SLANT; x += MESH_GAP) {
            mesh.drawLine(x, -R, x + MESH_SLANT, R);
            mesh.drawLine(x, -R, x - MESH_SLANT, R);
        }
        mesh.dispose();

        g2.setStroke(EDGE_STROKE);
        g2.draw(BLOB);

        g2.setPaint(FLOAT);
        g2.fillOval(-R / 2 - 5, TOP - 5, 10, 10);
        g2.fillOval(R / 2 - 5, TOP - 5, 10, 10);
        g2.setColor(FLOAT_SHINE);
        g2.fillOval(-R / 2 - 3, TOP - 4, 4, 3);
        g2.fillOval(R / 2 - 3, TOP - 4, 4, 3);

        g2.dispose();
        return picture;
    }
}
