package game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import model.LevelData;
import model.Loadout;

/**
 * One run through the story: which level the player is on, the levels
 * already cleared, the badges earned, and how many attempts failed.
 *
 * A GameManager only lives for one attempt at a level. The session is the
 * memory that lasts across them.
 */
public final class GameSession {

    private final Random random;
    private final List<LevelResult> clearedLevels = new ArrayList<>();
    private final Set<Badge> badges = EnumSet.noneOf(Badge.class);
    private int levelNumber;   // 1 for level 1-1, up to 15 for level 3-5; 16 once the story is finished
    private int retries;
    private LevelResult lastResult;

    public GameSession(Random random) {
        this.random = random;
        this.levelNumber = 1;
    }

    /** Starts an attempt at the current level. */
    public GameManager startLevel() {
        LevelData level = getLevelData();
        // Tools belong to the chapter: every level starts with the tools the crew has by then.
        return new GameManager(level, Loadout.forChapter(level.getChapter()), random);
    }

    /**
     * Records how an attempt ended. A cleared level is banked and the story
     * moves on; the last level of a chapter may also earn the chapter's
     * badge. A failed attempt only counts as a retry, so nothing already
     * won is ever lost.
     */
    public LevelResult onLevelEnded(GameManager game) {
        lastResult = LevelResult.from(game);
        if (lastResult.isCleared()) {
            clearedLevels.add(lastResult);
            Badge badge = Badge.forChapter(lastResult.getChapter());
            if (badge.isEarnedBy(lastResult)) {
                badges.add(badge);
            }
            levelNumber++;
        } else {
            retries++;
        }
        return lastResult;
    }

    /** True once the last level has been cleared. */
    public boolean isFinished() {
        return levelNumber > LevelData.LEVEL_COUNT;
    }

    /** The level to play next. */
    public LevelData getLevelData() {
        if (isFinished()) {
            throw new IllegalStateException("The story is already finished");
        }
        return LevelData.byNumber(levelNumber);
    }

    public int getRetries() {
        return retries;
    }

    public LevelResult getLastResult() {
        return lastResult;
    }

    public List<LevelResult> getClearedLevels() {
        return Collections.unmodifiableList(clearedLevels);
    }

    public int getLevelsCleared() {
        return clearedLevels.size();
    }

    public boolean hasBadge(Badge badge) {
        return badges.contains(badge);
    }

    /** The badges earned so far, as a read-only set. */
    public Set<Badge> getBadges() {
        return Collections.unmodifiableSet(badges);
    }

    /** Points from every cleared level. */
    public int getTotalPoints() {
        int total = 0;
        for (LevelResult result : clearedLevels) {
            total += result.getPoints();
        }
        return total;
    }

    /** The score of the whole run so far: cleared levels, badges and failed attempts. */
    public int getRunScore() {
        return ScoreCalculator.runScore(clearedLevels, badges, retries);
    }

    /** Pieces of trash collected in the cleared levels. */
    public int getTotalTrash() {
        int total = 0;
        for (LevelResult result : clearedLevels) {
            total += result.getCatches();
        }
        return total;
    }

    /** Fish hit by mistake in the cleared levels. */
    public int getTotalFishHits() {
        int total = 0;
        for (LevelResult result : clearedLevels) {
            total += result.getFishHits();
        }
        return total;
    }
}
