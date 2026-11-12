package model;

import util.GameConstants;
import util.Mathx;

/**
 * A dangerous fish that patrols until it notices the fishing line, flashes a
 * warning, and then charges at the hook. Hooking it costs double health.
 *
 * It is a small state machine: PATROL -> TELEGRAPH -> CHARGE -> COOLDOWN and
 * back to PATROL. Nothing outside this class knows about the phases.
 */
public class Barracuda extends Fish {

    private enum Phase { PATROL, TELEGRAPH, CHARGE, COOLDOWN }

    private Phase phase = Phase.PATROL;
    private double phaseTime;      // seconds spent in the current phase
    private double targetX;        // where the hook tip was last seen
    private double targetY;
    private double chargeDirX;     // direction of the charge, as a unit vector
    private double chargeDirY;

    public Barracuda(double x, double y, double speedX) {
        super(x, y, GameConstants.RADIUS_BARRACUDA, speedX, GameConstants.DAMAGE_DANGEROUS);
    }

    @Override
    public ObjectKind getKind() {
        return ObjectKind.BARRACUDA;
    }

    @Override
    public void sense(HookSensor hook) {
        targetX = hook.getTipX();
        targetY = hook.getTipY();
        boolean lineIsNear = hook.isLineOut()
                && Math.abs(targetX - getX()) <= GameConstants.BARRACUDA_SENSE_RANGE;
        if (phase == Phase.PATROL && lineIsNear) {
            enter(Phase.TELEGRAPH);
        }
    }

    @Override
    protected void move(double dt) {
        phaseTime += dt;
        switch (phase) {
            case PATROL:
                super.move(dt);
                break;

            case TELEGRAPH:
                // Holds still and flashes, which gives the player time to react.
                if (phaseTime >= GameConstants.BARRACUDA_TELEGRAPH_S) {
                    aimAtTarget();
                    enter(Phase.CHARGE);
                }
                break;

            case CHARGE:
                double step = GameConstants.BARRACUDA_CHARGE_SPEED * dt;
                double newY = Mathx.clamp(getY() + chargeDirY * step,
                        GameConstants.WATER_Y + getRadius(), GameConstants.SEABED_Y - getRadius());
                moveTo(getX() + chargeDirX * step, newY);
                if (phaseTime >= GameConstants.BARRACUDA_CHARGE_S) {
                    enter(Phase.COOLDOWN);
                }
                break;

            case COOLDOWN:
                super.move(dt);
                if (phaseTime >= GameConstants.BARRACUDA_COOLDOWN_S) {
                    enter(Phase.PATROL);
                }
                break;
        }
    }

    /** Works out the direction from here to where the hook was last seen. */
    private void aimAtTarget() {
        double dx = targetX - getX();
        double dy = targetY - getY();
        double distance = Math.hypot(dx, dy);
        if (distance < 1) {
            chargeDirX = getFacing();   // already there: just dash forward
            chargeDirY = 0;
        } else {
            chargeDirX = dx / distance;
            chargeDirY = dy / distance;
        }
        // Keep the patrol speed, but point it the way the charge goes.
        double patrolSpeed = Math.abs(getSpeedX());
        setSpeedX(chargeDirX < 0 ? -patrolSpeed : patrolSpeed);
    }

    private void enter(Phase next) {
        phase = next;
        phaseTime = 0;
    }

    @Override
    public double getAlert() {
        return (phase == Phase.TELEGRAPH || phase == Phase.CHARGE) ? 1 : 0;
    }
}
