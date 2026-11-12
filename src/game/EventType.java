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
    /** Another tool was put on the line. The event's text is the tool's name. */
    TOOL_SWITCHED,
    /** A tool change was refused: line out, too soon, or (with a text saying so) not unlocked yet. */
    SWAP_DENIED,
    /** The hook went all the way out and came back with nothing. */
    MISSED,
    /** A caught object reached the boat. */
    DELIVERED,
    /** One more second is gone in the last ten. The event's value is the seconds left. */
    LOW_TIME_TICK,
    /** A tip should be shown. The event's text is the tip. */
    TUTORIAL_PROMPT,
    /** The score reached the chapter's quota. The clock stops here. */
    QUOTA_REACHED,
    /** The chapter was won: the catch that met the quota reached the boat. */
    CHAPTER_CLEARED,
    /** The chapter was lost. GameManager.getStatus() says why. */
    CHAPTER_FAILED
}
