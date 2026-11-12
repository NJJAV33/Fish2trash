package model;

/**
 * The countdown for one chapter.
 *
 * It only counts. It can be frozen, which the game does once the quota is
 * met, so a catch that is still being reeled in can never be lost to the clock.
 */
public class TimeLimit {

    private final double total;
    private double remaining;
    private boolean frozen;

    public TimeLimit(double totalSeconds) {
        this.total = totalSeconds;
        this.remaining = totalSeconds;
        this.frozen = false;
    }

    /** Counts down by dt seconds, unless the clock is frozen. Never goes below zero. */
    public void tick(double dt) {
        if (!frozen) {
            remaining = Math.max(0, remaining - dt);
        }
    }

    /** Stops the clock for good. */
    public void freeze() {
        frozen = true;
    }

    public boolean isFrozen() {
        return frozen;
    }

    public boolean isTimeUp() {
        return remaining <= 0;
    }

    public double getRemaining() {
        return remaining;
    }

    public double getTotal() {
        return total;
    }

    /** 1.0 = all of the time is left, 0.0 = none. */
    public double getFractionLeft() {
        return remaining / total;
    }
}
