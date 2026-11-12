import game.EventType;
import game.FishStreakRule;
import game.GameEvent;
import game.GameManager;
import game.ChapterStatus;
import game.HintEngine;
import game.HintRule;
import game.IdleRule;
import game.LowHealthRule;
import game.LowTimeRule;
import game.SessionStats;
import game.SlipStreakRule;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import model.ChapterData;
import model.EquipmentType;
import model.Loadout;
import model.ObjectKind;
import model.OceanObject;
import util.GameConstants;
import util.StoryText;

/**
 * Checks the hints: each rule fires exactly at its threshold, and the engine
 * keeps hints apart, puts the most urgent first and stops at the limit.
 */
public final class HintTest {

    private static final double STEP = GameScript.STEP;

    private HintTest() {
        // only static methods
    }

    public static void run() {
        fishStreakRuleFiresAtThreeInARow();
        slipStreakRuleNamesTheRightTool();
        idleRuleFiresWhenTheLineStaysIn();
        lowTimeRuleNeedsLittleTimeAndAPoorScore();
        lowHealthRuleFiresOnTheLastHealth();
        theMostUrgentHintGoesFirst();
        aRuleSpeaksAgainOnlyAfterItsSituationCameBack();
        hintsKeepTheirDistanceAndStopAtTheLimit();
        threeFishInARowBringAHintInARealGame();
        twoSlipsBringTheToolHintInARealGame();
    }

    private static GameManager newGame(int chapter) {
        return new GameManager(ChapterData.forChapter(chapter), Loadout.forChapter(chapter), new Random(5));
    }

    private static GameEvent fishHit() {
        return GameEvent.about(EventType.HEALTH_LOST, ObjectKind.SMALL_FISH.create(0, 0, 0), 1);
    }

    private static GameEvent slip(ObjectKind kind) {
        return GameEvent.about(EventType.SLIPPED, kind.create(0, 0, 0), 0);
    }

    private static GameEvent trashCatch() {
        return GameEvent.about(EventType.OBJECT_CAUGHT, ObjectKind.PLASTIC_BOTTLE.create(0, 0, 0), 10);
    }

    private static void fishStreakRuleFiresAtThreeInARow() {
        HintRule rule = new FishStreakRule();
        GameManager game = newGame(1);
        SessionStats stats = new SessionStats();

        for (int i = 1; i < GameConstants.HINT_FISH_STREAK; i++) {
            stats.onGameEvent(fishHit());
            Check.assertTrue(i + " fish in a row is not enough", !rule.isTriggered(stats, game));
        }
        stats.onGameEvent(fishHit());
        Check.assertTrue("the third fish in a row triggers the rule", rule.isTriggered(stats, game));
        Check.assertEquals("fish hint text", StoryText.HINT_FISH_STREAK, rule.getText(stats));

        stats.onGameEvent(trashCatch());
        Check.assertTrue("a good catch ends the run", !rule.isTriggered(stats, game));
        Check.assertEquals("all fish are still counted", GameConstants.HINT_FISH_STREAK, stats.getFishHits());
    }

    private static void slipStreakRuleNamesTheRightTool() {
        HintRule rule = new SlipStreakRule();
        GameManager game = newGame(3);

        SessionStats stats = new SessionStats();
        stats.onGameEvent(slip(ObjectKind.CAN));
        Check.assertTrue("one slip is not enough", !rule.isTriggered(stats, game));
        stats.onGameEvent(slip(ObjectKind.CAN));
        Check.assertTrue("the second slip in a row triggers the rule", rule.isTriggered(stats, game));
        Check.assertEquals("cans: the magnet", StoryText.HINT_GEAR_CAN, rule.getText(stats));

        stats.onGameEvent(slip(ObjectKind.FISHING_NET));
        Check.assertEquals("the hint follows the last thing that slipped", StoryText.HINT_GEAR_NET, rule.getText(stats));
        stats.onGameEvent(slip(ObjectKind.PLASTIC_BOTTLE));
        Check.assertEquals("bottles: the hook", StoryText.HINT_GEAR_PLASTIC, rule.getText(stats));
        stats.onGameEvent(slip(ObjectKind.SMALL_FISH));
        Check.assertEquals("anything else: the general reminder", StoryText.HINT_SLIP, rule.getText(stats));

        stats.onGameEvent(trashCatch());
        Check.assertTrue("a good catch ends the run", !rule.isTriggered(stats, game));
    }

    private static void idleRuleFiresWhenTheLineStaysIn() {
        HintRule rule = new IdleRule();
        GameManager game = newGame(1);
        SessionStats stats = new SessionStats();

        stats.tick(GameConstants.HINT_IDLE_S - 0.1);
        Check.assertTrue("just under the limit: no hint", !rule.isTriggered(stats, game));
        stats.tick(0.2);
        Check.assertTrue("over the limit: hint", rule.isTriggered(stats, game));
        stats.onGameEvent(GameEvent.of(EventType.HOOK_LAUNCHED));
        Check.assertTrue("dropping the line starts the count again", !rule.isTriggered(stats, game));
    }

    private static void lowTimeRuleNeedsLittleTimeAndAPoorScore() {
        HintRule rule = new LowTimeRule();
        GameManager game = newGame(1);
        SessionStats stats = new SessionStats();

        playWithEmptyWater(game, game.getTimeTotal() - GameConstants.HINT_LOW_TIME_S - 0.5);
        Check.assertTrue("a little more than 15 s left: no hint", !rule.isTriggered(stats, game));
        playWithEmptyWater(game, 1.0);
        Check.assertTrue("under 15 s left with no points: hint", rule.isTriggered(stats, game));

        game.getPlayer().addPoints(game.getQuota() / 2);
        Check.assertTrue("half the quota is not far behind", !rule.isTriggered(stats, game));
    }

    private static void lowHealthRuleFiresOnTheLastHealth() {
        HintRule rule = new LowHealthRule();
        GameManager game = newGame(1);
        SessionStats stats = new SessionStats();

        game.getPlayer().loseHealth(game.getMaxHealth() - 2);
        Check.assertTrue("two health left: no hint", !rule.isTriggered(stats, game));
        game.getPlayer().loseHealth(1);
        Check.assertTrue("one health left: hint", rule.isTriggered(stats, game));
    }

    private static void theMostUrgentHintGoesFirst() {
        HintEngine engine = new HintEngine();
        GameManager game = newGame(1);
        game.getPlayer().loseHealth(game.getMaxHealth() - 1);   // last health
        SessionStats stats = new SessionStats();
        for (int i = 0; i < GameConstants.HINT_FISH_STREAK; i++) {
            stats.onGameEvent(fishHit());                        // a run of fish
        }
        stats.tick(GameConstants.HINT_IDLE_S + 1);               // and a long wait

        Check.assertEquals("first the most urgent", StoryText.HINT_LOW_HEALTH, engine.update(STEP, stats, game));
        Check.assertEquals("nothing right after a hint", "", engine.update(STEP, stats, game));
        Check.assertEquals("nothing halfway through the quiet time", "",
                engine.update(GameConstants.HINT_COOLDOWN_S / 2, stats, game));
        Check.assertEquals("then the next most urgent", StoryText.HINT_FISH_STREAK,
                engine.update(GameConstants.HINT_COOLDOWN_S / 2, stats, game));
        Check.assertEquals("then the least urgent", StoryText.HINT_IDLE,
                engine.update(GameConstants.HINT_COOLDOWN_S, stats, game));
        Check.assertEquals("three hints given", GameConstants.HINT_MAX_PER_CHAPTER, engine.getShownCount());
    }

    private static void aRuleSpeaksAgainOnlyAfterItsSituationCameBack() {
        HintEngine engine = new HintEngine();
        GameManager game = newGame(1);
        SessionStats stats = new SessionStats();
        for (int i = 0; i < GameConstants.HINT_FISH_STREAK; i++) {
            stats.onGameEvent(fishHit());
        }
        Check.assertEquals("the run of fish gets its hint", StoryText.HINT_FISH_STREAK, engine.update(STEP, stats, game));

        stats.onGameEvent(fishHit());   // a fourth fish: same run, same mistake
        Check.assertEquals("the same run is not mentioned twice", "",
                engine.update(GameConstants.HINT_COOLDOWN_S, stats, game));

        stats.onGameEvent(trashCatch());
        Check.assertEquals("a good catch: nothing to say", "", engine.update(STEP, stats, game));
        for (int i = 0; i < GameConstants.HINT_FISH_STREAK; i++) {
            stats.onGameEvent(fishHit());
        }
        Check.assertEquals("a new run of fish gets a new hint", StoryText.HINT_FISH_STREAK, engine.update(STEP, stats, game));
    }

    /**
     * A whole chapter 3 with empty water and a player who drops the line only
     * now and then. The idle hint is due at 12 s, and again 12 s after each drop.
     */
    private static void hintsKeepTheirDistanceAndStopAtTheLimit() {
        GameManager game = newGame(3);
        List<Double> hintTimes = new ArrayList<>();
        List<String> hintTexts = new ArrayList<>();
        double[] now = {0};
        game.addListener(event -> {
            if (event.getType() == EventType.HINT_SHOWN) {
                hintTimes.add(now[0]);
                hintTexts.add(event.getText());
            }
        });

        double[] dropAt = {13, 33, 53, 73};
        int nextDrop = 0;
        while (game.isPlaying()) {
            GameScript.clearTheWater(game);
            if (nextDrop < dropAt.length && now[0] >= dropAt[nextDrop]) {
                game.spaceAction();
                nextDrop++;
            }
            game.update(STEP);
            now[0] += STEP;
        }

        Check.assertEquals("the chapter ran out of time", ChapterStatus.FAILED_TIME, game.getStatus());
        Check.assertEquals("hints in the whole chapter", GameConstants.HINT_MAX_PER_CHAPTER, hintTimes.size());
        Check.assertEquals("first hint after 12 idle seconds", 12.0, hintTimes.get(0), 0.1);
        // Idle again from second 25, but the quiet time after the first hint lasts until 32.
        Check.assertEquals("second hint waits for the quiet time", 32.0, hintTimes.get(1), 0.1);
        Check.assertEquals("third hint waits for the quiet time", 52.0, hintTimes.get(2), 0.1);
        // Idle again at 85 and out of time from 90: both stay unsaid, the limit is reached.
        Check.assertEquals("all three are the idle hint",
                List.of(StoryText.HINT_IDLE, StoryText.HINT_IDLE, StoryText.HINT_IDLE), hintTexts);
    }

    private static void threeFishInARowBringAHintInARealGame() {
        GameManager game = newGame(3);   // five health, so three small fish leave two
        List<String> hints = recordHints(game);

        for (int i = 1; i <= GameConstants.HINT_FISH_STREAK; i++) {
            Check.assertTrue("no hint before fish number " + i, hints.isEmpty());
            GameScript.hookOnto(game, firstCatchable(game, ObjectKind.SMALL_FISH));
        }
        game.update(STEP);
        Check.assertEquals("the hint after the third fish", List.of(StoryText.HINT_FISH_STREAK), hints);
        Check.assertEquals("health after three small fish", game.getMaxHealth() - 3, game.getHealth());
    }

    private static void twoSlipsBringTheToolHintInARealGame() {
        GameManager game = newGame(2);
        game.selectTool(EquipmentType.HOOK);   // the wrong tool for cans
        List<String> hints = recordHints(game);

        GameScript.hookOnto(game, firstCatchable(game, ObjectKind.CAN));
        Check.assertTrue("no hint after one slip", hints.isEmpty());
        GameScript.hookOnto(game, firstCatchable(game, ObjectKind.CAN));
        game.update(STEP);
        Check.assertEquals("the hint after the second slip", List.of(StoryText.HINT_GEAR_CAN), hints);
        Check.assertEquals("slips cost no health", game.getMaxHealth(), game.getHealth());
    }

    private static List<String> recordHints(GameManager game) {
        List<String> hints = new ArrayList<>();
        game.addListener(event -> {
            if (event.getType() == EventType.HINT_SHOWN) {
                hints.add(event.getText());
            }
        });
        return hints;
    }

    /** Plays on until an object of this kind can be caught, and returns it. */
    private static OceanObject firstCatchable(GameManager game, ObjectKind kind) {
        for (int i = 0; i < 5000 && game.isPlaying(); i++) {
            for (OceanObject object : game.getObjects()) {
                if (object.getKind() == kind && object.isCatchable()) {
                    return object;
                }
            }
            game.update(STEP);
        }
        throw new IllegalStateException("no catchable " + kind);
    }

    private static void playWithEmptyWater(GameManager game, double seconds) {
        for (double t = 0; t < seconds; t += STEP) {
            GameScript.clearTheWater(game);
            game.update(STEP);
        }
    }
}
