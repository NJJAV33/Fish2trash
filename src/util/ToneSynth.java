package util;

/**
 * Makes sounds out of numbers, so the game needs no audio files.
 *
 * A sound is a list of samples: how far the speaker is pushed, 22,050 times a
 * second. A steady tone is a wave repeating at some frequency (440 times a
 * second is the note A). A sound effect here is built from one or more Tones
 * that are added together.
 */
public final class ToneSynth {

    /** Samples per second. Half of CD quality: plenty for short effects. */
    public static final int SAMPLE_RATE = 22050;

    // Every finished sound is scaled so its loudest point sits here (1.0 = as loud as possible).
    private static final double PEAK = 0.55;

    private ToneSynth() {
        // only static methods
    }

    /** The shape of one wave. Each shape sounds different at the same pitch. */
    public enum Wave {
        /** Smooth and pure, like a whistle. */
        SINE {
            @Override
            double at(double phase, double noise) {
                return Math.sin(2 * Math.PI * phase);
            }
        },
        /** Soft but a little brighter than a sine. */
        TRIANGLE {
            @Override
            double at(double phase, double noise) {
                return 4 * Math.abs(phase - 0.5) - 1;
            }
        },
        /** Harsh and buzzy, like an old video game. */
        SQUARE {
            @Override
            double at(double phase, double noise) {
                return phase < 0.5 ? 1 : -1;
            }
        },
        /** No pitch at all: a hiss. */
        NOISE {
            @Override
            double at(double phase, double noise) {
                return noise;
            }
        };

        /**
         * The height of the wave, from -1 to 1.
         *
         * @param phase how far through one repeat the wave is, from 0 to 1
         * @param noise a random value from -1 to 1, used only by NOISE
         */
        abstract double at(double phase, double noise);
    }

    /**
     * Adds the tones together into one sound.
     *
     * @param lengthMs how long the whole sound is, in milliseconds
     * @param pitch    1.0 plays the tones as written; higher or lower shifts them all
     */
    public static short[] render(int lengthMs, double pitch, Tone... tones) {
        double[] mix = new double[lengthMs * SAMPLE_RATE / 1000];
        for (Tone tone : tones) {
            tone.addTo(mix, pitch);
        }

        double loudest = 0;
        for (double value : mix) {
            loudest = Math.max(loudest, Math.abs(value));
        }
        short[] samples = new short[mix.length];
        if (loudest > 0) {
            // Scale to the peak level and convert to 16-bit numbers, which is what sound cards take.
            double scale = PEAK * Short.MAX_VALUE / loudest;
            for (int i = 0; i < mix.length; i++) {
                samples[i] = (short) Math.round(mix[i] * scale);
            }
        }
        return samples;
    }

    /**
     * One ingredient of a sound: a wave that may slide in pitch, with a
     * volume shape (how fast it starts, how it dies away).
     *
     * Built step by step, for example:
     *   Tone.of(Wave.SINE, 330, 660).gain(0.3).shape(4, 40, 0.4, 50)
     */
    public static final class Tone {

        private final Wave wave;
        private final double startHz;
        private final double endHz;
        private boolean glideByRatio;      // true: the pitch slides evenly "by ear"
        private int fromMs;                // when this tone starts inside the sound
        private int lastsMs;               // 0 = until the end of the sound
        private double gain = 1.0;         // loudness next to the other tones
        private double attackMs = 2;       // fade-in time
        private double decayMs = 0;        // time to fall from full to the hold level
        private double holdLevel = 1.0;    // loudness held after the decay, 0 to 1
        private double releaseMs = 10;     // fade-out time at the end
        private double overtoneRatio;      // a second, higher wave mixed in...
        private double overtoneGain;       // ...at this loudness (0 = none)
        private double hissGain;           // a little noise mixed in (0 = none)

        private Tone(Wave wave, double startHz, double endHz) {
            this.wave = wave;
            this.startHz = startHz;
            this.endHz = endHz;
        }

        /** A tone that slides from one frequency to another (use the same value twice for a steady note). */
        public static Tone of(Wave wave, double startHz, double endHz) {
            return new Tone(wave, startHz, endHz);
        }

        /** A steady note. */
        public static Tone of(Wave wave, double hz) {
            return new Tone(wave, hz, hz);
        }

        /** A hiss with no pitch. */
        public static Tone noise() {
            return new Tone(Wave.NOISE, 1, 1);
        }

        /** Slide in equal musical steps instead of equal Hz steps. Sounds smoother for big slides. */
        public Tone glideByRatio() {
            this.glideByRatio = true;
            return this;
        }

        /** Start this tone some time into the sound. */
        public Tone from(int ms) {
            this.fromMs = ms;
            return this;
        }

        public Tone lasting(int ms) {
            this.lastsMs = ms;
            return this;
        }

        public Tone gain(double newGain) {
            this.gain = newGain;
            return this;
        }

        /**
         * The volume shape: fade in over attackMs, fall to holdLevel over
         * decayMs, stay there, and fade out over the last releaseMs.
         */
        public Tone shape(double newAttackMs, double newDecayMs, double newHoldLevel, double newReleaseMs) {
            this.attackMs = newAttackMs;
            this.decayMs = newDecayMs;
            this.holdLevel = newHoldLevel;
            this.releaseMs = newReleaseMs;
            return this;
        }

        /** Mix in a second sine wave at ratio times the frequency. Gives a metallic or bell-like colour. */
        public Tone overtone(double ratio, double overtoneLoudness) {
            this.overtoneRatio = ratio;
            this.overtoneGain = overtoneLoudness;
            return this;
        }

        /** Mix in a little hiss. */
        public Tone hiss(double hissLoudness) {
            this.hissGain = hissLoudness;
            return this;
        }

        /** Adds this tone's samples onto the mix. */
        private void addTo(double[] mix, double pitch) {
            int first = fromMs * SAMPLE_RATE / 1000;
            int count = (lastsMs > 0) ? lastsMs * SAMPLE_RATE / 1000 : mix.length - first;
            count = Math.min(count, mix.length - first);
            double lengthMs = count * 1000.0 / SAMPLE_RATE;

            double phase = 0;           // position inside one repeat of the wave, 0 to 1
            double overtonePhase = 0;
            int random = 0x2545F491;    // fixed start, so the same sound is made every time

            for (int i = 0; i < count; i++) {
                double progress = (count > 1) ? (double) i / (count - 1) : 0;
                double hz = glideByRatio
                        ? startHz * Math.pow(endHz / startHz, progress)
                        : startHz + (endHz - startHz) * progress;
                hz *= pitch;

                // Step the wave forward by one sample. Keeping only the
                // fraction makes it repeat, with no click when the pitch slides.
                phase += hz / SAMPLE_RATE;
                phase -= Math.floor(phase);

                // A tiny random-number generator (xorshift) for the hiss.
                random ^= random << 13;
                random ^= random >>> 17;
                random ^= random << 5;
                double noise = random / (double) Integer.MAX_VALUE;

                double value = wave.at(phase, noise);
                if (overtoneGain > 0) {
                    overtonePhase += hz * overtoneRatio / SAMPLE_RATE;
                    overtonePhase -= Math.floor(overtonePhase);
                    value += overtoneGain * Math.sin(2 * Math.PI * overtonePhase);
                }
                value += hissGain * noise;

                double ms = i * 1000.0 / SAMPLE_RATE;
                mix[first + i] += value * gain * volumeAt(ms, lengthMs);
            }
        }

        /** How loud the tone is at a moment, from 0 to 1, following its volume shape. */
        private double volumeAt(double ms, double lengthMs) {
            double level;
            if (ms < attackMs) {
                level = ms / attackMs;
            } else if (ms < attackMs + decayMs) {
                level = 1 - (1 - holdLevel) * (ms - attackMs) / decayMs;
            } else {
                level = holdLevel;
            }
            double untilEnd = lengthMs - ms;
            if (releaseMs > 0 && untilEnd < releaseMs) {
                level *= untilEnd / releaseMs;   // fade to silence, so the sound never ends with a click
            }
            return level;
        }
    }
}
