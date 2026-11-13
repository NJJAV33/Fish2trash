import game.Badge;
import game.ChapterResult;
import game.FailCause;
import game.GameManager;
import game.GameSession;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import model.ChapterData;
import model.EquipmentType;
import model.ObjectKind;
import ui.ChapterClearScreen;
import ui.ScreenManager;
import ui.render.BadgePainter;
import ui.render.SceneRenderer;
import util.GameConstants;
import util.SoundEffect;

/**
 * Checks the badges: each rule just inside and just outside its limit, the
 * badges a whole run collects, and how the chapter-cleared screen presents one.
 */
public final class BadgeTest {

    private static final double TIME = 100;   // the made-up results below have a 100 second clock
    private static final int HEALTH = 5;      // and five health
    private static final int THROWS = 100;
    private static final int MEDAL_SIZE = 96;

    private BadgeTest() {
        // only static methods
    }

    public static void run() {
        everyChapterHasItsOwnBadge();
        ecoRookieOnlyNeedsAClear();
        sorterNeedsTheRightToolAndGoodAim();
        championNeedsSpeedAndHealth();
        aCleanRunCollectsEveryBadge();
        sloppySortingClearsTheChapterWithoutTheBadge();
        aFailedAttemptEarnsNothingButTheRetryCan();
        theClearScreenPresentsAnEarnedBadgeOnce();
        theClearScreenStaysQuietForAMissedBadge();
        everyBadgeHasItsOwnMedal();
    }

    // ---- The rules ----

    private static void everyChapterHasItsOwnBadge() {
        Set<String> names = new HashSet<>();
        for (int chapter = 1; chapter <= ChapterData.CHAPTER_COUNT; chapter++) {
            Badge badge = Badge.forChapter(chapter);
            Check.assertEquals("the badge of chapter " + chapter + " knows its chapter", chapter, badge.getChapter());
            names.add(badge.getDisplayName());
        }
        Check.assertEquals("one badge per chapter", ChapterData.CHAPTER_COUNT, Badge.values().length);
        Check.assertEquals("every badge has its own name", Badge.values().length, names.size());

        boolean refused = false;
        try {
            Badge.forChapter(ChapterData.CHAPTER_COUNT + 1);
        } catch (IllegalArgumentException expected) {
            refused = true;
        }
        Check.assertTrue("there is no badge for a chapter that does not exist", refused);
    }

    private static void ecoRookieOnlyNeedsAClear() {
        // A clear by the skin of the teeth: no time left, one health, wild throws.
        ChapterResult barely = cleared(1, 0.1, HEALTH - 1, THROWS, 10, 50);
        Check.assertTrue("any clear of chapter 1 earns Eco Rookie", Badge.ECO_ROOKIE.isEarnedBy(barely));

        ChapterResult failed = new ChapterResult(1, FailCause.TIME, 90, 0, TIME, HEALTH, HEALTH, 9, 9, 0, 0);
        Check.assertTrue("a failed chapter 1 earns nothing", !Badge.ECO_ROOKIE.isEarnedBy(failed));
        Check.assertTrue("clearing another chapter does not earn it",
                !Badge.ECO_ROOKIE.isEarnedBy(cleared(2, TIME, 0, THROWS, THROWS, 0)));
    }

    private static void sorterNeedsTheRightToolAndGoodAim() {
        int slipsAllowed = GameConstants.SORTER_MAX_SLIPS;
        int catchesNeeded = (int) Math.ceil(GameConstants.SORTER_MIN_ACCURACY * THROWS);

        Check.assertTrue("exactly at both limits earns Sorter",
                Badge.SORTER.isEarnedBy(cleared(2, 1, 3, THROWS, catchesNeeded, slipsAllowed)));
        Check.assertTrue("one slip too many loses it",
                !Badge.SORTER.isEarnedBy(cleared(2, 1, 3, THROWS, catchesNeeded, slipsAllowed + 1)));
        Check.assertTrue("one catch too few loses it",
                !Badge.SORTER.isEarnedBy(cleared(2, 1, 3, THROWS, catchesNeeded - 1, slipsAllowed)));
        Check.assertTrue("time and health do not matter for Sorter",
                Badge.SORTER.isEarnedBy(cleared(2, 0.1, HEALTH - 1, THROWS, THROWS, 0)));

        ChapterResult failed = new ChapterResult(2, FailCause.HEALTH, 150, 40, TIME, 0, HEALTH, 10, 10, 0, 3);
        Check.assertTrue("a failed chapter 2 earns nothing, however tidy", !Badge.SORTER.isEarnedBy(failed));
        Check.assertTrue("a tidy chapter 3 does not earn it",
                !Badge.SORTER.isEarnedBy(cleared(3, TIME, 0, THROWS, THROWS, 0)));
    }

    private static void championNeedsSpeedAndHealth() {
        double timeNeeded = GameConstants.CHAMPION_TIME_FRACTION * TIME;
        int healthAllowed = GameConstants.CHAMPION_MAX_HP_LOST;

        Check.assertTrue("exactly at both limits earns Champion",
                Badge.CHAMPION.isEarnedBy(cleared(3, timeNeeded, healthAllowed, THROWS, 50, 0)));
        Check.assertTrue("a moment too slow loses it",
                !Badge.CHAMPION.isEarnedBy(cleared(3, timeNeeded - 0.05, healthAllowed, THROWS, 50, 0)));
        Check.assertTrue("one health too many loses it",
                !Badge.CHAMPION.isEarnedBy(cleared(3, timeNeeded, healthAllowed + 1, THROWS, 50, 0)));
        Check.assertTrue("slips and misses do not matter for Champion",
                Badge.CHAMPION.isEarnedBy(cleared(3, TIME, 0, THROWS, 7, 40)));

        ChapterResult failed = new ChapterResult(3, FailCause.TIME, 300, 0, TIME, HEALTH, HEALTH, 6, 6, 0, 0);
        Check.assertTrue("a failed chapter 3 earns nothing", !Badge.CHAMPION.isEarnedBy(failed));
    }

    // ---- A whole run ----

    private static void aCleanRunCollectsEveryBadge() {
        GameSession session = new GameSession(new Random(21));
        Check.assertTrue("a new run has no badges", session.getBadges().isEmpty());

        for (int chapter = 1; chapter <= ChapterData.CHAPTER_COUNT; chapter++) {
            GameManager game = session.startChapter();
            GameScript.clearChapter(game);
            session.onChapterEnded(game);
            Check.assertTrue("a clean chapter " + chapter + " earns its badge",
                    session.hasBadge(Badge.forChapter(chapter)));
            Check.assertEquals("one badge per cleared chapter", chapter, session.getBadges().size());
        }
        Check.assertEquals("all three badges", EnumSet.allOf(Badge.class), session.getBadges());

        boolean refused = false;
        try {
            session.getBadges().add(Badge.ECO_ROOKIE);
        } catch (UnsupportedOperationException expected) {
            refused = true;
        }
        Check.assertTrue("the badge set cannot be changed from outside", refused);
    }

    private static void sloppySortingClearsTheChapterWithoutTheBadge() {
        GameSession session = new GameSession(new Random(22));
        clearCleanly(session);

        GameManager second = session.startChapter();
        clearChapterTwoWithTwoSlips(second);
        ChapterResult result = session.onChapterEnded(second);

        Check.assertTrue("chapter 2 is cleared all the same", result.isCleared());
        Check.assertEquals("with two slips on the record", 2, result.getSlips());
        Check.assertTrue("so there is no Sorter badge", !session.hasBadge(Badge.SORTER));
        Check.assertEquals("the story moves on without it", 3, session.getChapterNumber());
        Check.assertEquals("and the first badge is kept", EnumSet.of(Badge.ECO_ROOKIE), session.getBadges());
    }

    private static void aFailedAttemptEarnsNothingButTheRetryCan() {
        GameSession session = new GameSession(new Random(23));
        GameManager first = session.startChapter();
        while (first.isPlaying()) {
            first.update(0.05);   // do nothing until the clock runs out
        }
        session.onChapterEnded(first);
        Check.assertTrue("a failed chapter gives no badge", session.getBadges().isEmpty());

        clearCleanly(session);
        Check.assertTrue("the retry earns the badge", session.hasBadge(Badge.ECO_ROOKIE));
    }

    // ---- On screen ----

    private static void theClearScreenPresentsAnEarnedBadgeOnce() {
        RecordingAudioPlayer audio = new RecordingAudioPlayer();
        ScreenManager screens = new ScreenManager(audio, TempScores.newTable());
        screens.startNewRun();
        clearCleanly(screens.getSession());

        ChapterClearScreen screen = new ChapterClearScreen(screens, new SceneRenderer());
        screen.onEnter();
        screen.update(0.5);
        draw(screen);
        Check.assertTrue("the badge waits a moment before it appears", !audio.getPlayed().contains(SoundEffect.BADGE));

        screen.update(0.5);
        draw(screen);   // the medal mid-pop, with its sparkles
        Check.assertEquals("then it arrives with its chime", SoundEffect.BADGE, lastPlayed(audio));
        for (int i = 0; i < 60; i++) {
            screen.update(0.05);
        }
        draw(screen);
        Check.assertEquals("the chime plays only once", 1, audio.getPlayed().size());
    }

    private static void theClearScreenStaysQuietForAMissedBadge() {
        RecordingAudioPlayer audio = new RecordingAudioPlayer();
        ScreenManager screens = new ScreenManager(audio, TempScores.newTable());
        screens.startNewRun();
        clearCleanly(screens.getSession());
        GameManager second = screens.getSession().startChapter();
        clearChapterTwoWithTwoSlips(second);
        screens.getSession().onChapterEnded(second);

        ChapterClearScreen screen = new ChapterClearScreen(screens, new SceneRenderer());
        screen.onEnter();
        for (int i = 0; i < 60; i++) {
            screen.update(0.05);
        }
        draw(screen);   // the grey medal and how to earn it
        Check.assertTrue("no chime for a badge that was missed", audio.getPlayed().isEmpty());
    }

    private static void everyBadgeHasItsOwnMedal() {
        BadgePainter painter = new BadgePainter();
        Set<Integer> pictures = new HashSet<>();
        for (Badge badge : Badge.values()) {
            int[] earned = medal(painter, badge, true);
            int[] locked = medal(painter, badge, false);
            Check.assertTrue(badge + " draws something", !Arrays.equals(earned, new int[earned.length]));
            Check.assertTrue(badge + " looks different when it was not earned", !Arrays.equals(earned, locked));
            pictures.add(Arrays.hashCode(earned));
            pictures.add(Arrays.hashCode(locked));
        }
        Check.assertEquals("six different pictures: three medals, earned and not",
                2 * Badge.values().length, pictures.size());
    }

    // ---- helpers ----

    /** A cleared chapter with made-up numbers. Fish hooked is set to the health lost. */
    private static ChapterResult cleared(int chapter, double timeLeft, int healthLost,
            int launches, int catches, int slips) {
        return new ChapterResult(chapter, FailCause.NONE, 350, timeLeft, TIME,
                HEALTH - healthLost, HEALTH, launches, catches, slips, healthLost);
    }

    /** Plays the session's current chapter without a mistake. */
    private static void clearCleanly(GameSession session) {
        GameManager game = session.startChapter();
        GameScript.clearChapter(game);
        session.onChapterEnded(game);
    }

    /** Touches two cans with the plain hook first (wrong tool: they slip), then clears with the magnet. */
    private static void clearChapterTwoWithTwoSlips(GameManager game) {
        game.selectTool(EquipmentType.HOOK);
        GameScript.hookOnto(game, GameScript.firstCatchable(game, ObjectKind.CAN));
        GameScript.hookOnto(game, GameScript.firstCatchable(game, ObjectKind.CAN));
        for (double t = 0; t < GameConstants.TOOL_SWAP_COOLDOWN_S; t += GameScript.STEP) {
            game.update(GameScript.STEP);   // a tool can only be changed again after a short wait
        }
        game.selectTool(EquipmentType.MAGNET);
        GameScript.clearChapter(game);
    }

    private static SoundEffect lastPlayed(RecordingAudioPlayer audio) {
        return audio.getPlayed().isEmpty() ? null : audio.getPlayed().get(audio.getPlayed().size() - 1);
    }

    private static void draw(ChapterClearScreen screen) {
        BufferedImage image = new BufferedImage(
                GameConstants.WORLD_WIDTH, GameConstants.WORLD_HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        screen.paint(g);
        g.dispose();
    }

    private static int[] medal(BadgePainter painter, Badge badge, boolean earned) {
        BufferedImage image = new BufferedImage(MEDAL_SIZE, MEDAL_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        painter.paint(g, badge, MEDAL_SIZE / 2.0, MEDAL_SIZE / 3.0, MEDAL_SIZE / 4.0, earned);
        g.dispose();
        return image.getRGB(0, 0, MEDAL_SIZE, MEDAL_SIZE, null, 0, MEDAL_SIZE);
    }
}
