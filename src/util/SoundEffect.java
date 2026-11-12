package util;

import util.ToneSynth.Tone;
import util.ToneSynth.Wave;

/**
 * Every sound in the game, described as a recipe of tones. Nothing is loaded
 * from a file: ToneSynth turns each recipe into samples when the game starts.
 *
 * Musical note frequencies used below: C4 262, E4 330, G4 392, A4 440,
 * C5 523, E5 659, G5 784, C6 1047.
 */
public enum SoundEffect {

    /** The hook drops: a quick rising whoosh. */
    HOOK_LAUNCH(110, 3, true,
            Tone.of(Wave.SINE, 330, 660).glideByRatio().gain(0.30).shape(4, 40, 0.4, 50).hiss(0.08)),

    /** A bottle is caught: a light upward blip. */
    CATCH_PLASTIC(100, 6, true,
            Tone.of(Wave.SINE, 520, 1040).glideByRatio().gain(0.36).shape(2, 60, 0.2, 30).overtone(2, 0.25)),

    /** A can is caught: a metallic "tink" over a low hum. */
    CATCH_CAN(350, 6, true,
            Tone.of(Wave.SINE, 1480).lasting(220).gain(0.30).shape(1, 30, 0.15, 150).overtone(2.76, 0.5),
            Tone.of(Wave.TRIANGLE, 98, 110).from(30).lasting(320).gain(0.22).shape(5, 60, 0.5, 120)),

    /** A net is caught: two quick snips. */
    CATCH_NET(110, 6, true,
            Tone.noise().lasting(50).gain(0.30).shape(1, 30, 0.1, 9),
            Tone.of(Wave.SQUARE, 2400, 1400).lasting(50).gain(0.10).shape(1, 30, 0.1, 9),
            Tone.noise().from(60).lasting(50).gain(0.30).shape(1, 30, 0.1, 9),
            Tone.of(Wave.SQUARE, 2400, 1400).from(60).lasting(50).gain(0.10).shape(1, 30, 0.1, 9)),

    /** A fish is hooked: a falling "oh no". */
    FISH_HIT(180, 7, true,
            Tone.of(Wave.TRIANGLE, 420, 190).glideByRatio().gain(0.36).shape(3, 80, 0.35, 70)),

    /** Wrong tool, the trash slipped off: a dull clunk. */
    WRONG_TOOL(100, 5, true,
            Tone.of(Wave.TRIANGLE, 300, 240).gain(0.28).shape(1, 35, 0.1, 55).overtone(2.4, 0.3)),

    /** The quota is reached: two bright notes going up. */
    QUOTA(330, 9, false,
            Tone.of(Wave.SINE, 784).lasting(150).gain(0.32).shape(3, 60, 0.4, 60),
            Tone.of(Wave.SINE, 1047).from(110).lasting(220).gain(0.32).shape(3, 60, 0.4, 100)),

    /** Chapter cleared: four rising notes, then a chord. */
    CHAPTER_CLEAR(1000, 10, false,
            Tone.of(Wave.TRIANGLE, 392).lasting(120).gain(0.27).shape(4, 50, 0.6, 40),
            Tone.of(Wave.TRIANGLE, 523).from(90).lasting(120).gain(0.27).shape(4, 50, 0.6, 40),
            Tone.of(Wave.TRIANGLE, 659).from(180).lasting(120).gain(0.27).shape(4, 50, 0.6, 40),
            Tone.of(Wave.TRIANGLE, 784).from(270).lasting(120).gain(0.27).shape(4, 50, 0.6, 40),
            Tone.of(Wave.SINE, 523).from(360).gain(0.10).shape(4, 50, 0.6, 300),
            Tone.of(Wave.SINE, 659).from(360).gain(0.10).shape(4, 50, 0.6, 300),
            Tone.of(Wave.SINE, 784).from(360).gain(0.10).shape(4, 50, 0.6, 300)),

    /** Chapter failed: four gentle notes going down. */
    CHAPTER_FAIL(1160, 10, false,
            Tone.of(Wave.SINE, 440).lasting(200).gain(0.28).shape(8, 80, 0.5, 60),
            Tone.of(Wave.SINE, 330).from(170).lasting(200).gain(0.28).shape(8, 80, 0.5, 60),
            Tone.of(Wave.SINE, 262).from(340).lasting(200).gain(0.28).shape(8, 80, 0.5, 60),
            Tone.of(Wave.SINE, 220).from(510).gain(0.28).shape(8, 80, 0.5, 300)),

    /** One second passes in the last ten: a short tick. */
    LOW_TICK(40, 4, false,
            Tone.of(Wave.SINE, 1000).gain(0.22).shape(1, 12, 0.3, 25)),

    /** The same tick, higher, for the last five seconds. */
    LOW_TICK_URGENT(40, 4, false,
            Tone.of(Wave.SINE, 1320).gain(0.22).shape(1, 12, 0.3, 25)),

    /** A tip appears: a soft rising chirp. */
    HINT(60, 5, false,
            Tone.of(Wave.SINE, 880, 1320).gain(0.18).shape(4, 25, 0.2, 30)),

    /** Moving through a menu: a tiny click. */
    MENU_MOVE(25, 2, true,
            Tone.of(Wave.TRIANGLE, 660).gain(0.18).shape(1, 8, 0.5, 12)),

    /** Choosing in a menu, or turning a story page: two quick notes. */
    MENU_SELECT(135, 2, false,
            Tone.of(Wave.SINE, 660).lasting(70).gain(0.25).shape(2, 30, 0.4, 25),
            Tone.of(Wave.SINE, 880).from(60).gain(0.25).shape(2, 30, 0.4, 35)),

    /** The ending: a rising run of notes that opens into a long chord. */
    ENDING(1880, 10, false,
            Tone.of(Wave.TRIANGLE, 262).lasting(180).gain(0.26).shape(5, 60, 0.5, 60),
            Tone.of(Wave.TRIANGLE, 330).from(140).lasting(180).gain(0.26).shape(5, 60, 0.5, 60),
            Tone.of(Wave.TRIANGLE, 392).from(280).lasting(180).gain(0.26).shape(5, 60, 0.5, 60),
            Tone.of(Wave.TRIANGLE, 523).from(420).lasting(180).gain(0.26).shape(5, 60, 0.5, 60),
            Tone.of(Wave.TRIANGLE, 659).from(560).lasting(180).gain(0.26).shape(5, 60, 0.5, 60),
            Tone.of(Wave.TRIANGLE, 784).from(700).lasting(180).gain(0.26).shape(5, 60, 0.5, 60),
            Tone.of(Wave.TRIANGLE, 1047).from(840).lasting(180).gain(0.26).shape(5, 60, 0.5, 60),
            Tone.of(Wave.SINE, 523).from(980).gain(0.09).shape(5, 60, 0.6, 500),
            Tone.of(Wave.SINE, 659).from(980).gain(0.09).shape(5, 60, 0.6, 500),
            Tone.of(Wave.SINE, 784).from(980).gain(0.09).shape(5, 60, 0.6, 500),
            Tone.of(Wave.SINE, 1047).from(980).gain(0.09).shape(5, 60, 0.6, 500));

    private final int lengthMs;
    private final int priority;      // when too many sounds play at once, low numbers give way first
    private final boolean varied;    // true: played slightly higher or lower each time, so it never gets monotonous
    private final Tone[] tones;

    SoundEffect(int lengthMs, int priority, boolean varied, Tone... tones) {
        this.lengthMs = lengthMs;
        this.priority = priority;
        this.varied = varied;
        this.tones = tones;
    }

    /** Builds the samples of this effect. pitch 1.0 is the effect as written. */
    public short[] render(double pitch) {
        return ToneSynth.render(lengthMs, pitch, tones);
    }

    public int getLengthMs() {
        return lengthMs;
    }

    public int getPriority() {
        return priority;
    }

    public boolean isVaried() {
        return varied;
    }
}
