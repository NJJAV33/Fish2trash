package util;

/** Whether the game can make sound right now. */
public enum SoundState {
    ON,
    /** The player switched the sound off. */
    MUTED,
    /** This computer gave the game no way to play sound. */
    UNAVAILABLE
}
