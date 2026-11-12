import game.EventType;
import game.GameEvent;
import game.GameManager;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Random;
import model.ChapterData;
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
        GameManager game = new GameManager(ChapterData.forChapter(1), Loadout.standard(), new Random(1));
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
        effects.onGameEvent(GameEvent.of(EventType.HOOK_LAUNCHED));
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
    }

    private static void play(EffectsSystem effects, double seconds) {
        for (double t = 0; t < seconds; t += STEP) {
            effects.update(STEP);
        }
    }
}
