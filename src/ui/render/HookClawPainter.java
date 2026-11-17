package ui.render;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;

/**
 * The hook: a forged steel grapnel, the kind a clean-up crew drags debris
 * with. Two barbed prongs sweep up from the crown, a third points at the
 * viewer, and the rope is whipped to a welded eye on top of the shank.
 *
 * Every colour is one ready-made shape, so a frame is only five fills.
 */
public final class HookClawPainter implements ToolPainter {

    private static final Color OUTLINE = new Color(18, 26, 38);
    private static final Color STEEL = new Color(172, 183, 196);
    private static final Color STEEL_DARK = new Color(104, 117, 134);
    private static final Color SHINE = new Color(242, 247, 251);
    private static final Color TWINE = new Color(226, 172, 92);   // the whipping that holds the rope

    private static final BasicStroke RIM = new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    private static final BasicStroke EDGE = new BasicStroke(1.8f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND);
    private static final BasicStroke GLINT = new BasicStroke(0.9f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    private static final BasicStroke SEAM = new BasicStroke(0.7f);
    private static final AffineTransform MIRROR = AffineTransform.getScaleInstance(-1, 1);

    private static final double EYE_Y = -9;   // the middle of the eye

    // Built once, in this order: the later shapes are cut out of the earlier ones.
    private static final Area FRONT_PRONG = new Area(frontProng());
    private static final Area STEEL_SHAPE = steel();
    private static final Shape TWINE_SHAPE = whipping();
    private static final Shape OUTLINE_SHAPE = outline();
    private static final Shape SHADE_SHAPE = shade();
    private static final Shape SHINE_SHAPE = shine();

    @Override
    public void paint(Graphics2D g, double time, boolean working) {
        g.setColor(OUTLINE);
        g.fill(OUTLINE_SHAPE);
        g.setColor(STEEL);
        g.fill(STEEL_SHAPE);
        g.setColor(STEEL_DARK);
        g.fill(SHADE_SHAPE);
        g.setColor(SHINE);
        g.fill(SHINE_SHAPE);
        g.setColor(TWINE);
        g.fill(TWINE_SHAPE);
    }

    /** All the steel: the eye, the collar under it, the shank and the prongs. */
    private static Area steel() {
        Area steel = new Area(new Ellipse2D.Double(-4, EYE_Y - 4, 8, 8));
        steel.subtract(new Area(new Ellipse2D.Double(-2.1, EYE_Y - 2.1, 4.2, 4.2)));   // the hole the rope goes through
        steel.add(new Area(new Rectangle2D.Double(-2.4, -5.6, 4.8, 1.7)));             // collar

        Path2D.Double shank = new Path2D.Double();
        shank.moveTo(-1.5, -5);
        shank.lineTo(1.5, -5);
        shank.lineTo(2, 10);
        shank.lineTo(-2, 10);
        shank.closePath();
        steel.add(new Area(shank));

        Area prong = new Area(sidePronged());
        steel.add(prong);
        steel.add(prong.createTransformedArea(MIRROR));

        // A dark seam around the front prong, so it stands out from the crown behind it.
        steel.subtract(new Area(SEAM.createStrokedShape(FRONT_PRONG)));
        return steel;
    }

    /** The left prong with its barb. The right one is its mirror image. */
    private static Shape sidePronged() {
        Path2D.Double prong = new Path2D.Double();
        prong.moveTo(0.5, 13);
        prong.curveTo(-6.5, 13.4, -12, 10.2, -12, 3.5);   // the outside of the bend
        prong.lineTo(-10.9, -3.6);                        // the point
        prong.lineTo(-9.4, 3);
        prong.curveTo(-9.3, 7, -5.5, 9.3, 0.5, 9);        // the inside of the bend
        prong.closePath();

        // The barb leans back from the point, toward the shank.
        prong.moveTo(-10.4, -1.4);
        prong.lineTo(-6.9, 3.2);
        prong.lineTo(-9.6, 2.9);
        prong.closePath();
        return prong;
    }

    /** The third prong, seen end on: a short claw lying over the crown. */
    private static Shape frontProng() {
        Path2D.Double prong = new Path2D.Double();
        prong.moveTo(0, 4.6);
        prong.quadTo(2.1, 9, 1.7, 12.2);
        prong.lineTo(-1.7, 12.2);
        prong.quadTo(-2.1, 9, 0, 4.6);
        prong.closePath();
        return prong;
    }

    /** Three turns of twine above the eye. The gaps between them show as dark lines. */
    private static Shape whipping() {
        Path2D.Double turns = new Path2D.Double();
        for (int turn = 0; turn < 3; turn++) {
            turns.append(new Rectangle2D.Double(-2.3, -17.4 + turn * 1.6, 4.6, 1.1), false);
        }
        return turns;
    }

    /** Everything, a little fatter: the dark edge that shows on bright water. */
    private static Shape outline() {
        Area all = new Area(STEEL_SHAPE);
        all.add(FRONT_PRONG);
        all.add(new Area(new Rectangle2D.Double(-2.3, -17.4, 4.6, 4.3)));
        Area outline = new Area(RIM.createStrokedShape(all));
        outline.add(all);
        return new Path2D.Double(outline);
    }

    /** The sides that face away from the light: under the bend and down the right. */
    private static Shape shade() {
        Path2D.Double under = new Path2D.Double();
        under.moveTo(-12, 3.5);
        under.curveTo(-12, 10.2, -6.5, 13.4, 0, 13);
        under.curveTo(6.5, 13.4, 12, 10.2, 12, 3.5);
        under.lineTo(10.9, -3.6);
        Area shade = new Area(EDGE.createStrokedShape(under));
        shade.add(new Area(new Rectangle2D.Double(0.6, -5.6, 1.8, 14)));
        shade.add(new Area(GLINT.createStrokedShape(new Arc2D.Double(-3, EYE_Y - 3, 6, 6, 250, 110, Arc2D.OPEN))));
        shade.intersect(STEEL_SHAPE);
        shade.subtract(FRONT_PRONG);
        return new Path2D.Double(shade);
    }

    /** Light from the upper left: on the eye, down the shank and along the top of each prong. */
    private static Shape shine() {
        Path2D.Double lit = new Path2D.Double();
        lit.moveTo(-11.3, 3.4);        // the outside of the left point
        lit.lineTo(-10.8, -2.2);
        lit.moveTo(-8.6, 6);           // the top of the left bend
        lit.quadTo(-6.5, 9.2, -2.6, 9.7);
        lit.moveTo(9.8, 3.4);          // the inside of the right point
        lit.lineTo(10.6, -2);
        lit.moveTo(8.6, 6);
        lit.quadTo(6.5, 9.2, 2.6, 9.7);
        lit.moveTo(-1, -4.4);          // the shank
        lit.lineTo(-1.1, 4);
        lit.moveTo(-0.2, 7);           // the front prong
        lit.lineTo(-0.5, 11);
        Area shine = new Area(GLINT.createStrokedShape(lit));
        shine.add(new Area(GLINT.createStrokedShape(new Arc2D.Double(-3, EYE_Y - 3, 6, 6, 95, 110, Arc2D.OPEN))));
        return new Path2D.Double(shine);
    }
}
