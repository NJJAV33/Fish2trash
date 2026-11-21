import game.GameManager;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Random;
import model.LevelData;
import model.EquipmentType;
import model.Loadout;
import model.Stingray;
import ui.render.ChapterPalette;
import ui.render.PainterRegistry;
import ui.render.ParticleSystem;
import ui.render.SceneRenderer;
import ui.render.StingrayPainter;
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
        // The constructor throws if any ObjectKind or tool has no painter.
        new PainterRegistry();
        Check.assertTrue("every object kind and tool has a painter", true);

        BufferedImage image = new BufferedImage(
                GameConstants.WORLD_WIDTH, GameConstants.WORLD_HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        new SceneRenderer().paint(g, new GameManager(LevelData.of(1, 1), Loadout.forChapter(1), new Random(1)));
        g.dispose();

        int sky = image.getRGB(10, 10);
        int deepSea = image.getRGB(10, GameConstants.WORLD_HEIGHT - 40);
        Check.assertTrue("sky was painted", sky != 0xFF000000);
        Check.assertTrue("sea was painted", deepSea != 0xFF000000);
        Check.assertTrue("sky and sea differ", sky != deepSea);

        everyChapterHasItsOwnLook();
        everyToolHasItsOwnPicture();
        stingraySwimsInBothDirections();
        bubblesStayInsideTheirPool();
    }

    private static void everyChapterHasItsOwnLook() {
        SceneRenderer renderer = new SceneRenderer();
        int[] seaColor = new int[LevelData.CHAPTER_COUNT];
        for (int chapter = 1; chapter <= LevelData.CHAPTER_COUNT; chapter++) {
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

    private static void everyToolHasItsOwnPicture() {
        PainterRegistry painters = new PainterRegistry();
        EquipmentType[] tools = EquipmentType.values();
        int[][] pictures = new int[tools.length][];
        for (int i = 0; i < tools.length; i++) {
            pictures[i] = toolPicture(painters, tools[i], false);
            Check.assertTrue(tools[i] + " draws something", !Arrays.equals(pictures[i], new int[pictures[i].length]));
            toolPicture(painters, tools[i], true);   // the working look must draw without trouble too
        }
        for (int i = 1; i < tools.length; i++) {
            Check.assertTrue(tools[i] + " looks different from " + tools[i - 1],
                    !Arrays.equals(pictures[i], pictures[i - 1]));
        }
    }

    /** The pixels of one tool drawn in the middle of a small black image. */
    private static int[] toolPicture(PainterRegistry painters, EquipmentType tool, boolean working) {
        BufferedImage image = new BufferedImage(80, 80, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.translate(40, 40);
        painters.paintTool(g, tool, 0.3, working);
        g.dispose();
        return image.getRGB(0, 0, 80, 80, null, 0, 80);
    }

    private static void stingraySwimsInBothDirections() {
        BufferedImage right = stingrayPicture(1, 0);
        BufferedImage left = stingrayPicture(-1, 0);
        BufferedImage next = stingrayPicture(1, 0.7);
        Check.assertTrue("the visible disc covers the collision centre", right.getRGB(60, 40) >>> 24 > 0);
        // Only the whip tail reaches this far behind the centre.
        Check.assertTrue("the tail trails a right-facing ray", hasInk(right, 8, 30) && !hasInk(right, 90, 112));
        Check.assertTrue("the tail trails a left-facing ray", hasInk(left, 90, 112) && !hasInk(left, 8, 30));
        Check.assertTrue("the swim pose advances with elapsed time", !Arrays.equals(
                right.getRGB(0, 0, 120, 80, null, 0, 120), next.getRGB(0, 0, 120, 80, null, 0, 120)));
    }

    private static BufferedImage stingrayPicture(int direction, double time) {
        BufferedImage image = new BufferedImage(120, 80, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        new StingrayPainter().paint(g, new Stingray(60, 40, direction), time);
        g.dispose();
        return image;
    }

    private static boolean hasInk(BufferedImage image, int left, int right) {
        for (int x = left; x < right; x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                if (image.getRGB(x, y) >>> 24 > 0) {
                    return true;
                }
            }
        }
        return false;
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
