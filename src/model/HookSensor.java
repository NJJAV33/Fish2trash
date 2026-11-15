package model;

/**
 * What the rest of the ocean is allowed to know about the hook: where its tip
 * is, whether the line is out, and whether the tool on it is magnetic. Fish and the level use this, so they never
 * depend on the full Hook class.
 */
public interface HookSensor {

    double getTipX();

    double getTipY();

    /** True while the hook is away from the boat (going out or coming back). */
    boolean isLineOut();

    /** True while the tool on the line is a magnet, whether the line is out or not. Stingrays can feel that. */
    boolean isMagnetic();
}
