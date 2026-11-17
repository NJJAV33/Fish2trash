package ui.render;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

/**
 * The net cutter: heavy marine net shears. Two blunt-nosed stainless blades,
 * one of them serrated to grip the twine, a slotted pivot bolt, orange grips
 * (a long loop for the fingers, a short one for the thumb) and a lanyard ring
 * for the line. The blades snip on the way out.
 *
 * Each half is drawn standing upright and turned around the pivot at (0, 0).
 */
public final class CutterPainter implements ToolPainter {

    private static final Color OUTLINE = new Color(18, 26, 38);
    private static final Color BLADE = new Color(200, 209, 219);
    private static final Color SHINE = new Color(248, 251, 254);
    private static final Color GRIP = new Color(246, 150, 40);   // bright, so it shows in dark water
    private static final Color BOLT = new Color(226, 232, 238);

    private static final BasicStroke RIM = new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    private static final BasicStroke GLINT = new BasicStroke(1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    private static final BasicStroke GROOVE = new BasicStroke(0.6f);
    private static final AffineTransform MIRROR = AffineTransform.getScaleInstance(-1, 1);

    private static final double REST_ANGLE = 0.38;    // how far each half is turned, in radians
    private static final double SNIP_MIDDLE = 0.30;
    private static final double SNIP_SWING = 0.22;
    private static final double SNIP_SPEED = 22;
    private static final double TOOTH = 2.4;          // length of one tooth of the serrated edge

    private static final double LOOP_LEFT = -1.6;     // where a finger loop starts, before its half is turned
    private static final double LOOP_BOTTOM = -8.4;
    private static final double LOOP_WALL = 2.9;      // thickness of a loop's rim
    private static final double FINGER_WIDTH = 10.4;  // the long loop takes three fingers
    private static final double FINGER_HEIGHT = 13.8;
    private static final double THUMB_WIDTH = 9.2;    // the short loop takes the thumb
    private static final double THUMB_HEIGHT = 11.4;

    // The plain half: its blade ends up on the left, its long finger loop on the right.
    private static final Area PLAIN_BLADE = blade(false);
    private static final Area PLAIN_GRIP = grip(FINGER_WIDTH, FINGER_HEIGHT);
    private static final Shape PLAIN_OUTLINE = outline(PLAIN_BLADE, PLAIN_GRIP);
    private static final Shape PLAIN_SHINE = shine(0.1, LOOP_LEFT, FINGER_WIDTH, FINGER_HEIGHT);

    // The serrated half is a mirror image, with teeth along the cutting edge and the thumb loop.
    private static final Area SERRATED_BLADE = blade(true).createTransformedArea(MIRROR);
    private static final Area SERRATED_GRIP = grip(THUMB_WIDTH, THUMB_HEIGHT).createTransformedArea(MIRROR);
    private static final Shape SERRATED_OUTLINE = outline(SERRATED_BLADE, SERRATED_GRIP);
    private static final Shape SERRATED_SHINE = shine(-0.5, -LOOP_LEFT - THUMB_WIDTH, THUMB_WIDTH, THUMB_HEIGHT);

    private static final Shape PIVOT_DARK = pivotDark();
    private static final Shape PIVOT_STEEL = pivotSteel();

    @Override
    public void paint(Graphics2D g, double time, boolean working) {
        double open = working ? SNIP_MIDDLE + SNIP_SWING * Math.sin(time * SNIP_SPEED) : REST_ANGLE;

        g.rotate(open);
        paintHalf(g, PLAIN_OUTLINE, PLAIN_BLADE, PLAIN_GRIP, PLAIN_SHINE);
        g.rotate(-2 * open);
        paintHalf(g, SERRATED_OUTLINE, SERRATED_BLADE, SERRATED_GRIP, SERRATED_SHINE);
        g.rotate(open);   // upright again for the bolt, which does not turn

        g.setColor(OUTLINE);
        g.fill(PIVOT_DARK);
        g.setColor(BOLT);
        g.fill(PIVOT_STEEL);
    }

    /** One blade with its grip, in four fills. */
    private static void paintHalf(Graphics2D g, Shape outline, Shape blade, Shape grip, Shape shine) {
        g.setColor(OUTLINE);
        g.fill(outline);
        g.setColor(BLADE);
        g.fill(blade);
        g.setColor(GRIP);
        g.fill(grip);
        g.setColor(SHINE);
        g.fill(shine);
    }

    /**
     * A blade pointing down, with its cutting edge on the right and a blunt
     * nose, so it cannot spear a fish.
     */
    private static Area blade(boolean serrated) {
        Path2D.Double blade = new Path2D.Double();
        blade.moveTo(1.6, -3.4);
        if (serrated) {
            // Small notches all the way down the edge.
            for (double y = 3; y < 17; y += TOOTH) {
                blade.lineTo(1.6, y);
                blade.lineTo(0.5, y + TOOTH / 2);
            }
        }
        blade.lineTo(1.6, 17.6);
        blade.quadTo(1.6, 21.6, -0.6, 21.6);    // the nose
        blade.quadTo(-4.6, 17, -5.4, 5);        // the curved back
        blade.lineTo(-5.2, -1.4);
        blade.quadTo(-5, -3.8, -2.4, -3.8);
        blade.closePath();

        Area shape = new Area(blade);
        // A groove where the ground edge begins. The dark underneath shows through it.
        shape.subtract(new Area(GROOVE.createStrokedShape(new Line2D.Double(-0.9, 4.5, -0.6, 18))));
        return shape;
    }

    /** A grip pointing up: a short neck and a loop of the given size, set off to the right. */
    private static Area grip(double width, double height) {
        Path2D.Double neck = new Path2D.Double();
        neck.moveTo(-2.1, -2.6);
        neck.lineTo(2.1, -2.6);
        neck.lineTo(4.8, -10);
        neck.lineTo(0.6, -10);
        neck.closePath();

        double top = LOOP_BOTTOM - height;
        Area grip = new Area(new RoundRectangle2D.Double(LOOP_LEFT, top, width, height, width - 1, width - 1));
        grip.subtract(new Area(new RoundRectangle2D.Double(LOOP_LEFT + LOOP_WALL, top + LOOP_WALL,
                width - 2 * LOOP_WALL, height - 2 * LOOP_WALL, width - 6, width - 6)));   // the hole
        grip.add(new Area(neck));
        // Two ribs across the neck, for a wet hand to hold on to.
        grip.subtract(new Area(new Rectangle2D.Double(-4, -5.9, 12, 0.6)));
        grip.subtract(new Area(new Rectangle2D.Double(-4, -7.9, 12, 0.6)));
        return grip;
    }

    /** The blade and grip of one half, a little fatter: the dark edge around them. */
    private static Shape outline(Area blade, Area grip) {
        Area all = new Area(blade);
        all.add(grip);
        Area outline = new Area(RIM.createStrokedShape(all));
        outline.add(all);
        return new Path2D.Double(outline);
    }

    /**
     * Light on the ground edge of a blade and round the upper left of its loop.
     *
     * @param edgeX    left side of the bright strip along the cutting edge
     * @param loopLeft left side of the loop, after any mirroring
     */
    private static Shape shine(double edgeX, double loopLeft, double loopWidth, double loopHeight) {
        double inset = LOOP_WALL / 2 - 0.2;   // keeps the gleam in the middle of the rim
        Area shine = new Area(new Rectangle2D.Double(edgeX, 2.5, 0.9, 15));
        shine.add(new Area(GLINT.createStrokedShape(new Arc2D.Double(
                loopLeft + inset, LOOP_BOTTOM - loopHeight + inset,
                loopWidth - 2 * inset, loopHeight - 2 * inset, 80, 110, Arc2D.OPEN))));
        return new Path2D.Double(shine);
    }

    /** The dark parts in the middle: the bolt's rim and the edge of the lanyard ring. */
    private static Shape pivotDark() {
        Area dark = new Area(new Ellipse2D.Double(-3.6, -3.6, 7.2, 7.2));
        Area ring = new Area(new Ellipse2D.Double(-4.3, -11.4, 8.6, 8.6));
        ring.subtract(new Area(new Ellipse2D.Double(-1.5, -8.6, 3, 3)));
        dark.add(ring);
        return new Path2D.Double(dark);
    }

    /** The bolt head with its slot, and the lanyard ring the line is tied to. */
    private static Shape pivotSteel() {
        Area steel = new Area(new Ellipse2D.Double(-2.6, -2.6, 5.2, 5.2));
        Path2D.Double slot = new Path2D.Double();
        slot.moveTo(-2.7, 1.1);
        slot.lineTo(2.7, -1.1);
        steel.subtract(new Area(GROOVE.createStrokedShape(slot)));

        Area ring = new Area(new Ellipse2D.Double(-3.3, -10.4, 6.6, 6.6));
        ring.subtract(new Area(new Ellipse2D.Double(-2.1, -9.2, 4.2, 4.2)));
        ring.subtract(new Area(new Ellipse2D.Double(-3.6, -3.6, 7.2, 7.2)));   // it sits behind the bolt
        steel.add(ring);
        return new Path2D.Double(steel);
    }
}
