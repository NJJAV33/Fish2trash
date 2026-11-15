package game;

/** How an attempt at a level stands: still going, won, or lost (and why). */
public enum LevelStatus {
    RUNNING(FailCause.NONE),
    /** The quota was met and the last catch reached the boat. */
    CLEARED(FailCause.NONE),
    /** Time ran out before the quota was met. */
    FAILED_TIME(FailCause.TIME),
    /** Too many fish were hooked. */
    FAILED_HEALTH(FailCause.HEALTH);

    private final FailCause failCause;

    LevelStatus(FailCause failCause) {
        this.failCause = failCause;
    }

    public FailCause getFailCause() {
        return failCause;
    }

    public boolean isOver() {
        return this != RUNNING;
    }
}
