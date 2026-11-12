package game;

/**
 * Something that wants to be told what happens in the game.
 *
 * OBSERVER PATTERN: the GameManager does not know who is listening (sound,
 * screen effects, statistics, tests...). It only calls onGameEvent on every
 * listener, so new reactions can be added without touching the game rules.
 */
public interface GameListener {

    void onGameEvent(GameEvent event);
}
