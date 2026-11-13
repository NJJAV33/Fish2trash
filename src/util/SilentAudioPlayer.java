package util;

/**
 * An AudioPlayer that plays nothing. The tests use it, so running them makes
 * no noise, and it stands in whenever real sound is not wanted.
 *
 * This is the "null object" idea: instead of checking "is there a player?"
 * everywhere, the game always has a player, and this one simply does nothing.
 */
public final class SilentAudioPlayer implements AudioPlayer {

    private boolean muted;

    @Override
    public void play(SoundEffect effect) {
        // nothing to do
    }

    @Override
    public void setMuted(boolean muted) {
        this.muted = muted;
    }

    @Override
    public boolean isMuted() {
        return muted;
    }

    @Override
    public SoundState getState() {
        return SoundState.UNAVAILABLE;
    }
}
