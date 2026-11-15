package game;

import util.GameConstants;
import util.StoryText;

/**
 * The badges of the story, one per chapter. A badge is decided in the last
 * level of its chapter: clearing that level is always needed, and the later
 * badges also ask for a clean or a fast clear.
 *
 * POLYMORPHISM: every badge answers meetsRule() in its own way, so awarding
 * a badge never needs an if-chain over the three of them.
 */
public enum Badge {

    /** Chapter 1: clearing its last level is enough. */
    ECO_ROOKIE(1, GameConstants.BADGE_BONUS_1,
            StoryText.BADGE_1_NAME, StoryText.BADGE_1_EARNED, StoryText.BADGE_1_LOCKED) {
        @Override
        boolean meetsRule(LevelResult result) {
            return true;
        }
    },

    /** Chapter 2: the right tool nearly every time, and most throws on target. */
    SORTER(2, GameConstants.BADGE_BONUS_2,
            StoryText.BADGE_2_NAME, StoryText.BADGE_2_EARNED, StoryText.BADGE_2_LOCKED) {
        @Override
        boolean meetsRule(LevelResult result) {
            return result.getSlips() <= GameConstants.SORTER_MAX_SLIPS
                    && result.getAccuracy() >= GameConstants.SORTER_MIN_ACCURACY;
        }
    },

    /** Chapter 3: plenty of time left, and hardly any health lost. */
    CHAMPION(3, GameConstants.BADGE_BONUS_3,
            StoryText.BADGE_3_NAME, StoryText.BADGE_3_EARNED, StoryText.BADGE_3_LOCKED) {
        @Override
        boolean meetsRule(LevelResult result) {
            return result.getTimeFractionLeft() >= GameConstants.CHAMPION_TIME_FRACTION
                    && result.getHealthLost() <= GameConstants.CHAMPION_MAX_HP_LOST;
        }
    };

    private final int chapter;
    private final int bonus;
    private final String displayName;
    private final String earnedText;
    private final String lockedText;

    Badge(int chapter, int bonus, String displayName, String earnedText, String lockedText) {
        this.chapter = chapter;
        this.bonus = bonus;
        this.displayName = displayName;
        this.earnedText = earnedText;
        this.lockedText = lockedText;
    }

    /** The badge's own condition, checked only for a cleared attempt at its chapter's last level. */
    abstract boolean meetsRule(LevelResult result);

    /** True if this attempt wins the badge: the last level of its chapter, cleared, and its own rule met. */
    public boolean isEarnedBy(LevelResult result) {
        return result.isCleared() && result.getChapter() == chapter && result.isChapterFinale()
                && meetsRule(result);
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

    /** What the badge adds to the run score. */
    public int getBonus() {
        return bonus;
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
