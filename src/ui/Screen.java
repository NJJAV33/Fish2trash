package ui;

import java.awt.Graphics2D;
import java.util.Collections;
import java.util.List;

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

    /** Short screen title announced to assistive technologies. */
    default String getAccessibleName() {
        return getClass().getSimpleName();
    }

    /** Human-readable status or instructions for assistive technologies. */
    default String getAccessibleDescription() {
        return "";
    }

    /** Interactive choices on this screen, in navigation order. */
    default List<String> getAccessibleChoices() {
        return Collections.emptyList();
    }

    /** Index of the active choice in {@link #getAccessibleChoices()}, or -1 when none is active. */
    default int getAccessibleSelectedIndex() {
        return -1;
    }

    /** Activates one choice from {@link #getAccessibleChoices()}. */
    default void activateAccessibleChoice(int index) {
    }
}
