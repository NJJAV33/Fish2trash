package model;

/**
 * A tool on the end of the line.
 *
 * ABSTRACTION + TEMPLATE METHOD: attempt() is the fixed recipe every tool
 * follows (final, so no subclass can change it). The one step that differs
 * per tool - what it does to each kind of object - is the abstract
 * outcomeFor(), which each concrete tool fills in.
 */
public abstract class Equipment {

    private final String displayName;
    private final double tipRadius;   // size of the tool for collisions

    protected Equipment(String displayName, double tipRadius) {
        this.displayName = displayName;
        this.tipRadius = tipRadius;
    }

    /** What happens when this tool touches the object. */
    public final CatchOutcome attempt(OceanObject object) {
        if (!object.isCatchable()) {
            return CatchOutcome.PASS;   // already caught, or just got away
        }
        return outcomeFor(object.getKind());
    }

    /** What this tool does to an object of the given kind. Each tool decides. */
    protected abstract CatchOutcome outcomeFor(ObjectKind kind);

    public String getDisplayName() {
        return displayName;
    }

    public double getTipRadius() {
        return tipRadius;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
