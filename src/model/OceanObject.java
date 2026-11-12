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
    private double speedX;   // pixels per second, negative = moving left
    private int facing;      // 1 = looking right, -1 = looking left
    private boolean caught;
    private double immuneTimer;   // seconds left during which the hook cannot grab it
    private double age;           // seconds since it appeared

    protected OceanObject(double x, double y, double radius, double speedX) {
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.speedX = speedX;
        this.facing = (speedX < 0) ? -1 : 1;
        this.caught = false;
    }

    /**
     * Advances the object by dt seconds.
     *
     * TEMPLATE METHOD: this method is final, so every object follows the same
     * steps - grow older, move, bounce off the screen edges. Only the "move"
     * step can be replaced by a subclass.
     */
    public final void update(double dt, double worldWidth) {
        age += dt;
        if (immuneTimer > 0) {
            immuneTimer = Math.max(0, immuneTimer - dt);
        }
        if (caught) {
            return;   // the hook is carrying it now
        }
        move(dt);
        double r = getRadius();
        if (x - r < 0) {
            x = r;
            turnAround();
        } else if (x + r > worldWidth) {
            x = worldWidth - r;
            turnAround();
        }
    }

    /** How the object moves in dt seconds. The default is a steady sideways drift. */
    protected void move(double dt) {
        x += speedX * dt;
    }

    /**
     * Lets the object notice the hook. Most objects ignore it; some fish
     * override this to react.
     */
    public void sense(HookSensor hook) {
        // nothing by default
    }

    /** Changes the sideways speed; the object turns to face the way it now swims. */
    protected void setSpeedX(double speedX) {
        this.speedX = speedX;
        if (speedX != 0) {
            facing = (speedX < 0) ? -1 : 1;
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
        double reach = getRadius() + otherRadius;
        return dx * dx + dy * dy <= reach * reach;
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

    public double getSpeedX() {
        return speedX;
    }

    /** Calm by default. Dangerous fish override this to show a warning. */
    @Override
    public double getAlert() {
        return 0;
    }

    /** New objects fade in: 0 = invisible, 1 = fully there. */
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
     * and swims away.
     */
    public abstract void onCaught(Player player, Hook hook);

    @Override
    public String toString() {
        return getKind() + " at (" + Math.round(x) + ", " + Math.round(y) + ")";
    }
}
