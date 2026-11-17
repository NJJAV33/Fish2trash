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

    private final EquipmentType type;
    private final double tipRadius;   // size of the tool for collisions

    protected Equipment(EquipmentType type, double tipRadius) {
        this.type = type;
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

    /**
     * How much health it costs when this tool touches a fish that usually
     * costs the given amount. Most tools change nothing; a tool with blades
     * overrides this.
     */
    public int fishDamage(int usualDamage) {
        return usualDamage;
    }

    /**
     * What this tool does to a fish it touches, after the health is lost.
     * Each tool is different: one snags the fish, one knocks it out, one
     * kills it. The tool also decides what the hook does next.
     */
    public abstract void strike(Fish fish, Hook hook);

    /** True for a tool that stingrays can feel. Only the Magnet is. */
    public boolean isMagnetic() {
        return false;
    }

    public EquipmentType getType() {
        return type;
    }

    public String getDisplayName() {
        return type.getDisplayName();
    }

    public double getTipRadius() {
        return tipRadius;
    }

    @Override
    public String toString() {
        return type.getDisplayName();
    }
}
