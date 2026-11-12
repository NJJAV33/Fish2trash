package model;

import util.GameConstants;

/** An aluminium can. Worth more than a bottle. */
public class Can extends OceanTrash {

    public Can(double x, double y, double speedX) {
        super(x, y, GameConstants.RADIUS_CAN, speedX, GameConstants.POINTS_CAN);
    }

    @Override
    public ObjectKind getKind() {
        return ObjectKind.CAN;
    }
}
