package game;

import java.util.List;
import model.Boat;
import model.Difficulty;
import model.EquipmentType;
import model.LevelData;
import model.ObjectView;
import model.OceanEventType;
import model.Sea;

/**
 * What the screen is allowed to see of a running game.
 *
 * ABSTRACTION: the drawing code receives this interface, not the GameManager,
 * so it can read the game but cannot launch the hook or change the rules.
 */
public interface GameView {

    /** The level being played: its chapter, its number and its numbers. */
    LevelData getLevelData();

    /** The selected difficulty for this run. */
    Difficulty getDifficulty();

    Boat getBoat();

    List<? extends ObjectView> getObjects();

    /** The swell and the current right now. */
    Sea getSea();

    int getScore();

    int getQuota();

    int getHealth();

    int getMaxHealth();

    double getTimeLeft();

    double getTimeTotal();

    LevelStatus getStatus();

    /** Current consecutive trash catch streak. */
    int getCombo();

    /** Highest consecutive trash catch streak reached this attempt. */
    int getBestCombo();

    /** Extra run score earned from catch combos this attempt. */
    int getComboBonus();

    /** Currently active mid-game ocean event, or OceanEventType.NONE. */
    OceanEventType getActiveEvent();

    /** Remaining fraction of the active ocean event (1 down to 0). */
    double getActiveEventFraction();

    /** The tool on the line right now. */
    EquipmentType getSelectedTool();

    boolean isToolUnlocked(EquipmentType type);

    /** The wait after a tool change: 1 right after it, 0 when the next change is allowed. */
    double getSwapCooldownFraction();
}
