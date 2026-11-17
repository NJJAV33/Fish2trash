package ui.render;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.QuadCurve2D;
import java.awt.geom.Rectangle2D;
import model.ObjectView;

/**
 * Draws a small reef fish that faces the way it swims and wags its tail.
 * Small fish come in several kinds (a clownfish, a blue tang, a yellow
 * butterflyfish, a sergeant major, a fusilier), and each fish keeps the one
 * it was given.
 */
public final class SmallFishPainter implements ObjectPainter {

    /**
     * The kinds of small fish. Each keeps its own shapes and colours, made
     * once, and draws itself facing right around (0, 0).
     */
    private enum Look {
        /** Clownfish: round and orange, with three white bands edged in black. */
        CLOWNFISH {
            private final Paint skin = new GradientPaint(0, -10, new Color(255, 160, 48), 0, 10, new Color(232, 94, 16));
            private final Color fin = new Color(248, 122, 26);
            private final Color edge = new Color(46, 22, 10);
            private final Shape body = closed(18, 1, 17, -7, 8, -11, -3, -10, -10, -9.5, -15, -5, -16, 0,
                    -15, 5, -10, 9.5, -3, 10, 8, 11, 17, 8, 18, 1);
            private final Shape tail = closed(-14, -3, -19, -7, -27, -10, -29, -5, -30.5, -2, -30.5, 2, -29, 5,
                    -27, 10, -19, 7, -14, 3);
            private final Shape backFin = closed(-11, -8, -10, -15, -3, -16, 0, -12, 2, -16, 8, -15, 9, -8);
            private final Shape bellyFin = closed(5, 9, 4, 15, -1, 17, -3, 9.5);
            private final Shape rearFin = closed(-5, 9.5, -5, 15, -11, 15, -13, 7);
            private final Shape sideFin = new Ellipse2D.Double(0, 1, 7, 4.5);
            private final Shape bands = inside(body, bands(5.5, 4.5, -1.5, -8, 5, 1.5, -15.5, 3.5, 0.5));

            @Override
            void draw(Graphics2D g, double beat) {
                swung(g, tail, WAG * beat, -14, 0, fin, edge);
                part(g, backFin, fin, edge);
                part(g, bellyFin, fin, edge);
                part(g, rearFin, fin, edge);
                g.setPaint(skin);
                g.fill(body);
                g.setColor(WHITE);
                g.fill(bands);
                g.setColor(edge);   // each band has a thin black edge
                g.setStroke(FINE);
                g.draw(bands);
                outline(g, body, edge);
                swung(g, sideFin, 0.4 + 0.3 * beat, 0, 3, fin, edge);
                eye(g, 13, -3, 6);
                mouth(g, 15, 3.5, 18, 2.5, edge);
            }
        },

        /** Blue tang: a blue oval with a black mark along its back and a yellow tail. */
        BLUE_TANG {
            private final Paint skin = new GradientPaint(0, -12, new Color(84, 170, 255), 0, 12, new Color(30, 92, 224));
            private final Color fin = new Color(52, 128, 244);
            private final Color yellow = new Color(255, 216, 44);
            private final Color edge = new Color(10, 16, 46);
            private final Shape body = closed(19, 2, 14, -6, 6, -12, -3, -11.5, -10, -11, -15, -4, -17, -1.5,
                    -17, -0.5, -17, 0.5, -17, 1.5, -15, 4, -10, 11, -3, 11.5, 6, 12, 14, 8, 19, 2);
            private final Shape backFin = closed(10, -9, 4, -17, -10, -16, -16, -3);
            private final Shape lowFin = closed(4, 10, -1, 16.5, -10, 15.5, -16, 3);
            private final Shape tail = closed(-15.5, -1.5, -19, -4, -24, -8, -28.5, -10.5, -26, -4, -26, 4, -28.5, 10.5,
                    -24, 8, -19, 4, -15.5, 1.5);
            private final Shape sideFin = new Ellipse2D.Double(1, 2, 7, 4);
            // The black mark runs from the eye along the back to the tail and hooks forward again.
            private final Shape mark = inside(body, closed(14, -4.5, 8, -12, -8, -12, -18, -2, -18, 0, -18, 1, -17, 2.5,
                    -10, 3.5, -3, 3.5, 2, -1, 5, -3.5, 9, -3.5, 14, -4.5));
            private final Shape gap = new Ellipse2D.Double(-10.5, -6.5, 13, 5.5);   // blue showing inside the mark

            @Override
            void draw(Graphics2D g, double beat) {
                swung(g, tail, WAG * beat, -16, 0, yellow, edge);
                part(g, backFin, fin, edge);
                part(g, lowFin, fin, edge);
                g.setPaint(skin);
                g.fill(body);
                g.setColor(edge);
                g.fill(mark);
                g.setPaint(skin);
                g.fill(gap);
                shine(g, -2, 5, 12);
                outline(g, body, edge);
                swung(g, sideFin, 0.4 + 0.3 * beat, 1, 3, yellow, edge);
                eye(g, 12.5, -4, 5.5);
                mouth(g, 16.5, 3.5, 19, 2.5, edge);
            }
        },

        /** Butterflyfish: a tall yellow disc with a pointed snout and a black bar through the eye. */
        BUTTERFLYFISH {
            private final Paint skin = new GradientPaint(0, -15, new Color(255, 234, 76), 0, 15, new Color(250, 188, 26));
            private final Color fin = new Color(255, 238, 150);
            private final Color orange = new Color(238, 140, 22);
            private final Color dark = new Color(24, 20, 16);
            private final Color edge = new Color(78, 50, 8);
            private final Shape body = closed(20, 1.5, 15.5, 0.5, 14, -3.5, 11.5, -7.5, 8, -13, 3, -15.5, -2, -15.5,
                    -8, -15.5, -13, -11, -13.5, -2.2, -13.5, -1, -13.5, 1, -13.5, 2.2,
                    -13, 11, -8, 15.5, -2, 15.5, 3, 15.5, 8, 13, 11.5, 8, 13.5, 5, 15.5, 3, 20, 1.5);
            private final Shape tail = closed(-13, -2.2, -17, -3, -21, -6, -24, -8, -22.5, -3, -22.5, 3, -24, 8,
                    -21, 6, -17, 3, -13, 2.2);
            private final Shape spines = spiky(9, -10, 7.5, -15.5, 5, -13, 3, -17.5, 0.5, -14.5, -2, -18,
                    -4.5, -14.5, -7.5, -17, -9, -12);
            private final Shape bellyFin = closed(5, 12, 5, 17, 1, 19, 0, 13);
            private final Shape sideFin = new Ellipse2D.Double(-1, 1, 7, 4.5);
            private final Shape face = inside(body, new Rectangle2D.Double(10.5, -20, 15, 40));
            private final Shape eyeBar = inside(body, bands(8, 3.4, 2.5));
            private final Shape rearBand = inside(body, bands(-13.5, 2.6, 1.5));
            private final Shape spot = new Ellipse2D.Double(-10, -11.5, 5, 5);   // a false eye near the tail
            private final Shape lines = strokes(4, -10, -8, -3, 5, -5.5, -9, 1.5, 5, -1, -9, 6, 4, 4, -7, 10);

            @Override
            void draw(Graphics2D g, double beat) {
                swung(g, tail, WAG * beat, -13, 0, fin, edge);
                part(g, spines, skin, edge);
                part(g, bellyFin, orange, edge);
                g.setPaint(skin);
                g.fill(body);
                g.setColor(WHITE);
                g.fill(face);
                g.setColor(orange);
                g.fill(rearBand);
                g.setStroke(FINE);
                g.draw(lines);
                g.setColor(dark);
                g.fill(eyeBar);
                g.fill(spot);
                outline(g, body, edge);
                swung(g, sideFin, 0.4 + 0.3 * beat, -1, 3, fin, edge);
                eye(g, 11.5, -3, 5);
            }
        },

        /** Sergeant major: a deep, pale fish with a yellow back and five dark bars. */
        SERGEANT_MAJOR {
            private final Paint skin = new GradientPaint(0, -12, new Color(236, 242, 236), 0, 11, new Color(172, 196, 214));
            private final Color yellow = new Color(250, 212, 44, 220);
            private final Color fin = new Color(140, 160, 180);
            private final Color bar = new Color(22, 28, 44);
            private final Color edge = new Color(26, 34, 52);
            private final Shape body = closed(18, 1.5, 16, -6.5, 8, -12.5, -1, -12.5, -9, -12.5, -14, -6, -16, -1.5,
                    -16, -0.5, -16, 0.5, -16, 1.5, -14, 6, -9, 11.5, -1, 11.5, 8, 11.5, 16, 8, 18, 1.5);
            private final Shape backFin = spiky(9, -9, 6.5, -14.5, 4.5, -12, 2, -15.5, 0, -12.5, -3, -16, -5, -13,
                    -10, -17, -13.5, -11, -12, -6);
            private final Shape lowFin = spiky(-3, 10.5, -6, 14.5, -11, 13.5, -13, 6);
            private final Shape bellyFin = spiky(6, 10.5, 4.5, 15, 1, 11);
            private final Shape tail = closed(-14.5, -1.5, -19, -4, -24, -9, -29, -11, -24.5, -4, -24.5, 4, -29, 11,
                    -24, 9, -19, 4, -14.5, 1.5);
            private final Shape sideFin = new Ellipse2D.Double(1, 2, 6.5, 4);
            private final Shape back = inside(body, new Ellipse2D.Double(-14, -16, 26, 10.5));
            private final Shape bars = inside(body, bands(7.5, 2.6, 0, 2.5, 2.6, 0, -2.5, 2.6, 0, -7.5, 2.6, 0, -12.5, 2.6, 0));

            @Override
            void draw(Graphics2D g, double beat) {
                swung(g, tail, WAG * beat, -15, 0, fin, edge);
                part(g, backFin, fin, edge);
                part(g, lowFin, fin, edge);
                part(g, bellyFin, fin, edge);
                g.setPaint(skin);
                g.fill(body);
                g.setColor(yellow);
                g.fill(back);
                g.setColor(bar);
                g.fill(bars);
                outline(g, body, edge);
                swung(g, sideFin, 0.4 + 0.3 * beat, 1, 3, fin, edge);
                eye(g, 13, -3, 5.5);
                mouth(g, 15.5, 3.5, 18, 2.5, edge);
            }
        },

        /** Fusilier: a slim blue torpedo with a yellow back and a deeply forked yellow tail. */
        FUSILIER {
            private final Paint skin = new GradientPaint(0, -6, new Color(64, 156, 246), 0, 7, new Color(222, 238, 248));
            private final Color yellow = new Color(255, 220, 48);
            private final Color fin = new Color(96, 160, 226);
            private final Color stripe = new Color(24, 78, 170);
            private final Color edge = new Color(18, 46, 104);
            private final Shape body = closed(21, 0.5, 17, -5, 8, -8.5, -2, -8, -10, -7.5, -15, -3.5, -19, -1.3,
                    -19, -0.4, -19, 0.4, -19, 1.3, -15, 3.5, -10, 7.5, -2, 8, 8, 8.5, 17, 5, 21, 0.5);
            private final Shape tail = closed(-17.5, -1.3, -21, -3, -25, -7, -29, -9.5, -26, -5, -24, -2, -23, 0,
                    -24, 2, -26, 5, -29, 9.5, -25, 7, -21, 3, -17.5, 1.3);
            private final Shape backFin = closed(9, -7, 6, -12.5, -4, -11.5, -13, -4.5);
            private final Shape lowFin = closed(-3, 7.5, -6, 11.5, -11, 10, -14, 4);
            private final Shape sideFin = new Ellipse2D.Double(3, 1, 6.5, 3.5);
            // Yellow along the back, widening toward the tail.
            private final Shape back = inside(body, closed(7, -9, -2, -10, -12, -8, -20, -3, -20, -1, -20, 0, -19, 1.2,
                    -12, -1.5, -2, -4, 7, -5.5));
            private final Shape line = new QuadCurve2D.Double(15, -1.5, 0, -1.5, -17, 0.8);

            @Override
            void draw(Graphics2D g, double beat) {
                swung(g, tail, WAG * beat, -18, 0, yellow, edge);
                part(g, backFin, fin, edge);
                part(g, lowFin, fin, edge);
                g.setPaint(skin);
                g.fill(body);
                g.setColor(yellow);
                g.fill(back);
                g.setColor(stripe);
                g.setStroke(FINE);
                g.draw(line);
                outline(g, body, edge);
                swung(g, sideFin, 0.4 + 0.3 * beat, 3, 2, fin, edge);
                eye(g, 15, -2, 5);
                mouth(g, 18.5, 2.5, 21, 1.5, edge);
            }
        };

        /**
         * Draws this fish facing right around (0, 0).
         *
         * @param beat how far the tail has swung, from -1 to 1
         */
        abstract void draw(Graphics2D g, double beat);
    }

    private static final Color WHITE = new Color(252, 252, 248);
    private static final Color SHINE = new Color(255, 255, 255, 110);
    private static final Color PUPIL = new Color(20, 16, 14);
    private static final BasicStroke LINE = new BasicStroke(1.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    private static final BasicStroke FINE = new BasicStroke(0.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    private static final double WAG = 0.35;   // how far the tail swings, in radians

    private static final Look[] LOOKS = Look.values();
    private static final int STEPS = 12;                // tail positions from one side to the other
    private static final int POSES_PER_LOOK = 2 * STEPS;   // every tail position, facing left and right

    private final SpriteCache sprites = new SpriteCache(POSES_PER_LOOK * LOOKS.length, 34, 20, 20,
            SmallFishPainter::drawPose);

    @Override
    public void paint(Graphics2D g, ObjectView object, double time) {
        int step = SpriteCache.step(Math.sin(10 * time + object.getPhase()), STEPS);
        int look = ObjectPainter.designOf(object, LOOKS.length);
        sprites.paint(g, object.getX(), object.getY(),
                look * POSES_PER_LOOK + SpriteCache.pose(step, object.getFacing()));
    }

    /** Draws one kind of fish once, facing and wagging as the pose says. */
    private static void drawPose(Graphics2D g, int pose) {
        int swim = pose % POSES_PER_LOOK;   // the tail position and facing, without the kind of fish
        g.scale(SpriteCache.facingOf(swim), 1);   // drawn facing right; flipped when it swims left
        LOOKS[pose / POSES_PER_LOOK].draw(g, SpriteCache.swingOf(SpriteCache.stepOf(swim), STEPS));
    }

    // ---- Parts the fish share --------------------------------------------

    /** Fills a shape and draws a line round it. */
    private static void part(Graphics2D g, Shape shape, Paint paint, Color edge) {
        g.setPaint(paint);
        g.fill(shape);
        outline(g, shape, edge);
    }

    private static void outline(Graphics2D g, Shape shape, Color edge) {
        g.setColor(edge);
        g.setStroke(LINE);
        g.draw(shape);
    }

    /** A tail or fin, turned around the point (x, y) where it joins the body. */
    private static void swung(Graphics2D g, Shape shape, double angle, double x, double y, Paint paint, Color edge) {
        AffineTransform still = g.getTransform();
        g.rotate(angle, x, y);
        part(g, shape, paint, edge);
        g.setTransform(still);
    }

    /** Light on the fish's side, starting at (x, y). */
    private static void shine(Graphics2D g, double x, double y, double length) {
        g.setColor(SHINE);
        g.fill(new Ellipse2D.Double(x, y, length, 3.5));
    }

    /** A white eye with a dark pupil and a glint, its middle at (x, y). */
    private static void eye(Graphics2D g, double x, double y, double size) {
        g.setColor(Color.WHITE);
        g.fill(new Ellipse2D.Double(x - size / 2, y - size / 2, size, size));
        g.setColor(PUPIL);
        g.setStroke(FINE);
        g.draw(new Ellipse2D.Double(x - size / 2, y - size / 2, size, size));
        g.fill(new Ellipse2D.Double(x - size / 4 + 0.5, y - size / 4, size * 0.55, size * 0.55));
        g.setColor(Color.WHITE);
        g.fill(new Ellipse2D.Double(x + 0.3, y - size / 4, size * 0.25, size * 0.25));
    }

    private static void mouth(Graphics2D g, double x1, double y1, double x2, double y2, Color edge) {
        g.setColor(edge);
        g.setStroke(FINE);
        g.draw(new Line2D.Double(x1, y1, x2, y2));
    }

    // ---- Making the shapes -----------------------------------------------

    /** A closed outline of smooth curves: a start point, then six numbers for each curve. */
    private static Shape closed(double startX, double startY, double... curves) {
        Path2D shape = new Path2D.Double();
        shape.moveTo(startX, startY);
        for (int i = 0; i < curves.length; i += 6) {
            shape.curveTo(curves[i], curves[i + 1], curves[i + 2], curves[i + 3], curves[i + 4], curves[i + 5]);
        }
        shape.closePath();
        return shape;
    }

    /** A closed outline with straight edges through the given points: a fin with spines. */
    private static Shape spiky(double... points) {
        Path2D shape = new Path2D.Double();
        shape.moveTo(points[0], points[1]);
        for (int i = 2; i < points.length; i += 2) {
            shape.lineTo(points[i], points[i + 1]);
        }
        shape.closePath();
        return shape;
    }

    /** Separate straight lines, four numbers each, as one shape so they cost one stroke. */
    private static Shape strokes(double... ends) {
        Path2D lines = new Path2D.Double();
        for (int i = 0; i < ends.length; i += 4) {
            lines.moveTo(ends[i], ends[i + 1]);
            lines.lineTo(ends[i + 2], ends[i + 3]);
        }
        return lines;
    }

    /**
     * Bands running from the top of a fish to the bottom, as one shape.
     *
     * @param bands three numbers for each band: its rear edge x, its width,
     *              and how far its middle bows forward
     */
    private static Shape bands(double... bands) {
        Path2D shape = new Path2D.Double();
        for (int i = 0; i < bands.length; i += 3) {
            double x = bands[i];
            double width = bands[i + 1];
            double bow = 2 * bands[i + 2];   // a curve passes halfway to its control point
            shape.moveTo(x, -20);
            shape.quadTo(x + bow, 0, x, 20);
            shape.lineTo(x + width, 20);
            shape.quadTo(x + width + bow, 0, x + width, -20);
            shape.closePath();
        }
        return shape;
    }

    /** The part of a marking that lies on the body. */
    private static Shape inside(Shape body, Shape marking) {
        Area part = new Area(marking);
        part.intersect(new Area(body));
        return part;
    }
}
