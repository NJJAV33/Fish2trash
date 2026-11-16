package ui.render;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.QuadCurve2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import model.ObjectView;

/**
 * Draws a metal can, tipping slowly from side to side. Cans come in several
 * designs (a soda can, a tall energy drink, a lemon soda, a rusty food tin,
 * a crushed one), and each can keeps the one it was given.
 */
public final class CanPainter implements ObjectPainter {

    /** The can designs. Each draws itself standing up around (0, 0). */
    private enum Look {
        SODA {
            @Override
            void draw(Graphics2D g) {
                Shape body = new RoundRectangle2D.Double(-10, -13, 20, 27, 5, 5);
                metal(g, body, 10);
                band(g, body, -6, 12, RED_LIGHT, RED_DARK);
                drawInside(g, body, wave(-10, 1, 10), new Color(255, 245, 235, 220));   // a white wave across the label
                print(g, -7.5, 9, 7);
                dent(g, 3, 7);
                outline(g, body);
                lid(g, 10, -13, true);
            }
        },
        ENERGY {
            @Override
            void draw(Graphics2D g) {
                Shape body = new RoundRectangle2D.Double(-7.5, -16, 15, 32, 4, 4);
                metal(g, body, 7.5);
                band(g, body, -12, 26, new Color(60, 110, 230), new Color(20, 40, 120));
                Path2D bolt = new Path2D.Double();   // a lightning bolt
                bolt.moveTo(1.5, -9);
                bolt.lineTo(-3, 1);
                bolt.lineTo(0.5, 1);
                bolt.lineTo(-1.5, 9);
                bolt.lineTo(3.5, -1.5);
                bolt.lineTo(0, -1.5);
                bolt.closePath();
                g.setColor(new Color(150, 230, 255));
                g.fill(bolt);
                g.setColor(new Color(200, 205, 212));   // silver stripes at the edges of the label
                g.setStroke(FINE);
                g.draw(new Line2D.Double(-7.5, -11, 7.5, -11));
                g.draw(new Line2D.Double(-7.5, 13, 7.5, 13));
                outline(g, body);
                lid(g, 7.5, -16, true);
            }
        },
        LEMON {
            @Override
            void draw(Graphics2D g) {
                Shape body = new RoundRectangle2D.Double(-10, -13, 20, 27, 5, 5);
                metal(g, body, 10);
                band(g, body, -8, 18, new Color(90, 200, 100), new Color(30, 120, 50));
                g.setColor(new Color(250, 230, 70));   // a yellow stripe and a lemon slice
                g.fill(new Rectangle2D.Double(-10, 4, 20, 2.5));
                g.fill(new Ellipse2D.Double(-4, -5, 8, 8));
                g.setColor(new Color(200, 170, 20));
                g.setStroke(FINE);
                g.draw(new Line2D.Double(0, -5, 0, 3));
                g.draw(new Line2D.Double(-4, -1, 4, -1));
                g.setColor(new Color(255, 255, 255, 170));   // bubbles
                for (double[] b : new double[][] {{-7, -6}, {6, -5}, {-6, 1}, {7, 1}, {5, 8}}) {
                    g.draw(new Ellipse2D.Double(b[0] - 1, b[1] - 1, 2, 2));
                }
                outline(g, body);
                lid(g, 10, -13, true);
            }
        },
        TIN {
            @Override
            void draw(Graphics2D g) {
                // A wide food tin with ridged sides, a torn paper label and rust.
                Shape body = new RoundRectangle2D.Double(-11, -10, 22, 22, 3, 3);
                metal(g, body, 11);
                Path2D paper = new Path2D.Double();
                paper.moveTo(-11, -5);
                paper.lineTo(11, -5);
                paper.lineTo(11, 3);
                paper.lineTo(7, 5);
                paper.lineTo(4, 2.5);
                paper.lineTo(0, 6);
                paper.lineTo(-4, 3);
                paper.lineTo(-11, 7);
                paper.closePath();
                Graphics2D label = (Graphics2D) g.create();
                label.clip(body);
                label.setPaint(new LinearGradientPaint(-11, 0, 11, 0, ROUND,
                        new Color[] {new Color(200, 180, 130), new Color(250, 236, 196), new Color(226, 206, 160), new Color(190, 170, 120)}));
                label.fill(paper);
                label.dispose();
                Path2D fish = new Path2D.Double();   // a little fish printed on the label
                fish.moveTo(4, -1);
                fish.quadTo(0, -4, -3, -1);
                fish.lineTo(-6, -3);
                fish.lineTo(-6, 1);
                fish.lineTo(-3, -1);
                fish.quadTo(0, 2, 4, -1);
                g.setColor(new Color(60, 110, 170));
                g.fill(fish);
                g.setColor(new Color(150, 90, 50, 170));   // ridges
                g.setStroke(FINE);
                g.draw(new Line2D.Double(-11, -7.5, 11, -7.5));
                g.draw(new Line2D.Double(-11, 9, 11, 9));
                g.setColor(RUST);
                g.fill(new Ellipse2D.Double(5, 7, 5, 4));
                g.fill(new Ellipse2D.Double(-10, -9, 4, 3));
                g.fill(new Ellipse2D.Double(-6, 9, 3, 2.5));
                outline(g, body);
                // An opened lid, bent back with a jagged edge.
                Path2D lid = new Path2D.Double();
                lid.moveTo(-11, -10);
                lid.lineTo(-8, -12);
                lid.lineTo(-5, -10.5);
                lid.lineTo(-2, -12.5);
                lid.lineTo(1, -10.5);
                lid.lineTo(4, -12.5);
                lid.lineTo(7, -10.5);
                lid.lineTo(11, -10);
                lid.closePath();
                g.setColor(RIM);
                g.fill(lid);
                g.setColor(OUTLINE);
                g.draw(lid);
            }
        },
        CRUSHED {
            @Override
            void draw(Graphics2D g) {
                Path2D body = new Path2D.Double();   // squeezed in the middle and bent
                body.moveTo(-10, -11);
                body.lineTo(10, -11);
                body.curveTo(10, -6, 5, -3, 6, 1);
                body.curveTo(7, 5, 11, 8, 10, 13);
                body.lineTo(-9, 14);
                body.curveTo(-10, 9, -5, 5, -6, 1);
                body.curveTo(-7, -3, -10, -6, -10, -11);
                body.closePath();
                metal(g, body, 10);
                band(g, body, -5, 11, new Color(240, 240, 245), new Color(170, 175, 185));
                drawInside(g, body, wave(-10, 0, 10), new Color(40, 90, 200));   // a blue stripe
                g.setColor(new Color(70, 75, 85, 170));   // creases
                g.setStroke(FINE);
                g.draw(new Line2D.Double(-6, -2, 6, 3));
                g.draw(new Line2D.Double(-5, 4, 5, -1));
                g.draw(new Line2D.Double(-9, -8, -4, -5));
                outline(g, body);
                lid(g, 10, -11, false);
            }
        };

        /** Draws this design standing up around (0, 0). */
        abstract void draw(Graphics2D g);
    }

    private static final Look[] LOOKS = Look.values();

    // A can is a cylinder: dark at both edges, with a bright streak where the light hits it.
    private static final float[] ROUND = {0f, 0.3f, 0.65f, 1f};
    private static final Color[] METAL = {
        new Color(120, 126, 134), new Color(232, 235, 238), new Color(172, 178, 185), new Color(110, 116, 124)};
    private static final Color RED_LIGHT = new Color(240, 100, 95);
    private static final Color RED_DARK = new Color(140, 30, 30);
    private static final Color RIM = new Color(200, 204, 210);
    private static final Color RIM_EDGE = new Color(150, 156, 164);
    private static final Color TAB = new Color(170, 176, 184);
    private static final Color RUST = new Color(170, 80, 30, 200);
    private static final Color PRINT = new Color(255, 255, 255, 150);
    private static final Color OUTLINE = new Color(80, 85, 92);
    private static final BasicStroke LINE = new BasicStroke(1.1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    private static final BasicStroke FINE = new BasicStroke(0.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    private static final BasicStroke THICK = new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    private static final double TIP = 0.14;   // how far it tips, in radians
    private static final int STEPS = 12;      // tipping positions from one side to the other

    private final SpriteCache sprites = new SpriteCache(STEPS * LOOKS.length, 22, 22, 22, CanPainter::drawPose);

    @Override
    public void paint(Graphics2D g, ObjectView object, double time) {
        int step = SpriteCache.step(Math.sin(1.6 * time + object.getPhase()), STEPS);
        int look = ObjectPainter.designOf(object, LOOKS.length);
        sprites.paint(g, object.getX(), object.getY(), look * STEPS + step);
    }

    /** Draws one design, tipped as far as the pose says. */
    private static void drawPose(Graphics2D g, int pose) {
        g.rotate(TIP * SpriteCache.swingOf(pose % STEPS, STEPS));
        LOOKS[pose / STEPS].draw(g);
    }

    // ---- Parts the designs share -----------------------------------------

    /** Bare metal, shaded like a cylinder as wide as 2 x halfWidth. */
    private static void metal(Graphics2D g, Shape body, double halfWidth) {
        g.setPaint(new LinearGradientPaint((float) -halfWidth, 0, (float) halfWidth, 0, ROUND, METAL));
        g.fill(body);
    }

    /** A printed band around the can, shaded like the metal under it. */
    private static void band(Graphics2D g, Shape body, double top, double height, Color light, Color dark) {
        Graphics2D inside = (Graphics2D) g.create();
        inside.clip(body);
        inside.setPaint(new LinearGradientPaint(-10, 0, 10, 0, ROUND,
                new Color[] {dark, light, light.darker(), dark}));
        inside.fill(new Rectangle2D.Double(-12, top, 24, height));
        inside.dispose();
    }

    private static Shape wave(double left, double y, double right) {
        double middle = (left + right) / 2;
        Path2D wave = new Path2D.Double();
        wave.moveTo(left, y);
        wave.quadTo((left + middle) / 2, y - 5, middle, y);
        wave.quadTo((middle + right) / 2, y + 5, right, y);
        return wave;
    }

    /** Draws a line on the can without letting it stick out past the edge. */
    private static void drawInside(Graphics2D g, Shape body, Shape line, Color colour) {
        Graphics2D inside = (Graphics2D) g.create();
        inside.clip(body);
        inside.setColor(colour);
        inside.setStroke(THICK);
        inside.draw(line);
        inside.dispose();
    }

    /** Two lines of small print. */
    private static void print(Graphics2D g, double x, double y, double length) {
        g.setColor(PRINT);
        g.setStroke(FINE);
        g.draw(new Line2D.Double(x, y, x + length, y));
        g.draw(new Line2D.Double(x, y + 2, x + length * 0.6, y + 2));
    }

    private static void dent(Graphics2D g, double x, double y) {
        g.setColor(new Color(80, 85, 92, 150));
        g.setStroke(FINE);
        g.draw(new QuadCurve2D.Double(x, y, x + 3, y + 2, x + 6, y - 1));
    }

    private static void outline(Graphics2D g, Shape body) {
        g.setColor(OUTLINE);
        g.setStroke(LINE);
        g.draw(body);
    }

    /** The lid: a pale ellipse at y = top, with a ring pull if the can was opened by its tab. */
    private static void lid(Graphics2D g, double halfWidth, double top, boolean ringPull) {
        Shape lid = new Ellipse2D.Double(-halfWidth, top - 3, 2 * halfWidth, 6);
        g.setColor(RIM);
        g.fill(lid);
        g.setColor(RIM_EDGE);
        g.setStroke(FINE);
        g.draw(lid);
        if (ringPull) {
            Shape tab = new RoundRectangle2D.Double(-3, top - 1.5, 6, 2.8, 2, 2);
            g.setColor(TAB);
            g.fill(tab);
            g.setColor(OUTLINE);
            g.draw(tab);
        }
    }
}
