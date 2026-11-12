package game;

import java.util.EnumMap;
import java.util.Map;
import model.ObjectKind;
import util.GameConstants;
import util.StoryText;

/** Trash slipped off the wrong tool more than once: name the tool for that kind of trash. */
public final class SlipStreakRule implements HintRule {

    private static final int PRIORITY = 3;

    private final Map<ObjectKind, String> hintForKind = new EnumMap<>(ObjectKind.class);

    public SlipStreakRule() {
        hintForKind.put(ObjectKind.PLASTIC_BOTTLE, StoryText.HINT_GEAR_PLASTIC);
        hintForKind.put(ObjectKind.CAN, StoryText.HINT_GEAR_CAN);
        hintForKind.put(ObjectKind.FISHING_NET, StoryText.HINT_GEAR_NET);
    }

    @Override
    public boolean isTriggered(SessionStats stats, GameView game) {
        return stats.getSlipStreak() >= GameConstants.HINT_SLIP_STREAK;
    }

    @Override
    public int getPriority() {
        return PRIORITY;
    }

    @Override
    public String getText(SessionStats stats) {
        // The general reminder covers any kind without a hint of its own.
        return hintForKind.getOrDefault(stats.getLastSlippedKind(), StoryText.HINT_SLIP);
    }
}
