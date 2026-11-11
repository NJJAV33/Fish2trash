package model;

import util.GameConstants;

/**
 * A piece of trash. Catching it cleans the ocean and earns points.
 *
 * INHERITANCE: OceanTrash "is an" OceanObject, so it gets position, movement
 * and collision for free and only adds what makes trash different.
 */
public class OceanTrash extends OceanObject {

    public OceanTrash(double x, double y, double speedX) {
        super(x, y, GameConstants.RADIUS_TRASH, speedX);
    }

    @Override
    public ObjectKind getKind() {
        return ObjectKind.CAN;
    }

    @Override
    public void onCaught(Player player) {
        player.addPoints(GameConstants.POINTS_BOTTLE);
    }
}
