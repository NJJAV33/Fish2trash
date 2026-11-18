import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import model.LevelData;
import model.HookSensor;
import model.Level;
import model.ObjectKind;
import model.OceanObject;
import model.SpawnRule;
import util.GameConstants;
import util.StoryText;

/**
 * Checks that the levels follow their spawn tables: the right things appear
 * at the right time, never on top of the hook, and every level holds enough
 * trash to be finished - but never enough of one kind alone.
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

        @Override
        public boolean isExtending() {
            return false;
        }

        @Override
        public boolean isMagnetic() {
            return false;
        }
    };

    private LevelSpawnTest() {
        // only static methods
    }

    public static void run() {
        levelNumbersMatchThePlan();
        everyLevelHoldsEnoughTrashButNoKindIsEnough();
        everyLevelHasAtLeastAsManyFishAsTheOneBefore();
        rulesWakeUpOnTime();
        trashRunsOutWhenTheSupplyIsUsedUp();
        newObjectsFadeInAwayFromTheHook();
        tipsComeOnceAndInOrder();
    }

    private static void levelNumbersMatchThePlan() {
        int[] quotas = {40, 60, 70, 80, 100, 100, 120, 150, 170, 200, 200, 240, 280, 320, 350};
        int[] finalQuotas = {GameConstants.QUOTA_CH1, GameConstants.QUOTA_CH2, GameConstants.QUOTA_CH3};
        int[] health = {GameConstants.HEALTH_CH1, GameConstants.HEALTH_CH2, GameConstants.HEALTH_CH3};
        Check.assertEquals("fifteen levels", quotas.length, LevelData.LEVEL_COUNT);

        for (int n = 1; n <= LevelData.LEVEL_COUNT; n++) {
            LevelData level = LevelData.byNumber(n);
            String name = "level " + level.getChapter() + "-" + level.getLevel();
            Check.assertEquals(name + " number", n, level.getNumber());
            Check.assertEquals(name + " is the same level by chapter and place", level.getTitle(),
                    LevelData.of(level.getChapter(), level.getLevel()).getTitle());
            Check.assertEquals(name + " quota", quotas[n - 1], level.getQuota());
            Check.assertEquals(name + " health", health[level.getChapter() - 1], level.getStartHealth());
            Check.assertTrue(name + " has a clock", level.getTimeLimit() >= 30);
            Check.assertEquals(name + " is the last of its chapter", level.getLevel() == LevelData.LEVELS_PER_CHAPTER,
                    level.isChapterFinale());
            if (level.isChapterFinale()) {
                Check.assertEquals("the last level of chapter " + level.getChapter() + " asks for the chapter's quota",
                        finalQuotas[level.getChapter() - 1], level.getQuota());
            } else {
                Check.assertTrue(name + " asks for less than the level after it",
                        level.getQuota() < LevelData.byNumber(n + 1).getQuota());
            }
        }

        Check.assertTrue("there is no chapter 4", isRejected(() -> LevelData.of(4, 1)));
        Check.assertTrue("there is no level 6 in a chapter", isRejected(() -> LevelData.of(1, 6)));
        Check.assertTrue("there is no level number 0", isRejected(() -> LevelData.byNumber(0)));
        Check.assertTrue("there is no level number 16", isRejected(() -> LevelData.byNumber(16)));
    }

    private static boolean isRejected(Runnable lookup) {
        try {
            lookup.run();
            return false;
        } catch (IllegalArgumentException expected) {
            return true;
        }
    }

    /**
     * Trash is limited, so the tables themselves must make every level
     * winnable: all the trash together is worth clearly more than the quota.
     * From chapter 2 on, no single kind is worth the whole quota, which is
     * what makes the player change tools.
     */
    private static void everyLevelHoldsEnoughTrashButNoKindIsEnough() {
        for (int n = 1; n <= LevelData.LEVEL_COUNT; n++) {
            LevelData level = LevelData.byNumber(n);
            String name = "level " + level.getChapter() + "-" + level.getLevel();
            Map<ObjectKind, Integer> worth = new EnumMap<>(ObjectKind.class);
            int total = 0;
            for (SpawnRule rule : level.getSpawnRules()) {
                int points = rule.getSupply() * rule.getKind().create(0, 0, 0).getPoints();
                worth.merge(rule.getKind(), points, Integer::sum);
                total += points;
            }
            Check.assertTrue(name + " holds " + total + " points of trash for a quota of " + level.getQuota(),
                    total >= GameConstants.SUPPLY_MARGIN * level.getQuota());
            Check.assertTrue(name + " has its chapter's main trash", worth.getOrDefault(level.getPrimaryKind(), 0) > 0);
            if (level.getChapter() >= 2) {
                for (Map.Entry<ObjectKind, Integer> kind : worth.entrySet()) {
                    Check.assertTrue(name + ": " + kind.getKey() + " alone (" + kind.getValue()
                            + " points) cannot fill the quota", kind.getValue() < level.getQuota());
                }
            }
        }
    }

    /** "Harder as the game goes": inside a chapter the fish only ever get more. */
    private static void everyLevelHasAtLeastAsManyFishAsTheOneBefore() {
        for (int chapter = 1; chapter <= LevelData.CHAPTER_COUNT; chapter++) {
            int before = 0;
            for (int place = 1; place <= LevelData.LEVELS_PER_CHAPTER; place++) {
                int fish = 0;
                for (SpawnRule rule : LevelData.of(chapter, place).getSpawnRules()) {
                    if (!rule.getKind().isTrash()) {
                        fish += rule.getSupply();
                    }
                }
                Check.assertTrue("level " + chapter + "-" + place + " has " + fish + " fish, the one before had " + before,
                        fish >= before);
                before = fish;
            }
            Check.assertTrue("the last level of chapter " + chapter + " has more fish than the first",
                    before > fishIn(LevelData.of(chapter, 1)));
        }
    }

    private static int fishIn(LevelData level) {
        int fish = 0;
        for (SpawnRule rule : level.getSpawnRules()) {
            if (!rule.getKind().isTrash()) {
                fish += rule.getSupply();
            }
        }
        return fish;
    }

    private static void rulesWakeUpOnTime() {
        Level level = new Level(LevelData.of(1, 1), new Random(1));
        level.update(0, RESTING_HOOK);
        Check.assertEquals("bottles are there from the start", 4, count(level, ObjectKind.PLASTIC_BOTTLE));
        Check.assertEquals("no fish at the start", 0, count(level, ObjectKind.SMALL_FISH));

        play(level, 5.9);
        Check.assertEquals("still no fish just before second 6", 0, count(level, ObjectKind.SMALL_FISH));
        play(level, 0.2);
        Check.assertEquals("the first fish arrives at second 6", 1, count(level, ObjectKind.SMALL_FISH));
        play(level, 6.0);
        Check.assertEquals("one more fish arrives at second 12", 2, count(level, ObjectKind.SMALL_FISH));
        play(level, 30);
        Check.assertEquals("fish are never replaced or added to", 2, count(level, ObjectKind.SMALL_FISH));
        Check.assertEquals("nothing was collected, so no bottle was added", 4, count(level, ObjectKind.PLASTIC_BOTTLE));
    }

    /** Level 2-1 holds four cans and six bottles. Lifting everything leaves the water empty of trash. */
    private static void trashRunsOutWhenTheSupplyIsUsedUp() {
        LevelData data = LevelData.of(2, 1);
        Level level = new Level(data, new Random(2));
        Map<ObjectKind, Integer> lifted = new EnumMap<>(ObjectKind.class);
        for (double t = 0; t < data.getTimeLimit(); t += STEP) {
            level.update(STEP, RESTING_HOOK);
            for (OceanObject object : new ArrayList<>(level.getObjects())) {
                if (object.getKind().isTrash() && object.isCatchable()) {
                    level.remove(object);
                    lifted.merge(object.getKind(), 1, Integer::sum);
                }
            }
        }
        Check.assertEquals("cans handed out in the whole level", 4, lifted.get(ObjectKind.CAN).intValue());
        Check.assertEquals("bottles handed out in the whole level", 6, lifted.get(ObjectKind.PLASTIC_BOTTLE).intValue());
        Check.assertEquals("no trash is left", 0,
                count(level, ObjectKind.CAN) + count(level, ObjectKind.PLASTIC_BOTTLE));
    }

    private static void newObjectsFadeInAwayFromTheHook() {
        for (int n = 1; n <= LevelData.LEVEL_COUNT; n++) {
            LevelData data = LevelData.byNumber(n);
            String name = "level " + data.getChapter() + "-" + data.getLevel();
            Level level = new Level(data, new Random(n));
            Set<OceanObject> seen = new HashSet<>();
            boolean allFarEnough = true;
            boolean allStartImmune = true;
            boolean neverCrowded = true;
            Random collector = new Random(99);

            for (double t = 0; t < 40; t += STEP) {
                level.update(STEP, RESTING_HOOK);
                neverCrowded = neverCrowded && level.getObjects().size() <= totalMax(data);
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
            Check.assertTrue(name + ": nothing appears next to the hook", allFarEnough);
            Check.assertTrue(name + ": new objects fade in and cannot be hooked yet", allStartImmune);
            Check.assertTrue(name + ": the water never holds more than its tables allow", neverCrowded);

            play(level, GameConstants.SPAWN_FADE_IN_S + 0.1);
            OceanObject oldest = level.getObjects().get(0);
            Check.assertTrue(name + ": after fading in, an object can be hooked", oldest.isCatchable());
            Check.assertEquals(name + ": and is fully visible", 1.0, oldest.getOpacity(), 1e-9);
        }
    }

    private static void tipsComeOnceAndInOrder() {
        Level level = new Level(LevelData.of(1, 1), new Random(1));
        List<String> shown = new ArrayList<>();
        List<Double> times = new ArrayList<>();
        for (double t = 0; t < 30; t += STEP) {
            level.update(STEP, RESTING_HOOK);
            for (String tip : level.pollTutorialPrompts()) {
                shown.add(tip);
                times.add(level.getElapsed());
            }
        }
        Check.assertEquals("level 1-1 tips, in order",
                List.of(StoryText.TUT_AIM, StoryText.TUT_FISH, StoryText.TUT_QUOTA), shown);
        Check.assertEquals("first tip at second 1", 1.0, times.get(0), STEP * 1.5);
        Check.assertEquals("second tip at second 8", 8.0, times.get(1), STEP * 1.5);
        Check.assertEquals("third tip at second 16", 16.0, times.get(2), STEP * 1.5);
    }

    // ---- helpers ----

    private static void play(Level level, double seconds) {
        for (double t = 0; t < seconds - 1e-9; t += STEP) {
            level.update(STEP, RESTING_HOOK);
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

    private static int totalMax(LevelData chapter) {
        int total = 0;
        for (SpawnRule rule : chapter.getSpawnRules()) {
            total += rule.getMaxCount();
        }
        return total;
    }
}
