import game.ChapterResult;
import game.FailCause;
import game.GameManager;
import game.GameSession;
import java.util.Random;
import model.ChapterData;

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
        int[] quotas = {100, 200, 350};

        for (int n = 1; n <= ChapterData.CHAPTER_COUNT; n++) {
            Check.assertEquals("playing chapter", n, session.getChapterNumber());
            Check.assertTrue("story not finished before chapter " + n, !session.isFinished());

            GameManager game = session.startChapter();
            Check.assertEquals("chapter " + n + " quota", quotas[n - 1], game.getQuota());
            clearChapter(game);
            ChapterResult result = session.onChapterEnded(game);

            Check.assertTrue("chapter " + n + " cleared", result.isCleared());
            Check.assertEquals("no fail cause on a clear", FailCause.NONE, result.getFailCause());
            Check.assertTrue("result keeps the points", result.getPoints() >= quotas[n - 1]);
            Check.assertEquals("every launch was a catch", result.getLaunches(), result.getCatches());
            Check.assertEquals("no fish were hooked", 0, result.getFishHits());
        }

        Check.assertTrue("story is finished", session.isFinished());
        Check.assertEquals("three chapters banked", 3, session.getChaptersCleared());
        Check.assertEquals("no retries", 0, session.getRetries());
        Check.assertTrue("total points cover all three quotas", session.getTotalPoints() >= 650);
        Check.assertTrue("trash was counted", session.getTotalTrash() >= 10 + 10 + 7);

        boolean refused = false;
        try {
            session.startChapter();
        } catch (IllegalStateException expected) {
            refused = true;
        }
        Check.assertTrue("no chapter can start after the story ends", refused);
    }

    private static void aFailedChapterIsRetriedAndKeepsEarlierWins() {
        GameSession session = new GameSession(new Random(4));
        GameManager first = session.startChapter();
        clearChapter(first);
        session.onChapterEnded(first);
        int bankedPoints = session.getTotalPoints();

        // Chapter 2: do nothing until the clock runs out.
        GameManager second = session.startChapter();
        while (second.isPlaying()) {
            second.update(0.05);
        }
        ChapterResult failed = session.onChapterEnded(second);

        Check.assertTrue("chapter 2 failed", !failed.isCleared());
        Check.assertEquals("it failed on time", FailCause.TIME, failed.getFailCause());
        Check.assertEquals("still on chapter 2", 2, session.getChapterNumber());
        Check.assertEquals("one retry counted", 1, session.getRetries());
        Check.assertEquals("chapter 1 stays banked", 1, session.getChaptersCleared());
        Check.assertEquals("banked points are kept", bankedPoints, session.getTotalPoints());

        // The retry is a fresh attempt at the same chapter.
        GameManager retry = session.startChapter();
        Check.assertEquals("retry starts at zero points", 0, retry.getPlayer().getScore());
        Check.assertEquals("retry has the full clock", retry.getTimeTotal(), retry.getTimeLeft(), 1e-9);
        clearChapter(retry);
        session.onChapterEnded(retry);
        Check.assertEquals("moves on to chapter 3 after the retry", 3, session.getChapterNumber());
    }

    private static void anUnfinishedChapterCannotBeRecorded() {
        GameSession session = new GameSession(new Random(5));
        GameManager game = session.startChapter();
        boolean refused = false;
        try {
            session.onChapterEnded(game);
        } catch (IllegalStateException expected) {
            refused = true;
        }
        Check.assertTrue("a running chapter has no result yet", refused);
        Check.assertEquals("nothing was recorded", 0, session.getRetries());
    }

    /** Hooks trash, one piece after another, until the chapter is over. */
    private static void clearChapter(GameManager game) {
        for (int i = 0; i < 100 && game.isPlaying(); i++) {
            GameScript.hookOnto(game, GameScript.firstCatchable(game, true));
        }
    }
}
