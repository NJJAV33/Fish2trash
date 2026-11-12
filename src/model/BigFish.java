package model;

import util.GameConstants;

/** A big, fast fish. Just as harmless as a small one, but harder to avoid. */
public class BigFish extends Fish {

    public BigFish(double x, double y, double speedX) {
        super(x, y, GameConstants.RADIUS_BIG_FISH, speedX, GameConstants.DAMAGE_FISH);
    }

    @Override
    public ObjectKind getKind() {
        return ObjectKind.BIG_FISH;
    }
}
