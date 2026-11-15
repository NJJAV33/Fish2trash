package model;

import util.GameConstants;

/**
 * A magnet on the line. Only metal sticks to it: it lifts cans and goes
 * straight past bottles and nets. It is heavy, so it knocks any fish it
 * touches - and stingrays can feel it coming.
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
}
