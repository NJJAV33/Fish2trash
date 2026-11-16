package ui.render;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.QuadCurve2D;
import java.awt.geom.Rectangle2D;
import model.ObjectView;
import util.GameConstants;

/**
 * Draws a lost fishing net, breathing slowly in the current. Nets come in
 * several designs (a pale gill net, a blue nylon net, a knot of rope, a net
 * grown over with kelp), and each net keeps the one it was given.
 */
public final class NetPainter implements ObjectPainter {

    /** The net designs. Each draws itself around (0, 0). */
    private enum Look {
        GILL_NET {
            @Override
            void draw(Graphics2D g) {
                frayed(g, PALE_ROPE);
                mesh(g, BLOB, new Color(210, 225, 200, 40), PALE_ROPE, 9);
                edge(g, BLOB, PALE_ROPE);
                knots(g, PALE_ROPE);
                floats(g, new Color(255, 170, 70), new Color(215, 105, 20));
            }
        },
        NYLON {
            @Override
            void draw(Graphics2D g) {
                Color rope = new Color(90, 160, 235, 220);
                frayed(g, rope);
                mesh(g, WIDE_BLOB, new Color(80, 150, 230, 40), rope, 7);
                edge(g, WIDE_BLOB, new Color(220, 60, 50));   // a red rope along the edge
                floats(g, new Color(250, 250, 250), new Color(190, 195, 200));
            }
        },
        ROPE_KNOT {
            @Override
            void draw(Graphics2D g) {
                // Thick rope wound into a tangle, with a black float caught in it.
                Color rope = new Color(240, 180, 60);
                Color shade = new Color(170, 110, 30);
                for (int pass = 0; pass < 2; pass++) {
                    g.setColor(pass == 0 ? shade : rope);
                    g.setStroke(pass == 0 ? ROPE : ROPE_TOP);
                    g.draw(new Ellipse2D.Double(-26, -16, 40, 26));
                    g.draw(new Ellipse2D.Double(-14, -20, 36, 30));
                    g.draw(new Ellipse2D.Double(-22, -8, 44, 20));
                    g.draw(new QuadCurve2D.Double(-30, 4, -10, 30, 18, 14));
                    g.draw(new QuadCurve2D.Double(22, -6, 34, 10, 28, 22));
                }
                g.setColor(shade);   // twists in the rope
                g.setStroke(FINE);
                for (double[] t : new double[][] {{-24, -6}, {-6, -19}, {12, -18}, {20, 4}, {-12, 11}, {6, 12}}) {
                    g.draw(new Line2D.Double(t[0] - 1.5, t[1] - 1.5, t[0] + 1.5, t[1] + 1.5));
                }
                g.setPaint(new GradientPaint(0, -6, new Color(90, 90, 96), 0, 6, new Color(20, 20, 24)));
                g.fill(new Ellipse2D.Double(-5, -5, 11, 10));
                g.setColor(new Color(255, 255, 255, 120));
                g.fill(new Ellipse2D.Double(-3, -4, 4, 3));
            }
        },
        KELP_NET {
            @Override
            void draw(Graphics2D g) {
                Color rope = new Color(120, 160, 110, 220);
                frayed(g, rope);
                mesh(g, BLOB, new Color(60, 100, 60, 60), rope, 6);
                // Kelp woven through the mesh.
                g.setColor(new Color(150, 120, 40, 220));
                g.setStroke(KELP);
                g.draw(new QuadCurve2D.Double(-24, 10, -14, -4, -18, -20));
                g.draw(new QuadCurve2D.Double(6, 20, 14, 4, 6, -14));
                g.draw(new QuadCurve2D.Double(20, 12, 30, 2, 24, -12));
                edge(g, BLOB, rope);
                // A striped marker buoy on top.
                Shape buoy = new Ellipse2D.Double(-6, TOP - 9, 12, 12);
                g.setColor(Color.WHITE);
                g.fill(buoy);
                Graphics2D stripes = (Graphics2D) g.create();
                stripes.clip(buoy);
                stripes.setColor(new Color(220, 50, 45));
                stripes.fill(new Rectangle2D.Double(-6, TOP - 9, 12, 4));
                stripes.fill(new Rectangle2D.Double(-6, TOP - 1, 12, 4));
                stripes.dispose();
                g.setColor(new Color(90, 30, 30));
                g.setStroke(FINE);
                g.draw(buoy);
            }
        };

        /** Draws this design around (0, 0). */
        abstract void draw(Graphics2D g);
    }

    private static final Look[] LOOKS = Look.values();
    private static final Color PALE_ROPE = new Color(205, 215, 185, 200);
    private static final Color FLOAT_SHINE = new Color(255, 255, 255, 150);
    private static final BasicStroke MESH_STROKE = new BasicStroke(1.2f);
    private static final BasicStroke EDGE_STROKE = new BasicStroke(2);
    private static final BasicStroke FINE = new BasicStroke(1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    private static final BasicStroke ROPE = new BasicStroke(5, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    private static final BasicStroke ROPE_TOP = new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    private static final BasicStroke KELP = new BasicStroke(3.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    private static final int MESH_SLANT = 14;   // how far each mesh line leans
    private static final double SWAY = 0.05;    // it stretches and squeezes by 5%

    // Every net is the same size, so its outlines are made once and reused.
    private static final int R = (int) GameConstants.RADIUS_NET;
    private static final Shape BLOB = new Ellipse2D.Double(-R, -R * 0.8, 2 * R, 1.6 * R);
    private static final Shape WIDE_BLOB = wideBlob();
    private static final int TOP = (int) (-R * 0.8);
    private static final int BOTTOM = (int) (R * 0.8);

    // The mesh is slow to draw, so each design in each sway step is drawn
    // once and copied from then on.
    private static final int SWAY_STEPS = 9;   // from fully squeezed to fully stretched

    private final SpriteCache sprites = new SpriteCache(SWAY_STEPS * LOOKS.length, R + 10, R + 6, R + 12,
            NetPainter::drawPose);

    @Override
    public void paint(Graphics2D g, ObjectView object, double time) {
        int step = SpriteCache.step(Math.sin(1.5 * time + object.getPhase()), SWAY_STEPS);
        int look = ObjectPainter.designOf(object, LOOKS.length);
        sprites.paint(g, object.getX(), object.getY(), look * SWAY_STEPS + step);
    }

    /** Draws one design, stretched by the sway the pose stands for. */
    private static void drawPose(Graphics2D g, int pose) {
        double sway = SWAY * SpriteCache.swingOf(pose % SWAY_STEPS, SWAY_STEPS);
        g.scale(1 + sway, 1 - sway);   // wider when flatter, like something soft
        LOOKS[pose / SWAY_STEPS].draw(g);
    }

    // ---- Parts the designs share -----------------------------------------

    /** Diamond mesh: two sets of slanted lines, cut off at the outline. */
    private static void mesh(Graphics2D g, Shape outline, Color fill, Color rope, int gap) {
        g.setColor(fill);
        g.fill(outline);
        Graphics2D mesh = (Graphics2D) g.create();
        mesh.clip(outline);
        mesh.setColor(rope);
        mesh.setStroke(MESH_STROKE);
        for (int x = -R - MESH_SLANT - 6; x <= R + MESH_SLANT + 6; x += gap) {
            mesh.drawLine(x, -R, x + MESH_SLANT, R);
            mesh.drawLine(x, -R, x - MESH_SLANT, R);
        }
        mesh.dispose();
    }

    private static void edge(Graphics2D g, Shape outline, Color rope) {
        g.setColor(rope);
        g.setStroke(EDGE_STROKE);
        g.draw(outline);
    }

    /** Small knots where the mesh is tied to the edge rope. */
    private static void knots(Graphics2D g, Color rope) {
        g.setColor(rope.darker());
        for (int i = 0; i < 10; i++) {
            double angle = Math.PI * 2 * i / 10;
            g.fill(new Ellipse2D.Double(Math.cos(angle) * R - 1.5, Math.sin(angle) * R * 0.8 - 1.5, 3, 3));
        }
    }

    /** Frayed rope ends hanging below. */
    private static void frayed(Graphics2D g, Color rope) {
        g.setColor(rope);
        g.setStroke(MESH_STROKE);
        g.drawLine(-12, BOTTOM - 4, -16, BOTTOM + 9);
        g.drawLine(2, BOTTOM - 1, 0, BOTTOM + 12);
        g.drawLine(14, BOTTOM - 4, 19, BOTTOM + 8);
    }

    /** Two round floats on top of the net. */
    private static void floats(Graphics2D g, Color light, Color dark) {
        g.setPaint(new GradientPaint(0, TOP - 5, light, 0, TOP + 5, dark));
        g.fillOval(-R / 2 - 5, TOP - 5, 10, 10);
        g.fillOval(R / 2 - 5, TOP - 5, 10, 10);
        g.setColor(dark.darker());
        g.setStroke(FINE);
        g.drawOval(-R / 2 - 5, TOP - 5, 10, 10);
        g.drawOval(R / 2 - 5, TOP - 5, 10, 10);
        g.setColor(FLOAT_SHINE);
        g.fillOval(-R / 2 - 3, TOP - 4, 4, 3);
        g.fillOval(R / 2 - 3, TOP - 4, 4, 3);
    }

    /** A wider, lopsided outline, like a net snagged at one corner. */
    private static Shape wideBlob() {
        Path2D blob = new Path2D.Double();
        blob.moveTo(-R - 4, -4);
        blob.curveTo(-R, -R, R * 0.4, -R * 0.9, R + 4, -R * 0.5);
        blob.curveTo(R + 8, 0, R * 0.6, R * 0.9, 0, R * 0.8);
        blob.curveTo(-R * 0.6, R * 0.9, -R - 8, R * 0.3, -R - 4, -4);
        blob.closePath();
        return blob;
    }
}
