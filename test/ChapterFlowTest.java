import game.ChapterStatus;
import game.EventType;
import game.GameEvent;
import game.GameManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import model.Loadout;
import model.TimeLimit;
import util.GameConstants;

/** Checks how a chapter is won and lost: quota, clock and health. */
public final class ChapterFlowTest {

    private static final double TINY = 1e-9;

    private ChapterFlowTest() {
        // only static methods
    }

    public static void run() {
        timeLimitCountsDownAndFreezes();
        quotaStopsTheClockAndDeliveryWins();
        runningOutOfTimeLoses();
        runningOutOfHealthLoses();
        nothingMovesAfterTheChapterEnds();
    }

    private static void timeLimitCountsDownAndFreezes() {
        TimeLimit limit = new TimeLimit(10);
        limit.tick(4);
        Check.assertEquals("time left after 4 s", 6.0, limit.getRemaining(), TINY);
        Check.assertEquals("fraction left", 0.6, limit.getFractionLeft(), TINY);
        Check.assertTrue("not up yet", !limit.isTimeUp());

        limit.freeze();
        limit.tick(100);
        Check.assertEquals("a frozen clock does not move", 6.0, limit.getRemaining(), TINY);

        TimeLimit other = new TimeLimit(10);
        other.tick(50);
        Check.assertEquals("time never goes below zero", 0.0, other.getRemaining(), TINY);
        Check.assertTrue("time is up", other.isTimeUp());
    }

    private static void quotaStopsTheClockAndDeliveryWins() {
        GameManager game = newGame();
        List<EventType> events = record(game);
        // Remember what the clock showed at the moment the quota was met.
        double[] timeAtQuota = {-1};
        game.addListener((GameEvent event) -> {
            if (event.getType() == EventType.QUOTA_REACHED) {
                timeAtQuota[0] = game.getTimeLeft();
            }
        });

        // Catch trash until the quota is met.
        for (int i = 0; i < 50 && game.isPlaying(); i++) {
            GameScript.hookOnto(game, GameScript.firstCatchable(game, true));
        }
        int quotaAt = events.indexOf(EventType.QUOTA_REACHED);
        int clearedAt = events.indexOf(EventType.CHAPTER_CLEARED);
        boolean sawQuotaBeforeClear = quotaAt >= 0 && clearedAt > quotaAt;

        Check.assertEquals("chapter is cleared", ChapterStatus.CLEARED, game.getStatus());
        Check.assertTrue("score reached the quota", game.getPlayer().getScore() >= game.getQuota());
        Check.assertTrue("quota is announced before the clear", sawQuotaBeforeClear);
        Check.assertEquals("the clear follows the last delivery",
                EventType.DELIVERED, events.get(clearedAt - 1));
        Check.assertEquals("quota is announced only once",
                quotaAt, events.lastIndexOf(EventType.QUOTA_REACHED));

        Check.assertTrue("time was left on the clock", game.getTimeLeft() > 0);
        Check.assertEquals("the clock stood still while the last catch was reeled in",
                timeAtQuota[0], game.getTimeLeft(), TINY);
    }

    private static void runningOutOfTimeLoses() {
        GameManager game = newGame();
        List<EventType> events = record(game);
        GameScript.clearTheWater(game);

        for (double t = 0; t < GameConstants.TIME_CH1_S + 1; t += 0.05) {
            game.update(0.05);
        }
        Check.assertEquals("out of time", ChapterStatus.FAILED_TIME, game.getStatus());
        Check.assertEquals("clock shows zero", 0.0, game.getTimeLeft(), TINY);
        Check.assertEquals("failure is announced once", List.of(EventType.CHAPTER_FAILED), events);
    }

    private static void runningOutOfHealthLoses() {
        GameManager game = newGame();
        for (int i = 0; i < GameConstants.HEALTH_CH1; i++) {
            Check.assertTrue("still playing before fish hit " + (i + 1), game.isPlaying());
            GameScript.hookOnto(game, GameScript.firstCatchable(game, false));
            // Wait until the fish can be hooked again.
            for (double t = 0; t < GameConstants.FISH_RELEASE_IMMUNITY_S + 0.1 && game.isPlaying(); t += 0.05) {
                game.update(0.05);
            }
        }
        Check.assertEquals("out of health", ChapterStatus.FAILED_HEALTH, game.getStatus());
        Check.assertEquals("health is zero", 0, game.getPlayer().getHealth());
    }

    private static void nothingMovesAfterTheChapterEnds() {
        GameManager game = newGame();
        GameScript.clearTheWater(game);
        for (double t = 0; t < GameConstants.TIME_CH1_S + 1; t += 0.05) {
            game.update(0.05);
        }
        List<EventType> events = record(game);
        double boatX = game.getBoat().getX();

        game.setBoatDirection(1);
        game.spaceAction();
        game.update(1.0);

        Check.assertEquals("boat does not move after the end", boatX, game.getBoat().getX(), TINY);
        Check.assertTrue("hook cannot be dropped after the end", game.getBoat().getHook().isSwinging());
        Check.assertTrue("no more events after the end", events.isEmpty());
    }

    private static GameManager newGame() {
        return new GameManager(Loadout.standard(), new Random(11));
    }

    private static List<EventType> record(GameManager game) {
        List<EventType> events = new ArrayList<>();
        game.addListener((GameEvent event) -> events.add(event.getType()));
        return events;
    }
}
