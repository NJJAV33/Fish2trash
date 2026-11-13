import java.util.ArrayList;
import java.util.List;
import util.AudioPlayer;
import util.SoundEffect;
import util.SoundState;

/**
 * A stand-in AudioPlayer for tests: it makes no sound, but writes down every
 * effect it was asked to play, so a test can check what the game "played".
 */
final class RecordingAudioPlayer implements AudioPlayer {

    private final List<SoundEffect> played = new ArrayList<>();
    private boolean muted;

    @Override
    public void play(SoundEffect effect) {
        if (!muted) {
            played.add(effect);
        }
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
        return muted ? SoundState.MUTED : SoundState.ON;
    }

    /** Everything played so far, in order. */
    List<SoundEffect> getPlayed() {
        return played;
    }
}
