package game;

import java.util.EnumMap;
import java.util.Map;
import model.ObjectKind;

/**
 * Keeps count of what happened during a chapter attempt, including the
 * player's recent run of mistakes, which the hints look at.
 *
 * It is a GameListener: the GameManager does not update these numbers itself,
 * it only announces events and this class counts them (observer pattern).
 */
public final class SessionStats implements GameListener {

    private final Map<EventType, Integer> counts = new EnumMap<>(EventType.class);
    private int fishStreak;               // fish hooked in a row, with no trash caught in between
    private int slipStreak;               // wrong-tool touches in a row, with no trash caught in between
    private ObjectKind lastSlippedKind;   // what slipped off last; null until something has
    private double secondsSinceLaunch;    // how long the line has not been dropped

    @Override
    public void onGameEvent(GameEvent event) {
        EventType type = event.getType();
        counts.merge(type, 1, Integer::sum);   // add one to this event's count

        if (type == EventType.HEALTH_LOST) {
            fishStreak++;
        } else if (type == EventType.SLIPPED) {
            slipStreak++;
            lastSlippedKind = event.getKind();
        } else if (type == EventType.OBJECT_CAUGHT) {
            fishStreak = 0;   // a good catch ends both runs of mistakes
            slipStreak = 0;
        } else if (type == EventType.HOOK_LAUNCHED) {
            secondsSinceLaunch = 0;
        }
    }

    /** Lets dt seconds of play pass. Paused time never reaches here. */
    public void tick(double dt) {
        secondsSinceLaunch += dt;
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

    /** Fish hooked in a row since the last piece of trash was caught. */
    public int getFishStreak() {
        return fishStreak;
    }

    /** Wrong-tool touches in a row since the last piece of trash was caught. */
    public int getSlipStreak() {
        return slipStreak;
    }

    /** The kind of object that slipped off last. Only meaningful once getSlipStreak() is above 0. */
    public ObjectKind getLastSlippedKind() {
        return lastSlippedKind;
    }

    public double getSecondsSinceLaunch() {
        return secondsSinceLaunch;
    }
}
