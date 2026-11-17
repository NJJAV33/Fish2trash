package ui.render;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

/**
 * The magnet: a salvage pot magnet, as used for magnet fishing. A squat red
 * housing, a steel eyebolt on top with the rope knotted through it, and the
 * bright steel face showing as a band along the bottom.
 *
 * While it works, rings of pull close in on the face from below.
 */
public final class MagnetPainter implements ToolPainter {

    private static final Color OUTLINE = new Color(18, 26, 38);
    private static final Color PAINT = new Color(208, 50, 46);
    private static final Color PAINT_LIGHT = new Color(242, 104, 88);
    private static final Color PAINT_DARK = new Color(148, 28, 36);
    private static final Color STEEL = new Color(186, 196, 207);
    private static final Color STEEL_DARK = new Color(108, 120, 137);
    private static final Color SHINE = new Color(246, 250, 253);
    private static final Color ROPE = new Color(226, 172, 92);

    private static final BasicStroke RIM = new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    private static final BasicStroke GLINT = new BasicStroke(1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    private static final BasicStroke ROPE_END = new BasicStroke(1.9f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    private static final BasicStroke TURN = new BasicStroke(0.6f);
    private static final BasicStroke PULL_STROKE = new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);

    private static final double HALF = 15;      // half the width of the housing
    private static final double TOP_Y = -2.5;   // the middle of the top face
    private static final double BASE_Y = 7.5;   // the middle of the bottom face
    private static final double TILT = 3.2;     // how much of the round faces shows
    private static final double BAND = 3.4;     // height of the steel showing under the paint
    private static final double EYE_Y = -11;    // the middle of the eyebolt's ring
    private static final Shape KNOT_BODY = new RoundRectangle2D.Double(-2.8, -19.8, 5.6, 4.6, 3, 3);

    // Built once, in this order: the later shapes are cut out of the earlier ones.
    private static final Area HOUSING = housing();
    private static final Area PAINT_SHAPE = paintedPart();
    private static final Area BAND_SHAPE = steelBand();
    private static final Area EYEBOLT = eyebolt();
    private static final Area KNOT = knot();
    private static final Shape OUTLINE_SHAPE = outline();
    private static final Shape PAINT_LIGHT_SHAPE = paintLight();
    private static final Shape PAINT_DARK_SHAPE = paintDark();
    private static final Shape STEEL_SHAPE = steel();
    private static final Shape STEEL_DARK_SHAPE = steelDark();
    private static final Shape SHINE_SHAPE = shine();

    // The pull: one ring for every step of its way in, and how bright it is at that step.
    private static final int PULL_STEPS = 24;
    private static final int PULL_RINGS = 3;
    private static final double PULL_SPEED = 1.5;   // trips from far to near, per second
    private static final double PULL_FAR = 18;
    private static final double PULL_NEAR = 9;
    private static final Shape[] PULL_SHAPES = new Shape[PULL_STEPS];
    private static final Color[] PULL_COLORS = new Color[PULL_STEPS];

    static {
        for (int step = 0; step < PULL_STEPS; step++) {
            double way = (double) step / PULL_STEPS;
            double radius = PULL_FAR + (PULL_NEAR - PULL_FAR) * way;
            PULL_SHAPES[step] = PULL_STROKE.createStrokedShape(
                    new Arc2D.Double(-radius, 5 - radius, 2 * radius, 2 * radius, 222, 96, Arc2D.OPEN));
            // Faint far away, brightest half way, gone as it reaches the face.
            PULL_COLORS[step] = new Color(255, 246, 170, (int) (235 * Math.sin(Math.PI * way)));
        }
    }

    @Override
    public void paint(Graphics2D g, double time, boolean working) {
        if (working) {
            double turn = time * PULL_SPEED;
            for (int ring = 0; ring < PULL_RINGS; ring++) {
                double way = turn + (double) ring / PULL_RINGS;
                int step = (int) ((way - Math.floor(way)) * PULL_STEPS);
                g.setColor(PULL_COLORS[step]);
                g.fill(PULL_SHAPES[step]);
            }
        }

        g.setColor(OUTLINE);
        g.fill(OUTLINE_SHAPE);
        g.setColor(PAINT);
        g.fill(PAINT_SHAPE);
        g.setColor(PAINT_LIGHT);
        g.fill(PAINT_LIGHT_SHAPE);
        g.setColor(PAINT_DARK);
        g.fill(PAINT_DARK_SHAPE);
        g.setColor(STEEL);
        g.fill(STEEL_SHAPE);
        g.setColor(STEEL_DARK);
        g.fill(STEEL_DARK_SHAPE);
        g.setColor(SHINE);
        g.fill(SHINE_SHAPE);
        g.setColor(ROPE);
        g.fill(KNOT);
    }

    /** A round face of the housing, seen from a little above, so it shows as a flat oval. */
    private static Area face(double y, double inset) {
        double half = HALF - inset;
        double tilt = TILT - inset;
        return new Area(new Ellipse2D.Double(-half, y - tilt, 2 * half, 2 * tilt));
    }

    /** A tall strip, used to pick out one side of a shape. */
    private static Area strip(double left, double width) {
        return new Area(new Rectangle2D.Double(left, -30, width, 60));
    }

    /** The whole housing: a short, wide cylinder. */
    private static Area housing() {
        Area housing = new Area(new Rectangle2D.Double(-HALF, TOP_Y, 2 * HALF, BASE_Y - TOP_Y));
        housing.add(face(TOP_Y, 0));
        housing.add(face(BASE_Y, 0));
        return housing;
    }

    /** The housing down to where the paint stops, or down to a little lower. */
    private static Area downTo(double y) {
        Area upper = new Area(new Rectangle2D.Double(-HALF, -30, 2 * HALF, 30 + y));
        upper.add(face(y, 0));
        upper.intersect(HOUSING);
        return upper;
    }

    private static Area paintedPart() {
        return downTo(BASE_Y - BAND);
    }

    /** The bare steel below the paint. It starts a hair lower, which leaves a dark seam. */
    private static Area steelBand() {
        Area band = new Area(HOUSING);
        band.subtract(downTo(BASE_Y - BAND + 0.7));
        return band;
    }

    /** The eyebolt: a ring on a short stem, held by a nut on the top face. */
    private static Area eyebolt() {
        Area bolt = new Area(new Ellipse2D.Double(-4.6, EYE_Y - 4.6, 9.2, 9.2));
        bolt.subtract(new Area(new Ellipse2D.Double(-2.5, EYE_Y - 2.5, 5, 5)));
        bolt.add(new Area(new Rectangle2D.Double(-1.6, -7, 3.2, 1.6)));

        Path2D.Double nut = new Path2D.Double();
        nut.moveTo(-2.8, -5.9);
        nut.lineTo(2.8, -5.9);
        nut.lineTo(3.8, -4.8);
        nut.lineTo(3.8, -2.6);
        nut.lineTo(-3.8, -2.6);
        nut.lineTo(-3.8, -4.8);
        nut.closePath();
        bolt.add(new Area(nut));
        return bolt;
    }

    /**
     * The rope tied to the eyebolt: a loop through the ring, a knot above it
     * and the loose end sticking up beside the line.
     */
    private static Area knot() {
        Area knot = new Area(KNOT_BODY);
        knot.add(new Area(ROPE_END.createStrokedShape(new Ellipse2D.Double(-2.3, -17.4, 4.6, 5.6))));
        knot.add(new Area(ROPE_END.createStrokedShape(new Line2D.Double(1.8, -17.6, 4.7, -20.3))));
        // Two turns of the rope, left as gaps so the dark underneath shows through.
        Path2D.Double turns = new Path2D.Double();
        turns.moveTo(-3.4, -17.5);
        turns.lineTo(3.4, -18.9);
        turns.moveTo(-3.4, -15.8);
        turns.lineTo(3.4, -17.2);
        knot.subtract(new Area(TURN.createStrokedShape(turns)));
        return knot;
    }

    /** Everything, a little fatter: the dark edge that shows on bright water. */
    private static Shape outline() {
        Area all = new Area(HOUSING);
        all.add(EYEBOLT);
        all.add(new Area(KNOT_BODY));
        all.add(KNOT);
        Area outline = new Area(RIM.createStrokedShape(all));
        outline.add(all);
        return new Path2D.Double(outline);
    }

    /** The top face, which catches the light, and a soft gleam down the left of the wall. */
    private static Shape paintLight() {
        Area light = face(TOP_Y, 0.9);
        Area gleam = new Area(PAINT_SHAPE);
        gleam.subtract(face(TOP_Y + 1.2, 0));
        gleam.subtract(strip(-30, 30 - 11.8));
        gleam.subtract(strip(-8.6, 40));
        light.add(gleam);
        return new Path2D.Double(light);
    }

    /** The right of the wall, turned away from the light. */
    private static Shape paintDark() {
        Area dark = new Area(PAINT_SHAPE);
        dark.subtract(face(TOP_Y, 0));
        dark.intersect(strip(7.2, 30));
        return new Path2D.Double(dark);
    }

    private static Shape steel() {
        Area steel = new Area(BAND_SHAPE);
        steel.add(EYEBOLT);
        // A dark edge around the rope where it lies over the ring.
        steel.subtract(new Area(TURN.createStrokedShape(KNOT)));
        return new Path2D.Double(steel);
    }

    /** The shaded right side of the band, the nut and the ring. */
    private static Shape steelDark() {
        Area dark = new Area(BAND_SHAPE);
        dark.intersect(strip(7.2, 30));
        Area nutSide = new Area(EYEBOLT);
        nutSide.intersect(new Area(new Rectangle2D.Double(1.4, -5.9, 6, 4)));
        dark.add(nutSide);
        dark.add(new Area(GLINT.createStrokedShape(
                new Arc2D.Double(-3.55, EYE_Y - 3.55, 7.1, 7.1, 250, 110, Arc2D.OPEN))));
        return new Path2D.Double(dark);
    }

    /** Bright glints on the ring, the nut and the left of the band. */
    private static Shape shine() {
        Area shine = new Area(BAND_SHAPE);
        Area glints = strip(-11.8, 3.2);
        glints.add(strip(-6.4, 1.2));
        shine.intersect(glints);
        Area nutSide = new Area(EYEBOLT);
        nutSide.intersect(new Area(new Rectangle2D.Double(-3.8, -5.9, 2, 4)));
        shine.add(nutSide);
        shine.add(new Area(GLINT.createStrokedShape(
                new Arc2D.Double(-3.55, EYE_Y - 3.55, 7.1, 7.1, 95, 110, Arc2D.OPEN))));
        return new Path2D.Double(shine);
    }
}
