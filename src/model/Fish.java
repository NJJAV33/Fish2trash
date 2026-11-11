package model;

import util.GameConstants;

/**
 * A fish. Catching one hurts the ocean, so the player loses health.
 *
 * INHERITANCE: Fish "is an" OceanObject, just like OceanTrash, but it
 * overrides onCaught() with its own behavior.
 */
public class Fish extends OceanObject {

    public Fish(double x, double y, double speedX) {
        super(x, y, GameConstants.RADIUS_SMALL_FISH, speedX);
    }

    @Override
    public ObjectKind getKind() {
        return ObjectKind.SMALL_FISH;
    }

    @Override
    public void onCaught(Player player) {
        player.loseHealth(GameConstants.DAMAGE_FISH);
    }
}
