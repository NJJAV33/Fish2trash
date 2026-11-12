import util.SoundEffect;
import util.ToneSynth;
import util.ToneSynth.Tone;
import util.ToneSynth.Wave;

/**
 * Checks the sound maker by looking at the numbers it produces: right length,
 * right loudness, right pitch, and no clicks at the start or end.
 */
public final class SynthTest {

    private static final int LOUDEST_ALLOWED = (int) (0.56 * Short.MAX_VALUE);
    private static final int NEARLY_SILENT = (int) (0.05 * Short.MAX_VALUE);

    private SynthTest() {
        // only static methods
    }

    public static void run() {
        aToneHasTheRightPitch();
        theSameRecipeAlwaysGivesTheSameSound();
        everyEffectIsWellFormed();
    }

    /** A wave crosses zero twice per repeat, so 1000 Hz for 50 ms crosses about 100 times. */
    private static void aToneHasTheRightPitch() {
        short[] samples = ToneSynth.render(50, 1.0, Tone.of(Wave.SINE, 1000).shape(0, 0, 1, 0));
        Check.assertEquals("50 ms is 1102 samples", 50 * ToneSynth.SAMPLE_RATE / 1000, samples.length);
        Check.assertEquals("1000 Hz crosses zero about 100 times in 50 ms", 100, zeroCrossings(samples), 2);

        short[] octaveUp = ToneSynth.render(50, 2.0, Tone.of(Wave.SINE, 1000).shape(0, 0, 1, 0));
        Check.assertEquals("pitch 2.0 doubles the frequency", 200, zeroCrossings(octaveUp), 3);
        Check.assertEquals("pitch does not change the length", samples.length, octaveUp.length);

        short[] silence = ToneSynth.render(50, 1.0);
        Check.assertEquals("no tones gives silence", 0, loudest(silence));
    }

    private static void theSameRecipeAlwaysGivesTheSameSound() {
        short[] first = SoundEffect.CATCH_NET.render(1.0);    // this one uses random noise
        short[] second = SoundEffect.CATCH_NET.render(1.0);
        boolean identical = first.length == second.length;
        for (int i = 0; identical && i < first.length; i++) {
            identical = first[i] == second[i];
        }
        Check.assertTrue("rendering twice gives identical samples", identical);
    }

    private static void everyEffectIsWellFormed() {
        for (SoundEffect effect : SoundEffect.values()) {
            short[] samples = effect.render(1.0);
            String name = effect.name();

            Check.assertEquals(name + " has the length it declares",
                    effect.getLengthMs() * ToneSynth.SAMPLE_RATE / 1000, samples.length);
            Check.assertTrue(name + " is not silent", loudest(samples) > Short.MAX_VALUE / 4);
            Check.assertTrue(name + " is not too loud", loudest(samples) <= LOUDEST_ALLOWED);
            Check.assertTrue(name + " starts quietly (no click)", Math.abs(samples[0]) <= NEARLY_SILENT);
            Check.assertTrue(name + " ends quietly (no click)",
                    Math.abs(samples[samples.length - 1]) <= NEARLY_SILENT);

            short[] higher = effect.render(1.04);
            Check.assertEquals(name + " keeps its length at another pitch", samples.length, higher.length);
        }
    }

    private static int zeroCrossings(short[] samples) {
        int crossings = 0;
        for (int i = 1; i < samples.length; i++) {
            boolean wasNegative = samples[i - 1] < 0;
            boolean isNegative = samples[i] < 0;
            if (wasNegative != isNegative) {
                crossings++;
            }
        }
        return crossings;
    }

    private static int loudest(short[] samples) {
        int loudest = 0;
        for (short sample : samples) {
            loudest = Math.max(loudest, Math.abs(sample));
        }
        return loudest;
    }
}
