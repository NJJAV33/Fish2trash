package util;

/**
 * Something that can play the game's sound effects.
 *
 * ABSTRACTION: the rest of the game only knows this interface. Whether the
 * sounds really come out of a speaker (SynthAudioPlayer) or go nowhere
 * (SilentAudioPlayer) makes no difference to the code that calls play().
 */
public interface AudioPlayer {

    /** Plays an effect. Returns at once; it never waits for the sound to finish. */
    void play(SoundEffect effect);

    void setMuted(boolean muted);

    boolean isMuted();

    SoundState getState();
}
