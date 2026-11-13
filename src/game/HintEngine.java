package game;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import util.GameConstants;

/**
 * Decides when the player gets a hint.
 *
 * Every frame it asks each HintRule whether it is triggered and lets the most
 * urgent one speak. To keep hints helpful instead of nagging there is a pause
 * after each hint, a limit per chapter, and a rule that has spoken stays
 * quiet until its situation has passed and come back.
 */
public final class HintEngine {

    // POLYMORPHISM: five different rules, all used through the HintRule interface.
    private final List<HintRule> rules = List.of(
            new LowHealthRule(), new FishStreakRule(), new SlipStreakRule(), new LowTimeRule(), new IdleRule());
    private final Set<HintRule> spoken = new HashSet<>();   // rules that gave their hint and are still triggered
    private double cooldown;   // seconds until the next hint is allowed
    private int shown;         // hints given in this chapter attempt

    /**
     * Lets dt seconds pass and picks a hint if one is due.
     *
     * @return the hint to show now, or an empty text when there is none
     */
    public String update(double dt, SessionStats stats, GameView game) {
        cooldown = Math.max(0, cooldown - dt);

        HintRule mostUrgent = null;
        for (HintRule rule : rules) {
            if (!rule.isTriggered(stats, game)) {
                spoken.remove(rule);   // the situation has passed, so the rule may speak again next time
            } else if (!spoken.contains(rule)
                    && (mostUrgent == null || rule.getPriority() > mostUrgent.getPriority())) {
                mostUrgent = rule;
            }
        }

        if (mostUrgent == null || cooldown > 0 || shown >= GameConstants.HINT_MAX_PER_CHAPTER) {
            return "";   // a rule that had to wait is asked again on a later frame
        }
        spoken.add(mostUrgent);
        cooldown = GameConstants.HINT_COOLDOWN_S;
        shown++;
        return mostUrgent.getText(stats);
    }

    /** Hints given so far in this chapter attempt. */
    public int getShownCount() {
        return shown;
    }
}
