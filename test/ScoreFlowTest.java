import game.Badge;
import game.ChapterResult;
import game.FailCause;
import game.GameManager;
import game.GameSession;
import game.ScoreCalculator;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import model.ChapterData;
import util.GameConstants;

/**
 * Checks the run score: what a cleared chapter is worth, what badges add,
 * and what failed attempts cost. The expected numbers are worked out from
 * GameConstants, so the test still holds after the scoring is rebalanced.
 */
public final class ScoreFlowTest {

    private ScoreFlowTest() {
        // only static methods
    }

    public static void run() {
        aClearedChapterScoresPointsTimeAndHealth();
        aFailedChapterScoresNothing();
        everyRetryCostsMoreThanTheLast();
        badgesAddTheirBonus();
        theRunScoreNeverGoesBelowZero();
        aCleanRunScoresItsChaptersAndBadges();
        aFailedAttemptLowersTheRunScore();
    }

    private static void aClearedChapterScoresPointsTimeAndHealth() {
        ChapterResult result = cleared(1, 120, 30.9, 2);
        int expected = GameConstants.SCORE_POINT_MULT * 120
                + GameConstants.TIME_BONUS_PER_S * 30      // 30.9 seconds left: only whole seconds count
                + GameConstants.HEALTH_BONUS_PER_HP * 2;
        Check.assertEquals("points, whole seconds left and health left", expected, ScoreCalculator.chapterScore(result));

        int base = ScoreCalculator.chapterScore(cleared(1, 100, 20, 2));
        Check.assertTrue("more points score higher", ScoreCalculator.chapterScore(cleared(1, 110, 20, 2)) > base);
        Check.assertTrue("more time left scores higher", ScoreCalculator.chapterScore(cleared(1, 100, 21, 2)) > base);
        Check.assertTrue("more health left scores higher", ScoreCalculator.chapterScore(cleared(1, 100, 20, 3)) > base);
    }

    private static void aFailedChapterScoresNothing() {
        ChapterResult failed = new ChapterResult(2, FailCause.HEALTH, 180, 40, 90, 0, 4, 12, 9, 0, 4);
        Check.assertEquals("a failed attempt is worth nothing, whatever it collected",
                0, ScoreCalculator.chapterScore(failed));
    }

    private static void everyRetryCostsMoreThanTheLast() {
        Check.assertEquals("no failed attempt, no cost", 0, ScoreCalculator.retryPenalty(0));
        Check.assertEquals("the first costs the base amount",
                GameConstants.RETRY_PENALTY_BASE, ScoreCalculator.retryPenalty(1));
        int previousStep = 0;
        for (int retries = 1; retries <= 5; retries++) {
            int step = ScoreCalculator.retryPenalty(retries) - ScoreCalculator.retryPenalty(retries - 1);
            Check.assertTrue("failed attempt " + retries + " costs more than the one before", step > previousStep);
            previousStep = step;
        }
    }

    private static void badgesAddTheirBonus() {
        List<ChapterResult> chapters = Arrays.asList(cleared(1, 100, 30, 3), cleared(2, 200, 40, 4));
        int plain = ScoreCalculator.runScore(chapters, Collections.emptySet(), 0);
        Check.assertEquals("without badges the run is the sum of its chapters",
                ScoreCalculator.chapterScore(chapters.get(0)) + ScoreCalculator.chapterScore(chapters.get(1)), plain);

        int total = plain;
        int previousBonus = 0;
        for (Badge badge : Badge.values()) {
            Check.assertEquals(badge + " adds its bonus", plain + badge.getBonus(),
                    ScoreCalculator.runScore(chapters, EnumSet.of(badge), 0));
            Check.assertTrue(badge + " is worth more than the badge before it", badge.getBonus() > previousBonus);
            previousBonus = badge.getBonus();
            total += badge.getBonus();
        }
        Check.assertEquals("all badges add up", total,
                ScoreCalculator.runScore(chapters, EnumSet.allOf(Badge.class), 0));
        Check.assertEquals("failed attempts are taken off", total - ScoreCalculator.retryPenalty(2),
                ScoreCalculator.runScore(chapters, EnumSet.allOf(Badge.class), 2));
    }

    private static void theRunScoreNeverGoesBelowZero() {
        Check.assertEquals("failing before clearing anything scores zero, not less",
                0, ScoreCalculator.runScore(Collections.emptyList(), Collections.emptySet(), 3));
        Check.assertEquals("a small run with many failed attempts stops at zero",
                0, ScoreCalculator.runScore(Arrays.asList(cleared(1, 100, 0, 1)), Collections.emptySet(), 20));
    }

    // ---- Whole runs ----

    private static void aCleanRunScoresItsChaptersAndBadges() {
        GameSession session = new GameSession(new Random(31));
        Check.assertEquals("a new run starts at zero", 0, session.getRunScore());

        int expected = 0;
        for (int chapter = 1; chapter <= ChapterData.CHAPTER_COUNT; chapter++) {
            GameManager game = session.startChapter();
            GameScript.clearChapter(game);
            ChapterResult result = session.onChapterEnded(game);
            expected += ScoreCalculator.chapterScore(result) + Badge.forChapter(chapter).getBonus();
            Check.assertEquals("the run score after chapter " + chapter, expected, session.getRunScore());
        }
        Check.assertTrue("a clean run is worth more than its trash points alone",
                session.getRunScore() > GameConstants.SCORE_POINT_MULT * session.getTotalPoints());
    }

    private static void aFailedAttemptLowersTheRunScore() {
        GameSession session = new GameSession(new Random(32));
        GameManager first = session.startChapter();
        while (first.isPlaying()) {
            first.update(0.05);   // do nothing until the clock runs out
        }
        session.onChapterEnded(first);
        Check.assertEquals("a run that only failed scores zero", 0, session.getRunScore());

        GameManager retry = session.startChapter();
        GameScript.clearChapter(retry);
        ChapterResult result = session.onChapterEnded(retry);
        int withoutTheFail = ScoreCalculator.chapterScore(result) + Badge.ECO_ROOKIE.getBonus();
        Check.assertEquals("the failed attempt is taken off the cleared chapter",
                withoutTheFail - ScoreCalculator.retryPenalty(1), session.getRunScore());
    }

    /** A cleared chapter with made-up numbers on a 100 second clock with 5 health. */
    private static ChapterResult cleared(int chapter, int points, double timeLeft, int healthLeft) {
        return new ChapterResult(chapter, FailCause.NONE, points, timeLeft, 100, healthLeft, 5, 12, 10, 0, 0);
    }
}
