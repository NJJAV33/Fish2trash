package game;

/** How a chapter attempt stands: still going, won, or lost (and why). */
public enum ChapterStatus {
    RUNNING(FailCause.NONE),
    /** The quota was met and the last catch reached the boat. */
    CLEARED(FailCause.NONE),
    /** Time ran out before the quota was met. */
    FAILED_TIME(FailCause.TIME),
    /** Too many fish were hooked. */
    FAILED_HEALTH(FailCause.HEALTH);

    private final FailCause failCause;

    ChapterStatus(FailCause failCause) {
        this.failCause = failCause;
    }

    public FailCause getFailCause() {
        return failCause;
    }

    public boolean isOver() {
        return this != RUNNING;
    }
}
