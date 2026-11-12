package game;

/** How a chapter attempt stands: still going, won, or lost (and why). */
public enum ChapterStatus {
    RUNNING,
    /** The quota was met and the last catch reached the boat. */
    CLEARED,
    /** Time ran out before the quota was met. */
    FAILED_TIME,
    /** Too many fish were hooked. */
    FAILED_HEALTH
}
