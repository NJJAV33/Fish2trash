package ui;

import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Remembers which keys are held down right now.
 *
 * Key events only say "pressed" or "released", but the boat must keep moving
 * for as long as a key is held. It also tells a fresh key press apart from
 * the repeats the keyboard sends while a key is held.
 */
final class InputState {

    // A held key makes the system send release + press pairs with (almost)
    // the same time stamp. A real new press comes noticeably later.
    private static final long REPEAT_GAP_MS = 5;

    private final Set<Integer> held = new HashSet<>();
    private final Map<Integer, Long> lastRelease = new HashMap<>();

    /**
     * Records that a key went down.
     *
     * @return true for a fresh press, false for an automatic repeat
     */
    boolean press(int keyCode, long when) {
        boolean alreadyHeld = !held.add(keyCode);
        Long released = lastRelease.get(keyCode);
        boolean rightAfterRelease = released != null && when - released <= REPEAT_GAP_MS;
        return !alreadyHeld && !rightAfterRelease;
    }

    void release(int keyCode, long when) {
        held.remove(keyCode);
        lastRelease.put(keyCode, when);
    }

    /** Forgets every held key, for when the window loses focus. */
    void releaseAll() {
        held.clear();
    }

    /** Steering: -1 = left, 1 = right, 0 = neither (or both at once). */
    int getDirection() {
        boolean left = held.contains(KeyEvent.VK_A) || held.contains(KeyEvent.VK_LEFT);
        boolean right = held.contains(KeyEvent.VK_D) || held.contains(KeyEvent.VK_RIGHT);
        return (right ? 1 : 0) - (left ? 1 : 0);
    }
}
