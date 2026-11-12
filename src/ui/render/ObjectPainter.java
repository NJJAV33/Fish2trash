package ui.render;

import java.awt.Graphics2D;
import model.ObjectView;

/**
 * Draws one kind of ocean object.
 *
 * POLYMORPHISM: every kind has its own painter class behind this one
 * interface, so the renderer just calls paint() and the right drawing runs.
 */
public interface ObjectPainter {

    /** @param time seconds of animation so far, for swaying, wagging and blinking */
    void paint(Graphics2D g, ObjectView object, double time);
}
