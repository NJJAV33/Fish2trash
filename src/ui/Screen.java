package ui;

import java.awt.Graphics2D;

/**
 * One screen of the game (menu, story, playing...).
 *
 * POLYMORPHISM: the ScreenManager holds the current Screen and calls these
 * methods on it without knowing which screen it is. Only paint() must be
 * written; the others do nothing unless a screen overrides them.
 */
public interface Screen {

    /** Called each time the screen is shown. */
    default void onEnter() {
    }

    /** Called when the screen is left. */
    default void onExit() {
    }

    /** Advances the screen by dt seconds. */
    default void update(double dt) {
    }

    void paint(Graphics2D g);

    /** A key went down. keyCode is one of the KeyEvent.VK_ constants. */
    default void keyPressed(int keyCode) {
    }

    /** A character was typed. For screens that take text. */
    default void keyTyped(char character) {
    }

    /** True while the screen wants letters as text, so that M (sound) is a letter there and not a command. */
    default boolean isTakingText() {
        return false;
    }
}
