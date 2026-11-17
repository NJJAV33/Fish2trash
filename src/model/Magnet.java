package model;

import util.GameConstants;

/**
 * A magnet on the line. Only metal sticks to it: it lifts cans, pulls cans
 * that are close toward it (see Can), and goes straight past bottles and
 * nets. It is heavy, so it knocks out any fish it touches - and stingrays
 * can feel it coming.
 */
public class Magnet extends Equipment {

    public Magnet() {
        super(EquipmentType.MAGNET, GameConstants.RADIUS_MAGNET);
    }

    @Override
    protected CatchOutcome outcomeFor(ObjectKind kind) {
        if (kind == ObjectKind.CAN) {
            return CatchOutcome.CATCH;
        }
        // Other trash does not stick; wildlife gets knocked.
        return kind.isTrash() ? CatchOutcome.PASS : CatchOutcome.CATCH;
    }

    @Override
    public boolean isMagnetic() {
        return true;
    }

    /** A heavy block of steel: the fish is knocked out and the line comes back empty. */
    @Override
    public void strike(Fish fish, Hook hook) {
        fish.daze();
        hook.beginRetractEmpty();
    }
}
