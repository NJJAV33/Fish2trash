package game;

/**
 * One reason to give the player a hint.
 *
 * Each rule is its own small class (strategy pattern). The HintEngine holds
 * a list of them and treats them all alike, so a new kind of hint is a new
 * class and one more line in that list.
 */
public interface HintRule {

    /** True while the player is in the situation this rule watches for. */
    boolean isTriggered(SessionStats stats, GameView game);

    /** When several rules are triggered at once, the highest priority speaks. */
    int getPriority();

    /** The hint to show. Only asked while the rule is triggered. */
    String getText(SessionStats stats);
}
