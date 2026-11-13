package game;

import util.GameConstants;
import util.StoryText;

/** Several fish hooked in a row: the player may not be telling fish from trash. */
public final class FishStreakRule implements HintRule {

    private static final int PRIORITY = 4;

    @Override
    public boolean isTriggered(SessionStats stats, GameView game) {
        return stats.getFishStreak() >= GameConstants.HINT_FISH_STREAK;
    }

    @Override
    public int getPriority() {
        return PRIORITY;
    }

    @Override
    public String getText(SessionStats stats) {
        return StoryText.HINT_FISH_STREAK;
    }
}
