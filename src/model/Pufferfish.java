package model;

import util.GameConstants;
import util.Mathx;

/**
 * A dangerous fish that weaves up and down and swells when the hook comes
 * near. Swollen, it is easier to hit - and hooking it costs double health.
 *
 * POLYMORPHISM: it overrides move() and sense(), so the level treats it like
 * any other object while it behaves in its own way.
 */
public class Pufferfish extends Fish {

    private final double homeY;   // the depth it weaves around
    private double weaveTime;     // seconds, drives the up-and-down motion
    private double inflate;       // 0 = calm, 1 = fully swollen
    private boolean hookNear;

    public Pufferfish(double x, double y, double speedX) {
        super(x, y, GameConstants.RADIUS_PUFFER, speedX, GameConstants.DAMAGE_DANGEROUS);
        this.homeY = y;
    }

    @Override
    public ObjectKind getKind() {
        return ObjectKind.PUFFERFISH;
    }

    @Override
    public void sense(HookSensor hook) {
        double distance = Math.hypot(hook.getTipX() - getX(), hook.getTipY() - getY());
        hookNear = distance <= GameConstants.PUFFER_SENSE_RANGE;
    }

    @Override
    protected void move(double dt) {
        super.move(dt);   // the usual sideways drift

        // A sine wave carries it smoothly above and below its home depth.
        weaveTime += dt;
        double wave = Math.sin(2 * Math.PI * weaveTime / GameConstants.PUFFER_WEAVE_PERIOD_S);
        moveTo(getX(), homeY + GameConstants.PUFFER_WEAVE_AMPLITUDE * wave);

        // Swell while the hook is near, shrink again when it is gone.
        double change = GameConstants.PUFFER_INFLATE_PER_S * dt;
        inflate = Mathx.clamp(inflate + (hookNear ? change : -change), 0, 1);
    }

    /** A swollen pufferfish really is bigger, so collisions use the larger size. */
    @Override
    public double getRadius() {
        return super.getRadius() * (1 + GameConstants.PUFFER_SWELL * inflate);
    }

    @Override
    public double getAlert() {
        return inflate;
    }
}
