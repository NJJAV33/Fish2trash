package model;

import util.GameConstants;

/**
 * The fishing hook hanging under the boat.
 *
 * It is always in one of three states:
 *   SWINGING   - rocks left and right on a short rope, waiting for Space
 *   EXTENDING  - shoots out in the direction it was pointing
 *   RETRACTING - reels back in, carrying a caught object if it has one
 */
public class Hook {

    public enum State { SWINGING, EXTENDING, RETRACTING }

    private static final double MAX_ANGLE = Math.toRadians(GameConstants.SWING_MAX_DEG);

    private final double anchorX;
    private final double anchorY;

    private State state;
    private double angle;       // 0 = straight down, negative = left, positive = right
    private double length;
    private double swingTime;   // seconds spent swinging, drives the swing motion
    private OceanObject caughtObject;

    public Hook(double anchorX, double anchorY) {
        this.anchorX = anchorX;
        this.anchorY = anchorY;
        this.state = State.SWINGING;
        this.length = GameConstants.HOOK_REST;
    }

    /** Called when Space is pressed. Only a swinging hook can be launched. */
    public void launch() {
        if (state == State.SWINGING) {
            state = State.EXTENDING;
        }
    }

    /** Grab an object and start reeling it in. */
    public void attach(OceanObject object) {
        caughtObject = object;
        state = State.RETRACTING;
    }

    /** Start reeling in with nothing on the hook. */
    public void beginRetractEmpty() {
        if (state == State.EXTENDING) {
            state = State.RETRACTING;
        }
    }

    /** Moves the hook forward by dt seconds of game time. */
    public void update(double dt) {
        switch (state) {
            case SWINGING:
                // A sine wave goes smoothly from -1 to 1 and back, which gives
                // a pendulum-like swing between -MAX_ANGLE and +MAX_ANGLE.
                swingTime += dt;
                angle = MAX_ANGLE * Math.sin(swingTime * GameConstants.SWING_SPEED);
                break;

            case EXTENDING:
                // The angle is frozen, so the hook travels in a straight line.
                length += GameConstants.HOOK_EXTEND * dt;
                if (length >= GameConstants.HOOK_MAX) {
                    length = GameConstants.HOOK_MAX;
                    state = State.RETRACTING;
                }
                break;

            case RETRACTING:
                double speed = (caughtObject == null) ? GameConstants.HOOK_RETRACT : GameConstants.HOOK_CARRY;
                length -= speed * dt;
                if (length <= GameConstants.HOOK_REST) {
                    length = GameConstants.HOOK_REST;
                    state = State.SWINGING;
                }
                break;
        }

        // A caught object is dragged along with the hook tip.
        if (caughtObject != null) {
            caughtObject.moveTo(getTipX(), getTipY());
        }
    }

    // The tip position comes from the angle and rope length (basic trigonometry).
    public double getTipX() {
        return anchorX + Math.sin(angle) * length;
    }

    public double getTipY() {
        return anchorY + Math.cos(angle) * length;
    }

    public double getAnchorX() {
        return anchorX;
    }

    public double getAnchorY() {
        return anchorY;
    }

    public State getState() {
        return state;
    }

    public boolean isSwinging() {
        return state == State.SWINGING;
    }

    public boolean isExtending() {
        return state == State.EXTENDING;
    }

    public OceanObject getCaughtObject() {
        return caughtObject;
    }

    public void clearCaughtObject() {
        caughtObject = null;
    }
}
