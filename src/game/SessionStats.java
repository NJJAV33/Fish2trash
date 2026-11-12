package game;

import java.util.EnumMap;
import java.util.Map;

/**
 * Keeps count of what happened during a chapter attempt.
 *
 * It is a GameListener: the GameManager does not update these numbers itself,
 * it only announces events and this class counts them (observer pattern).
 */
public final class SessionStats implements GameListener {

    private final Map<EventType, Integer> counts = new EnumMap<>(EventType.class);

    @Override
    public void onGameEvent(GameEvent event) {
        counts.merge(event.getType(), 1, Integer::sum);   // add one to this event's count
    }

    private int count(EventType type) {
        return counts.getOrDefault(type, 0);
    }

    public int getLaunches() {
        return count(EventType.HOOK_LAUNCHED);
    }

    /** Pieces of trash caught. */
    public int getCatches() {
        return count(EventType.OBJECT_CAUGHT);
    }

    /** Wrong-tool touches. */
    public int getSlips() {
        return count(EventType.SLIPPED);
    }

    /** Fish hooked by mistake. */
    public int getFishHits() {
        return count(EventType.HEALTH_LOST);
    }
}
