package game;

import java.util.Collection;
import java.util.List;
import util.GameConstants;

/**
 * Works out the score of a run. Points, time left and health left of every
 * cleared chapter count; badges add a bonus; every failed attempt costs
 * more than the one before.
 *
 * All the numbers are in GameConstants, so the scoring can be rebalanced
 * without touching this class.
 */
public final class ScoreCalculator {

    private ScoreCalculator() {
        // only static methods
    }

    /** What one chapter is worth. A failed attempt is worth nothing. */
    public static int chapterScore(ChapterResult result) {
        if (!result.isCleared()) {
            return 0;
        }
        return GameConstants.SCORE_POINT_MULT * result.getPoints()
                + GameConstants.TIME_BONUS_PER_S * (int) Math.floor(result.getTimeLeft())
                + GameConstants.HEALTH_BONUS_PER_HP * result.getHealthLeft();
    }

    /**
     * What the failed attempts cost together: 1 x base for the first,
     * 2 x base for the second, 3 x base for the third, and so on.
     */
    public static int retryPenalty(int retries) {
        return GameConstants.RETRY_PENALTY_BASE * retries * (retries + 1) / 2;
    }

    /** The score of a whole run. It never goes below zero. */
    public static int runScore(List<ChapterResult> clearedChapters, Collection<Badge> badges, int retries) {
        int total = 0;
        for (ChapterResult result : clearedChapters) {
            total += chapterScore(result);
        }
        for (Badge badge : badges) {
            total += badge.getBonus();
        }
        return Math.max(0, total - retryPenalty(retries));
    }
}
