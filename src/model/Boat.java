package model;

import util.GameConstants;
import util.Mathx;

/**
 * The boat floating at the top of the ocean.
 *
 * The boat owns its hook: the Hook is created inside the Boat constructor and
 * hangs from the middle of the hull (composition - a Boat "has a" Hook).
 */
public class Boat {

    private double x;         // center of the boat
    private final double y;   // waterline (bottom of the hull)
    private int facing;       // 1 = pointing right, -1 = pointing left
    private final Hook hook;

    public Boat(double x, double y, Loadout loadout) {
        this.x = x;
        this.y = y;
        this.facing = 1;
        this.hook = new Hook(x, y, loadout);
    }

    /**
     * Steers the boat: direction is -1 (left), 0 (stay) or 1 (right).
     * The boat only moves while the hook is swinging - once the line is out
     * the crew holds position - and it stays inside the screen.
     */
    public void moveBy(int direction, double dt) {
        if (direction == 0 || !hook.isSwinging()) {
            return;
        }
        facing = (direction < 0) ? -1 : 1;
        x = Mathx.clamp(x + facing * GameConstants.BOAT_SPEED * dt,
                GameConstants.BOAT_MIN_X, GameConstants.BOAT_MAX_X);
        hook.setAnchorX(x);   // the hook hangs from the boat, so it follows
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public int getFacing() {
        return facing;
    }

    public Hook getHook() {
        return hook;
    }
}
