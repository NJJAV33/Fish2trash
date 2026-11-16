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

    /**
     * Which of a painter's designs this object gets, from 0 to designs - 1.
     * It comes from the object's phase, which never changes, so a bottle
     * keeps its look for as long as it floats, and bottles side by side
     * usually differ.
     */
    static int designOf(ObjectView object, int designs) {
        return (int) Math.floor(object.getPhase() * 97) % designs;
    }
}
