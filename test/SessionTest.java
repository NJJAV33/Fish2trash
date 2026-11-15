import game.LevelResult;
import game.FailCause;
import game.GameManager;
import game.GameSession;
import java.util.Random;
import model.LevelData;

/** Checks the run through the story: moving on after a clear, retrying after a fail. */
public final class SessionTest {

    private SessionTest() {
        // only static methods
    }

    public static void run() {
        clearingEveryChapterFinishesTheStory();
        aFailedChapterIsRetriedAndKeepsEarlierWins();
        anUnfinishedChapterCannotBeRecorded();
    }

    private static void clearingEveryChapterFinishesTheStory() {
        GameSession session = new GameSession(new Random(3));
        int[] finalQuotas = {100, 200, 350};
        int quotaTotal = 0;

        for (int n = 1; n <= LevelData.LEVEL_COUNT; n++) {
            LevelData level = session.getLevelData();
            String name = "level " + level.getChapter() + "-" + level.getLevel();
            Check.assertEquals("the levels come in order", n, level.getNumber());
            Check.assertEquals(name + " is in the right chapter",
                    (n - 1) / LevelData.LEVELS_PER_CHAPTER + 1, level.getChapter());
            Check.assertTrue("story not finished before " + name, !session.isFinished());

            GameManager game = session.startLevel();
            Check.assertEquals(name + " quota", level.getQuota(), game.getQuota());
            if (level.isChapterFinale()) {
                Check.assertEquals("the last level of chapter " + level.getChapter() + " asks for the chapter's quota",
                        finalQuotas[level.getChapter() - 1], game.getQuota());
            }
            quotaTotal += game.getQuota();
            GameScript.clearLevel(game);
            LevelResult result = session.onLevelEnded(game);

            Check.assertTrue(name + " cleared", result.isCleared());
            Check.assertEquals("the result remembers its chapter", level.getChapter(), result.getChapter());
            Check.assertEquals("the result remembers its level", level.getLevel(), result.getLevel());
            Check.assertEquals("no fail cause on a clear", FailCause.NONE, result.getFailCause());
            Check.assertTrue("result keeps the points", result.getPoints() >= level.getQuota());
            Check.assertEquals("every launch was a catch", result.getLaunches(), result.getCatches());
            Check.assertEquals("no fish were hooked", 0, result.getFishHits());
        }

        Check.assertTrue("story is finished", session.isFinished());
        Check.assertEquals("fifteen levels banked", LevelData.LEVEL_COUNT, session.getLevelsCleared());
        Check.assertEquals("no retries", 0, session.getRetries());
        Check.assertTrue("total points cover every quota", session.getTotalPoints() >= quotaTotal);
        Check.assertTrue("trash was counted", session.getTotalTrash() >= 100);

        boolean refused = false;
        try {
            session.startLevel();
        } catch (IllegalStateException expected) {
            refused = true;
        }
        Check.assertTrue("no level can start after the story ends", refused);
    }

    private static void aFailedChapterIsRetriedAndKeepsEarlierWins() {
        GameSession session = new GameSession(new Random(4));
        GameManager first = session.startLevel();
        GameScript.clearLevel(first);
        session.onLevelEnded(first);
        int bankedPoints = session.getTotalPoints();

        // Level 1-2: do nothing until the clock runs out.
        GameManager second = session.startLevel();
        while (second.isPlaying()) {
            second.update(0.05);
        }
        LevelResult failed = session.onLevelEnded(second);

        Check.assertTrue("level 1-2 failed", !failed.isCleared());
        Check.assertEquals("it failed on time", FailCause.TIME, failed.getFailCause());
        Check.assertEquals("still on level 2", 2, session.getLevelData().getNumber());
        Check.assertEquals("one retry counted", 1, session.getRetries());
        Check.assertEquals("level 1-1 stays banked", 1, session.getLevelsCleared());
        Check.assertEquals("banked points are kept", bankedPoints, session.getTotalPoints());

        // The retry is a fresh attempt at the same level.
        GameManager retry = session.startLevel();
        Check.assertEquals("retry starts at zero points", 0, retry.getPlayer().getScore());
        Check.assertEquals("retry has the full clock", retry.getTimeTotal(), retry.getTimeLeft(), 1e-9);
        GameScript.clearLevel(retry);
        session.onLevelEnded(retry);
        Check.assertEquals("moves on to level 1-3 after the retry", 3, session.getLevelData().getNumber());
    }

    private static void anUnfinishedChapterCannotBeRecorded() {
        GameSession session = new GameSession(new Random(5));
        GameManager game = session.startLevel();
        boolean refused = false;
        try {
            session.onLevelEnded(game);
        } catch (IllegalStateException expected) {
            refused = true;
        }
        Check.assertTrue("a running chapter has no result yet", refused);
        Check.assertEquals("nothing was recorded", 0, session.getRetries());
    }
}
