package ui.render;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Arrays;

/**
 * Pictures of one kind of object in a fixed number of poses, each drawn once
 * and then copied to the screen (see PixelImages).
 *
 * Every antialiased shape costs about the same to draw, however small it is,
 * and a detailed fish is made of more than a dozen of them. Copying a finished
 * picture costs about as much as one shape. So an animated object is drawn in
 * a set of poses (its tail swung a little further each time), and every frame
 * copies the pose closest to the real one.
 */
final class SpriteCache {

    /** Draws one pose of the object around (0, 0), in world units. */
    interface PoseDrawer {
        void draw(Graphics2D g, int pose);
    }

    private final PoseDrawer drawer;
    private final int halfWidth;   // the picture's size around the object's middle, in world units
    private final int above;
    private final int below;
    private final BufferedImage[] pictures;
    private double pictureScale;   // the size the pictures were made for

    /**
     * @param poses     how many different poses there are
     * @param halfWidth room left and right of the object's middle
     * @param above     room above the middle
     * @param below     room below the middle
     */
    SpriteCache(int poses, int halfWidth, int above, int below, PoseDrawer drawer) {
        this.drawer = drawer;
        this.halfWidth = halfWidth;
        this.above = above;
        this.below = below;
        this.pictures = new BufferedImage[poses];
    }

    /** Copies the picture of the given pose with the object's middle at (x, y), drawing it first if needed. */
    void paint(Graphics2D g, double x, double y, int pose) {
        double scale = PixelImages.scaleOf(g);
        if (scale != pictureScale) {
            Arrays.fill(pictures, null);   // the window shows the game at another size now
            pictureScale = scale;
        }
        if (pictures[pose] == null) {
            BufferedImage picture = PixelImages.create(2 * halfWidth, above + below, scale, true);
            Graphics2D pg = PixelImages.draw(picture, scale);
            pg.translate(halfWidth, above);
            drawer.draw(pg, pose);
            pg.dispose();
            pictures[pose] = picture;
        }
        PixelImages.paint(g, pictures[pose], x - halfWidth, y - above, scale);
    }

    /** Rounds a swing between -1 and 1 to one of the given number of steps. */
    static int step(double swing, int steps) {
        return (int) Math.round((swing + 1) / 2 * (steps - 1));
    }

    /** The swing a step stands for: the opposite of step(). */
    static double swingOf(int step, int steps) {
        return 2.0 * step / (steps - 1) - 1;
    }

    /** One pose number for a step and a facing (1 = right, -1 = left). */
    static int pose(int step, int facing) {
        return 2 * step + (facing > 0 ? 1 : 0);
    }

    static int stepOf(int pose) {
        return pose / 2;
    }

    static int facingOf(int pose) {
        return pose % 2 == 1 ? 1 : -1;
    }
}
