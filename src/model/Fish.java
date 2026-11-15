package model;

import util.GameConstants;

/**
 * A fish. Any tool that touches one hurts it, so the player loses health
 * and the fish swims away again.
 *
 * INHERITANCE: Fish "is an" OceanObject, just like OceanTrash, but it
 * overrides onCaught() with the opposite result. Concrete fish extend it.
 */
public abstract class Fish extends OceanObject {

    private final int damage;

    protected Fish(double x, double y, double radius, double speedX, int damage) {
        super(x, y, radius, speedX);
        this.damage = damage;
    }

    @Override
    public void onCaught(Player player, Hook hook) {
        // The tool on the line has a say: the Net Cutter's blades cut deeper.
        player.loseHealth(hook.getLoadout().getSelected().fishDamage(damage));
        // The fish gets away, and cannot be hooked again for a moment.
        release(GameConstants.FISH_RELEASE_IMMUNITY_S);
        hook.beginRetractEmpty();
    }

    public int getDamage() {
        return damage;
    }
}
