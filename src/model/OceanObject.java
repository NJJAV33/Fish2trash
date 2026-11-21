package model;

import util.GameConstants;

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
    private double speedX;   // its own speed in pixels per second, negative = moving left
    private double drift;    // how fast the current carried it in the last step
    private int facing;      // 1 = looking right, -1 = looking left
    private boolean caught;
    private double immuneTimer;   // seconds left during which the hook cannot grab it
    private double age;           // seconds since it appeared
    private final double phase;   // differs per object; see getPhase()

    protected OceanObject(double x, double y, double radius, double speedX) {
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.speedX = speedX;
        this.facing = (speedX < 0) ? -1 : 1;
        this.caught = false;
        this.phase = (x * 0.37 + y * 0.61) % (2 * Math.PI);   // from where it appeared
    }

    /**
     * Advances the object by dt seconds.
     *
     * TEMPLATE METHOD: this method is final, so every object follows the same
     * steps - grow older, move, get carried by the current. Only the "move"
     * step can be replaced by a subclass.
     *
     * Nothing stops at the edge of the screen: objects drift and swim out of
     * view, and the level brings others in (see isGone).
     */
    public final void update(double dt, Sea sea) {
        age += dt;
        if (immuneTimer > 0) {
            immuneTimer = Math.max(0, immuneTimer - dt);
        }
        if (caught) {
            return;   // the hook is carrying it now
        }
        move(dt);
        drift = sea.getCurrentAt(y) * getCurrentShare();
        x += drift * dt;
    }

    /** How the object moves by itself in dt seconds. The default is a steady sideways drift. */
    protected void move(double dt) {
        x += speedX * dt;
    }

    /** How much of the current carries this object along: all of it, unless a subclass swims against it. */
    protected double getCurrentShare() {
        return 1;
    }

    /**
     * Something made this object think again. Trash cannot think, so nothing
     * happens; fish override this to turn around or change depth.
     *
     * @param roll a random number from 0 to 1 that decides what it does
     */
    public void changeCourse(double roll) {
        // nothing by default
    }

    /**
     * True once the object is out of the game: so far outside the screen
     * that it will not be seen again. The level takes it out and lets
     * another one come in.
     */
    public boolean isGone() {
        return !caught && (x < -GameConstants.EXIT_OUTSIDE
                || x > GameConstants.WORLD_WIDTH + GameConstants.EXIT_OUTSIDE);
    }

    /**
     * Lets the object notice the hook. Most objects ignore it; some fish
     * override this to react.
     */
    public void sense(HookSensor hook) {
        // nothing by default
    }

    /** Changes the object's own sideways speed; it turns to face the way it now swims. */
    protected void setSpeedX(double speedX) {
        this.speedX = speedX;
        if (speedX != 0) {
            facing = (speedX < 0) ? -1 : 1;
        }
    }

    /** The object's own speed, without the current. */
    protected double getOwnSpeedX() {
        return speedX;
    }

    /** Circle-vs-circle collision: true if the two circles overlap. */
    public boolean collidesWith(double otherX, double otherY, double otherRadius) {
        double dx = x - otherX;
        double dy = y - otherY;
        double reach = getRadius() + otherRadius;
        return dx * dx + dy * dy <= reach * reach;
    }

    /** True if this object is completely outside the visible water area. */
    public boolean isWhollyOutsideWater() {
        return x + getRadius() < 0
                || x - getRadius() > GameConstants.WORLD_WIDTH
                || y + getRadius() < GameConstants.WATER_Y
                || y - getRadius() > GameConstants.WORLD_HEIGHT;
    }

    public void moveTo(double newX, double newY) {
        x = newX;
        y = newY;
    }

    public void markCaught() {
        caught = true;
    }

    /** Lets a caught object go again; the hook cannot grab it for the given time. */
    public void release(double immunitySeconds) {
        caught = false;
        grantImmunity(immunitySeconds);
    }

    public void grantImmunity(double seconds) {
        immuneTimer = Math.max(immuneTimer, seconds);
    }

    /** True if the hook may grab this object right now. Stops double catches. */
    public boolean isCatchable() {
        return !caught && immuneTimer <= 0;
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

    /** How fast it really moves sideways: its own speed plus what the current adds. */
    @Override
    public double getSpeedX() {
        return speedX + drift;
    }

    /** Trash never changes; fish override this. */
    @Override
    public Condition getCondition() {
        return Condition.NORMAL;
    }

    @Override
    public double getPhase() {
        return phase;
    }

    /** Calm by default. Dangerous fish override this to show a warning. */
    @Override
    public double getAlert() {
        return 0;
    }

    /** At the start of a level objects fade in: 0 = invisible, 1 = fully there. */
    @Override
    public double getOpacity() {
        return Math.min(1.0, age / GameConstants.SPAWN_FADE_IN_S);
    }

    /** Points for collecting this object. Wildlife is worth nothing; trash overrides this. */
    public int getPoints() {
        return 0;
    }

    /**
     * What happens when the hook catches this object. Each subclass decides:
     * trash gives the player points and rides the hook, a fish costs health
     * and what becomes of it depends on the tool.
     */
    public abstract void onCaught(Player player, Hook hook);

    /**
     * The line has brought this object up to the boat.
     *
     * @return true if it stays aboard (trash), false if it goes back into the sea (a fish)
     */
    public abstract boolean reachBoat();

    @Override
    public String toString() {
        return getKind() + " at (" + Math.round(x) + ", " + Math.round(y) + ")";
    }
}
