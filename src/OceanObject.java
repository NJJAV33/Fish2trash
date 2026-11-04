import java.awt.Graphics2D;

/**
 * Anything floating or swimming in the ocean that the hook can catch.
 *
 * ABSTRACTION: this class describes what every ocean object has (a position,
 * a size, a speed) and what every ocean object can do (move, be caught, be
 * drawn), without saying what happens when it is caught or how it looks.
 * It is abstract, so you can never create a plain "OceanObject" - only a
 * concrete subclass such as OceanTrash or Fish.
 */
public abstract class OceanObject {

    // protected: subclasses can read these when drawing themselves.
    protected double x;
    protected double y;
    protected double radius;
    protected double speedX;   // pixels per second, negative = moving left

    // private: only this class decides when an object counts as caught.
    private boolean caught;

    public OceanObject(double x, double y, double radius, double speedX) {
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.speedX = speedX;
        this.caught = false;
    }

    /** Moves the object sideways and bounces it off the edges of the screen. */
    public void update(double dt, int worldWidth) {
        if (caught) {
            return;   // the hook is carrying it now
        }
        x += speedX * dt;
        if (x - radius < 0) {
            x = radius;
            speedX = -speedX;
        } else if (x + radius > worldWidth) {
            x = worldWidth - radius;
            speedX = -speedX;
        }
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

    public boolean isCaught() {
        return caught;
    }

    public void markCaught() {
        caught = true;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getRadius() {
        return radius;
    }

    /** What happens to the player when this object is caught. Each subclass decides. */
    public abstract void onCaught(Player player);

    /** How this object looks. Each subclass decides. */
    public abstract void draw(Graphics2D g);
}
