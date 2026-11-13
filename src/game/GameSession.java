package game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import model.ChapterData;
import model.Loadout;

/**
 * One run through the story: which chapter the player is on, the chapters
 * already cleared, the badges earned, and how many attempts failed.
 *
 * A GameManager only lives for one chapter attempt. The session is the memory
 * that lasts across them.
 */
public final class GameSession {

    private final Random random;
    private final List<ChapterResult> clearedChapters = new ArrayList<>();
    private final Set<Badge> badges = EnumSet.noneOf(Badge.class);
    private int chapterNumber;
    private int retries;
    private ChapterResult lastResult;

    public GameSession(Random random) {
        this.random = random;
        this.chapterNumber = 1;
    }

    /** Starts an attempt at the current chapter. */
    public GameManager startChapter() {
        if (isFinished()) {
            throw new IllegalStateException("The story is already finished");
        }
        // Each chapter starts with the tools the crew has by then.
        return new GameManager(ChapterData.forChapter(chapterNumber), Loadout.forChapter(chapterNumber), random);
    }

    /**
     * Records how an attempt ended. A cleared chapter is banked, may earn
     * its badge, and the story moves on; a failed one only counts as a
     * retry, so nothing already won is ever lost.
     */
    public ChapterResult onChapterEnded(GameManager game) {
        lastResult = ChapterResult.from(game);
        if (lastResult.isCleared()) {
            clearedChapters.add(lastResult);
            Badge badge = Badge.forChapter(chapterNumber);
            if (badge.isEarnedBy(lastResult)) {
                badges.add(badge);
            }
            chapterNumber++;
        } else {
            retries++;
        }
        return lastResult;
    }

    /** True once the last chapter has been cleared. */
    public boolean isFinished() {
        return chapterNumber > ChapterData.CHAPTER_COUNT;
    }

    /** The chapter to play next (1 to 3). */
    public int getChapterNumber() {
        return chapterNumber;
    }

    public int getRetries() {
        return retries;
    }

    public ChapterResult getLastResult() {
        return lastResult;
    }

    public List<ChapterResult> getClearedChapters() {
        return Collections.unmodifiableList(clearedChapters);
    }

    public int getChaptersCleared() {
        return clearedChapters.size();
    }

    public boolean hasBadge(Badge badge) {
        return badges.contains(badge);
    }

    /** The badges earned so far, as a read-only set. */
    public Set<Badge> getBadges() {
        return Collections.unmodifiableSet(badges);
    }

    /** Points from every cleared chapter. */
    public int getTotalPoints() {
        int total = 0;
        for (ChapterResult result : clearedChapters) {
            total += result.getPoints();
        }
        return total;
    }

    /** The score of the whole run so far: cleared chapters, badges and failed attempts. */
    public int getRunScore() {
        return ScoreCalculator.runScore(clearedChapters, badges, retries);
    }

    /** Pieces of trash collected in the cleared chapters. */
    public int getTotalTrash() {
        int total = 0;
        for (ChapterResult result : clearedChapters) {
            total += result.getCatches();
        }
        return total;
    }

    /** Fish hooked by mistake in the cleared chapters. */
    public int getTotalFishHits() {
        int total = 0;
        for (ChapterResult result : clearedChapters) {
            total += result.getFishHits();
        }
        return total;
    }
}
