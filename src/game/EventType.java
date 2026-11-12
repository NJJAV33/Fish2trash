package game;

/** The things that can happen in a game that other parts may want to react to. */
public enum EventType {
    /** The player dropped the hook. */
    HOOK_LAUNCHED,
    /** The player pulled the hook back before it touched anything. */
    HOOK_RECALLED,
    /** The hook grabbed trash. The event's value is the points earned. */
    OBJECT_CAUGHT,
    /** The hook hit wildlife. The event's value is the health lost. */
    HEALTH_LOST,
    /** Wrong tool: the object slipped off the hook. */
    SLIPPED,
    /** The hook went all the way out and came back with nothing. */
    MISSED,
    /** A caught object reached the boat. */
    DELIVERED,
    /** The score reached the chapter's quota. The clock stops here. */
    QUOTA_REACHED,
    /** The chapter was won: the catch that met the quota reached the boat. */
    CHAPTER_CLEARED,
    /** The chapter was lost. GameManager.getStatus() says why. */
    CHAPTER_FAILED
}
