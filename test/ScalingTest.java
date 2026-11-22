import game.GameManager;
import game.GameState;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import model.EquipmentType;
import model.LevelData;
import model.Loadout;
import model.ObjectKind;
import model.OceanObject;
import model.Sea;
import ui.GamePanel;
import ui.GameWindow;
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
        windowStartingSizeFitsInsideWorkArea();
        windowBoundsClampToWorkArea();
        theSeaHasElevenLooksFromDirtyToClean();
        theSeaClearsAsTheQuotaFills();
        overQuotaScoreDoesNotOverClearSea();
        deepObjectsAreHazier();
        aNetLooksTheSameEveryTimeAndGrowsWithTheWindow();
        onlyAFewBackgroundsAreKept();
        aCaughtObjectCanBeDrawnStretched();
    }

    private static void windowStartingSizeFitsInsideWorkArea() {
        Dimension smallLaptop = GameWindow.startingSizeFor(new Dimension(1024, 560), new Insets(30, 8, 8, 8));
        Check.assertTrue("small laptop window width fits usable work area", smallLaptop.width <= 1008);
        Check.assertTrue("small laptop window height fits usable work area", smallLaptop.height <= 522);
        Check.assertTrue("small laptop window preserves 4:3 aspect",
                Math.abs(smallLaptop.width * 3 - smallLaptop.height * 4) <= 4);

        Dimension fhd = GameWindow.startingSizeFor(new Dimension(1920, 1040), new Insets(0, 0, 0, 0));
        Check.assertEquals("1920x1040 starts at 800x600", new Dimension(800, 600), fhd);

        Dimension uhd = GameWindow.startingSizeFor(new Dimension(3840, 2100), new Insets(0, 0, 0, 0));
        Check.assertEquals("3840x2100 starts at 1600x1200", new Dimension(1600, 1200), uhd);
    }

    private static void windowBoundsClampToWorkArea() {
        Rectangle work = new Rectangle(0, 0, 1920, 1040);
        Rectangle inside = new Rectangle(120, 80, 900, 675);
        Check.assertEquals("in-bounds rectangle stays unchanged",
                inside, GameWindow.clampToWorkArea(inside, work));

        Rectangle offscreen = GameWindow.clampToWorkArea(new Rectangle(2500, 1500, 2200, 1400), work);
        Check.assertTrue("clamped width fits work area", offscreen.width <= work.width);
        Check.assertTrue("clamped height fits work area", offscreen.height <= work.height);
        Check.assertTrue("clamped right edge fits work area", offscreen.x >= work.x && offscreen.x + offscreen.width <= work.x + work.width);
        Check.assertTrue("clamped bottom edge fits work area", offscreen.y >= work.y && offscreen.y + offscreen.height <= work.y + work.height);

        Rectangle negative = GameWindow.clampToWorkArea(new Rectangle(-200, -100, 800, 600), work);
        Check.assertEquals("negative origin clamps onto screen", new Rectangle(0, 0, 800, 600), negative);
    }

    private static void overQuotaScoreDoesNotOverClearSea() {
        SceneRenderer renderer = new SceneRenderer();
        GameManager exact = new GameManager(LevelData.of(2, 1), Loadout.forChapter(2), new Random(5));
        for (int i = 0; i < 4; i++) {
            catchNext(exact, ObjectKind.CAN);
        }
        swapTool(exact, EquipmentType.HOOK);
        for (int i = 0; i < 2; i++) {
            catchNext(exact, ObjectKind.PLASTIC_BOTTLE);
        }
        Check.assertEquals("exact quota reached on 2-1", exact.getQuota(), exact.getScore());
        GameScript.clearTheWater(exact);

        GameManager over = new GameManager(LevelData.of(2, 1), Loadout.forChapter(2), new Random(5));
        for (int i = 0; i < 3; i++) {
            catchNext(over, ObjectKind.CAN);
        }
        swapTool(over, EquipmentType.HOOK);
        for (int i = 0; i < 3; i++) {
            catchNext(over, ObjectKind.PLASTIC_BOTTLE);
        }
        swapTool(over, EquipmentType.MAGNET);
        catchNext(over, ObjectKind.CAN);
        Check.assertTrue("score exceeds quota on 2-1", over.getScore() > over.getQuota());
        GameScript.clearTheWater(over);

        Check.assertEquals("over-quota score produces the same sea color as exact quota",
                seaColor(renderer, exact), seaColor(renderer, over));
    }

    private static void swapTool(GameManager game, EquipmentType tool) {
        for (double t = 0; t < GameConstants.TOOL_SWAP_COOLDOWN_S + 0.1; t += GameScript.STEP) {
            game.update(GameScript.STEP);
        }
        game.selectTool(tool);
    }

    private static void catchNext(GameManager game, ObjectKind kind) {
        for (int i = 0; i < 2000; i++) {
            try {
                OceanObject found = GameScript.firstCatchable(game, kind);
                GameScript.hookOnto(game, found);
                return;
            } catch (IllegalStateException noneYet) {
                game.update(GameScript.STEP);
            }
        }
        Check.fail("timed out waiting for " + kind + " to spawn");
    }

    // ---- The window ----

    private static void theGameFillsABiggerWindow() {
        GamePanel panel = new GamePanel(new RecordingAudioPlayer(), TempScores.newTable(), TempScores.newProgress());
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
        GamePanel panel = new GamePanel(new RecordingAudioPlayer(), TempScores.newTable(), TempScores.newProgress());
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
        GameManager game = new GameManager(LevelData.of(1, 1), Loadout.forChapter(1), new Random(7));
        GameScript.clearTheWater(game);   // nothing in the way of the spot that is looked at
        int atStart = seaColor(renderer, game);
        Check.assertEquals("the same score gives the same sea", atStart, seaColor(renderer, game));

        // A chapter clears up over its five levels, so one level moves the sea a fifth of the way.
        while (game.getScore() < game.getQuota() / 2) {
            GameScript.hookOnto(game, GameScript.firstCatchable(game, ObjectKind.PLASTIC_BOTTLE));
        }
        GameScript.clearTheWater(game);
        int atHalf = seaColor(renderer, game);
        Check.assertTrue("half the quota of the first level: the sea has changed", atHalf != atStart);
        Check.assertTrue("and it is bluer than at the start", blue(atHalf) > blue(atStart));

        GameManager lastLevel = new GameManager(
                LevelData.of(1, LevelData.LEVELS_PER_CHAPTER), Loadout.forChapter(1), new Random(7));
        GameScript.clearTheWater(lastLevel);
        Check.assertTrue("the last level of the chapter starts with a much cleaner sea",
                blue(seaColor(renderer, lastLevel)) > blue(atHalf));
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
        net.update(1, Sea.CALM);   // let it finish fading in

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
        for (int chapter = 1; chapter <= LevelData.CHAPTER_COUNT; chapter++) {
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

    private static void aCaughtObjectCanBeDrawnStretched() {
        SceneRenderer renderer = new SceneRenderer();
        // Chapter 3: the catch is a net, which is drawn from a reused picture.
        GameManager game = new GameManager(LevelData.of(3, 1), Loadout.forChapter(3), new Random(9));
        OceanObject net = GameScript.firstCatchable(game, ObjectKind.FISHING_NET);
        GameScript.clearTheWater(game);
        game.spaceAction();
        for (int i = 0; i < 2000 && !net.isCaught(); i++) {
            net.moveTo(game.getBoat().getHook().getTipX(), game.getBoat().getHook().getTipY() + 10);
            game.update(GameScript.STEP);
        }
        Check.assertTrue("a net is on the line", net.isCaught());

        int[] normal = scenePixels(renderer, game, 0);
        Check.assertTrue("without a wobble the scene is drawn the same each time",
                Arrays.equals(normal, scenePixels(renderer, game, 0)));
        Check.assertTrue("with a wobble the catch is drawn differently",
                !Arrays.equals(normal, scenePixels(renderer, game, 0.3)));
        Check.assertTrue("and afterwards it is back to normal",
                Arrays.equals(normal, scenePixels(renderer, game, 0)));
    }

    // ---- helpers ----

    private static int[] scenePixels(SceneRenderer renderer, GameManager game, double squash) {
        BufferedImage image = new BufferedImage(W, H, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        renderer.paint(g, game, squash);
        g.dispose();
        return image.getRGB(0, 0, W, H, null, 0, W);
    }

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
        object.update(1, Sea.CALM);   // let it finish fading in
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
