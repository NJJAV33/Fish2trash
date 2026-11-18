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
public class Hook implements HookSensor {

    public enum State { SWINGING, EXTENDING, RETRACTING }

    private static final double MAX_ANGLE = Math.toRadians(GameConstants.SWING_MAX_DEG);

    private double anchorX;       // moves with the boat
    private final double anchorY;

    private State state;
    private double angle;       // 0 = straight down, negative = left, positive = right
    private double length;
    private double swingTime;   // seconds spent swinging, drives the swing motion
    private double sway;        // how far the rocking boat throws the swing off, in radians
    private double speedScale = 1.0;
    private OceanObject caughtObject;
    private final Loadout loadout;   // the tools that can go on this line

    public Hook(double anchorX, double anchorY, Loadout loadout) {
        this.anchorX = anchorX;
        this.anchorY = anchorY;
        this.loadout = loadout;
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

    /** The player pulls the hook back early. Only works while it is still going out. */
    public void recall() {
        beginRetractEmpty();
    }

    public void setAnchorX(double anchorX) {
        this.anchorX = anchorX;
    }

    /**
     * The boat is tipped by this much (radians, positive = to the right). The
     * line hangs from the boat, so the whole swing leans with it and the
     * moment the hook points straight down keeps shifting.
     */
    public void setSway(double sway) {
        this.sway = sway;
    }

    /** Multiplies extend and retract speed (for Flow streak or Slack Tide). */
    public void setSpeedScale(double speedScale) {
        this.speedScale = Math.max(0.5, speedScale);
    }

    /** Moves the hook forward by dt seconds of game time. */
    public void update(double dt) {
        switch (state) {
            case SWINGING:
                // A sine wave goes smoothly from -1 to 1 and back, which gives
                // a pendulum-like swing between -MAX_ANGLE and +MAX_ANGLE.
                // The rocking boat leans the whole swing to one side.
                swingTime += dt;
                angle = sway + MAX_ANGLE * Math.sin(swingTime * GameConstants.SWING_SPEED);
                break;

            case EXTENDING:
                // The angle is frozen, so the hook travels in a straight line.
                length += GameConstants.HOOK_EXTEND * speedScale * dt;
                if (length >= GameConstants.HOOK_MAX) {
                    length = GameConstants.HOOK_MAX;
                    state = State.RETRACTING;
                }
                break;

            case RETRACTING:
                double speed = ((caughtObject == null) ? GameConstants.HOOK_RETRACT : GameConstants.HOOK_CARRY) * speedScale;
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
    @Override
    public double getTipX() {
        return anchorX + Math.sin(angle) * length;
    }

    @Override
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

    public boolean isRetracting() {
        return state == State.RETRACTING;
    }

    @Override
    public boolean isLineOut() {
        return state != State.SWINGING;
    }

    /** True while the Magnet is the tool on the line. */
    @Override
    public boolean isMagnetic() {
        return loadout.getSelected().isMagnetic();
    }

    public double getAngle() {
        return angle;
    }

    public double getLength() {
        return length;
    }

    public OceanObject getCaughtObject() {
        return caughtObject;
    }

    public void clearCaughtObject() {
        caughtObject = null;
    }

    public Loadout getLoadout() {
        return loadout;
    }
}
