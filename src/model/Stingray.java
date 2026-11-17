package model;

import util.GameConstants;
import util.Mathx;

/**
 * A ray that rests on the sand. Rays can feel magnets: while the Magnet is
 * the tool on the line and the boat is near, it throws sparks as a warning,
 * then leaves the sand and swims up at the Magnet - and stays there. Taking
 * the Magnet off the line (or steering away) sends it back down. Touching it
 * costs double health. With any other tool on the line it never leaves the
 * sand.
 *
 * Like the barracuda it is a small state machine:
 * REST -> WARNING -> CHASE -> SINK and back to REST.
 * A sinking ray ignores the Magnet until it is back on the sand, which gives
 * the player a moment to use it.
 */
public class Stingray extends Fish {

    private enum Phase { REST, WARNING, CHASE, SINK }

    private final double homeY;    // its depth on the sand
    private Phase phase = Phase.REST;
    private double phaseTime;      // seconds spent in the current phase
    private double magnetX;        // where the Magnet was last felt
    private double magnetY;
    private boolean magnetFelt;    // the Magnet is on the line and close enough to notice
    private boolean magnetInReach; // the Magnet is on the line and not yet too far to follow

    public Stingray(double x, double y, double speedX) {
        super(x, y, GameConstants.RADIUS_STINGRAY, speedX, GameConstants.DAMAGE_DANGEROUS);
        this.homeY = y;
    }

    @Override
    public ObjectKind getKind() {
        return ObjectKind.STINGRAY;
    }

    @Override
    public void sense(HookSensor hook) {
        magnetX = hook.getTipX();
        magnetY = hook.getTipY();
        // Only the sideways distance counts: it feels a Magnet that is right above it, however high.
        double sideways = Math.abs(magnetX - getX());
        magnetFelt = hook.isMagnetic() && sideways <= GameConstants.STINGRAY_SENSE_RANGE;
        magnetInReach = hook.isMagnetic() && sideways <= GameConstants.STINGRAY_LOSE_RANGE;
    }

    @Override
    protected void swim(double dt) {
        phaseTime += dt;
        switch (phase) {
            case REST:
                super.swim(dt);   // a slow glide over the sand
                if (magnetFelt) {
                    enter(Phase.WARNING);
                }
                break;

            case WARNING:
                // Holds still and sparks, which gives the player time to change tools.
                if (!magnetInReach) {
                    enter(Phase.REST);
                } else if (phaseTime >= GameConstants.STINGRAY_WARNING_S) {
                    enter(Phase.CHASE);
                }
                break;

            case CHASE:
                swimToward(magnetX, magnetY, GameConstants.STINGRAY_CHASE_SPEED * dt);
                if (!magnetInReach) {
                    enter(Phase.SINK);
                }
                break;

            case SINK:
                swimToward(getX(), homeY, GameConstants.STINGRAY_SINK_SPEED * dt);
                if (getY() >= homeY) {
                    settleAt(homeY);
                    enter(Phase.REST);
                }
                break;
        }
    }

    /** Moves up to the given distance in a straight line toward a point, never above the water. */
    private void swimToward(double targetX, double targetY, double step) {
        double dx = targetX - getX();
        double dy = targetY - getY();
        double distance = Math.hypot(dx, dy);
        if (distance <= step) {
            moveTo(targetX, Math.min(targetY, homeY));
            return;
        }
        double newY = Mathx.clamp(getY() + dy / distance * step,
                GameConstants.WATER_Y + getRadius(), homeY);
        moveTo(getX() + dx / distance * step, newY);
    }

    /** It only changes its mind on the sand, and then it just turns around. */
    @Override
    protected void wander(double roll) {
        if (phase == Phase.REST) {
            turnAround();
        }
    }

    /** Let go at the boat, it sinks back to the sand. */
    @Override
    protected void onFreed() {
        enter(Phase.SINK);
    }

    private void enter(Phase next) {
        phase = next;
        phaseTime = 0;
    }

    /** Sparks while it warns and while it chases. */
    @Override
    public double getAlert() {
        return (phase == Phase.WARNING || phase == Phase.CHASE) ? 1 : 0;
    }
}
