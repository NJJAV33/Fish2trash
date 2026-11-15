package ui.render;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

/**
 * Helpers for pictures that are drawn once and then reused (the background,
 * the fishing net).
 *
 * The window can show the game larger than 800 x 600. A reused picture is
 * therefore made at exactly the size it appears on screen, and copied to the
 * screen pixel for pixel. That keeps it sharp, and a plain copy is by far
 * the fastest way to draw an image.
 */
final class PixelImages {

    private static final double MIN_SCALE = 0.25;
    private static final double MAX_SCALE = 3;        // above this, enlarging the picture is good enough
    private static final double ROUND_TO = 1000;      // scales are compared to three decimals
    private static final double SAME_SCALE = 0.002;

    private PixelImages() {
        // only static methods
    }

    /** How much the target enlarges what is drawn on it: 1 for 800 x 600, 2 for twice that. */
    static double scaleOf(Graphics2D target) {
        AffineTransform view = target.getTransform();
        double scale = Math.hypot(view.getScaleX(), view.getShearY());
        scale = Math.round(scale * ROUND_TO) / ROUND_TO;
        return Math.max(MIN_SCALE, Math.min(MAX_SCALE, scale));
    }

    /**
     * A new, empty picture for an area of the game world.
     *
     * @param scale      how much larger than the world area the picture is
     * @param seeThrough true for a picture with see-through parts
     */
    static BufferedImage create(double worldWidth, double worldHeight, double scale, boolean seeThrough) {
        return new BufferedImage((int) Math.ceil(worldWidth * scale), (int) Math.ceil(worldHeight * scale),
                seeThrough ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB);
    }

    /** A graphics for drawing on the picture in world units, with smooth edges. */
    static Graphics2D draw(BufferedImage image, double scale) {
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.scale(scale, scale);
        return g;
    }

    /**
     * Copies a picture onto the target with its top left corner at world
     * position (x, y).
     *
     * @param scale the scale the picture was made for
     */
    static void paint(Graphics2D target, BufferedImage image, double x, double y, double scale) {
        AffineTransform view = target.getTransform();
        boolean upright = view.getShearX() == 0 && view.getShearY() == 0;
        if (upright && Math.abs(view.getScaleX() - scale) < SAME_SCALE
                && Math.abs(view.getScaleY() - scale) < SAME_SCALE) {
            // The picture has exactly the size it gets on the target: copy it
            // pixel for pixel, lined up with the target's pixels.
            Graphics2D exact = (Graphics2D) target.create();
            exact.setTransform(AffineTransform.getTranslateInstance(
                    Math.round(view.getTranslateX() + x * view.getScaleX()),
                    Math.round(view.getTranslateY() + y * view.getScaleY())));
            exact.drawImage(image, 0, 0, null);
            exact.dispose();
        } else {
            // Any other size: let Java shrink or stretch it (slower, and a little soft).
            AffineTransform place = AffineTransform.getTranslateInstance(x, y);
            place.scale(1 / scale, 1 / scale);
            target.drawImage(image, place, null);
        }
    }
}
