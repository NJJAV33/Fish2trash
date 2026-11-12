package util;

import java.util.EnumMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

/**
 * Plays the sound effects through the computer's speakers.
 *
 * Sound must never slow the game down, so all the audio work happens on its
 * own thread. play() only drops a request into a queue and returns; the audio
 * thread picks requests up, mixes the sounds that are playing into one stream
 * and feeds it to the sound card a small piece at a time.
 *
 * If the computer has no usable sound output, the player reports
 * UNAVAILABLE and play() quietly does nothing. Sound can never crash the game.
 */
public final class SynthAudioPlayer implements AudioPlayer {

    private static final double[] PITCHES = {0.96, 1.0, 1.04};   // the variations of a "varied" effect
    private static final int MAX_VOICES = 12;          // sounds that can play at the same moment
    private static final int CHUNK_FRAMES = 256;       // samples mixed per step, about 12 ms
    private static final int LINE_BUFFER_BYTES = 2048; // small, so a sound starts quickly
    private static final int QUEUE_SIZE = 32;
    private static final long REPEAT_GAP_MS = 40;      // the same effect is not restarted faster than this
    private static final int[] SAMPLE_RATES = {ToneSynth.SAMPLE_RATE, ToneSynth.SAMPLE_RATE * 2};

    // Requests travel from the game's thread to the audio thread through this queue.
    private final BlockingQueue<short[]> requests = new ArrayBlockingQueue<>(QUEUE_SIZE);
    private final Map<SoundEffect, Long> lastPlayedAt = new EnumMap<>(SoundEffect.class);
    private final Random random = new Random();

    // "volatile": written by one thread and read by another, so each must always see the newest value.
    private volatile Map<SoundEffect, short[][]> sounds;   // null until the effects are rendered
    private volatile boolean muted;
    private volatile boolean unavailable;
    private int sampleRepeat = 1;   // used only by the audio thread

    /** Starts the audio thread. Rendering the effects and opening the sound card happen there. */
    public SynthAudioPlayer() {
        this(false);
    }

    /** @param withoutDevice true to skip the sound card entirely (used by tests) */
    public SynthAudioPlayer(boolean withoutDevice) {
        if (withoutDevice) {
            unavailable = true;
            return;
        }
        Thread thread = new Thread(this::runAudioThread, "audio");
        thread.setDaemon(true);   // a daemon thread does not keep the program alive when the window closes
        thread.start();
    }

    @Override
    public void play(SoundEffect effect) {
        Map<SoundEffect, short[][]> ready = sounds;
        if (muted || unavailable || ready == null) {
            return;
        }
        long now = System.currentTimeMillis();
        Long last = lastPlayedAt.get(effect);
        if (last != null && now - last < REPEAT_GAP_MS) {
            return;   // the same sound twice in an instant would only be louder, not clearer
        }
        lastPlayedAt.put(effect, now);

        short[][] variations = ready.get(effect);
        short[] samples = variations[random.nextInt(variations.length)];
        requests.offer(samples);   // offer never waits: if the queue is full, this sound is skipped
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
        if (unavailable) {
            return SoundState.UNAVAILABLE;
        }
        return muted ? SoundState.MUTED : SoundState.ON;
    }

    // ---- Everything below runs on the audio thread ----

    private void runAudioThread() {
        try {
            Map<SoundEffect, short[][]> rendered = renderAll();
            SourceDataLine line = openLine();
            sounds = rendered;          // from now on play() accepts requests
            mixForever(line);
        } catch (Exception | LinkageError problem) {
            // No sound card, no permission, a missing audio library...
            // whatever it is, the game simply goes on without sound.
            unavailable = true;
            System.err.println("Sound is off: " + problem);
        }
    }

    /** Turns every effect recipe into samples, once, before any sound is needed. */
    private static Map<SoundEffect, short[][]> renderAll() {
        Map<SoundEffect, short[][]> rendered = new EnumMap<>(SoundEffect.class);
        for (SoundEffect effect : SoundEffect.values()) {
            short[][] variations;
            if (effect.isVaried()) {
                variations = new short[PITCHES.length][];
                for (int i = 0; i < PITCHES.length; i++) {
                    variations[i] = effect.render(PITCHES[i]);
                }
            } else {
                variations = new short[][] {effect.render(1.0)};
            }
            rendered.put(effect, variations);
        }
        return rendered;
    }

    /**
     * Opens the sound output. Some sound cards refuse 22,050 samples a
     * second; then 44,100 is tried, and every sample is simply sent twice
     * (sampleRepeat remembers which).
     */
    private SourceDataLine openLine() throws LineUnavailableException {
        LineUnavailableException lastProblem = null;
        for (int i = 0; i < SAMPLE_RATES.length; i++) {
            // 16-bit, one channel (mono), signed, low byte first
            AudioFormat format = new AudioFormat(SAMPLE_RATES[i], 16, 1, true, false);
            try {
                SourceDataLine line = AudioSystem.getSourceDataLine(format);
                line.open(format, LINE_BUFFER_BYTES * (i + 1));
                line.start();
                sampleRepeat = i + 1;
                return line;
            } catch (LineUnavailableException problem) {
                lastProblem = problem;
            }
        }
        throw lastProblem;
    }

    /** The mixing loop: runs for as long as the game does. */
    private void mixForever(SourceDataLine line) {
        int repeat = sampleRepeat;
        short[][] voice = new short[MAX_VOICES][];   // the sounds playing now (null = free slot)
        int[] position = new int[MAX_VOICES];        // how far each one has played
        byte[] bytes = new byte[CHUNK_FRAMES * 2 * repeat];

        while (true) {
            // Start every sound that was requested since the last step.
            short[] requested = requests.poll();
            while (requested != null) {
                int slot = slotFor(voice, position);
                voice[slot] = requested;
                position[slot] = 0;
                requested = requests.poll();
            }

            // Mix: at each moment, add up the samples of all playing sounds.
            int out = 0;
            for (int frame = 0; frame < CHUNK_FRAMES; frame++) {
                int sum = 0;
                for (int v = 0; v < MAX_VOICES; v++) {
                    if (voice[v] != null) {
                        sum += voice[v][position[v]];
                        position[v]++;
                        if (position[v] >= voice[v].length) {
                            voice[v] = null;   // finished: the slot is free again
                        }
                    }
                }
                if (muted) {
                    sum = 0;
                }
                // Two loud sounds can add up past the limit; cut the sum off instead of letting it wrap.
                sum = Math.max(Short.MIN_VALUE, Math.min(Short.MAX_VALUE, sum));
                for (int r = 0; r < repeat; r++) {
                    bytes[out++] = (byte) sum;           // low byte
                    bytes[out++] = (byte) (sum >> 8);    // high byte
                }
            }

            // write() waits while the sound card's buffer is full. That wait
            // is what paces this loop: it never runs faster than real time.
            line.write(bytes, 0, bytes.length);
        }
    }

    /** A free slot, or, if every slot is busy, the sound that is closest to finishing. */
    private static int slotFor(short[][] voice, int[] position) {
        int nearlyDone = 0;
        int leastLeft = Integer.MAX_VALUE;
        for (int v = 0; v < MAX_VOICES; v++) {
            if (voice[v] == null) {
                return v;
            }
            int left = voice[v].length - position[v];
            if (left < leastLeft) {
                leastLeft = left;
                nearlyDone = v;
            }
        }
        return nearlyDone;
    }
}
