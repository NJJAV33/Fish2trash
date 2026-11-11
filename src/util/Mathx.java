package util;

/**
 * Small math helpers that java.lang.Math does not have.
 */
public final class Mathx {

    private static final double BACK_OVERSHOOT = 1.70158;

    private Mathx() {
        // only static methods
    }

    /** Keeps value inside the range [min, max]. */
    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    /** Linear interpolation: t = 0 gives from, t = 1 gives to, 0.5 is halfway. */
    public static double lerp(double from, double to, double t) {
        return from + (to - from) * t;
    }

    /**
     * Easing for "pop in" animations: goes from 0 to 1 as t goes from 0 to 1,
     * but shoots a little past 1 before settling, which reads as a bounce.
     */
    public static double easeOutBack(double t) {
        double u = t - 1;
        return 1 + (BACK_OVERSHOOT + 1) * u * u * u + BACK_OVERSHOOT * u * u;
    }
}
