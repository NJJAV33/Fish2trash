import game.GameManager;
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

    /** The first object in the game that is (or is not) trash and can still be caught. */
    static OceanObject firstCatchable(GameManager game, boolean trash) {
        for (OceanObject object : game.getObjects()) {
            if (object.getKind().isTrash() == trash && object.isCatchable()) {
                return object;
            }
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

    static void playUntilHookIsBack(GameManager game) {
        Hook hook = game.getBoat().getHook();
        for (int i = 0; i < MAX_STEPS && hook.isLineOut() && game.isPlaying(); i++) {
            game.update(STEP);
        }
    }
}
