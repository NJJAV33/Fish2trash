package ui.render;

import game.Badge;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.util.EnumMap;
import java.util.Map;

/**
 * Draws a badge as a medal on a ribbon: bronze, silver and gold, each with
 * its own emblem. A badge that was not earned is drawn in plain grey.
 *
 * Every shape is laid out once for a medal of radius 100 and scaled to the
 * size that is asked for.
 */
public final class BadgePainter {

    private static final double DESIGN_RADIUS = 100;
    private static final Color LOCKED_METAL = new Color(88, 98, 108);
    private static final Color LOCKED_RIBBON = new Color(62, 70, 78);
    private static final Color LOCKED_INK = new Color(60, 68, 76);
    private static final Color SHINE = new Color(255, 255, 255, 130);
    private static final BasicStroke RIM = new BasicStroke(9);
    private static final BasicStroke RING = new BasicStroke(3);
    private static final BasicStroke SHINE_STROKE = new BasicStroke(7, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);

    private static final Shape DISC = new Ellipse2D.Double(-100, -100, 200, 200);
    private static final Shape DISC_EDGE = new Ellipse2D.Double(-95, -95, 190, 190);
    private static final Shape INNER_RING = new Ellipse2D.Double(-76, -76, 152, 152);
    private static final Shape SHINE_ARC = new Arc2D.Double(-86, -86, 172, 172, 115, 45, Arc2D.OPEN);
    private static final Shape LEFT_TAIL = ribbonTail(Math.toRadians(22));
    private static final Shape RIGHT_TAIL = ribbonTail(Math.toRadians(-22));

    /** One coloured shape of an emblem. */
    private static final class Part {
        private final Shape shape;
        private final Color color;

        Part(Shape shape, Color color) {
            this.shape = shape;
            this.color = color;
        }
    }

    /** How one badge looks: its metal, its ribbon and the parts of its emblem. */
    private static final class Look {
        private final Color metal;
        private final Color ribbon;
        private final Part[] emblem;

        Look(Color metal, Color ribbon, Part... emblem) {
            this.metal = metal;
            this.ribbon = ribbon;
            this.emblem = emblem;
        }
    }

    private final Map<Badge, Look> looks = new EnumMap<>(Badge.class);

    public BadgePainter() {
        // Eco Rookie: a green leaf on bronze.
        AffineTransform tilt = AffineTransform.getRotateInstance(Math.toRadians(35));
        Path2D leaf = new Path2D.Double();
        leaf.moveTo(0, -58);
        leaf.quadTo(62, -8, 0, 58);
        leaf.quadTo(-62, -8, 0, -58);
        looks.put(Badge.ECO_ROOKIE, new Look(UiPalette.BRONZE, new Color(59, 178, 115),
                new Part(tilt.createTransformedShape(leaf), new Color(34, 112, 58)),
                new Part(tilt.createTransformedShape(new Rectangle2D.Double(-2.5, -44, 5, 112)),
                        new Color(160, 214, 150))));

        // Sorter: three dots on silver, one for each kind of trash.
        looks.put(Badge.SORTER, new Look(UiPalette.SILVER, new Color(46, 134, 222),
                new Part(new Ellipse2D.Double(-21, -51, 42, 42), new Color(46, 134, 222)),
                new Part(new Ellipse2D.Double(-53, 3, 42, 42), new Color(192, 57, 43)),
                new Part(new Ellipse2D.Double(11, 3, 42, 42), new Color(232, 140, 30))));

        // Champion: a star on gold.
        Path2D star = new Path2D.Double();
        for (int i = 0; i < 10; i++) {
            double angle = Math.toRadians(-90 + i * 36);
            double reach = i % 2 == 0 ? 60 : 26;   // points and notches take turns
            if (i == 0) {
                star.moveTo(reach * Math.cos(angle), reach * Math.sin(angle));
            } else {
                star.lineTo(reach * Math.cos(angle), reach * Math.sin(angle));
            }
        }
        star.closePath();
        looks.put(Badge.CHAMPION, new Look(UiPalette.GOLD, new Color(214, 69, 65),
                new Part(star, new Color(150, 92, 0))));

        // Fail at startup, not in the middle of a game, if a badge has no look.
        for (Badge badge : Badge.values()) {
            if (!looks.containsKey(badge)) {
                throw new IllegalStateException("No medal for " + badge);
            }
        }
    }

    /** Draws the badge with the middle of its medal at (x, y). */
    public void paint(Graphics2D g, Badge badge, double x, double y, double radius, boolean earned) {
        Look look = looks.get(badge);
        Color metal = earned ? look.metal : LOCKED_METAL;
        Color ribbon = earned ? look.ribbon : LOCKED_RIBBON;

        Graphics2D medal = (Graphics2D) g.create();
        medal.translate(x, y);
        medal.scale(radius / DESIGN_RADIUS, radius / DESIGN_RADIUS);

        medal.setColor(ribbon.darker());
        medal.fill(RIGHT_TAIL);
        medal.setColor(ribbon);
        medal.fill(LEFT_TAIL);

        medal.setColor(metal);
        medal.fill(DISC);
        medal.setStroke(RIM);
        medal.setColor(metal.darker());
        medal.draw(DISC_EDGE);
        medal.setStroke(RING);
        medal.setColor(metal.brighter());
        medal.draw(INNER_RING);

        for (Part part : look.emblem) {
            medal.setColor(earned ? part.color : LOCKED_INK);
            medal.fill(part.shape);
        }
        if (earned) {
            medal.setStroke(SHINE_STROKE);
            medal.setColor(SHINE);
            medal.draw(SHINE_ARC);
        }
        medal.dispose();
    }

    /** One tail of the ribbon, hanging down from the middle of the medal and turned a little outwards. */
    private static Shape ribbonTail(double angle) {
        Path2D tail = new Path2D.Double();
        tail.moveTo(-26, 0);
        tail.lineTo(26, 0);
        tail.lineTo(26, 175);
        tail.lineTo(0, 150);    // the notch cut into the end
        tail.lineTo(-26, 175);
        tail.closePath();
        return AffineTransform.getRotateInstance(angle).createTransformedShape(tail);
    }
}
