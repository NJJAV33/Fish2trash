import game.EventType;
import game.GameEvent;
import game.GameManager;
import java.util.ArrayList;
import java.util.List;
import model.Hook;
import model.OceanObject;

/**
 * Helpers that "play" a game step by step, so tests can script a catch
 * without a window or a real player.
 */
final class GameScript {

    static final double STEP = 0.016;          // one frame
    private static final int MAX_STEPS = 5000; // safety limit, far more than any throw needs
    private static final double FAR_AWAY = 5000;

    private GameScript() {
        // only static methods
    }

    /**
     * The first object in the game that is (or is not) trash and can be caught.
     * Some objects only appear later in a chapter, and new ones fade in first,
     * so this plays on until one is ready.
     */
    static OceanObject firstCatchable(GameManager game, boolean trash) {
        for (int i = 0; i < MAX_STEPS && game.isPlaying(); i++) {
            for (OceanObject object : game.getObjects()) {
                if (object.getKind().isTrash() == trash && object.isCatchable()) {
                    return object;
                }
            }
            game.update(STEP);
        }
        throw new IllegalStateException("no catchable object with trash=" + trash);
    }

    /** Moves every object far below the screen, so the hook cannot touch any of them. */
    static void clearTheWater(GameManager game) {
        for (OceanObject object : game.getObjects()) {
            object.moveTo(object.getX(), FAR_AWAY);
        }
    }

    /**
     * Drops the hook onto the target and plays on until the hook is back at
     * the boat. Everything else is moved out of the way first.
     */
    static void hookOnto(GameManager game, OceanObject target) {
        clearTheWater(game);
        Hook hook = game.getBoat().getHook();
        game.spaceAction();
        for (int i = 0; i < MAX_STEPS && hook.isExtending(); i++) {
            target.moveTo(hook.getTipX(), hook.getTipY() + 10);   // keep it right under the tip
            game.update(STEP);
        }
        playUntilHookIsBack(game);
    }

    /**
     * Registers a listener that writes down every gameplay event.
     * Tips, hints and low-time ticks are left out: they are advice and
     * clock signals, not part of what the player did.
     */
    static List<GameEvent> record(GameManager game) {
        List<GameEvent> events = new ArrayList<>();
        game.addListener(event -> {
            if (event.getType() != EventType.TUTORIAL_PROMPT && event.getType() != EventType.HINT_SHOWN
                    && event.getType() != EventType.LOW_TIME_TICK) {
                events.add(event);
            }
        });
        return events;
    }

    static List<EventType> types(List<GameEvent> events) {
        List<EventType> types = new ArrayList<>();
        for (GameEvent event : events) {
            types.add(event.getType());
        }
        return types;
    }

    static void playUntilHookIsBack(GameManager game) {
        Hook hook = game.getBoat().getHook();
        for (int i = 0; i < MAX_STEPS && hook.isLineOut() && game.isPlaying(); i++) {
            game.update(STEP);
        }
    }
}
