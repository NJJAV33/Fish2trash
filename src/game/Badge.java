package game;

import util.GameConstants;
import util.StoryText;

/**
 * The badges of the story, one per chapter. Clearing the chapter is always
 * needed; the later badges also ask for a clean or a fast clear.
 *
 * POLYMORPHISM: every badge answers meetsRule() in its own way, so awarding
 * a badge never needs an if-chain over the three of them.
 */
public enum Badge {

    /** Chapter 1: clearing it is enough. */
    ECO_ROOKIE(1, StoryText.BADGE_1_NAME, StoryText.BADGE_1_EARNED, StoryText.BADGE_1_LOCKED) {
        @Override
        boolean meetsRule(ChapterResult result) {
            return true;
        }
    },

    /** Chapter 2: the right tool nearly every time, and most throws on target. */
    SORTER(2, StoryText.BADGE_2_NAME, StoryText.BADGE_2_EARNED, StoryText.BADGE_2_LOCKED) {
        @Override
        boolean meetsRule(ChapterResult result) {
            return result.getSlips() <= GameConstants.SORTER_MAX_SLIPS
                    && result.getAccuracy() >= GameConstants.SORTER_MIN_ACCURACY;
        }
    },

    /** Chapter 3: plenty of time left, and hardly any health lost. */
    CHAMPION(3, StoryText.BADGE_3_NAME, StoryText.BADGE_3_EARNED, StoryText.BADGE_3_LOCKED) {
        @Override
        boolean meetsRule(ChapterResult result) {
            return result.getTimeFractionLeft() >= GameConstants.CHAMPION_TIME_FRACTION
                    && result.getHealthLost() <= GameConstants.CHAMPION_MAX_HP_LOST;
        }
    };

    private final int chapter;
    private final String displayName;
    private final String earnedText;
    private final String lockedText;

    Badge(int chapter, String displayName, String earnedText, String lockedText) {
        this.chapter = chapter;
        this.displayName = displayName;
        this.earnedText = earnedText;
        this.lockedText = lockedText;
    }

    /** The badge's own condition, checked only for a cleared attempt at its chapter. */
    abstract boolean meetsRule(ChapterResult result);

    /** True if this attempt wins the badge: its chapter, cleared, and its own rule met. */
    public boolean isEarnedBy(ChapterResult result) {
        return result.isCleared() && result.getChapter() == chapter && meetsRule(result);
    }

    /** The badge that chapter 1, 2 or 3 can award. */
    public static Badge forChapter(int chapter) {
        for (Badge badge : values()) {
            if (badge.chapter == chapter) {
                return badge;
            }
        }
        throw new IllegalArgumentException("No badge for chapter " + chapter);
    }

    public int getChapter() {
        return chapter;
    }

    public String getDisplayName() {
        return displayName;
    }

    /** The line shown with the badge once it is earned. */
    public String getEarnedText() {
        return earnedText;
    }

    /** What the player has to do to earn it. */
    public String getLockedText() {
        return lockedText;
    }
}
