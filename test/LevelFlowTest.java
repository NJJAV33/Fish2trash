import game.LevelStatus;
import game.EventType;
import game.GameEvent;
import game.GameManager;
import game.GameSession;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import model.LevelData;
import model.Loadout;
import model.ObjectKind;
import model.OceanObject;
import model.TimeLimit;
import ui.render.HudPainter;
import ui.render.PainterRegistry;
import util.GameConstants;
import util.SoundState;

/** Checks how a chapter is won and lost: quota, clock and health. */
public final class LevelFlowTest {

    private static final double TINY = 1e-9;

    private LevelFlowTest() {
        // only static methods
    }

    public static void run() {
        timeLimitCountsDownAndFreezes();
        quotaStopsTheClockAndDeliveryWins();
        frozenTimerMatchesReportAndDoesNotPulseRed();
        runningOutOfTimeLoses();
        runningOutOfHealthLoses();
        nothingMovesAfterTheChapterEnds();
    }

    private static void frozenTimerMatchesReportAndDoesNotPulseRed() {
        GameManager game = newGame();
        while (game.getTimeLeft() > 14.0) {
            game.update(0.05);
        }
        for (int i = 0; i < 3; i++) {
            GameScript.hookOnto(game, GameScript.firstCatchable(game, ObjectKind.PLASTIC_BOTTLE));
        }
        while (game.getTimeLeft() > 4.45) {
            game.update(0.05);
        }

        // Catch the quota bottle at ~4.4 s remaining and inspect the HUD mid-retraction.
        OceanObject winning = GameScript.firstCatchable(game, ObjectKind.PLASTIC_BOTTLE);
        GameScript.clearTheWater(game);
        game.getObjects().stream().count(); // no-op readability
        double tipX = game.getBoat().getHook().getTipX();
        game.spaceAction();
        for (int i = 0; i < 400 && !game.isTimeFrozen(); i++) {
            winning.moveTo(game.getBoat().getHook().getTipX(), Math.max(game.getBoat().getHook().getTipY() + 8, 250));
            game.update(0.02);
        }
        Check.assertTrue("clock freezes as soon as the quota bottle is hooked (tipX=" + tipX + ")", game.isTimeFrozen());
        Check.assertEquals("whole-second floor matches report for fractional time < 5",
                4, (int) Math.floor(game.getTimeLeft()));

        HudPainter hud = new HudPainter(new PainterRegistry());
        int[] firstTimerBox = timerBoxPixels(hud, game, 0.0);
        int[] quarterPhase = timerBoxPixels(hud, game, 0.25);
        Check.assertTrue("frozen timer stops pulsing red warning colors", Arrays.equals(firstTimerBox, quarterPhase));

        for (int i = 0; i < 500 && game.isPlaying(); i++) {
            game.update(0.02);
        }
        Check.assertEquals("delivery finishes the level", LevelStatus.CLEARED, game.getStatus());
        Check.assertEquals("session report sees the exact same floored seconds", 4,
                (int) Math.floor(new GameSession(new Random(11), 1).onLevelEnded(game).getTimeLeft()));
    }

    private static int[] timerBoxPixels(HudPainter hud, GameManager game, double phase) {
        BufferedImage img = new BufferedImage(
                GameConstants.WORLD_WIDTH, GameConstants.WORLD_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        hud.paint(g, game, phase, SoundState.ON, 0);
        g.dispose();
        return img.getRGB(580, 0, 120, 56, null, 0, 120);
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
        List<GameEvent> events = GameScript.record(game);
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
        List<EventType> types = GameScript.types(events);
        int quotaAt = types.indexOf(EventType.QUOTA_REACHED);
        int clearedAt = types.indexOf(EventType.LEVEL_CLEARED);
        boolean sawQuotaBeforeClear = quotaAt >= 0 && clearedAt > quotaAt;

        Check.assertEquals("chapter is cleared", LevelStatus.CLEARED, game.getStatus());
        Check.assertTrue("score reached the quota", game.getPlayer().getScore() >= game.getQuota());
        Check.assertTrue("quota is announced before the clear", sawQuotaBeforeClear);
        Check.assertEquals("the clear follows the last delivery",
                EventType.DELIVERED, types.get(clearedAt - 1));
        Check.assertEquals("quota is announced only once",
                quotaAt, types.lastIndexOf(EventType.QUOTA_REACHED));

        Check.assertTrue("time was left on the clock", game.getTimeLeft() > 0);
        Check.assertEquals("the clock stood still while the last catch was reeled in",
                timeAtQuota[0], game.getTimeLeft(), TINY);
    }

    private static void runningOutOfTimeLoses() {
        GameManager game = newGame();
        List<GameEvent> events = GameScript.record(game);
        GameScript.clearTheWater(game);

        for (double t = 0; t < game.getTimeTotal() + 1; t += 0.05) {
            game.update(0.05);
        }
        Check.assertEquals("out of time", LevelStatus.FAILED_TIME, game.getStatus());
        Check.assertEquals("clock shows zero", 0.0, game.getTimeLeft(), TINY);
        Check.assertEquals("failure is announced once", List.of(EventType.LEVEL_FAILED), GameScript.types(events));
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
        Check.assertEquals("out of health", LevelStatus.FAILED_HEALTH, game.getStatus());
        Check.assertEquals("health is zero", 0, game.getPlayer().getHealth());
    }

    private static void nothingMovesAfterTheChapterEnds() {
        GameManager game = newGame();
        GameScript.clearTheWater(game);
        for (double t = 0; t < game.getTimeTotal() + 1; t += 0.05) {
            game.update(0.05);
        }
        List<GameEvent> events = GameScript.record(game);
        double boatX = game.getBoat().getX();

        game.setBoatDirection(1);
        game.spaceAction();
        game.update(1.0);

        Check.assertEquals("boat does not move after the end", boatX, game.getBoat().getX(), TINY);
        Check.assertTrue("hook cannot be dropped after the end", game.getBoat().getHook().isSwinging());
        Check.assertTrue("no more events after the end", events.isEmpty());
    }

    private static GameManager newGame() {
        return new GameManager(LevelData.of(1, 1), Loadout.forChapter(1), new Random(11));
    }
}
