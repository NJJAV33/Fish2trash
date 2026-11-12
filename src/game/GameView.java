package game;

import java.util.List;
import model.Boat;
import model.ChapterData;
import model.ObjectView;

/**
 * What the screen is allowed to see of a running game.
 *
 * ABSTRACTION: the drawing code receives this interface, not the GameManager,
 * so it can read the game but cannot launch the hook or change the rules.
 */
public interface GameView {

    ChapterData getChapter();

    Boat getBoat();

    List<? extends ObjectView> getObjects();

    int getScore();

    int getQuota();

    int getHealth();

    int getMaxHealth();

    double getTimeLeft();

    double getTimeTotal();

    ChapterStatus getStatus();
}
