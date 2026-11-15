import game.EventType;
import game.GameEvent;
import game.GameManager;
import game.LevelResult;
import game.GameSession;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import model.CatchOutcome;
import model.EquipmentType;
import model.Hook;
import model.ObjectKind;
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
        return firstCatchable(game, object -> object.getKind().isTrash() == trash, "trash=" + trash);
    }

    /** The first object of one kind that can be caught, playing on until there is one. */
    static OceanObject firstCatchable(GameManager game, ObjectKind kind) {
        return firstCatchable(game, object -> object.getKind() == kind, kind.toString());
    }

    private static OceanObject firstCatchable(GameManager game, Predicate<OceanObject> wanted, String what) {
        for (int i = 0; i < MAX_STEPS && game.isPlaying(); i++) {
            for (OceanObject object : game.getObjects()) {
                if (wanted.test(object) && object.isCatchable()) {
                    return object;
                }
            }
            game.update(STEP);
        }
        throw new IllegalStateException("no catchable object with " + what);
    }

    /**
     * Wins the level without a single mistake: lifts one piece of trash after
     * another, each with the tool made for it.
     */
    static void clearLevel(GameManager game) {
        for (int i = 0; i < 100 && game.isPlaying(); i++) {
            OceanObject trash = firstCatchable(game, true);
            putOnToolFor(game, trash);
            hookOnto(game, trash);
        }
    }

    /** Plays the session's current level without a mistake and records it. */
    static LevelResult clearCleanly(GameSession session) {
        GameManager game = session.startLevel();
        clearLevel(game);
        return session.onLevelEnded(game);
    }

    /** Clears levels without a mistake until the session stands at the last level of a chapter. */
    static void clearUpToFinale(GameSession session) {
        while (!session.getLevelData().isChapterFinale()) {
            clearCleanly(session);
        }
    }

    /** The tool the crew has that lifts this object, or the one on the line if none does. */
    static EquipmentType toolFor(GameManager game, OceanObject object) {
        for (EquipmentType type : EquipmentType.values()) {
            if (game.isToolUnlocked(type) && type.create().attempt(object) == CatchOutcome.CATCH) {
                return type;
            }
        }
        return game.getSelectedTool();
    }

    /** Puts the tool that lifts the object on the line, waiting out the pause between two tool changes. */
    static void putOnToolFor(GameManager game, OceanObject object) {
        EquipmentType wanted = toolFor(game, object);
        for (int i = 0; i < MAX_STEPS && game.isPlaying() && game.getSelectedTool() != wanted; i++) {
            if (game.getSwapCooldownFraction() == 0) {
                game.selectTool(wanted);
            } else {
                game.update(STEP);
            }
        }
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
