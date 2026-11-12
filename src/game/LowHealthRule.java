package game;

import util.GameConstants;
import util.StoryText;

/** One more fish ends the chapter: the most urgent hint there is. */
public final class LowHealthRule implements HintRule {

    private static final int PRIORITY = 5;

    @Override
    public boolean isTriggered(SessionStats stats, GameView game) {
        return game.getHealth() <= GameConstants.HINT_LOW_HEALTH;
    }

    @Override
    public int getPriority() {
        return PRIORITY;
    }

    @Override
    public String getText(SessionStats stats) {
        return StoryText.HINT_LOW_HEALTH;
    }
}
