import game.ChapterStatus;
import game.GameManager;
import java.util.Random;
import model.ChapterData;
import model.Hook;
import model.Loadout;
import model.OceanObject;
import util.GameConstants;

/**
 * Lets a simple computer player play every chapter many times, to check that
 * the chapters can really be won with the numbers in ChapterData and
 * GameConstants - under the real rules, with no cheating.
 *
 * The bot is not clever: it picks a piece of trash, steers the boat over the
 * spot where that piece will be, and drops the hook when it hangs straight
 * down. It never looks at the fish. If this bot cannot win a chapter at least
 * half the time, the chapter is too hard and its numbers need changing.
 */
public final class BalanceBotTest {

    private static final double STEP = 0.016;            // one frame
    private static final int RUNS = 100;
    private static final int MUST_CLEAR = 50;            // of the 100 runs
    private static final double CLOSE_ENOUGH = 6;        // pixels: the boat is "over" the target
    private static final double STRAIGHT_DOWN = Math.toRadians(2.5);
    private static final int MAX_STEPS = 20000;          // safety limit, longer than any chapter

    private BalanceBotTest() {
        // only static methods
    }

    public static void run() {
        for (int chapter = 1; chapter <= ChapterData.CHAPTER_COUNT; chapter++) {
            int cleared = 0;
            int outOfTime = 0;
            double timeLeftTotal = 0;
            for (long seed = 1; seed <= RUNS; seed++) {
                GameManager game = new GameManager(
                        ChapterData.forChapter(chapter), Loadout.forChapter(chapter), new Random(seed));
                play(game);
                if (game.getStatus() == ChapterStatus.CLEARED) {
                    cleared++;
                    timeLeftTotal += game.getTimeLeft();
                } else if (game.getStatus() == ChapterStatus.FAILED_TIME) {
                    outOfTime++;
                }
            }

            double timeLimit = ChapterData.forChapter(chapter).getTimeLimit();
            int averageLeft = (cleared == 0) ? 0 : (int) (timeLeftTotal / cleared);
            System.out.println("  chapter " + chapter + ": bot cleared " + cleared + " of " + RUNS
                    + ", ran out of time " + outOfTime + " times, average "
                    + averageLeft + " s left of " + (int) timeLimit);

            Check.assertTrue("chapter " + chapter + " can be won: bot cleared " + cleared + " of " + RUNS,
                    cleared >= MUST_CLEAR);
            Check.assertTrue("chapter " + chapter + " leaves the bot enough time",
                    outOfTime <= RUNS / 4);
        }
    }

    /** Plays one chapter attempt to its end. */
    private static void play(GameManager game) {
        Hook hook = game.getBoat().getHook();
        OceanObject target = null;

        for (int i = 0; i < MAX_STEPS && game.isPlaying(); i++) {
            int steer = 0;
            if (hook.isSwinging()) {
                if (target == null || !target.isCatchable() || !game.getObjects().contains(target)) {
                    target = pickTarget(game);
                }
                if (target != null) {
                    double offset = aimX(target) - game.getBoat().getX();
                    if (Math.abs(offset) > CLOSE_ENOUGH) {
                        steer = (offset > 0) ? 1 : -1;
                    } else if (Math.abs(hook.getAngle()) < STRAIGHT_DOWN) {
                        game.spaceAction();   // drop the hook
                        target = null;        // choose again after this throw
                    }
                }
            }
            game.setBoatDirection(steer);
            game.update(STEP);
        }
    }

    /** The trash that gives the most points for the least travel. */
    private static OceanObject pickTarget(GameManager game) {
        OceanObject best = null;
        double bestWorth = -1;
        double boatX = game.getBoat().getX();
        for (OceanObject object : game.getObjects()) {
            if (!object.getKind().isTrash() || !object.isCatchable()) {
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
