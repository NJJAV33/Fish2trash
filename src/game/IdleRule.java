package game;

import util.GameConstants;
import util.StoryText;

/** The line has not been dropped for a while: nudge the player to try. */
public final class IdleRule implements HintRule {

    private static final int PRIORITY = 1;

    @Override
    public boolean isTriggered(SessionStats stats, GameView game) {
        return stats.getSecondsSinceLaunch() >= GameConstants.HINT_IDLE_S;
    }

    @Override
    public int getPriority() {
        return PRIORITY;
    }

    @Override
    public String getText(SessionStats stats) {
        return StoryText.HINT_IDLE;
    }
}
