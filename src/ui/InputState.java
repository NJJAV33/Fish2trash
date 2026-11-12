package ui;

/**
 * Remembers which movement keys are held down right now.
 *
 * Key events only say "pressed" or "released", but the boat must keep moving
 * for as long as a key is held, so the panel stores the held keys here and
 * asks for the direction once per frame.
 */
final class InputState {

    private boolean leftHeld;
    private boolean rightHeld;

    void setLeftHeld(boolean held) {
        leftHeld = held;
    }

    void setRightHeld(boolean held) {
        rightHeld = held;
    }

    /** -1 = left, 1 = right, 0 = neither (or both at once). */
    int getDirection() {
        return (rightHeld ? 1 : 0) - (leftHeld ? 1 : 0);
    }
}
