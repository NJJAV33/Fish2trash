import game.GameManager;
import game.GameState;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import model.ChapterData;
import model.Loadout;
import model.ObjectKind;
import model.OceanObject;
import ui.GamePanel;
import ui.render.BackgroundPainter;
import ui.render.ChapterPalette;
import ui.render.PainterRegistry;
import ui.render.SceneRenderer;
import util.GameConstants;

/**
 * Checks the picture as a whole: that it grows with the window, that the sea
 * clears as the quota fills, that deep objects look hazier, and that the
 * pictures kept for reuse stay few.
 */
public final class ScalingTest {

    private static final int W = GameConstants.WORLD_WIDTH;
    private static final int H = GameConstants.WORLD_HEIGHT;
    private static final int BLACK = 0xFF000000;

    private ScalingTest() {
        // only static methods
    }

    public static void run() {
        theGameFillsABiggerWindow();
        aWindowOfAnotherShapeGetsBarsAtTheSides();
        theSeaHasElevenLooksFromDirtyToClean();
        theSeaClearsAsTheQuotaFills();
        deepObjectsAreHazier();
        aNetLooksTheSameEveryTimeAndGrowsWithTheWindow();
        onlyAFewBackgroundsAreKept();
    }

    // ---- The window ----

    private static void theGameFillsABiggerWindow() {
        GamePanel panel = new GamePanel(new RecordingAudioPlayer(), TempScores.newTable());
        panel.setSize(W, H);
        Check.assertEquals("at 800 x 600 nothing is enlarged", 1.0, panel.getViewScale(), 1e-9);
        BufferedImage small = picture(panel, W, H);

        panel.setSize(2 * W, 2 * H);
        Check.assertEquals("a window twice as large shows the game twice as large", 2.0, panel.getViewScale(), 1e-9);
        BufferedImage large = picture(panel, 2 * W, 2 * H);
        Check.assertEquals("the menu is still showing", GameState.MENU, panel.getState());

        // The same spot of the sky, the sea and the sea floor, in both sizes.
        int[][] spots = {{30, 70}, {400, 400}, {700, 595}};
        for (int[] spot : spots) {
            Check.assertTrue("the picture at (" + spot[0] + ", " + spot[1] + ") is the same, only larger",
                    close(small.getRGB(spot[0], spot[1]), large.getRGB(2 * spot[0], 2 * spot[1])));
        }
        Check.assertTrue("the far corner of the large window is painted too",
                large.getRGB(2 * W - 3, 2 * H - 3) != BLACK);
    }

    private static void aWindowOfAnotherShapeGetsBarsAtTheSides() {
        GamePanel panel = new GamePanel(new RecordingAudioPlayer(), TempScores.newTable());
        int width = 2 * W;          // twice as wide,
        int height = H * 3 / 2;     // but only one and a half times as high
        panel.setSize(width, height);
        Check.assertEquals("the game grows as far as the smaller side allows", 1.5, panel.getViewScale(), 1e-9);

        BufferedImage image = picture(panel, width, height);
        int barWidth = (width - W * 3 / 2) / 2;
        Check.assertEquals("the bar on the left is black", BLACK, image.getRGB(barWidth / 2, height / 2));
        Check.assertEquals("the bar on the right is black", BLACK, image.getRGB(width - barWidth / 2, height / 2));
        Check.assertTrue("the game is in the middle", image.getRGB(width / 2, height / 2) != BLACK);
        Check.assertTrue("right up to the bar", image.getRGB(barWidth + 3, height / 2) != BLACK);
    }

    // ---- The sea ----

    private static void theSeaHasElevenLooksFromDirtyToClean() {
        ChapterPalette dirty = ChapterPalette.SHALLOWS;
        Check.assertTrue("no progress: the look it starts with", dirty.cleanedBy(0) == dirty);
        Check.assertTrue("all the way: the cleaned look", dirty.cleanedBy(1) == dirty.getCleaned());
        Check.assertTrue("more than all the way is still the cleaned look", dirty.cleanedBy(1.4) == dirty.getCleaned());
        Check.assertTrue("the look only changes when a step is completed", dirty.cleanedBy(0.50) == dirty.cleanedBy(0.59));
        Check.assertTrue("and then it does change", dirty.cleanedBy(0.59) != dirty.cleanedBy(0.60));
        Check.assertTrue("a clean place stays as it is", dirty.getCleaned().cleanedBy(0.3) == dirty.getCleaned());

        Set<ChapterPalette> looks = new HashSet<>();
        int previousBlue = -1;
        for (int step = 0; step <= ChapterPalette.CLEAN_STEPS; step++) {
            ChapterPalette look = dirty.cleanedBy((double) step / ChapterPalette.CLEAN_STEPS);
            looks.add(look);
            int blue = look.getSeaTop().getBlue();
            Check.assertTrue("step " + step + ": the murky green sea turns bluer", blue > previousBlue);
            previousBlue = blue;
        }
        Check.assertEquals("one look per step, and the start", ChapterPalette.CLEAN_STEPS + 1, looks.size());
    }

    private static void theSeaClearsAsTheQuotaFills() {
        SceneRenderer renderer = new SceneRenderer();
        GameManager game = new GameManager(ChapterData.forChapter(1), Loadout.forChapter(1), new Random(7));
        GameScript.clearTheWater(game);   // nothing in the way of the spot that is looked at
        int atStart = seaColor(renderer, game);
        Check.assertEquals("the same score gives the same sea", atStart, seaColor(renderer, game));

        GameScript.hookOnto(game, GameScript.firstCatchable(game, ObjectKind.PLASTIC_BOTTLE));
        GameScript.clearTheWater(game);
        int afterOne = seaColor(renderer, game);
        Check.assertTrue("one bottle, a tenth of the quota: the sea has changed", afterOne != atStart);

        for (int i = 0; i < 4; i++) {
            GameScript.hookOnto(game, GameScript.firstCatchable(game, ObjectKind.PLASTIC_BOTTLE));
        }
        GameScript.clearTheWater(game);
        int atHalf = seaColor(renderer, game);
        Check.assertEquals("half the quota", game.getQuota() / 2, game.getScore());
        Check.assertTrue("the sea is bluer at half the quota than at the start", blue(atHalf) > blue(atStart));
        Check.assertTrue("and bluer than after one bottle", blue(atHalf) > blue(afterOne));
    }

    private static void deepObjectsAreHazier() {
        PainterRegistry painters = new PainterRegistry();
        int nearSurface = GameConstants.WATER_Y + 30;
        int nearFloor = GameConstants.SEABED_Y - 30;
        for (ObjectKind kind : new ObjectKind[] {ObjectKind.CAN, ObjectKind.SMALL_FISH, ObjectKind.BIG_FISH}) {
            long high = brightness(painters, kind, nearSurface);
            long low = brightness(painters, kind, nearFloor);
            Check.assertTrue(kind + " is drawn", high > 0 && low > 0);
            Check.assertTrue(kind + " shows less clearly near the sea floor", low < high);
            Check.assertTrue(kind + " is still easy to see down there", low > high * 0.6);
        }
    }

    private static void aNetLooksTheSameEveryTimeAndGrowsWithTheWindow() {
        PainterRegistry painters = new PainterRegistry();
        OceanObject net = ObjectKind.FISHING_NET.create(100, 100, 0);
        net.update(1, W);   // let it finish fading in

        int[] first = objectPixels(painters, net, 1);
        int[] again = objectPixels(painters, net, 1);
        Check.assertTrue("a net is drawn", lit(first) > 200);
        Check.assertTrue("the same net at the same moment is the same picture", Arrays.equals(first, again));

        int[] doubled = objectPixels(painters, net, 2);
        double growth = (double) lit(doubled) / lit(first);
        Check.assertTrue("drawn twice as large it covers about four times the pixels (" + growth + ")",
                growth > 3 && growth < 5);
    }

    private static void onlyAFewBackgroundsAreKept() {
        BackgroundPainter background = new BackgroundPainter();
        BufferedImage image = new BufferedImage(W, H, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        // A whole story: every step of every chapter, and the reef at the end.
        for (int chapter = 1; chapter <= ChapterData.CHAPTER_COUNT; chapter++) {
            for (int step = 0; step <= ChapterPalette.CLEAN_STEPS; step++) {
                background.paint(g, ChapterPalette.forChapter(chapter).cleanedBy((double) step / ChapterPalette.CLEAN_STEPS));
            }
        }
        background.paint(g, ChapterPalette.REEF);
        int kept = background.getCachedCount();
        Check.assertTrue("old looks are dropped: " + kept + " kept of 34 shown", kept >= 1 && kept <= 8);

        g.scale(2, 2);   // the window is twice as large now
        background.paint(g, ChapterPalette.REEF);
        Check.assertEquals("pictures made for another window size are thrown away", 1, background.getCachedCount());
        g.dispose();
    }

    // ---- helpers ----

    private static BufferedImage picture(GamePanel panel, int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        panel.paint(g);
        g.dispose();
        return image;
    }

    /** The colour of the open sea at one fixed spot, away from the boat. */
    private static int seaColor(SceneRenderer renderer, GameManager game) {
        BufferedImage image = new BufferedImage(W, H, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        renderer.paint(g, game);
        g.dispose();
        return image.getRGB(20, GameConstants.WATER_Y + 150);
    }

    private static int blue(int rgb) {
        return rgb & 0xFF;
    }

    /** True if two colours are nearly the same: enlarging smooths edges a little differently. */
    private static boolean close(int a, int b) {
        for (int shift = 0; shift <= 16; shift += 8) {
            if (Math.abs(((a >> shift) & 0xFF) - ((b >> shift) & 0xFF)) > 12) {
                return false;
            }
        }
        return true;
    }

    /** How bright one object of this kind is in total, drawn on black at depth y. */
    private static long brightness(PainterRegistry painters, ObjectKind kind, int y) {
        OceanObject object = kind.create(100, y, 20);
        object.update(1, W);   // let it finish fading in
        object.moveTo(100, y);
        BufferedImage image = new BufferedImage(200, H, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        painters.paint(g, object, 0.5);
        g.dispose();
        long total = 0;
        for (int rgb : image.getRGB(0, 0, 200, H, null, 0, 200)) {
            total += ((rgb >> 16) & 0xFF) + ((rgb >> 8) & 0xFF) + (rgb & 0xFF);
        }
        return total;
    }

    /** The pixels of an object drawn on black, with the whole picture enlarged by scale. */
    private static int[] objectPixels(PainterRegistry painters, OceanObject object, int scale) {
        BufferedImage image = new BufferedImage(200 * scale, 200 * scale, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.scale(scale, scale);
        painters.paint(g, object, 0.5);
        g.dispose();
        return image.getRGB(0, 0, 200 * scale, 200 * scale, null, 0, 200 * scale);
    }

    private static int lit(int[] pixels) {
        int count = 0;
        for (int pixel : pixels) {
            if (pixel != BLACK) {
                count++;
            }
        }
        return count;
    }
}
