package model;

/**
 * Anything floating or swimming in the ocean that the hook can catch.
 *
 * ABSTRACTION: this class describes what every ocean object has (a position,
 * a size, a speed) and what every ocean object can do (move, be caught),
 * without saying what happens when it is caught. It is abstract, so you can
 * never create a plain "OceanObject" - only a concrete subclass.
 *
 * It holds no drawing code: the ui package draws objects through ObjectView.
 */
public abstract class OceanObject implements ObjectView {

    // ENCAPSULATION: every field is private. Subclasses and other classes
    // can only read or change them through the methods below.
    private double x;
    private double y;
    private final double radius;
    private double speedX;   // pixels per second, negative = moving left
    private int facing;      // 1 = looking right, -1 = looking left
    private boolean caught;

    protected OceanObject(double x, double y, double radius, double speedX) {
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.speedX = speedX;
        this.facing = (speedX < 0) ? -1 : 1;
        this.caught = false;
    }

    /** Moves the object sideways and bounces it off the edges of the screen. */
    public void update(double dt, double worldWidth) {
        if (caught) {
            return;   // the hook is carrying it now
        }
        x += speedX * dt;
        if (x - radius < 0) {
            x = radius;
            turnAround();
        } else if (x + radius > worldWidth) {
            x = worldWidth - radius;
            turnAround();
        }
    }

    private void turnAround() {
        speedX = -speedX;
        facing = -facing;
    }

    /** Circle-vs-circle collision: true if the two circles overlap. */
    public boolean collidesWith(double otherX, double otherY, double otherRadius) {
        double dx = x - otherX;
        double dy = y - otherY;
        double reach = radius + otherRadius;
        return dx * dx + dy * dy <= reach * reach;
    }

    public void moveTo(double newX, double newY) {
        x = newX;
        y = newY;
    }

    public void markCaught() {
        caught = true;
    }

    @Override
    public boolean isCaught() {
        return caught;
    }

    @Override
    public double getX() {
        return x;
    }

    @Override
    public double getY() {
        return y;
    }

    @Override
    public double getRadius() {
        return radius;
    }

    @Override
    public int getFacing() {
        return facing;
    }

    /** What happens to the player when this object is caught. Each subclass decides. */
    public abstract void onCaught(Player player);

    @Override
    public String toString() {
        return getKind() + " at (" + Math.round(x) + ", " + Math.round(y) + ")";
    }
}
