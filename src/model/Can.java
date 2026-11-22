package model;

import util.GameConstants;

/**
 * An aluminium can. Worth more than a bottle - and it is metal, so the
 * Magnet pulls it: a can close to the Magnet on its way slides toward it.
 */
public class Can extends OceanTrash {

    private boolean pulled;   // the Magnet is out and close enough to feel
    private double magnetX;
    private double magnetY;

    public Can(double x, double y, double speedX) {
        super(x, y, GameConstants.RADIUS_CAN, speedX, GameConstants.POINTS_CAN);
    }

    @Override
    public ObjectKind getKind() {
        return ObjectKind.CAN;
    }

    @Override
    public void sense(HookSensor hook) {
        magnetX = hook.getTipX();
        magnetY = hook.getTipY();
        // Pull cans only while the Magnet is on its way down, not while reeling back up.
        pulled = hook.isMagnetic() && hook.isExtending() && isCatchable()
                && Math.hypot(magnetX - getX(), magnetY - getY()) <= GameConstants.MAGNET_PULL_RANGE;
    }

    @Override
    protected void move(double dt) {
        if (!pulled) {
            super.move(dt);   // the usual drift
            return;
        }
        double dx = magnetX - getX();
        double dy = magnetY - getY();
        double distance = Math.hypot(dx, dy);
        double step = Math.min(distance, GameConstants.MAGNET_PULL_SPEED * dt);
        if (distance > 0) {
            moveTo(getX() + dx / distance * step, getY() + dy / distance * step);
        }
    }
}
