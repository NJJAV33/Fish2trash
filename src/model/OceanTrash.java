package model;

/**
 * A piece of trash. Catching it earns points and it rides the hook up to
 * the boat.
 *
 * INHERITANCE: OceanTrash "is an" OceanObject, so it gets position, movement
 * and collision for free. It is still abstract: the concrete kinds of trash
 * (PlasticBottle, Can, FishingNet) extend it and only say what they are and
 * what they are worth.
 */
public abstract class OceanTrash extends OceanObject {

    private final int points;

    protected OceanTrash(double x, double y, double radius, double speedX, int points) {
        super(x, y, radius, speedX);
        this.points = points;
    }

    @Override
    public void onCaught(Player player, Hook hook) {
        markCaught();
        player.addPoints(points);
        hook.attach(this);
    }

    @Override
    public int getPoints() {
        return points;
    }
}
