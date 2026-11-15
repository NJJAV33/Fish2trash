package game;

import java.util.List;
import model.Boat;
import model.LevelData;
import model.EquipmentType;
import model.ObjectView;

/**
 * What the screen is allowed to see of a running game.
 *
 * ABSTRACTION: the drawing code receives this interface, not the GameManager,
 * so it can read the game but cannot launch the hook or change the rules.
 */
public interface GameView {

    /** The level being played: its chapter, its number and its numbers. */
    LevelData getLevelData();

    Boat getBoat();

    List<? extends ObjectView> getObjects();

    int getScore();

    int getQuota();

    int getHealth();

    int getMaxHealth();

    double getTimeLeft();

    double getTimeTotal();

    LevelStatus getStatus();

    /** The tool on the line right now. */
    EquipmentType getSelectedTool();

    boolean isToolUnlocked(EquipmentType type);

    /** The wait after a tool change: 1 right after it, 0 when the next change is allowed. */
    double getSwapCooldownFraction();
}
