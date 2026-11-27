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
    HOOK_LAUNCH(110, true,
            Tone.of(Wave.SINE, 330, 660).glideByRatio().gain(0.30).shape(4, 40, 0.4, 50).hiss(0.08)),

    /** The player reels the line back early: a quick downward zip. */
    HOOK_RECALL(95, true,
            Tone.of(Wave.SINE, 480, 260).glideByRatio().gain(0.24).shape(3, 35, 0.3, 45).hiss(0.05)),

    /** A bottle is caught: a light upward blip. */
    CATCH_PLASTIC(100, true,
            Tone.of(Wave.SINE, 520, 1040).glideByRatio().gain(0.36).shape(2, 60, 0.2, 30).overtone(2, 0.25)),

    /** A can is caught: a metallic "tink" over a low hum. */
    CATCH_CAN(350, true,
            Tone.of(Wave.SINE, 1480).lasting(220).gain(0.30).shape(1, 30, 0.15, 150).overtone(2.76, 0.5),
            Tone.of(Wave.TRIANGLE, 98, 110).from(30).lasting(320).gain(0.22).shape(5, 60, 0.5, 120)),

    /** A net is caught: two quick snips. */
    CATCH_NET(110, true,
            Tone.noise().lasting(50).gain(0.30).shape(1, 30, 0.1, 9),
            Tone.of(Wave.SQUARE, 2400, 1400).lasting(50).gain(0.10).shape(1, 30, 0.1, 9),
            Tone.noise().from(60).lasting(50).gain(0.30).shape(1, 30, 0.1, 9),
            Tone.of(Wave.SQUARE, 2400, 1400).from(60).lasting(50).gain(0.10).shape(1, 30, 0.1, 9)),

    /** A fish is hooked: a falling "oh no". */
    FISH_HIT(180, true,
            Tone.of(Wave.TRIANGLE, 420, 190).glideByRatio().gain(0.36).shape(3, 80, 0.35, 70)),

    /** The Magnet knocks a fish out: a heavy metallic thud. */
    FISH_STUN(180, true,
            Tone.of(Wave.TRIANGLE, 190, 95).glideByRatio().gain(0.36).shape(2, 60, 0.3, 90),
            Tone.of(Wave.SINE, 520, 310).lasting(70).gain(0.18).shape(2, 30, 0.2, 30)),

    /** The Net Cutter strikes a fish: a sharp snip and low drop. */
    FISH_CUT(160, true,
            Tone.noise().lasting(55).gain(0.28).shape(1, 25, 0.15, 20),
            Tone.of(Wave.SQUARE, 1800, 420).lasting(90).gain(0.20).shape(1, 35, 0.2, 40),
            Tone.of(Wave.TRIANGLE, 300, 140).from(40).lasting(120).gain(0.24).shape(2, 40, 0.2, 60)),

    /** A snagged fish is released back into the water at the boat. */
    FISH_FREED(110, true,
            Tone.noise().lasting(45).gain(0.18).shape(3, 20, 0.2, 18),
            Tone.of(Wave.SINE, 440, 740).glideByRatio().gain(0.30).shape(3, 40, 0.3, 45)),

    /** A dangerous fish flashes or sparks a warning. */
    THREAT_ALERT(140, false,
            Tone.of(Wave.SQUARE, 880).lasting(45).gain(0.18).shape(2, 18, 0.3, 18),
            Tone.of(Wave.SQUARE, 1180).from(65).lasting(75).gain(0.20).shape(2, 25, 0.3, 35)),

    /** Wrong tool, the trash slipped off: a dull clunk. */
    WRONG_TOOL(100, true,
            Tone.of(Wave.TRIANGLE, 300, 240).gain(0.28).shape(1, 35, 0.1, 55).overtone(2.4, 0.3)),

    /** Another tool goes on the line: two quick clicks, the second higher. */
    TOOL_SWITCH(100, false,
            Tone.of(Wave.TRIANGLE, 523).lasting(45).gain(0.22).shape(2, 20, 0.2, 12),
            Tone.of(Wave.TRIANGLE, 698).from(45).gain(0.22).shape(2, 25, 0.2, 12)),

    /** The crew gets a new tool: three bright notes going up. */
    TOOL_UNLOCK(270, false,
            Tone.of(Wave.SINE, 1047).lasting(110).gain(0.25).shape(2, 40, 0.3, 50),
            Tone.of(Wave.SINE, 1319).from(70).lasting(110).gain(0.25).shape(2, 40, 0.3, 50),
            Tone.of(Wave.SINE, 1568).from(140).gain(0.25).shape(2, 40, 0.3, 60)),

    /** The quota is reached: two bright notes going up. */
    QUOTA(330, false,
            Tone.of(Wave.SINE, 784).lasting(150).gain(0.32).shape(3, 60, 0.4, 60),
            Tone.of(Wave.SINE, 1047).from(110).lasting(220).gain(0.32).shape(3, 60, 0.4, 100)),

    /** Level cleared: four rising notes, then a chord. */
    LEVEL_CLEAR(1000, false,
            Tone.of(Wave.TRIANGLE, 392).lasting(120).gain(0.27).shape(4, 50, 0.6, 40),
            Tone.of(Wave.TRIANGLE, 523).from(90).lasting(120).gain(0.27).shape(4, 50, 0.6, 40),
            Tone.of(Wave.TRIANGLE, 659).from(180).lasting(120).gain(0.27).shape(4, 50, 0.6, 40),
            Tone.of(Wave.TRIANGLE, 784).from(270).lasting(120).gain(0.27).shape(4, 50, 0.6, 40),
            Tone.of(Wave.SINE, 523).from(360).gain(0.10).shape(4, 50, 0.6, 300),
            Tone.of(Wave.SINE, 659).from(360).gain(0.10).shape(4, 50, 0.6, 300),
            Tone.of(Wave.SINE, 784).from(360).gain(0.10).shape(4, 50, 0.6, 300)),

    /** A badge is pinned on: two high, bell-like notes. */
    BADGE(390, false,
            Tone.of(Wave.SINE, 1319).lasting(160).gain(0.24).shape(1, 30, 0.25, 100),
            Tone.of(Wave.SINE, 1760).from(130).gain(0.24).shape(1, 30, 0.25, 120)),

    /** Level failed: four gentle notes going down. */
    LEVEL_FAIL(1160, false,
            Tone.of(Wave.SINE, 440).lasting(200).gain(0.28).shape(8, 80, 0.5, 60),
            Tone.of(Wave.SINE, 330).from(170).lasting(200).gain(0.28).shape(8, 80, 0.5, 60),
            Tone.of(Wave.SINE, 262).from(340).lasting(200).gain(0.28).shape(8, 80, 0.5, 60),
            Tone.of(Wave.SINE, 220).from(510).gain(0.28).shape(8, 80, 0.5, 300)),

    /** One second passes in the last ten: a short tick. */
    LOW_TICK(40, false,
            Tone.of(Wave.SINE, 1000).gain(0.22).shape(1, 12, 0.3, 25)),

    /** The same tick, higher, for the last five seconds. */
    LOW_TICK_URGENT(40, false,
            Tone.of(Wave.SINE, 1320).gain(0.22).shape(1, 12, 0.3, 25)),

    /** A tip appears: a soft rising chirp. */
    HINT(60, false,
            Tone.of(Wave.SINE, 880, 1320).gain(0.18).shape(4, 25, 0.2, 30)),

    /** Consecutive trash streak gained: two bright ascending chimes. */
    COMBO(220, true,
            Tone.of(Wave.SINE, 659).lasting(90).gain(0.26).shape(3, 30, 0.3, 45),
            Tone.of(Wave.SINE, 988).from(65).lasting(145).gain(0.30).shape(3, 40, 0.3, 70).overtone(2, 0.2)),

    /** A storm squall rolls in: low swell rumble and wind wash. */
    EVENT_SQUALL(320, false,
            Tone.of(Wave.TRIANGLE, 130, 88).glideByRatio().lasting(290).gain(0.32).shape(8, 100, 0.4, 140),
            Tone.noise().lasting(280).gain(0.18).shape(12, 100, 0.3, 140)),

    /** A trash tide brings bonus debris: bright rising arpeggio. */
    EVENT_BONUS(280, false,
            Tone.of(Wave.SINE, 523).lasting(95).gain(0.25).shape(3, 35, 0.25, 40),
            Tone.of(Wave.SINE, 659).from(70).lasting(95).gain(0.25).shape(3, 35, 0.25, 40),
            Tone.of(Wave.SINE, 784).from(140).lasting(130).gain(0.28).shape(3, 40, 0.25, 65)),

    /** A feeding frenzy starts: brisk warning double-pulse. */
    EVENT_FRENZY(230, false,
            Tone.of(Wave.TRIANGLE, 440, 587).lasting(95).gain(0.28).shape(3, 35, 0.25, 40),
            Tone.of(Wave.TRIANGLE, 587, 784).from(105).lasting(115).gain(0.28).shape(3, 35, 0.25, 55)),

    /** A slack tide calms the water: gentle warm chord. */
    EVENT_CALM(300, false,
            Tone.of(Wave.SINE, 523).lasting(270).gain(0.22).shape(10, 90, 0.4, 140),
            Tone.of(Wave.SINE, 659).from(40).lasting(240).gain(0.22).shape(10, 80, 0.4, 130)),

    /** The opening's storm: a crack of thunder over a long, low rumble. */
    THUNDER(1400, false,
            Tone.noise().lasting(160).gain(0.40).shape(1, 60, 0.3, 90),
            Tone.of(Wave.TRIANGLE, 70, 42).glideByRatio().from(40).lasting(1300).gain(0.42).shape(30, 300, 0.45, 700),
            Tone.noise().from(80).lasting(1250).gain(0.20).shape(60, 300, 0.4, 700)),

    /** The cleanup permit is stamped: a dull thud and a slap of paper. */
    PERMIT_STAMP(220, false,
            Tone.of(Wave.TRIANGLE, 150, 70).glideByRatio().gain(0.40).shape(1, 50, 0.25, 110),
            Tone.noise().lasting(70).gain(0.25).shape(1, 25, 0.1, 30)),

    /** The title of the opening: two quick notes that open into a bright chord. */
    TITLE_STING(1100, false,
            Tone.of(Wave.TRIANGLE, 392).lasting(160).gain(0.24).shape(4, 50, 0.5, 60),
            Tone.of(Wave.TRIANGLE, 523).from(120).lasting(160).gain(0.24).shape(4, 50, 0.5, 60),
            Tone.of(Wave.SINE, 523).from(240).gain(0.20).shape(10, 120, 0.6, 500),
            Tone.of(Wave.SINE, 659).from(240).gain(0.20).shape(10, 120, 0.6, 500),
            Tone.of(Wave.SINE, 784).from(240).gain(0.20).shape(10, 120, 0.6, 500).overtone(2, 0.15)),

    /** Moving through a menu: a tiny click. */
    MENU_MOVE(25, true,
            Tone.of(Wave.TRIANGLE, 660).gain(0.18).shape(1, 8, 0.5, 12)),

    /** Choosing in a menu, or turning a story page: two quick notes. */
    MENU_SELECT(135, false,
            Tone.of(Wave.SINE, 660).lasting(70).gain(0.25).shape(2, 30, 0.4, 25),
            Tone.of(Wave.SINE, 880).from(60).gain(0.25).shape(2, 30, 0.4, 35)),

    /** The run made the high-score list: five quick notes up to a bright last one. */
    HIGH_SCORE(600, false,
            Tone.of(Wave.SINE, 523).lasting(100).gain(0.24).shape(2, 35, 0.3, 40),
            Tone.of(Wave.SINE, 659).from(70).lasting(100).gain(0.24).shape(2, 35, 0.3, 40),
            Tone.of(Wave.SINE, 784).from(140).lasting(100).gain(0.24).shape(2, 35, 0.3, 40),
            Tone.of(Wave.SINE, 1047).from(210).lasting(100).gain(0.24).shape(2, 35, 0.3, 40),
            Tone.of(Wave.SINE, 1319).from(280).lasting(100).gain(0.24).shape(2, 35, 0.3, 40),
            Tone.of(Wave.SINE, 1568).from(350).gain(0.24).shape(2, 35, 0.3, 150)),

    /** The ending: a rising run of notes that opens into a long chord. */
    ENDING(1880, false,
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
    private final boolean varied;    // true: played slightly higher or lower each time, so it never gets monotonous
    private final Tone[] tones;

    SoundEffect(int lengthMs, boolean varied, Tone... tones) {
        this.lengthMs = lengthMs;
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

    public boolean isVaried() {
        return varied;
    }
}
