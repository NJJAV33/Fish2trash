package game;

import util.GameConstants;
import util.StoryText;

/** Little time left and less than half the quota: go for the pieces worth the most. */
public final class LowTimeRule implements HintRule {

    private static final int PRIORITY = 2;

    @Override
    public boolean isTriggered(SessionStats stats, GameView game) {
        boolean littleTime = game.getTimeLeft() <= GameConstants.HINT_LOW_TIME_S;
        boolean farBehind = game.getScore() * 2 < game.getQuota();
        return littleTime && farBehind;
    }

    @Override
    public int getPriority() {
        return PRIORITY;
    }

    @Override
    public String getText(SessionStats stats) {
        return StoryText.HINT_LOW_TIME;
    }
}
