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
import model.ObjectView;

/**
 * Draws a big fish that faces the way it swims and beats its tail. Fast ones
 * trail speed lines. Big fish come in several kinds (a grouper, a parrotfish,
 * a red snapper, a trevally, a tuna), and each fish keeps the one it was given.
 */
public final class BigFishPainter implements ObjectPainter {

    /**
     * The kinds of big fish. Each keeps its own shapes and colours, made
     * once, and draws itself facing right around (0, 0).
     */
    private enum Look {
        /** Grouper (lapu-lapu): heavy and brown with dark spots, a big jaw and a round tail. */
        GROUPER {
            private final Paint skin = new GradientPaint(0, -15, new Color(184, 128, 80), 0, 14, new Color(222, 188, 140));
            private final Color fin = new Color(150, 100, 62);
            private final Color spot = new Color(84, 44, 24, 215);
            private final Color edge = new Color(52, 28, 14);
            private final Shape body = closed(31, 2, 29, -8, 17, -15, 2, -15, -12, -15, -21, -8, -27, -4.5,
                    -27, -1.5, -27, 1.5, -27, 4.5, -21, 9, -12, 14, 2, 14, 15, 14.5, 28, 12, 31, 2);
            private final Shape tail = closed(-25, -4.5, -31, -9, -41, -13, -44, -7, -46, -3, -46, 3, -44, 7,
                    -41, 13, -31, 9, -25, 4.5);
            private final Shape spines = spiky(15, -12, 12, -20, 9, -15.5, 6, -21.5, 3, -16.5, 0, -22, -3, -16.5, -5, -12);
            private final Shape backFin = closed(-3, -14, -5, -23, -18, -22, -21, -8);
            private final Shape lowFin = closed(-6, 13, -8, 21, -18, 20, -21, 7);
            private final Shape bellyFin = closed(11, 13, 10, 20, 4, 22, 2, 13);
            private final Shape sideFin = new Ellipse2D.Double(2, 1, 11, 7);
            private final Shape spots = inside(body, dots(1.9, -21, -3, -16, -9, -15, 3, -10, -4, -9, 8, -5, -11,
                    -3, 1, 1, -7, 3, 9, 7, -11, 9, -3, 15, 4, 20, 9, -20, 5, 13, -9, 24, -9));
            private final Shape tailSpots = dots(1.7, -33, -4, -34, 4, -40, -6, -40, 6, -39, 0);
            private final Shape mouth = new QuadCurve2D.Double(31, 2.5, 26, 3, 21.5, 7.5);
            private final Shape gill = new QuadCurve2D.Double(15, -10, 9, 0, 15, 10);

            @Override
            void draw(Graphics2D g, double beat) {
                AffineTransform still = g.getTransform();
                g.rotate(WAG * beat, -26, 0);   // the tail swings where it joins the body
                part(g, tail, fin, edge);
                g.setColor(spot);
                g.fill(tailSpots);
                g.setTransform(still);
                part(g, spines, fin, edge);
                part(g, backFin, fin, edge);
                part(g, lowFin, fin, edge);
                part(g, bellyFin, fin, edge);
                g.setPaint(skin);
                g.fill(body);
                g.setColor(spot);
                g.fill(spots);
                outline(g, body, edge);
                g.setStroke(FINE);
                g.draw(gill);
                g.setStroke(THICK);
                g.draw(mouth);
                swung(g, sideFin, 0.5 + 0.2 * beat, 2, 4, fin, edge);
                eye(g, 21, -6, 7, Color.WHITE);
            }
        },

        /** Parrotfish: blue-green with pink fins, a blunt head and a pale beak. */
        PARROTFISH {
            private final Paint skin = new GradientPaint(0, -14, new Color(70, 212, 190), 0, 13, new Color(34, 140, 208));
            private final Color pink = new Color(255, 130, 166);
            private final Color belly = new Color(206, 240, 150, 150);
            private final Color scale = new Color(16, 104, 136, 190);
            private final Color beak = new Color(252, 242, 206);
            private final Color edge = new Color(10, 58, 76);
            private final Shape body = closed(31, 1, 31, -8, 22, -14, 4, -14, -10, -14, -20, -8, -27, -4,
                    -27, -1.3, -27, 1.3, -27, 4, -20, 8, -10, 13, 4, 13, 20, 13, 31, 9, 31, 1);
            private final Shape tail = closed(-26, -4, -32, -8, -39, -12.5, -45, -14.5, -42, -5, -42, 5, -45, 14.5,
                    -39, 12.5, -32, 8, -26, 4);
            private final Shape tailEdges = strokes(-30, -5.5, -41.5, -11.5, -30, 5.5, -41.5, 11.5);
            private final Shape backFin = closed(18, -12.5, 14, -20.5, -14, -20, -22, -6.5);
            private final Shape lowFin = closed(-4, 12.5, -8, 19, -17, 18, -22, 6);
            private final Shape bellyFin = closed(12, 12.5, 11, 19, 5, 20, 3, 12.5);
            private final Shape sideFin = new Ellipse2D.Double(4, 1, 11, 6);
            private final Shape under = inside(body, new Ellipse2D.Double(-14, 4, 40, 14));
            private final Shape scales = arcs(-18, -4, -18, 3, -12, -8, -12, -1, -12, 6, -6, -5, -6, 2, 0, -9, 0, -2,
                    6, -6, 12, -9);
            private final Shape beakShape = closed(27, -5, 30, -5.5, 33.5, -3, 33.5, 1.5, 33.5, 5, 30, 7, 27, 7);
            private final Shape cheek = strokes(27, -3, 18, 0.5, 27, 5.5, 20, 8);   // pink lines behind the beak

            @Override
            void draw(Graphics2D g, double beat) {
                AffineTransform still = g.getTransform();
                g.rotate(WAG * beat, -26, 0);   // the tail swings where it joins the body
                part(g, tail, skin, edge);
                g.setColor(pink);
                g.setStroke(THICK);
                g.draw(tailEdges);
                g.setTransform(still);
                part(g, backFin, pink, edge);
                part(g, lowFin, pink, edge);
                part(g, bellyFin, pink, edge);
                g.setPaint(skin);
                g.fill(body);
                g.setColor(belly);
                g.fill(under);
                g.setColor(scale);
                g.setStroke(FINE);
                g.draw(scales);
                g.setColor(pink);
                g.setStroke(THICK);
                g.draw(cheek);
                outline(g, body, edge);
                part(g, beakShape, beak, edge);
                g.setStroke(FINE);
                g.draw(new Line2D.Double(27.5, 1.3, 33.3, 1.5));   // where the two halves of the beak meet
                swung(g, sideFin, 0.5 + 0.2 * beat, 4, 4, pink, edge);
                eye(g, 21.5, -5.5, 6, Color.WHITE);
            }
        },

        /** Red snapper (maya-maya): red with a pointed head, a spiny back fin and a yellow eye. */
        SNAPPER {
            private final Paint skin = new GradientPaint(0, -13, new Color(236, 70, 60), 0, 12, new Color(255, 182, 166));
            private final Color fin = new Color(216, 50, 48);
            private final Color darker = new Color(160, 24, 34, 150);
            private final Color iris = new Color(255, 218, 96);
            private final Color edge = new Color(98, 14, 20);
            private final Shape body = closed(33, 3, 28, -5, 18, -13, 4, -13, -10, -13, -20, -7, -27, -3.5,
                    -27, -1, -27, 1, -27, 3.5, -20, 7, -10, 12, 4, 12, 18, 12, 28, 9, 33, 3);
            private final Shape tail = closed(-26, -3.5, -32, -7, -39, -12, -45, -14.5, -39.5, -5, -39.5, 5, -45, 14.5,
                    -39, 12, -32, 7, -26, 3.5);
            private final Shape backFin = spiky(16, -11, 13, -19, 10.5, -14.5, 7, -20.5, 4.5, -15.5, 1, -21,
                    -1.5, -16, -5, -20.5, -7.5, -15.5, -11, -19.5, -18, -15, -21, -6);
            private final Shape lowFin = spiky(-6, 11, -9, 19, -16, 15.5, -20, 6);
            private final Shape bellyFin = spiky(11, 11.5, 7, 20, 2, 12);
            private final Shape sideFin = closed(10, 1.5, 6, 1.5, 0, 5, -5, 10.5, 1, 9, 7, 6, 10, 1.5);
            private final Shape sideLine = new QuadCurve2D.Double(15, -6, 0, -9, -24, -1);
            private final Shape gill = new QuadCurve2D.Double(17, -9, 11, 0, 17, 9);

            @Override
            void draw(Graphics2D g, double beat) {
                swung(g, tail, WAG * beat, -26, 0, fin, edge);
                part(g, backFin, fin, edge);
                part(g, lowFin, fin, edge);
                part(g, bellyFin, fin, edge);
                g.setPaint(skin);
                g.fill(body);
                g.setColor(darker);
                g.setStroke(FINE);
                g.draw(sideLine);
                shine(g, -14, -10.5, 26);
                outline(g, body, edge);
                g.setStroke(FINE);
                g.draw(gill);
                g.draw(new Line2D.Double(32.5, 4.5, 26.5, 6));   // mouth
                swung(g, sideFin, 0.2 * beat, 10, 2, fin, edge);
                eye(g, 23, -3.5, 7.5, iris);
            }
        },

        /** Trevally (talakitok): a deep silver jack with a steep head, blue fins and a scythe tail. */
        TREVALLY {
            private final Paint skin = new GradientPaint(0, -15, new Color(124, 158, 184), 0, 4, new Color(240, 244, 246));
            private final Color fin = new Color(44, 108, 196);
            private final Color dark = new Color(30, 44, 66);
            private final Color edge = new Color(22, 36, 58);
            private final Shape body = closed(30, 3, 30, -6, 22, -15, 6, -15.5, -8, -16, -18, -7, -26, -2,
                    -26, -0.7, -26, 0.7, -26, 2, -18, 6, -8, 13, 6, 13, 18, 13, 28, 10, 30, 3);
            private final Shape tail = closed(-25, -2, -30, -5, -38, -12, -45, -16.5, -40, -8.5, -35, -3, -33, 0,
                    -35, 3, -40, 8.5, -45, 16.5, -38, 12, -30, 5, -25, 2);
            private final Shape spines = spiky(17, -12.5, 13, -18.5, 8, -14.5);
            private final Shape backFin = closed(6, -15, 4, -20, 1, -23, -2.5, -24, -2.5, -18.5, -10, -13, -24, -3.5);
            private final Shape lowFin = closed(2, 13, 0, 17, -2, 20, -5.5, 21, -5.5, 16.5, -11, 11, -24, 3.5);
            private final Shape sideFin = closed(12, 1, 6, 2, -1, 7, -6, 12.5, -1.5, 9.5, 5, 6, 12, 1);
            private final Shape sideLine = new QuadCurve2D.Double(17, -6, 4, -7.5, -10, 0.3);
            private final Shape scutes = new Line2D.Double(-10, 0.3, -24.5, 0);   // hard scales along the tail stem
            private final Shape spot = new Ellipse2D.Double(13, -8.5, 3, 3.5);    // a dark spot on the gill cover

            @Override
            void draw(Graphics2D g, double beat) {
                swung(g, tail, WAG * beat, -25, 0, fin, edge);
                part(g, spines, fin, edge);
                part(g, backFin, fin, edge);
                part(g, lowFin, fin, edge);
                g.setPaint(skin);
                g.fill(body);
                g.setColor(dark);
                g.setStroke(FINE);
                g.draw(sideLine);
                g.setStroke(THICK);
                g.draw(scutes);
                g.fill(spot);
                outline(g, body, edge);
                g.setStroke(FINE);
                g.draw(new Line2D.Double(29.5, 5, 24, 6.5));   // mouth
                swung(g, sideFin, 0.2 * beat, 12, 1, fin, edge);
                eye(g, 22.5, -4.5, 7, Color.WHITE);
            }
        },

        /** Tuna: a torpedo with a dark back, a silver belly, yellow fins and finlets, and a crescent tail. */
        TUNA {
            private final Paint skin = new GradientPaint(0, -8, new Color(36, 72, 136), 0, 3, new Color(228, 234, 239));
            private final Color dark = new Color(42, 80, 146);
            private final Color yellow = new Color(255, 214, 40);
            private final Color edge = new Color(10, 20, 46);
            private final Shape body = closed(35, 1, 28, -5, 16, -11.5, 2, -11.5, -12, -11.5, -22, -5, -29, -1.8,
                    -29, -0.6, -29, 0.6, -29, 1.8, -22, 4.5, -12, 10.5, 2, 10.5, 16, 10.5, 28, 6, 35, 1);
            private final Shape tail = closed(-28, -1.8, -32, -5, -38, -12, -44, -17, -41, -9, -37.5, -3, -36.5, 0,
                    -37.5, 3, -41, 9, -44, 17, -38, 12, -32, 5, -28, 1.8);
            private final Shape spines = spiky(13, -10, 9, -17.5, -1, -11);
            private final Shape backFin = closed(-2, -11, -3, -17, -6, -20.5, -9.5, -21.5, -8, -16, -8, -13, -9.5, -9.5);
            private final Shape lowFin = closed(-4, 10, -5, 15, -8, 19, -11.5, 20, -10, 15, -10, 12, -11.5, 8.5);
            private final Shape finlets = finlets(-13.5, -9, -17.5, -7.4, -21.5, -5.6, -25, -4,
                    -14, 8.3, -18, 6.6, -22, 5, -25.5, 3.5);
            private final Shape sideFin = closed(13, 2, 7, 2.5, 0, 6.5, -5, 10.5, 0, 8, 6, 5.5, 13, 2);
            private final Shape stripe = new QuadCurve2D.Double(22, -2.5, 0, -3.5, -26, -0.5);
            private final Shape gill = new QuadCurve2D.Double(20, -7.5, 15, 0, 20, 7);

            @Override
            void draw(Graphics2D g, double beat) {
                swung(g, tail, WAG * beat, -28, 0, dark, edge);
                part(g, spines, dark, edge);
                part(g, backFin, yellow, edge);
                part(g, lowFin, yellow, edge);
                g.setColor(yellow);
                g.fill(finlets);
                g.setColor(edge);
                g.setStroke(FINE);
                g.draw(finlets);
                g.setPaint(skin);
                g.fill(body);
                g.setColor(yellow);
                g.setStroke(LINE);
                g.draw(stripe);
                outline(g, body, edge);
                g.setStroke(FINE);
                g.draw(gill);
                g.draw(new Line2D.Double(34, 2.2, 28.5, 4));   // mouth
                swung(g, sideFin, 0.2 * beat, 13, 2, dark, edge);
                eye(g, 25.5, -2, 6, Color.WHITE);
            }
        };

        /**
         * Draws this fish facing right around (0, 0).
         *
         * @param beat how far the tail has swung, from -1 to 1
         */
        abstract void draw(Graphics2D g, double beat);
    }

    private static final Color SHINE = new Color(255, 255, 255, 90);
    private static final Color SPEED_LINE = new Color(255, 255, 255, 90);
    private static final Color PUPIL = new Color(16, 16, 20);
    private static final BasicStroke LINE = new BasicStroke(1.3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    private static final BasicStroke FINE = new BasicStroke(1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    private static final BasicStroke THICK = new BasicStroke(1.7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    private static final BasicStroke SPEED_STROKE = new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    private static final double WAG = 0.28;   // how far the tail swings, in radians
    private static final double FAST = 120;   // pixels per second: from here on it shows speed lines
    private static final Shape SPEED_LINES = speedLines();

    private static final Look[] LOOKS = Look.values();
    private static final int STEPS = 12;                   // tail positions from one side to the other
    private static final int POSES_PER_LOOK = 2 * STEPS;   // every tail position, facing left and right

    private final SpriteCache sprites = new SpriteCache(POSES_PER_LOOK * LOOKS.length, 50, 26, 26,
            BigFishPainter::drawPose);

    @Override
    public void paint(Graphics2D g, ObjectView object, double time) {
        if (Math.abs(object.getSpeedX()) > FAST) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.translate(object.getX(), object.getY());
            g2.scale(object.getFacing(), 1);
            g2.setColor(SPEED_LINE);
            g2.setStroke(SPEED_STROKE);
            g2.draw(SPEED_LINES);
            g2.dispose();
        }
        int step = SpriteCache.step(Math.sin(14 * time + object.getPhase()), STEPS);
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

    /** Light on the fish's back, starting at (x, y). */
    private static void shine(Graphics2D g, double x, double y, double length) {
        g.setColor(SHINE);
        g.fill(new Ellipse2D.Double(x, y, length, 4));
    }

    /** An eye with a dark pupil and a glint, its middle at (x, y). */
    private static void eye(Graphics2D g, double x, double y, double size, Color iris) {
        g.setColor(iris);
        g.fill(new Ellipse2D.Double(x - size / 2, y - size / 2, size, size));
        g.setColor(PUPIL);
        g.setStroke(FINE);
        g.draw(new Ellipse2D.Double(x - size / 2, y - size / 2, size, size));
        g.fill(new Ellipse2D.Double(x - size / 4 + 0.5, y - size / 4, size * 0.55, size * 0.55));
        g.setColor(Color.WHITE);
        g.fill(new Ellipse2D.Double(x + 0.4, y - size / 4, size * 0.25, size * 0.25));
    }

    // ---- Making the shapes -----------------------------------------------

    /** Three short lines trailing behind a fast fish, as one shape so they cost one stroke. */
    private static Shape speedLines() {
        return strokes(-52, -8, -68, -8, -56, 0, -76, 0, -52, 8, -68, 8);
    }

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

    /** Round spots of one size at the given middles, as one shape. */
    private static Shape dots(double radius, double... middles) {
        Path2D spots = new Path2D.Double();
        for (int i = 0; i < middles.length; i += 2) {
            spots.append(new Ellipse2D.Double(middles[i] - radius, middles[i + 1] - radius, 2 * radius, 2 * radius), false);
        }
        return spots;
    }

    /** Scale marks: small arcs open toward the tail, at the given middles, as one shape. */
    private static Shape arcs(double... middles) {
        Path2D scales = new Path2D.Double();
        for (int i = 0; i < middles.length; i += 2) {
            scales.moveTo(middles[i], middles[i + 1] - 3);
            scales.quadTo(middles[i] + 5, middles[i + 1], middles[i], middles[i + 1] + 3);
        }
        return scales;
    }

    /**
     * A tuna's finlets: small triangles standing on the body's edge at the
     * given points, leaning back. Those above the middle point up, the others down.
     */
    private static Shape finlets(double... points) {
        Path2D finlets = new Path2D.Double();
        for (int i = 0; i < points.length; i += 2) {
            double x = points[i];
            double y = points[i + 1];
            double out = y < 0 ? -1 : 1;
            finlets.moveTo(x + 1.8, y - out);   // the base sits just inside the body
            finlets.lineTo(x - 2.4, y + 3 * out);
            finlets.lineTo(x - 1.6, y - out);
            finlets.closePath();
        }
        return finlets;
    }

    /** The part of a marking that lies on the body. */
    private static Shape inside(Shape body, Shape marking) {
        Area part = new Area(marking);
        part.intersect(new Area(body));
        return part;
    }
}
