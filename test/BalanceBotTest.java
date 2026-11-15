import game.Badge;
import game.EventType;
import game.GameManager;
import game.LevelResult;
import game.LevelStatus;
import java.util.EnumMap;
import java.util.Map;
import java.util.Random;
import model.Equipment;
import model.EquipmentType;
import model.Hook;
import model.LevelData;
import model.Loadout;
import model.ObjectKind;
import model.OceanObject;
import model.SpawnRule;
import util.GameConstants;

/**
 * Lets simple computer players play every level many times, to check the
 * numbers in LevelData and GameConstants under the real rules, with no
 * cheating. Four bots, four questions:
 *
 * - The careful bot plays like a player who has learned the game: it picks a
 *   piece of trash, puts the right tool on the line, steers over the spot,
 *   waits until no fish is about to cross the line, pulls the line back when
 *   a barracuda charges, and takes the Magnet off when a ray comes up. If it cannot win a level at least half
 *   the time, the level is too hard.
 * - The slow bot is the careful bot with a pause after every throw and a
 *   sloppy aim. It shows whether the clock is fair to a beginner.
 * - The blind bot never looks at the fish. It shows that the fish matter:
 *   in the last level of every chapter it must lose more often than it wins,
 *   and where there are stingrays its Magnet must get stung.
 * - The one-tool bot never changes tools. From chapter 2 on it must never
 *   win, which proves that changing tools is needed.
 */
public final class BalanceBotTest {

    private static final double STEP = 0.016;            // one frame
    private static final int RUNS = 50;
    private static final int ONE_TOOL_RUNS = 5;
    private static final int MAX_STEPS = 20000;          // safety limit, longer than any level
    private static final double CLOSE_ENOUGH = 6;        // pixels: the boat is "over" the target
    private static final double STRAIGHT_DOWN = Math.toRadians(2.5);
    private static final double SLOW_PAUSE_S = 1.5;      // the slow bot waits this long after every throw
    private static final double SLOW_AIM_ERROR = 30;     // and is up to this many pixels off target

    // How the careful bot judges whether a fish is about to cross the line.
    private static final double LOOK_AHEAD_STEP = 0.04;  // seconds between the moments it checks
    private static final double SIDE_MARGIN = 14;        // pixels of room it wants beside the line
    private static final double DEPTH_MARGIN = 26;       // and above and below (fish may weave)
    private static final double RECALL_RANGE = 150;      // a charging barracuda this close: pull the line back
    private static final double RAY_KEEP_AWAY = GameConstants.STINGRAY_SENSE_RANGE + 20;
    private static final double RAY_TOO_NEAR = 190;      // a sparking ray this close to the tool: change tools

    // One of each tool, only used to ask about a tool's size.
    private static final Map<EquipmentType, Equipment> SAMPLE_TOOLS = new EnumMap<>(EquipmentType.class);

    static {
        for (EquipmentType type : EquipmentType.values()) {
            SAMPLE_TOOLS.put(type, type.create());
        }
    }

    /** How a bot plays. */
    private static final class Style {

        private final String name;
        private final boolean looksAtFish;
        private final boolean changesTools;
        private final double pauseSeconds;   // how long it does nothing after each throw
        private final double aimError;       // how far off target it may steer, in pixels

        private Style(String name, boolean looksAtFish, boolean changesTools, double pauseSeconds, double aimError) {
            this.name = name;
            this.looksAtFish = looksAtFish;
            this.changesTools = changesTools;
            this.pauseSeconds = pauseSeconds;
            this.aimError = aimError;
        }
    }

    private static final Style CAREFUL = new Style("careful bot", true, true, 0, 0);
    private static final Style SLOW = new Style("slow bot", true, true, SLOW_PAUSE_S, SLOW_AIM_ERROR);
    private static final Style BLIND = new Style("blind bot", false, true, 0, 0);
    private static final Style ONE_TOOL = new Style("one-tool bot", true, false, 0, 0);

    /** What happened over the runs of one level. */
    private static final class Tally {

        private int runs;
        private int cleared;
        private int outOfTime;
        private int toolChanges;
        private int badges;
        private int healthLost;
        private int rayHits;
        private double timeLeftTotal;

        private String describe(Style style, LevelData level) {
            int averageLeft = (cleared == 0) ? 0 : (int) (timeLeftTotal / cleared);
            return String.format("  level %d-%d: %-12s cleared %2d of %d, out of time %2d, out of health %2d,"
                    + " %2d s left of %3d, %.1f health lost (%.1f ray hits) and %.1f tool changes per run",
                    level.getChapter(), level.getLevel(), style.name, cleared, runs, outOfTime,
                    runs - cleared - outOfTime, averageLeft, (int) level.getTimeLimit(),
                    (double) healthLost / runs, (double) rayHits / runs, (double) toolChanges / runs);
        }
    }

    private BalanceBotTest() {
        // only static methods
    }

    public static void run() {
        for (int number = 1; number <= LevelData.LEVEL_COUNT; number++) {
            LevelData level = LevelData.byNumber(number);
            String name = "level " + level.getChapter() + "-" + level.getLevel();

            Tally careful = playLevel(level, CAREFUL, RUNS);
            System.out.println(careful.describe(CAREFUL, level));
            Check.assertTrue(name + " can be won: the careful bot cleared " + careful.cleared + " of " + RUNS,
                    careful.cleared >= RUNS * 3 / 5);
            Check.assertTrue(name + " leaves the careful bot enough time: ran out " + careful.outOfTime + " times",
                    careful.outOfTime <= RUNS / 4);
            if (level.isChapterFinale()) {
                Check.assertTrue("the badge of chapter " + level.getChapter() + " can be earned", careful.badges > 0);
            }
            if (level.getChapter() >= 2) {
                Check.assertTrue(name + " makes the careful bot change tools", careful.toolChanges >= RUNS);
            }

            Tally slow = playLevel(level, SLOW, RUNS);
            System.out.println(slow.describe(SLOW, level));
            if (!level.isChapterFinale()) {
                // The last level of a chapter is meant to need practice; the others must be fair to a beginner.
                Check.assertTrue(name + " gives a slow, sloppy player a fair clock: ran out " + slow.outOfTime
                        + " times of " + RUNS, slow.outOfTime <= RUNS * 3 / 5);
            }

            Tally blind = playLevel(level, BLIND, RUNS);
            System.out.println(blind.describe(BLIND, level));
            Check.assertTrue(name + ": looking at the fish pays off (careful " + careful.cleared
                    + ", blind " + blind.cleared + ")", careful.cleared >= blind.cleared);
            if (level.isChapterFinale()) {
                Check.assertTrue(name + " cannot be won by ignoring the fish: the blind bot cleared "
                        + blind.cleared + " of " + RUNS, blind.cleared < RUNS / 2);
            }
            if (hasStingray(level)) {
                Check.assertTrue(name + ": a careless Magnet gets stung (" + blind.rayHits + " ray hits in " + RUNS
                        + " runs)", blind.rayHits > 0);
                Check.assertTrue(name + ": and a careful player can avoid it (" + careful.rayHits + " ray hits)",
                        careful.rayHits < blind.rayHits);
            }

            if (level.getChapter() >= 2) {
                Tally oneTool = playLevel(level, ONE_TOOL, ONE_TOOL_RUNS);
                Check.assertEquals(name + " cannot be won with one tool", 0, oneTool.cleared);
            }
        }
    }

    private static boolean hasStingray(LevelData level) {
        for (SpawnRule rule : level.getSpawnRules()) {
            if (rule.getKind() == ObjectKind.STINGRAY) {
                return true;
            }
        }
        return false;
    }

    /** Plays one level many times, each time with a different (but repeatable) game and aim. */
    private static Tally playLevel(LevelData level, Style style, int runs) {
        Tally tally = new Tally();
        tally.runs = runs;
        for (long seed = 1; seed <= runs; seed++) {
            GameManager game = new GameManager(level, Loadout.forChapter(level.getChapter()), new Random(seed));
            game.addListener(event -> {
                if (event.getType() == EventType.HEALTH_LOST && event.getKind() == ObjectKind.STINGRAY) {
                    tally.rayHits++;
                }
            });
            tally.toolChanges += play(game, style, new Random(seed * 31));
            tally.healthLost += game.getMaxHealth() - game.getHealth();
            if (game.getStatus() == LevelStatus.CLEARED) {
                tally.cleared++;
                tally.timeLeftTotal += game.getTimeLeft();
                if (Badge.forChapter(level.getChapter()).isEarnedBy(LevelResult.from(game))) {
                    tally.badges++;
                }
            } else if (game.getStatus() == LevelStatus.FAILED_TIME) {
                tally.outOfTime++;
            }
        }
        return tally;
    }

    /**
     * Plays one attempt at a level to its end.
     *
     * @param aim picks the error of each throw
     * @return how many times the bot changed tools
     */
    private static int play(GameManager game, Style style, Random aim) {
        Hook hook = game.getBoat().getHook();
        OceanObject target = null;
        double miss = 0;       // how far off this throw is aimed
        double pause = 0;      // time left of the pause after the last throw
        boolean lineWasOut = false;
        int toolChanges = 0;

        for (int i = 0; i < MAX_STEPS && game.isPlaying(); i++) {
            int steer = 0;
            if (lineWasOut && hook.isSwinging()) {
                pause = style.pauseSeconds;   // the line just came back
                target = null;                // choose again after every throw
            }
            lineWasOut = hook.isLineOut();

            if (hook.isExtending() && style.looksAtFish && aBarracudaIsCharging(game, hook)) {
                game.spaceAction();   // pull the line back
            } else if (hook.isSwinging() && style.looksAtFish && style.changesTools
                    && aRayIsComing(game, hook) && game.getSwapCooldownFraction() == 0) {
                game.selectTool(EquipmentType.HOOK);   // the ray loses interest and sinks
                toolChanges++;
                target = null;
            } else if (pause > 0) {
                pause -= STEP;
            } else if (hook.isSwinging()) {
                if (target == null || !target.isCatchable() || !game.getObjects().contains(target)
                        || (style.looksAtFish && rayIsTooClose(game, target))) {
                    target = pickTarget(game, style, true);
                    if (target == null) {
                        target = pickTarget(game, style, false);   // only risky pieces are left
                    }
                    miss = (aim.nextDouble() * 2 - 1) * style.aimError;
                }
                if (target != null && game.getSelectedTool() != GameScript.toolFor(game, target)) {
                    // Wrong tool on the line: change it (the game refuses while the last change is too recent).
                    if (game.getSwapCooldownFraction() == 0) {
                        game.selectTool(GameScript.toolFor(game, target));
                        toolChanges++;
                    }
                } else if (target != null) {
                    double dropX = aimX(target) + miss;
                    double offset = dropX - game.getBoat().getX();
                    if (Math.abs(offset) > CLOSE_ENOUGH) {
                        steer = (offset > 0) ? 1 : -1;
                    } else if (Math.abs(hook.getAngle()) < STRAIGHT_DOWN
                            && (!style.looksAtFish || laneIsClear(game, game.getBoat().getX(), target.getY()))) {
                        game.spaceAction();   // drop the line
                    }
                }
            }
            game.setBoatDirection(steer);
            game.update(STEP);
        }
        return toolChanges;
    }

    /**
     * The trash that gives the most points for the least travel. A bot that
     * never changes tools only looks at what its tool lifts; a bot that looks
     * at the fish leaves a can alone while a ray is near it, unless told
     * that risky pieces are fine too.
     */
    private static OceanObject pickTarget(GameManager game, Style style, boolean onlySafe) {
        OceanObject best = null;
        double bestWorth = -1;
        double boatX = game.getBoat().getX();
        for (OceanObject object : game.getObjects()) {
            if (!object.getKind().isTrash() || !object.isCatchable()) {
                continue;
            }
            if (!style.changesTools && GameScript.toolFor(game, object) != game.getSelectedTool()) {
                continue;
            }
            if (onlySafe && style.looksAtFish && rayIsTooClose(game, object)) {
                continue;
            }
            double travel = Math.abs(object.getX() - boatX) + (object.getY() - GameConstants.WATER_Y);
            double worth = object.getPoints() / (200 + travel);
            if (worth > bestWorth) {
                bestWorth = worth;
                best = object;
            }
        }
        return best;
    }

    /** True if lifting this piece needs the Magnet and a stingray would feel the Magnet above it. */
    private static boolean rayIsTooClose(GameManager game, OceanObject trash) {
        if (!SAMPLE_TOOLS.get(GameScript.toolFor(game, trash)).isMagnetic()) {
            return false;
        }
        for (OceanObject object : game.getObjects()) {
            if (object.getKind() == ObjectKind.STINGRAY && Math.abs(object.getX() - aimX(trash)) < RAY_KEEP_AWAY) {
                return true;
            }
        }
        return false;
    }

    /** True if the Magnet is on the line and a sparking ray is getting close to it. */
    private static boolean aRayIsComing(GameManager game, Hook hook) {
        if (!hook.isMagnetic()) {
            return false;
        }
        for (OceanObject object : game.getObjects()) {
            if (object.getKind() == ObjectKind.STINGRAY && object.getAlert() >= 1
                    && Math.hypot(object.getX() - hook.getTipX(), object.getY() - hook.getTipY()) < RAY_TOO_NEAR) {
                return true;
            }
        }
        return false;
    }

    /**
     * Looks ahead over the time the line needs to go down: true if no fish
     * will be near the line on the way. Fish are assumed to keep swimming
     * as they do now, which is what a player would guess too.
     */
    private static boolean laneIsClear(GameManager game, double dropX, double targetY) {
        double toolRadius = SAMPLE_TOOLS.get(game.getSelectedTool()).getTipRadius();
        double startY = GameConstants.WATER_Y + GameConstants.HOOK_REST;
        double flightSeconds = (targetY - startY) / GameConstants.HOOK_EXTEND;
        for (OceanObject fish : game.getObjects()) {
            if (fish.getKind().isTrash()) {
                continue;
            }
            double reach = fish.getRadius() + toolRadius;
            for (double t = 0; t <= flightSeconds; t += LOOK_AHEAD_STEP) {
                double tipY = startY + GameConstants.HOOK_EXTEND * t;
                double fishX = fish.getX() + fish.getSpeedX() * t;
                if (Math.abs(fishX - dropX) < reach + SIDE_MARGIN && Math.abs(fish.getY() - tipY) < reach + DEPTH_MARGIN) {
                    return false;
                }
            }
        }
        return true;
    }

    /** True if a barracuda near the tip is flashing or already charging. */
    private static boolean aBarracudaIsCharging(GameManager game, Hook hook) {
        for (OceanObject fish : game.getObjects()) {
            if (fish.getKind() == ObjectKind.BARRACUDA && fish.getAlert() >= 1
                    && Math.hypot(fish.getX() - hook.getTipX(), fish.getY() - hook.getTipY()) < RECALL_RANGE) {
                return true;
            }
        }
        return false;
    }

    /**
     * Where the boat should be to hit the target. The hook needs time to get
     * down there, and the target keeps drifting meanwhile, so aim ahead of it.
     */
    private static double aimX(OceanObject target) {
        double depth = target.getY() - GameConstants.WATER_Y;
        double flightSeconds = (depth - GameConstants.HOOK_REST) / GameConstants.HOOK_EXTEND;
        double predictedX = target.getX() + target.getSpeedX() * flightSeconds;
        return Math.max(GameConstants.BOAT_MIN_X, Math.min(GameConstants.BOAT_MAX_X, predictedX));
    }
}
