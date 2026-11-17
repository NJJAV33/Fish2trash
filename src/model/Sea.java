package model;

import util.GameConstants;
import util.Mathx;

/**
 * The state of the sea during one level: a swell that rocks the boat, and a
 * current that carries along everything in the water.
 *
 * Both follow the clock, so the same level with the same start always gives
 * the same sea. How strong they are comes from the level's data.
 */
public final class Sea {

    /** A sea with no swell and no current, for places that need none. */
    public static final Sea CALM = new Sea(0, 0, 0);

    private static final double FULL_TURN = 2 * Math.PI;
    private static final double DEPTH = GameConstants.SEABED_Y - GameConstants.WATER_Y;

    private final double swell;      // the widest the boat tips, in radians
    private final double strength;   // the fastest the current flows, in pixels per second
    private final double start;      // where in its rhythm this sea begins
    private double time;             // seconds since the level began

    /**
     * @param swellDegrees how far the swell tips the boat at most
     * @param currentSpeed how fast the current flows at most, in pixels per second
     * @param start        where in its rhythm the sea begins, as an angle in radians
     */
    public Sea(double swellDegrees, double currentSpeed, double start) {
        this.swell = Math.toRadians(swellDegrees);
        this.strength = currentSpeed;
        this.start = start;
    }

    public void update(double dt) {
        time += dt;
    }

    /**
     * How far the boat is tipped right now, in radians: positive leans it to
     * the right. A slow roll with a faster, smaller wave on top, so no two
     * rolls are quite the same.
     */
    public double getTilt() {
        double roll = Math.sin(FULL_TURN * time / GameConstants.SWELL_PERIOD_S + start);
        double chop = Math.sin(FULL_TURN * time / (GameConstants.SWELL_PERIOD_S * 0.37) + 2.3 * start);
        return swell * (roll + GameConstants.SWELL_CHOP * chop) / (1 + GameConstants.SWELL_CHOP);
    }

    /** The current at the surface right now, in pixels per second. Positive flows to the right. */
    public double getCurrent() {
        return strength * Math.sin(FULL_TURN * time / GameConstants.CURRENT_PERIOD_S + start);
    }

    /** The current at a depth: strongest at the surface, nearly gone at the sea floor. */
    public double getCurrentAt(double y) {
        double share = Mathx.clamp((GameConstants.SEABED_Y - y) / DEPTH, GameConstants.CURRENT_FLOOR_SHARE, 1);
        return getCurrent() * share;
    }

    /** The widest the boat tips in this sea, in radians. The drawing uses it for the height of the waves. */
    public double getSwell() {
        return swell;
    }

    /** The fastest the current flows in this sea, in pixels per second. */
    public double getStrength() {
        return strength;
    }

    @Override
    public String toString() {
        return String.format("swell %.0f deg, current %.0f px/s", Math.toDegrees(swell), strength);
    }
}
