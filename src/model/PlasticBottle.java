package model;

import util.GameConstants;

/** A plastic bottle, the most common trash and the easiest catch. */
public class PlasticBottle extends OceanTrash {

    public PlasticBottle(double x, double y, double speedX) {
        super(x, y, GameConstants.RADIUS_BOTTLE, speedX, GameConstants.POINTS_BOTTLE);
    }

    @Override
    public ObjectKind getKind() {
        return ObjectKind.PLASTIC_BOTTLE;
    }
}
