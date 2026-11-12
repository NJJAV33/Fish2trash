package game;

import model.ObjectKind;
import model.ObjectView;

/**
 * One thing that happened in the game, sent to every GameListener.
 *
 * It is immutable: all fields are final and there are no setters, so a
 * listener can never change an event that another listener will also read.
 */
public final class GameEvent {

    private final EventType type;
    private final ObjectKind kind;   // the object involved, or null if there is none
    private final double x;
    private final double y;
    private final int value;         // points earned or health lost; 0 if not used
    private final String text;       // extra message; empty if not used

    private GameEvent(EventType type, ObjectKind kind, double x, double y, int value, String text) {
        this.type = type;
        this.kind = kind;
        this.x = x;
        this.y = y;
        this.value = value;
        this.text = text;
    }

    /** An event with no extra details. */
    public static GameEvent of(EventType type) {
        return new GameEvent(type, null, 0, 0, 0, "");
    }

    /** An event about one ocean object, at that object's position. */
    public static GameEvent about(EventType type, ObjectView object, int value) {
        return new GameEvent(type, object.getKind(), object.getX(), object.getY(), value, "");
    }

    /** An event that carries only a number. */
    public static GameEvent withValue(EventType type, int value) {
        return new GameEvent(type, null, 0, 0, value, "");
    }

    /** An event that carries a message. */
    public static GameEvent withText(EventType type, String text) {
        return new GameEvent(type, null, 0, 0, 0, text);
    }

    public EventType getType() {
        return type;
    }

    public ObjectKind getKind() {
        return kind;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public int getValue() {
        return value;
    }

    public String getText() {
        return text;
    }

    @Override
    public String toString() {
        return type + (kind == null ? "" : " " + kind) + (value == 0 ? "" : " " + value)
                + (text.isEmpty() ? "" : " \"" + text + "\"");
    }
}
