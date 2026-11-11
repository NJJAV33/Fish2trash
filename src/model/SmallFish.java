package model;

import util.GameConstants;

/** A small, harmless reef fish. */
public class SmallFish extends Fish {

    public SmallFish(double x, double y, double speedX) {
        super(x, y, GameConstants.RADIUS_SMALL_FISH, speedX, GameConstants.DAMAGE_FISH);
    }

    @Override
    public ObjectKind getKind() {
        return ObjectKind.SMALL_FISH;
    }
}
