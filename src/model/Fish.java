package model;

import util.GameConstants;
import util.Mathx;

/**
 * A fish. Any tool that touches one hurts it, so the player loses health.
 * What becomes of the fish depends on the tool: the Hook snags it and hauls
 * it up to the boat, the Magnet knocks it out, the Net Cutter kills it.
 *
 * A fish has a mind of its own: now and then it turns around or changes
 * depth, and it swims off the screen when it likes. Turning takes a moment,
 * so its speed eases toward where it wants to go instead of jumping.
 *
 * INHERITANCE: Fish "is an" OceanObject, just like OceanTrash, but it
 * overrides onCaught() with the opposite result. Concrete fish extend it.
 *
 * TEMPLATE METHOD: move() is final here. It handles a dazed or lifeless
 * fish, and otherwise calls swim(), the step each kind of fish can replace.
 */
public abstract class Fish extends OceanObject {

    private final int damage;
    private final double cruiseSpeed;   // how fast it likes to swim, always positive
    private double goalSpeed;           // the sideways speed it is easing toward
    private double homeDepth;           // the depth it lives at
    private double depth;               // the depth it is at now, before any weaving on top
    private double goalDepth;           // the depth it is easing toward
    private Condition condition = Condition.NORMAL;
    private double dazeLeft;            // seconds until a dazed fish comes round
    private double surfaceTime;         // seconds a lifeless fish has floated at the surface

    protected Fish(double x, double y, double radius, double speedX, int damage) {
        super(x, y, radius, speedX);
        this.damage = damage;
        this.cruiseSpeed = Math.abs(speedX);
        this.goalSpeed = speedX;
        this.homeDepth = y;
        this.depth = y;
        this.goalDepth = y;
    }

    @Override
    public void onCaught(Player player, Hook hook) {
        // The tool on the line has a say twice: how much it hurts, and what it does to the fish.
        Equipment tool = hook.getLoadout().getSelected();
        player.loseHealth(tool.fishDamage(damage));
        tool.strike(this, hook);
    }

    // ---- What a tool can do to a fish ----

    /** The Hook: the fish is snagged and rides the line up to the boat. */
    public void snag(Hook hook) {
        markCaught();
        hook.attach(this);
    }

    /** The Magnet: the fish is knocked out and hangs in the water for a while. Nothing can touch it meanwhile. */
    public void daze() {
        condition = Condition.DAZED;
        dazeLeft = GameConstants.DAZE_S;
        setSpeedX(0);   // it stops swimming; where it was heading is kept in goalSpeed
    }

    /** The Net Cutter: the fish is dead. It floats up and is gone; the hook can never touch it again. */
    public void cut() {
        condition = Condition.LIFELESS;
        setSpeedX(0);
    }

    /** At the boat the crew lets a snagged fish go: it darts away and swims back down to its depth. */
    @Override
    public boolean reachBoat() {
        release(GameConstants.FISH_RELEASE_IMMUNITY_S);
        depth = getY() - getDepthOffset();   // it starts from where the hook left it
        int away = (getX() < GameConstants.WORLD_WIDTH / 2.0) ? -1 : 1;   // toward the nearer edge
        goalSpeed = away * cruiseSpeed;
        setSpeedX(goalSpeed * GameConstants.FISH_FLEE_BOOST);
        onFreed();
        return false;
    }

    /** Called when a snagged fish is let go. A fish with its own routine picks it up again here. */
    protected void onFreed() {
        // nothing by default
    }

    // ---- Swimming ----

    @Override
    protected final void move(double dt) {
        if (condition == Condition.LIFELESS) {
            floatUp(dt);
        } else if (condition == Condition.DAZED) {
            dazeLeft -= dt;
            if (dazeLeft <= 0) {
                condition = Condition.NORMAL;
                turnAround();   // it comes round and swims off the other way
            }
        } else {
            swim(dt);
        }
    }

    /**
     * How a healthy fish swims in dt seconds: it eases toward the speed and
     * the depth it wants. Fish with a routine of their own override this.
     */
    protected void swim(double dt) {
        double speed = Mathx.approach(getOwnSpeedX(), goalSpeed, GameConstants.FISH_TURN_ACCEL * dt);
        setSpeedX(speed);
        depth = Mathx.approach(depth, goalDepth, GameConstants.FISH_DEPTH_SPEED * dt);
        moveTo(getX() + speed * dt, depth + getDepthOffset());
    }

    /** Extra up-and-down on top of the fish's depth. None by default; a pufferfish weaves. */
    protected double getDepthOffset() {
        return 0;
    }

    private void floatUp(double dt) {
        double top = GameConstants.WATER_Y + getRadius();
        if (getY() > top) {
            moveTo(getX(), Math.max(top, getY() - GameConstants.LIFELESS_RISE_SPEED * dt));
        } else {
            surfaceTime += dt;
        }
    }

    /** A fish swims against the current, so it is carried less than trash. */
    @Override
    protected double getCurrentShare() {
        return GameConstants.FISH_CURRENT_SHARE;
    }

    // ---- A mind of its own ----

    /** Only a healthy, free fish well inside the screen changes its mind. */
    @Override
    public final void changeCourse(double roll) {
        boolean wellInside = getX() > GameConstants.FISH_EDGE_SHY
                && getX() < GameConstants.WORLD_WIDTH - GameConstants.FISH_EDGE_SHY;
        if (condition == Condition.NORMAL && !isCaught() && wellInside) {
            wander(roll);
        }
    }

    /**
     * What the fish does when it changes its mind: half of the time it turns
     * around, otherwise it picks a new depth near its home depth.
     *
     * @param roll a random number from 0 to 1
     */
    protected void wander(double roll) {
        if (roll < 0.5) {
            turnAround();
        } else {
            double offset = (roll * 4 - 3) * GameConstants.FISH_DEPTH_ROAM;   // from -roam to +roam
            goalDepth = Mathx.clamp(homeDepth + offset,
                    GameConstants.WATER_Y + getRadius() + 20, GameConstants.SEABED_Y - getRadius() - 10);
        }
    }

    /** Starts a turn: the fish slows, then swims the other way at its usual speed. */
    protected void turnAround() {
        goalSpeed = (goalSpeed < 0) ? cruiseSpeed : -cruiseSpeed;
    }

    /** Swims at its usual speed in the given direction (1 = right, -1 = left), at once. */
    protected void headTo(int direction) {
        goalSpeed = direction * cruiseSpeed;
        setSpeedX(goalSpeed);
    }

    /** Makes the given depth the one the fish lives at and is at. */
    protected void settleAt(double y) {
        homeDepth = y;
        depth = y;
        goalDepth = y;
    }

    // ---- What others may know ----

    /** A dazed or lifeless fish cannot be touched: the tool goes past it. */
    @Override
    public boolean isCatchable() {
        return condition == Condition.NORMAL && super.isCatchable();
    }

    /** A lifeless fish is out of the game once it has faded away at the surface. */
    @Override
    public boolean isGone() {
        return super.isGone() || surfaceTime >= GameConstants.LIFELESS_FADE_S;
    }

    /** A lifeless fish fades away while it floats at the surface. */
    @Override
    public double getOpacity() {
        return super.getOpacity() * Mathx.clamp(1 - surfaceTime / GameConstants.LIFELESS_FADE_S, 0, 1);
    }

    @Override
    public Condition getCondition() {
        return condition;
    }

    public int getDamage() {
        return damage;
    }
}
