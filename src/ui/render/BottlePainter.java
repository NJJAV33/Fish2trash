package ui.render;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.QuadCurve2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import model.ObjectView;

/**
 * Draws a plastic bottle floating on its side, rocking gently. Bottles come
 * in several designs (water, soda, a milk jug, iced tea, a crushed one), and
 * each bottle keeps the one it was given.
 */
public final class BottlePainter implements ObjectPainter {

    /** The bottle designs. Each draws itself standing up, cap at the top, around (0, 0). */
    private enum Look {
        WATER {
            @Override
            void draw(Graphics2D g) {
                Shape body = bottle(-13, 3, 7, -3, 13.5);
                glass(g, body, new Color(220, 242, 250, 200), new Color(130, 192, 218, 170));
                liquid(g, body, 4, new Color(120, 190, 235, 110));
                label(g, body, -1, 7, new Color(100, 210, 145), new Color(38, 136, 86));
                ribs(g, body, 9, 11, new Color(110, 170, 195, 160));
                outline(g, body, new Color(90, 150, 175));
                cap(g, 4, -18, new Color(96, 170, 245), new Color(28, 96, 180));
                shine(g, -4, -5, 10);
            }
        },
        SODA {
            @Override
            void draw(Graphics2D g) {
                Shape body = bottle(-14, 3, 8, -2, 13);
                glass(g, body, new Color(200, 240, 200, 200), new Color(90, 170, 110, 170));
                liquid(g, body, -4, new Color(150, 220, 120, 90));
                label(g, body, 0, 8, new Color(235, 70, 60), new Color(160, 30, 30));
                g.setColor(new Color(255, 255, 255, 220));   // a swoosh on the label
                g.setStroke(THICK);
                g.draw(new QuadCurve2D.Double(-7, 6, 0, 0, 7, 3));
                feet(g, 13, new Color(120, 190, 130));
                outline(g, body, new Color(60, 130, 80));
                cap(g, 4, -19, new Color(240, 80, 70), new Color(170, 30, 30));
                shine(g, -5, -4, 9);
            }
        },
        JUG {
            @Override
            void draw(Graphics2D g) {
                Shape body = bottle(-12, 3.2, 8, -4, 14);
                Shape handle = new RoundRectangle2D.Double(5, -6, 7, 12, 6, 6);
                g.setColor(new Color(240, 238, 228));
                g.setStroke(HANDLE);
                g.draw(handle);
                g.setPaint(new GradientPaint(-8, 0, new Color(252, 252, 246), 8, 0, new Color(205, 205, 196)));
                g.fill(body);
                label(g, body, 0, 9, new Color(70, 140, 220), new Color(30, 80, 160));
                g.setColor(Color.WHITE);   // a cow-spot on the milk label
                g.fill(new Ellipse2D.Double(-3, 2.5, 5, 4));
                outline(g, body, new Color(150, 150, 140));
                g.setColor(new Color(150, 150, 140));
                g.setStroke(FINE);
                g.draw(handle);
                cap(g, 4.2, -17, new Color(255, 170, 60), new Color(210, 110, 20));
            }
        },
        TEA {
            @Override
            void draw(Graphics2D g) {
                Shape body = bottle(-15, 2.5, 6, -1, 14);
                glass(g, body, new Color(220, 150, 80, 210), new Color(130, 70, 25, 210));
                label(g, body, 2, 7, new Color(250, 210, 70), new Color(200, 150, 20));
                g.setColor(new Color(60, 150, 60));   // a tea leaf on the label
                g.fill(new Ellipse2D.Double(-2.5, 3.5, 5, 3));
                outline(g, body, new Color(100, 55, 20));
                cap(g, 3.5, -20, new Color(250, 250, 250), new Color(190, 190, 190));
                shine(g, -3, -2, 11);
                algae(g, new double[][] {{3, 11}, {-2, 12}, {4, -4}});
            }
        },
        CRUSHED {
            @Override
            void draw(Graphics2D g) {
                Shape body = crushed();
                glass(g, body, new Color(225, 240, 245, 190), new Color(150, 185, 200, 170));
                label(g, body, -2, 6, new Color(150, 200, 170), new Color(90, 140, 115));   // a faded label
                g.setColor(new Color(110, 150, 170, 200));   // crumples
                g.setStroke(FINE);
                g.draw(new Line2D.Double(-6, 6, 2, 9));
                g.draw(new Line2D.Double(-1, 3, 6, 7));
                g.draw(new Line2D.Double(-6, -4, -1, -1));
                outline(g, body, new Color(100, 140, 160));
                cap(g, 4, -18, new Color(80, 190, 170), new Color(30, 120, 110));
                algae(g, new double[][] {{-4, 10}, {3, 12}, {5, -3}, {-5, -1}});
            }
        };

        /** Draws this design standing up, cap at the top, around (0, 0). */
        abstract void draw(Graphics2D g);
    }

    private static final Look[] LOOKS = Look.values();
    private static final BasicStroke LINE = new BasicStroke(1.1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    private static final BasicStroke FINE = new BasicStroke(0.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    private static final BasicStroke THICK = new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    private static final BasicStroke HANDLE = new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    private static final Color SHINE = new Color(255, 255, 255, 200);
    private static final Color ALGAE = new Color(70, 140, 70, 200);
    private static final Color TEXT = new Color(255, 255, 255, 150);
    private static final double TILT = Math.toRadians(75);
    private static final double ROCK = 0.12;   // how far it rocks, in radians
    private static final int STEPS = 12;       // rocking positions from one side to the other

    private final SpriteCache sprites = new SpriteCache(STEPS * LOOKS.length, 22, 22, 22, BottlePainter::drawPose);

    @Override
    public void paint(Graphics2D g, ObjectView object, double time) {
        int step = SpriteCache.step(Math.sin(2 * time + object.getPhase()), STEPS);
        int look = ObjectPainter.designOf(object, LOOKS.length);
        sprites.paint(g, object.getX(), object.getY(), look * STEPS + step);
    }

    /** Draws one design, tipped over and rocked as far as the pose says. */
    private static void drawPose(Graphics2D g, int pose) {
        g.rotate(TILT + ROCK * SpriteCache.swingOf(pose % STEPS, STEPS));
        LOOKS[pose / STEPS].draw(g);
    }

    // ---- Parts the designs share -----------------------------------------

    /**
     * A bottle outline: a neck from y = top, shoulders down to shoulderY,
     * straight sides and a rounded bottom.
     */
    private static Shape bottle(double top, double neck, double half, double shoulderY, double bottom) {
        Path2D body = new Path2D.Double();
        body.moveTo(-neck, top);
        body.lineTo(neck, top);
        body.lineTo(neck, top + 3);
        body.curveTo(neck, top + 5, half, shoulderY - 3, half, shoulderY);
        body.lineTo(half, bottom - 2.5);
        body.quadTo(half, bottom, half - 2.5, bottom);
        body.lineTo(-half + 2.5, bottom);
        body.quadTo(-half, bottom, -half, bottom - 2.5);
        body.lineTo(-half, shoulderY);
        body.curveTo(-half, shoulderY - 3, -neck, top + 5, -neck, top + 3);
        body.closePath();
        return body;
    }

    /** A water bottle squeezed in the middle. */
    private static Shape crushed() {
        Path2D body = new Path2D.Double();
        body.moveTo(-3, -13);
        body.lineTo(3, -13);
        body.lineTo(3, -10);
        body.curveTo(3, -8, 7, -7, 7, -3);
        body.curveTo(7, 1, 3, 2, 4, 5);
        body.curveTo(5, 8, 8, 9, 7, 12);
        body.quadTo(6.5, 13.5, 4, 13.5);
        body.lineTo(-4.5, 13.5);
        body.quadTo(-7, 13.5, -6.5, 11);
        body.curveTo(-6, 8, -3, 6, -4, 3);
        body.curveTo(-5, 0, -7, -1, -7, -3);
        body.curveTo(-7, -7, -3, -8, -3, -10);
        body.closePath();
        return body;
    }

    /** See-through plastic: lighter on one side, as round things are. */
    private static void glass(Graphics2D g, Shape body, Color light, Color dark) {
        Paint paint = new GradientPaint(-7, 0, light, 7, 0, dark);
        g.setPaint(paint);
        g.fill(body);
    }

    /** Some liquid left inside, from y = level down. */
    private static void liquid(Graphics2D g, Shape body, double level, Color colour) {
        Graphics2D inside = (Graphics2D) g.create();
        inside.clip(body);
        inside.setColor(colour);
        inside.fill(new Rectangle2D.Double(-10, level, 20, 20));
        inside.setColor(new Color(255, 255, 255, 120));
        inside.setStroke(FINE);
        inside.draw(new Line2D.Double(-8, level, 8, level));
        inside.dispose();
    }

    /** A label band wrapped round the body, with two lines of small print on it. */
    private static void label(Graphics2D g, Shape body, double top, double height, Color light, Color dark) {
        Graphics2D band = (Graphics2D) g.create();
        band.clip(body);
        band.setPaint(new GradientPaint(-8, 0, light, 8, 0, dark));
        band.fill(new Rectangle2D.Double(-10, top, 20, height));
        band.setColor(TEXT);
        band.setStroke(FINE);
        band.draw(new Line2D.Double(-4, top + height * 0.35, 3, top + height * 0.35));
        band.draw(new Line2D.Double(-4, top + height * 0.7, 1, top + height * 0.7));
        band.dispose();
    }

    private static void ribs(Graphics2D g, Shape body, double first, double second, Color colour) {
        Graphics2D inside = (Graphics2D) g.create();
        inside.clip(body);
        inside.setColor(colour);
        inside.setStroke(FINE);
        inside.draw(new Line2D.Double(-8, first, 8, first));
        inside.draw(new Line2D.Double(-8, second, 8, second));
        inside.dispose();
    }

    /** The bumpy "feet" at the bottom of a soda bottle. */
    private static void feet(Graphics2D g, double bottom, Color colour) {
        g.setColor(colour);
        for (int i = -1; i <= 1; i++) {
            g.fill(new Ellipse2D.Double(i * 5 - 2.5, bottom - 2.5, 5, 4));
        }
    }

    private static void outline(Graphics2D g, Shape body, Color colour) {
        g.setColor(colour);
        g.setStroke(LINE);
        g.draw(body);
    }

    /** A screw cap with grip lines, its top edge at y = top. */
    private static void cap(Graphics2D g, double halfWidth, double top, Color light, Color dark) {
        Shape cap = new RoundRectangle2D.Double(-halfWidth, top, 2 * halfWidth, 5, 2, 2);
        g.setPaint(new GradientPaint((float) -halfWidth, 0, light, (float) halfWidth, 0, dark));
        g.fill(cap);
        g.setColor(dark.darker());
        g.setStroke(FINE);
        g.draw(cap);
        for (double x = -halfWidth + 2; x < halfWidth - 1; x += 2) {
            g.draw(new Line2D.Double(x, top + 1, x, top + 4));
        }
    }

    private static void shine(Graphics2D g, double x, double top, double length) {
        g.setColor(SHINE);
        g.setStroke(THICK);
        g.draw(new Line2D.Double(x, top, x, top + length));
    }

    /** Small green growths on a bottle that has been in the sea a while. */
    private static void algae(Graphics2D g, double[][] spots) {
        g.setColor(ALGAE);
        for (double[] spot : spots) {
            g.fill(new Ellipse2D.Double(spot[0] - 1.2, spot[1] - 1.2, 2.4, 2.4));
        }
    }
}
