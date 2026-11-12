package model;

/**
 * A read-only window onto an ocean object.
 *
 * ABSTRACTION: the drawing code only receives this interface, so it can look
 * at an object but has no way to move it or change it.
 */
public interface ObjectView {

    ObjectKind getKind();

    double getX();

    double getY();

    double getRadius();

    /** 1 when the object looks to the right, -1 when it looks to the left. */
    int getFacing();

    boolean isCaught();

    /**
     * How alarmed the object looks: 0 = calm, 1 = fully alarmed. Dangerous
     * fish use it to warn the player (a pufferfish swells, a barracuda flashes).
     */
    double getAlert();

    /** 0 = invisible, 1 = fully visible. Objects fade in when they appear. */
    double getOpacity();
}
