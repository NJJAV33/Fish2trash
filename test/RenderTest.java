import game.GameManager;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Random;
import model.Loadout;
import ui.render.PainterRegistry;
import ui.render.SceneRenderer;
import util.GameConstants;

/**
 * Draws the scene into an off-screen image, so drawing is checked without
 * opening a window.
 */
public final class RenderTest {

    private RenderTest() {
        // only static methods
    }

    public static void run() {
        // The constructor throws if any ObjectKind has no painter.
        new PainterRegistry();
        Check.assertTrue("every object kind has a painter", true);

        BufferedImage image = new BufferedImage(
                GameConstants.WORLD_WIDTH, GameConstants.WORLD_HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        new SceneRenderer().paint(g, new GameManager(Loadout.standard(), new Random(1)));
        g.dispose();

        int sky = image.getRGB(10, 10);
        int deepSea = image.getRGB(10, GameConstants.WORLD_HEIGHT - 40);
        Check.assertTrue("sky was painted", sky != 0xFF000000);
        Check.assertTrue("sea was painted", deepSea != 0xFF000000);
        Check.assertTrue("sky and sea differ", sky != deepSea);
    }
}
