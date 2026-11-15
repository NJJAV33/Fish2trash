package ui.render;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.RadialGradientPaint;
import java.awt.image.BufferedImage;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import util.GameConstants;

/**
 * Draws the part of the scene that never moves: sky, sun, the sea's colour
 * and the sea floor with its scenery, darkened a little toward the corners.
 *
 * Painting gradients every frame is slow, so each look is drawn once into an
 * image and that image is reused (a cache). A look only costs time the first
 * time it is shown. The images are made at the size the window shows them,
 * so they are drawn again when the window changes size.
 */
public final class BackgroundPainter {

    private static final int W = GameConstants.WORLD_WIDTH;
    private static final int H = GameConstants.WORLD_HEIGHT;
    private static final int WATER_Y = GameConstants.WATER_Y;
    private static final int SEABED_Y = GameConstants.SEABED_Y;

    private static final Color[] CORAL = {
        new Color(255, 111, 145), new Color(255, 150, 113), new Color(255, 199, 95),
        new Color(195, 74, 155), new Color(79, 209, 197)
    };
    private static final BasicStroke GRASS_STROKE = new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);

    // The vignette: clear in the middle, darker toward the corners, which pulls the eye to the play area.
    private static final float VIGNETTE_RADIUS = 560;
    private static final float[] VIGNETTE_STOPS = {0f, 0.55f, 1f};
    private static final Color[] VIGNETTE_COLORS = {
        new Color(0, 0, 0, 0), new Color(0, 0, 0, 0), new Color(0, 0, 0, 105)
    };

    // A chapter shows up to eleven looks as its sea clears. Only the latest few are kept.
    private static final int MAX_CACHED = 6;

    @SuppressWarnings("serial")   // a LinkedHashMap is Serializable by inheritance; this one is never saved
    private final Map<ChapterPalette, BufferedImage> cache =
            new LinkedHashMap<ChapterPalette, BufferedImage>(16, 0.75f, true) {   // true: ordered by last use
                @Override
                protected boolean removeEldestEntry(Map.Entry<ChapterPalette, BufferedImage> eldest) {
                    return size() > MAX_CACHED;
                }
            };
    private double cacheScale = 1;   // the size the cached images were made for

    // Which method draws which scenery, looked up instead of an if/else chain.
    private final Map<ChapterPalette.Scenery, BiConsumer<Graphics2D, ChapterPalette>> sceneryPainters =
            new EnumMap<>(ChapterPalette.Scenery.class);

    public BackgroundPainter() {
        sceneryPainters.put(ChapterPalette.Scenery.SHALLOWS, this::drawShallows);
        sceneryPainters.put(ChapterPalette.Scenery.WRECK, this::drawWreck);
        sceneryPainters.put(ChapterPalette.Scenery.DEEP, this::drawDeep);
        sceneryPainters.put(ChapterPalette.Scenery.REEF, this::drawReef);
    }

    public void paint(Graphics2D g, ChapterPalette palette) {
        double scale = PixelImages.scaleOf(g);
        if (scale != cacheScale) {
            cache.clear();   // the window shows the game at another size now
            cacheScale = scale;
        }
        BufferedImage image = cache.get(palette);
        if (image == null) {
            image = render(palette, scale);
            cache.put(palette, image);
        }
        PixelImages.paint(g, image, 0, 0, scale);
    }

    /** How many looks are in the cache right now. */
    public int getCachedCount() {
        return cache.size();
    }

    private BufferedImage render(ChapterPalette palette, double scale) {
        BufferedImage image = PixelImages.create(W, H, scale, false);
        Graphics2D g = PixelImages.draw(image, scale);

        g.setPaint(new GradientPaint(0, 0, palette.getSkyTop(), 0, WATER_Y, palette.getSkyBottom()));
        g.fillRect(0, 0, W, WATER_Y);

        // The sun sits below the HUD bar, with a soft glow around it.
        Color sun = palette.getSun();
        g.setColor(new Color(sun.getRed(), sun.getGreen(), sun.getBlue(), 60));
        g.fillOval(W - 112, 56, 76, 76);
        g.setColor(sun);
        g.fillOval(W - 100, 68, 52, 52);

        // The sea gets darker with depth, in three steps.
        g.setPaint(new LinearGradientPaint(0, WATER_Y, 0, SEABED_Y, new float[] {0f, 0.5f, 1f},
                new Color[] {palette.getSeaTop(), palette.getSeaMiddle(), palette.getSeaBottom()}));
        g.fillRect(0, WATER_Y, W, SEABED_Y - WATER_Y);

        sceneryPainters.get(palette.getScenery()).accept(g, palette);

        g.setColor(palette.getSeabed());
        g.fillRect(0, SEABED_Y, W, H - SEABED_Y);

        g.setPaint(new RadialGradientPaint(W / 2f, H / 2f, VIGNETTE_RADIUS, VIGNETTE_STOPS, VIGNETTE_COLORS));
        g.fillRect(0, 0, W, H);

        g.dispose();
        return image;
    }

    // ---- Scenery: low, quiet shapes on the sea floor, so they never look like trash ----

    /** Sand dunes, stones and a little seagrass. */
    private void drawShallows(Graphics2D g, ChapterPalette palette) {
        g.setColor(shade(palette.getSeabed(), 0.85));
        for (int i = 0; i < 5; i++) {
            g.fillOval(-60 + i * 190, SEABED_Y - 16, 240, 44);
        }
        g.setColor(shade(palette.getSeabed(), 0.6));
        for (int i = 0; i < 7; i++) {
            int size = 14 + (i * 13) % 16;
            g.fillOval(40 + i * 113, SEABED_Y - size / 2, size + 10, size);
        }
        drawSeagrass(g, shade(palette.getSeaTop(), 1.15), 12);
    }

    /** A sunken boat and the posts of an old pier, on the right where the cans collect. */
    private void drawWreck(Graphics2D g, ChapterPalette palette) {
        Color silhouette = shade(palette.getSeaBottom(), 0.7);
        g.setColor(silhouette);
        for (int i = 0; i < 4; i++) {
            int x = 470 + i * 80;
            g.fillRect(x, SEABED_Y - 150 + (i % 2) * 25, 12, 170);   // pier posts
        }
        g.fillRect(470, SEABED_Y - 110, 252, 8);                      // cross beam
        // The wreck: a tilted hull lying on the bottom
        g.fillPolygon(new int[] {520, 760, 730, 560}, new int[] {SEABED_Y - 50, SEABED_Y - 74, SEABED_Y, SEABED_Y}, 4);
        g.fillRect(620, SEABED_Y - 96, 60, 30);
        g.setColor(shade(palette.getSeabed(), 0.85));
        for (int i = 0; i < 4; i++) {
            g.fillOval(-80 + i * 150, SEABED_Y - 14, 220, 40);
        }
    }

    /** Jagged dark rocks and a drop-off on the left. */
    private void drawDeep(Graphics2D g, ChapterPalette palette) {
        g.setColor(shade(palette.getSeabed(), 1.25));
        g.fillPolygon(new int[] {0, 0, 70, 120, 150}, new int[] {SEABED_Y, SEABED_Y - 210, SEABED_Y - 150, SEABED_Y - 60, SEABED_Y}, 5);
        for (int i = 0; i < 7; i++) {
            int x = 150 + i * 100;
            int height = 24 + (i * 29) % 46;
            g.fillPolygon(new int[] {x, x + 30, x + 58, x + 90}, new int[] {SEABED_Y, SEABED_Y - height, SEABED_Y - height / 2, SEABED_Y}, 4);
        }
    }

    /** Bright coral and seagrass: the reef is alive again. */
    private void drawReef(Graphics2D g, ChapterPalette palette) {
        for (int i = 0; i < 16; i++) {
            int x = 20 + i * 50;
            int height = 26 + (i * 37) % 30;   // varied, but the same every time
            g.setColor(CORAL[i % CORAL.length]);
            g.fillRoundRect(x, SEABED_Y - height, 16, height, 12, 12);
            g.fillRoundRect(x - 12, SEABED_Y - height / 2, 12, height / 2, 10, 10);
            g.fillRoundRect(x + 16, SEABED_Y - height * 2 / 3, 12, height * 2 / 3, 10, 10);
        }
        drawSeagrass(g, new Color(60, 190, 120), 16);
    }

    private void drawSeagrass(Graphics2D g, Color color, int tufts) {
        g.setColor(color);
        g.setStroke(GRASS_STROKE);
        for (int i = 0; i < tufts; i++) {
            int x = 30 + i * (W - 60) / tufts + (i * 17) % 23;
            int height = 18 + (i * 11) % 16;
            g.drawLine(x, SEABED_Y, x - 5, SEABED_Y - height);
            g.drawLine(x + 4, SEABED_Y, x + 6, SEABED_Y - height + 5);
            g.drawLine(x + 8, SEABED_Y, x + 14, SEABED_Y - height + 2);
        }
    }

    /** A darker (factor below 1) or lighter (above 1) version of a colour. */
    private static Color shade(Color color, double factor) {
        return new Color(
                Math.min(255, (int) (color.getRed() * factor)),
                Math.min(255, (int) (color.getGreen() * factor)),
                Math.min(255, (int) (color.getBlue() * factor)));
    }
}
