package ui.render;

import java.awt.Color;
import java.util.Arrays;

/**
 * The colours and scenery of one place in the story. Each chapter has its own
 * look, so the sea visibly changes as the crew cleans it: murky green
 * shallows, a clearer wreck, the dark deep, and the bright reef at the end.
 *
 * While a chapter is played the sea clears up in steps, a little with every
 * tenth of the quota. Each step is a palette of its own, made once.
 *
 * There is one shared object per look, so a palette can be used as a key to
 * find its cached background picture.
 */
public final class ChapterPalette {

    /** What is drawn on the sea floor. */
    public enum Scenery { SHALLOWS, WRECK, DEEP, REEF }

    public static final ChapterPalette SHALLOWS = new ChapterPalette(Scenery.SHALLOWS,
            rgb(0xA7B5A0), rgb(0xC9D1B8), rgb(0x5E7F5A), rgb(0x3F5F45), rgb(0x26382B),
            rgb(0x6B6A5C), rgb(0xE6DFA6), rgb(0xD8E8B0), 18, rgb(0xC7D15A));

    public static final ChapterPalette WRECK = new ChapterPalette(Scenery.WRECK,
            rgb(0x9CCBE0), rgb(0xD4EBF2), rgb(0x3E9CB0), rgb(0x2A7390), rgb(0x174B63),
            rgb(0xB7A67A), rgb(0xFFE27A), rgb(0xE8FAFF), 40, rgb(0x4FD1C5));

    public static final ChapterPalette DEEP = new ChapterPalette(Scenery.DEEP,
            rgb(0x2B3A5A), rgb(0x46587A), rgb(0x14407A), rgb(0x0B2655), rgb(0x050F2A),
            rgb(0x1C2438), rgb(0xC9D4F0), rgb(0x6FA8FF), 14, rgb(0x5FB0FF));

    /** The recovered reef of the ending. */
    public static final ChapterPalette REEF = new ChapterPalette(Scenery.REEF,
            rgb(0x7FD3F5), rgb(0xFFF1C9), rgb(0x2FB6E6), rgb(0x1B8FD0), rgb(0x0B5FA3),
            rgb(0xF2DFA0), rgb(0xFFD166), rgb(0xFFF6C8), 70, rgb(0xFFD166));

    /** The backdrop of the menu and other screens outside the story. */
    public static final ChapterPalette MENU = WRECK;

    private static final ChapterPalette[] BY_CHAPTER = {SHALLOWS, WRECK, DEEP};

    /** In how many steps a chapter's sea goes from dirty to clean. */
    public static final int CLEAN_STEPS = 10;

    // How far a cleaned-up chapter moves toward the look of the recovered reef.
    private static final double CLEANED_AMOUNT = 0.6;

    private final Scenery scenery;
    private final Color skyTop;
    private final Color skyBottom;
    private final Color seaTop;
    private final Color seaMiddle;
    private final Color seaBottom;
    private final Color seabed;
    private final Color sun;
    private final Color ray;        // sunbeams through the water
    private final int rayAlpha;     // 0 to 255: how strong the sunbeams are
    private final Color accent;
    private ChapterPalette[] cleanSteps;   // [0] is this look itself; the others are made when first asked for

    private ChapterPalette(Scenery scenery, Color skyTop, Color skyBottom, Color seaTop, Color seaMiddle,
                           Color seaBottom, Color seabed, Color sun, Color ray, int rayAlpha, Color accent) {
        this.scenery = scenery;
        this.skyTop = skyTop;
        this.skyBottom = skyBottom;
        this.seaTop = seaTop;
        this.seaMiddle = seaMiddle;
        this.seaBottom = seaBottom;
        this.seabed = seabed;
        this.sun = sun;
        this.ray = ray;
        this.rayAlpha = rayAlpha;
        this.accent = accent;
    }

    private static Color rgb(int hex) {
        return new Color(hex);
    }

    /** The look of chapter 1, 2 or 3. */
    public static ChapterPalette forChapter(int chapter) {
        return BY_CHAPTER[chapter - 1];
    }

    /**
     * The same place after the crew has cleaned it: every colour moved part
     * of the way toward the recovered reef. Shown when a chapter is cleared.
     */
    public ChapterPalette getCleaned() {
        return cleanedBy(1);
    }

    /**
     * The same place, partly cleaned.
     *
     * @param progress 0 = as dirty as it starts, 1 = fully cleaned; rounded
     *                 down to a whole step, so the look only changes when a
     *                 step is completed
     */
    public ChapterPalette cleanedBy(double progress) {
        if (cleanSteps == null) {
            cleanSteps = new ChapterPalette[CLEAN_STEPS + 1];
            cleanSteps[0] = this;
        }
        int step = (int) Math.floor(Math.max(0, Math.min(1, progress)) * CLEAN_STEPS + 1e-9);
        if (cleanSteps[step] == null) {
            double amount = CLEANED_AMOUNT * step / CLEAN_STEPS;
            ChapterPalette mixed = new ChapterPalette(scenery,
                    mix(skyTop, REEF.skyTop, amount), mix(skyBottom, REEF.skyBottom, amount),
                    mix(seaTop, REEF.seaTop, amount), mix(seaMiddle, REEF.seaMiddle, amount),
                    mix(seaBottom, REEF.seaBottom, amount), mix(seabed, REEF.seabed, amount),
                    mix(sun, REEF.sun, amount), mix(ray, REEF.ray, amount),
                    (int) (rayAlpha + (REEF.rayAlpha - rayAlpha) * amount), mix(accent, REEF.accent, amount));
            if (step == CLEAN_STEPS) {
                // Cleaning a clean place changes nothing.
                mixed.cleanSteps = new ChapterPalette[CLEAN_STEPS + 1];
                Arrays.fill(mixed.cleanSteps, mixed);
            }
            cleanSteps[step] = mixed;
        }
        return cleanSteps[step];
    }

    private static Color mix(Color from, Color to, double amount) {
        return new Color(
                (int) (from.getRed() + (to.getRed() - from.getRed()) * amount),
                (int) (from.getGreen() + (to.getGreen() - from.getGreen()) * amount),
                (int) (from.getBlue() + (to.getBlue() - from.getBlue()) * amount));
    }

    public Scenery getScenery() {
        return scenery;
    }

    public Color getSkyTop() {
        return skyTop;
    }

    public Color getSkyBottom() {
        return skyBottom;
    }

    public Color getSeaTop() {
        return seaTop;
    }

    public Color getSeaMiddle() {
        return seaMiddle;
    }

    public Color getSeaBottom() {
        return seaBottom;
    }

    public Color getSeabed() {
        return seabed;
    }

    public Color getSun() {
        return sun;
    }

    public Color getRay() {
        return ray;
    }

    public int getRayAlpha() {
        return rayAlpha;
    }

    public Color getAccent() {
        return accent;
    }
}
