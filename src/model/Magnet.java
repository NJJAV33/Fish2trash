package model;

import util.GameConstants;

/**
 * A magnet on the line. Only metal sticks to it: it lifts cans and goes
 * straight past everything else, so it can never hurt a fish.
 */
public class Magnet extends Equipment {

    public Magnet() {
        super(EquipmentType.MAGNET, GameConstants.RADIUS_MAGNET);
    }

    @Override
    protected CatchOutcome outcomeFor(ObjectKind kind) {
        return (kind == ObjectKind.CAN) ? CatchOutcome.CATCH : CatchOutcome.PASS;
    }
}
