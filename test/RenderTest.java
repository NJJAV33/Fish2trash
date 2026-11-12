import game.GameManager;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Random;
import model.ChapterData;
import model.Loadout;
import ui.render.ChapterPalette;
import ui.render.PainterRegistry;
import ui.render.ParticleSystem;
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
        new SceneRenderer().paint(g, new GameManager(ChapterData.forChapter(1), Loadout.forChapter(1), new Random(1)));
        g.dispose();

        int sky = image.getRGB(10, 10);
        int deepSea = image.getRGB(10, GameConstants.WORLD_HEIGHT - 40);
        Check.assertTrue("sky was painted", sky != 0xFF000000);
        Check.assertTrue("sea was painted", deepSea != 0xFF000000);
        Check.assertTrue("sky and sea differ", sky != deepSea);

        everyChapterHasItsOwnLook();
        bubblesStayInsideTheirPool();
    }

    private static void everyChapterHasItsOwnLook() {
        SceneRenderer renderer = new SceneRenderer();
        int[] seaColor = new int[ChapterData.CHAPTER_COUNT];
        for (int chapter = 1; chapter <= ChapterData.CHAPTER_COUNT; chapter++) {
            BufferedImage image = new BufferedImage(
                    GameConstants.WORLD_WIDTH, GameConstants.WORLD_HEIGHT, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = image.createGraphics();
            renderer.paintBackdrop(g, ChapterPalette.forChapter(chapter));
            g.dispose();
            seaColor[chapter - 1] = image.getRGB(5, GameConstants.SEABED_Y - 5);
        }
        Check.assertTrue("chapters 1 and 2 look different", seaColor[0] != seaColor[1]);
        Check.assertTrue("chapters 2 and 3 look different", seaColor[1] != seaColor[2]);
        Check.assertTrue("a cleaned-up place is its own look",
                ChapterPalette.SHALLOWS.getCleaned() != ChapterPalette.SHALLOWS);
        Check.assertTrue("asking twice gives the same cleaned look (so its picture is cached once)",
                ChapterPalette.SHALLOWS.getCleaned() == ChapterPalette.SHALLOWS.getCleaned());
    }

    private static void bubblesStayInsideTheirPool() {
        ParticleSystem bubbles = new ParticleSystem();
        int most = 0;
        for (int i = 0; i < 6000; i++) {   // five minutes of frames
            bubbles.update(0.05);
            most = Math.max(most, bubbles.getActiveCount());
        }
        Check.assertTrue("bubbles appear", most > 0);
        Check.assertTrue("never more bubbles than the pool holds", most <= ParticleSystem.MAX_BUBBLES);
    }
}
