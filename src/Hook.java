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

    public static final double TIP_RADIUS = 8;     // size of the hook for collisions

    private static final double REST_LENGTH = 40;  // rope length while swinging
    private static final double MAX_LENGTH = 440;  // furthest the hook can reach
    private static final double MAX_ANGLE = Math.toRadians(60);
    private static final double SWING_SPEED = 1.8;     // how fast it rocks
    private static final double EXTEND_SPEED = 380;    // pixels per second
    private static final double RETRACT_SPEED = 420;   // pixels per second, empty
    private static final double CARRY_SPEED = 260;     // pixels per second, loaded

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
        this.length = REST_LENGTH;
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

    /** Moves the hook forward by dt seconds of game time. */
    public void update(double dt) {
        switch (state) {
            case SWINGING:
                // A sine wave goes smoothly from -1 to 1 and back, which gives
                // a pendulum-like swing between -MAX_ANGLE and +MAX_ANGLE.
                swingTime += dt;
                angle = MAX_ANGLE * Math.sin(swingTime * SWING_SPEED);
                break;

            case EXTENDING:
                // The angle is frozen, so the hook travels in a straight line.
                length += EXTEND_SPEED * dt;
                if (length >= MAX_LENGTH) {
                    length = MAX_LENGTH;
                    state = State.RETRACTING;
                }
                break;

            case RETRACTING:
                double speed = (caughtObject == null) ? RETRACT_SPEED : CARRY_SPEED;
                length -= speed * dt;
                if (length <= REST_LENGTH) {
                    length = REST_LENGTH;
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
