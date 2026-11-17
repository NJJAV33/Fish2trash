package model;

/** The shape an ocean object is in. Trash is always NORMAL; a fish changes when a tool touches it. */
public enum Condition {
    /** Swimming or drifting as usual. */
    NORMAL,
    /** Knocked out by the Magnet: it hangs in the water until it comes round. */
    DAZED,
    /** Cut by the Net Cutter: it floats up to the surface and is gone. */
    LIFELESS
}
