package util;

/**
 * Every piece of text the player reads, in one place, so the story can be
 * edited (or translated) without touching the game code.
 *
 * "\n" starts a new line on screen.
 */
public final class StoryText {

    private StoryText() {
        // only constants, never created
    }

    // ---- Chapter titles ----
    public static final String CH1_TITLE = "CHAPTER 1: PLASTIC SHALLOWS";
    public static final String CH2_TITLE = "CHAPTER 2: OLD PIER WRECK";
    public static final String CH3_TITLE = "CHAPTER 3: GHOST NET DEEP";

    // ---- Tips shown while playing ----
    public static final String TUT_AIM = "Time the swing, then press SPACE to drop the hook.";
    public static final String TUT_FISH = "Fish are not trash! Each one you hook costs 1 health.";
    public static final String TUT_QUOTA = "Reach 100 points before time or health runs out.";
    public static final String TUT_MAGNET = "Press 2 for the Magnet. Cans stick to it.";
    public static final String TUT_MAGNET_ALT = "Cans are worth 20 points. Hook them!";
    public static final String TUT_BIGFISH = "Big fish are fast. Wait for a gap before you drop.";
    public static final String TUT_CUTTER = "Press 3 for the Net Cutter. It frees nets.";
    public static final String TUT_CUTTER_ALT = "Nets are worth 50 points. Hook them!";
    public static final String TUT_PUFFER = "Pufferfish swell up near the hook. Steer clear!";
    public static final String TUT_BARRACUDA = "Barracuda flash, then charge. Dodge or recall!";
}
