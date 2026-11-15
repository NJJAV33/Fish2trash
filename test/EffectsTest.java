import game.EventType;
import game.GameEvent;
import game.GameManager;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Random;
import model.LevelData;
import model.Loadout;
import model.OceanObject;
import ui.render.EffectsSystem;
import util.GameConstants;

/** Checks the visual feedback: it reacts to the right events, ends on time and stays in its pool. */
public final class EffectsTest {

    private static final double STEP = 0.016;

    private EffectsTest() {
        // only static methods
    }

    public static void run() {
        GameManager game = new GameManager(LevelData.of(1, 1), Loadout.forChapter(1), new Random(1));
        OceanObject something = game.getObjects().get(0);
        EffectsSystem effects = new EffectsSystem();
        effects.reset(game);

        Check.assertEquals("nothing on screen at the start", 0, effects.getActiveCount());
        Check.assertEquals("no shake at the start", 0.0, effects.getShakeX(), 1e-9);

        // Catching trash: one popup, no shake.
        effects.onGameEvent(GameEvent.about(EventType.OBJECT_CAUGHT, something, 10));
        Check.assertEquals("a catch shows one popup", 1, effects.getActiveCount());
        Check.assertEquals("a catch does not shake the screen", 0.0, effects.getShakeX(), 1e-9);
        play(effects, 1.0);
        Check.assertEquals("the popup is gone after a second", 0, effects.getActiveCount());

        // Hooking a fish: popup plus a shake that dies away.
        effects.onGameEvent(GameEvent.about(EventType.HEALTH_LOST, something, 1));
        effects.update(STEP);
        boolean shaking = Math.abs(effects.getShakeX()) + Math.abs(effects.getShakeY()) > 1;
        Check.assertTrue("hooking a fish shakes the screen", shaking);
        play(effects, 0.5);
        Check.assertEquals("the shake stops", 0.0, effects.getShakeX(), 1e-9);
        Check.assertEquals("and stops in both directions", 0.0, effects.getShakeY(), 1e-9);

        // A slip throws sparks; reaching the quota throws stars.
        play(effects, 1.0);
        effects.onGameEvent(GameEvent.about(EventType.SLIPPED, something, 0));
        Check.assertEquals("a slip throws eight sparks", 8, effects.getActiveCount());
        play(effects, 1.0);
        effects.onGameEvent(GameEvent.of(EventType.QUOTA_REACHED));
        Check.assertEquals("reaching the quota throws 24 stars", 24, effects.getActiveCount());

        // Events it has no reaction for change nothing.
        effects.onGameEvent(GameEvent.of(EventType.MISSED));
        Check.assertEquals("other events are ignored", 24, effects.getActiveCount());

        // A flood of events cannot overflow the pool, and drawing a full pool works.
        for (int i = 0; i < 200; i++) {
            effects.onGameEvent(GameEvent.about(EventType.OBJECT_CAUGHT, something, 10));
        }
        Check.assertEquals("the pool never holds more than its size",
                EffectsSystem.MAX_EFFECTS, effects.getActiveCount());
        BufferedImage image = new BufferedImage(
                GameConstants.WORLD_WIDTH, GameConstants.WORLD_HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        effects.paint(g);
        effects.paintFlash(g);
        g.dispose();

        effects.reset(game);
        Check.assertEquals("a new chapter starts with a clean screen", 0, effects.getActiveCount());

        theLineSplashesAndLeavesBubbles();
        aFreshCatchWobblesAndSettles();
    }

    private static void theLineSplashesAndLeavesBubbles() {
        GameManager game = new GameManager(LevelData.of(1, 1), Loadout.forChapter(1), new Random(2));
        EffectsSystem effects = new EffectsSystem();
        effects.reset(game);
        game.addListener(effects);
        GameScript.clearTheWater(game);   // nothing for the hook to touch

        game.spaceAction();
        Check.assertEquals("dropping the line throws a ring and five drops", 6, effects.getActiveCount());

        // A third of a second on the way down: the tool leaves bubbles behind.
        for (double t = 0; t < 0.3; t += STEP) {
            game.update(STEP);
            effects.update(STEP);
        }
        Check.assertTrue("the line is still going out", game.getBoat().getHook().isExtending());
        Check.assertTrue("bubbles trail behind the tool", effects.getActiveCount() > 6);

        // Back at the boat with nothing: everything has faded, and no new bubbles came.
        GameScript.playUntilHookIsBack(game);
        play(effects, 1.0);
        Check.assertEquals("nothing is left a second after the line is back", 0, effects.getActiveCount());

        // Bringing something up splashes again, a little bigger.
        GameScript.hookOnto(game, GameScript.firstCatchable(game, true));
        effects.update(STEP);
        Check.assertTrue("the catch breaks the surface with a splash", effects.getActiveCount() >= 9);

        BufferedImage image = new BufferedImage(
                GameConstants.WORLD_WIDTH, GameConstants.WORLD_HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        effects.paint(g);   // rings, drops and bubbles can all be drawn
        g.dispose();
    }

    private static void aFreshCatchWobblesAndSettles() {
        GameManager game = new GameManager(LevelData.of(1, 1), Loadout.forChapter(1), new Random(3));
        OceanObject something = game.getObjects().get(0);
        EffectsSystem effects = new EffectsSystem();
        effects.reset(game);
        Check.assertEquals("nothing wobbles before a catch", 0.0, effects.getCatchSquash(), 1e-9);

        effects.onGameEvent(GameEvent.about(EventType.OBJECT_CAUGHT, something, 10));
        double first = effects.getCatchSquash();
        Check.assertTrue("a fresh catch is drawn wide and flat", first > 0.2);

        boolean wentTall = false;
        double biggest = 0;
        for (double t = 0; t < 0.3; t += STEP) {
            effects.update(STEP);
            wentTall |= effects.getCatchSquash() < 0;
            biggest = Math.max(biggest, Math.abs(effects.getCatchSquash()));
        }
        Check.assertTrue("then it swings the other way, tall and thin", wentTall);
        Check.assertTrue("the wobble never grows past its first stretch", biggest <= first);
        play(effects, 0.2);
        Check.assertEquals("and after a moment it is still", 0.0, effects.getCatchSquash(), 1e-9);

        effects.onGameEvent(GameEvent.about(EventType.HEALTH_LOST, something, 1));
        Check.assertEquals("a hooked fish does not wobble: it swims away", 0.0, effects.getCatchSquash(), 1e-9);
    }

    private static void play(EffectsSystem effects, double seconds) {
        for (double t = 0; t < seconds; t += STEP) {
            effects.update(STEP);
        }
    }
}
