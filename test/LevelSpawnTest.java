import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import model.ChapterData;
import model.HookSensor;
import model.Level;
import model.ObjectKind;
import model.OceanObject;
import model.SpawnRule;
import util.GameConstants;
import util.StoryText;

/**
 * Checks that the levels follow their spawn tables: the right things appear
 * at the right time, never on top of the hook, and there is always enough
 * trash in the water to finish the chapter.
 */
public final class LevelSpawnTest {

    private static final double STEP = 0.05;
    private static final long[] SEEDS = {1, 2, 3, 4, 5};

    // A hook that just hangs under the boat in the middle of the screen.
    private static final HookSensor RESTING_HOOK = new HookSensor() {
        @Override
        public double getTipX() {
            return GameConstants.WORLD_WIDTH / 2.0;
        }

        @Override
        public double getTipY() {
            return GameConstants.WATER_Y + GameConstants.HOOK_REST;
        }

        @Override
        public boolean isLineOut() {
            return false;
        }
    };

    private LevelSpawnTest() {
        // only static methods
    }

    public static void run() {
        chapterNumbersMatchThePlan();
        rulesWakeUpOnTime();
        newObjectsFadeInAwayFromTheHook();
        thereIsAlwaysEnoughTrashToFinish();
        tipsComeOnceAndInOrder();
    }

    private static void chapterNumbersMatchThePlan() {
        int[] quotas = {100, 200, 350};
        double[] seconds = {65, 90, 105};
        int[] health = {3, 4, 5};
        for (int n = 1; n <= ChapterData.CHAPTER_COUNT; n++) {
            ChapterData chapter = ChapterData.forChapter(n);
            Check.assertEquals("chapter " + n + " number", n, chapter.getNumber());
            Check.assertEquals("chapter " + n + " quota", quotas[n - 1], chapter.getQuota());
            Check.assertEquals("chapter " + n + " time", seconds[n - 1], chapter.getTimeLimit(), 1e-9);
            Check.assertEquals("chapter " + n + " health", health[n - 1], chapter.getStartHealth());
        }
        boolean rejected = false;
        try {
            ChapterData.forChapter(4);
        } catch (IllegalArgumentException expected) {
            rejected = true;
        }
        Check.assertTrue("there is no chapter 4", rejected);
    }

    private static void rulesWakeUpOnTime() {
        Level level = new Level(ChapterData.forChapter(1), new Random(1));
        level.update(0, RESTING_HOOK, 0);
        Check.assertTrue("bottles are there from the start", count(level, ObjectKind.PLASTIC_BOTTLE) >= 6);
        Check.assertEquals("no fish at the start", 0, count(level, ObjectKind.SMALL_FISH));

        play(level, 5.9, 0);
        Check.assertEquals("still no fish just before second 6", 0, count(level, ObjectKind.SMALL_FISH));
        play(level, 0.2, 0);
        Check.assertEquals("the first fish arrives at second 6", 1, count(level, ObjectKind.SMALL_FISH));
        play(level, 6.0, 0);
        Check.assertEquals("three more fish arrive at second 12", 4, count(level, ObjectKind.SMALL_FISH));
        play(level, 30, 0);
        Check.assertEquals("fish are never replaced or added to", 4, count(level, ObjectKind.SMALL_FISH));
    }

    private static void newObjectsFadeInAwayFromTheHook() {
        for (int n = 1; n <= ChapterData.CHAPTER_COUNT; n++) {
            Level level = new Level(ChapterData.forChapter(n), new Random(n));
            Set<OceanObject> seen = new HashSet<>();
            boolean allFarEnough = true;
            boolean allStartImmune = true;
            Random collector = new Random(99);

            for (double t = 0; t < 40; t += STEP) {
                level.update(STEP, RESTING_HOOK, 0);
                for (OceanObject object : level.getObjects()) {
                    if (seen.add(object)) {   // true the first time we meet it
                        double keepAway = object.getKind().isTrash()
                                ? GameConstants.SPAWN_MIN_DIST_TRASH : GameConstants.SPAWN_MIN_DIST_FISH;
                        double distance = Math.hypot(object.getX() - RESTING_HOOK.getTipX(),
                                object.getY() - RESTING_HOOK.getTipY());
                        // One step of movement has already happened, so allow that much slack.
                        allFarEnough = allFarEnough && distance >= keepAway - 15;
                        allStartImmune = allStartImmune && !object.isCatchable() && object.getOpacity() < 1;
                    }
                }
                collectOneNowAndThen(level, collector);
            }
            Check.assertTrue("chapter " + n + ": nothing appears next to the hook", allFarEnough);
            Check.assertTrue("chapter " + n + ": new objects fade in and cannot be hooked yet", allStartImmune);

            play(level, GameConstants.SPAWN_FADE_IN_S + 0.1, 0);
            OceanObject oldest = level.getObjects().get(0);
            Check.assertTrue("chapter " + n + ": after fading in, an object can be hooked", oldest.isCatchable());
            Check.assertEquals("chapter " + n + ": and is fully visible", 1.0, oldest.getOpacity(), 1e-9);
        }
    }

    private static void thereIsAlwaysEnoughTrashToFinish() {
        for (int n = 1; n <= ChapterData.CHAPTER_COUNT; n++) {
            ChapterData chapter = ChapterData.forChapter(n);
            SpawnRule mainRule = firstRuleFor(chapter, chapter.getPrimaryKind());
            for (long seed : SEEDS) {
                Level level = new Level(chapter, new Random(seed));
                Random collector = new Random(seed + 100);
                int score = 0;
                boolean neverShort = true;
                boolean neverCrowded = true;

                // Play the whole chapter while a pretend player keeps collecting trash.
                for (double t = 0; t < chapter.getTimeLimit(); t += STEP) {
                    level.update(STEP, RESTING_HOOK, score);
                    // Right after an update the level must have topped itself up.
                    int mainTrash = count(level, chapter.getPrimaryKind());
                    neverShort = neverShort && mainTrash >= mainRule.getMinCount();
                    neverCrowded = neverCrowded && level.getObjects().size() <= totalMax(chapter) + 1;
                    score += collectOneNowAndThen(level, collector);
                }
                Check.assertTrue("chapter " + n + " seed " + seed + ": main trash never runs low", neverShort);
                Check.assertTrue("chapter " + n + " seed " + seed + ": the water never overfills", neverCrowded);
                Check.assertTrue("chapter " + n + " seed " + seed + ": the pretend player could reach the quota",
                        score >= chapter.getQuota());
            }
        }
    }

    private static void tipsComeOnceAndInOrder() {
        Level level = new Level(ChapterData.forChapter(1), new Random(1));
        List<String> shown = new ArrayList<>();
        List<Double> times = new ArrayList<>();
        for (double t = 0; t < 30; t += STEP) {
            level.update(STEP, RESTING_HOOK, 0);
            for (String tip : level.pollTutorialPrompts()) {
                shown.add(tip);
                times.add(level.getElapsed());
            }
        }
        Check.assertEquals("chapter 1 tips, in order",
                List.of(StoryText.TUT_AIM, StoryText.TUT_FISH, StoryText.TUT_QUOTA), shown);
        Check.assertEquals("first tip at second 1", 1.0, times.get(0), STEP * 1.5);
        Check.assertEquals("second tip at second 8", 8.0, times.get(1), STEP * 1.5);
        Check.assertEquals("third tip at second 16", 16.0, times.get(2), STEP * 1.5);
    }

    // ---- helpers ----

    private static void play(Level level, double seconds, int score) {
        for (double t = 0; t < seconds - 1e-9; t += STEP) {
            level.update(STEP, RESTING_HOOK, score);
        }
    }

    private static int count(Level level, ObjectKind kind) {
        int count = 0;
        for (OceanObject object : level.getObjects()) {
            if (object.getKind() == kind) {
                count++;
            }
        }
        return count;
    }

    /** About once every two seconds, takes one piece of trash out, like a player would. Returns its points. */
    private static int collectOneNowAndThen(Level level, Random collector) {
        if (collector.nextDouble() >= STEP / 2.0) {
            return 0;
        }
        for (OceanObject object : level.getObjects()) {
            if (object.getKind().isTrash() && object.isCatchable()) {
                level.remove(object);
                return object.getPoints();
            }
        }
        return 0;
    }

    private static SpawnRule firstRuleFor(ChapterData chapter, ObjectKind kind) {
        for (SpawnRule rule : chapter.getSpawnRules()) {
            if (rule.getKind() == kind) {
                return rule;
            }
        }
        throw new IllegalStateException("no rule for " + kind);
    }

    private static int totalMax(ChapterData chapter) {
        int total = 0;
        for (SpawnRule rule : chapter.getSpawnRules()) {
            total += rule.getMaxCount();
        }
        return total;
    }
}
