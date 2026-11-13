package ui.render;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.util.HashMap;
import java.util.Map;
import util.GameConstants;

/**
 * Draws the moving parts of the water: two rows of waves at the surface and
 * sunbeams slanting down through the sea. Everything is driven by the time,
 * so the same time always gives the same picture.
 */
public final class WaterPainter {

    private static final int W = GameConstants.WORLD_WIDTH;
    private static final int WATER_Y = GameConstants.WATER_Y;
    private static final int WAVE_STEP = 16;        // pixels between points along a wave
    private static final int RAY_COUNT = 5;
    private static final int RAY_DEPTH = 380;       // how far down a sunbeam reaches

    // One reusable outline, so no new shape object is made every frame.
    private final Path2D.Double shape = new Path2D.Double();
    // The wave colours of each look, worked out once.
    private final Map<ChapterPalette, Color[]> waveColors = new HashMap<>();

    /** The far row of waves. Drawn before the boat, so the boat sits in front of it. */
    public void paintBackWaves(Graphics2D g, ChapterPalette palette, double time) {
        g.setColor(colorsFor(palette)[0]);
        fillWave(g, WATER_Y - 3, 4, 0.020, time * 1.3, 18);
    }

    /** The near row of waves. Drawn after the boat, so it laps over the hull. */
    public void paintFrontWaves(Graphics2D g, ChapterPalette palette, double time) {
        g.setColor(colorsFor(palette)[1]);
        fillWave(g, WATER_Y + 2, 3, 0.027, -time * 1.7, 9);
    }

    /**
     * Fills a band whose top edge is a sine wave.
     *
     * @param baseY     the middle height of the wave
     * @param amplitude how many pixels it rises and falls
     * @param frequency how tightly the wave is packed along x
     * @param shift     moves the wave sideways; changing it over time animates it
     * @param thickness how far the band reaches below the wave
     */
    private void fillWave(Graphics2D g, double baseY, double amplitude, double frequency, double shift, double thickness) {
        shape.reset();
        shape.moveTo(0, baseY + thickness);
        for (int x = 0; x <= W; x += WAVE_STEP) {
            shape.lineTo(x, baseY + amplitude * Math.sin(x * frequency + shift));
        }
        shape.lineTo(W, baseY + thickness);
        shape.closePath();
        g.fill(shape);
    }

    /** Sunbeams: slanted bands of light that sway slowly and fade out with depth. */
    public void paintRays(Graphics2D g, ChapterPalette palette, double time) {
        Color light = palette.getRay();
        Color clear = new Color(light.getRed(), light.getGreen(), light.getBlue(), 0);
        for (int i = 0; i < RAY_COUNT; i++) {
            // Each beam sways and brightens on its own rhythm.
            double topX = 60 + i * 165 + 22 * Math.sin(time * 0.3 + i * 1.7);
            double strength = 0.7 + 0.3 * Math.sin(time * 0.5 + i * 2.1);
            int alpha = (int) (palette.getRayAlpha() * strength);
            Color bright = new Color(light.getRed(), light.getGreen(), light.getBlue(), alpha);

            g.setPaint(new GradientPaint(0, WATER_Y, bright, 0, WATER_Y + RAY_DEPTH, clear));
            shape.reset();
            shape.moveTo(topX, WATER_Y);
            shape.lineTo(topX + 46, WATER_Y);
            shape.lineTo(topX + 190, WATER_Y + RAY_DEPTH);
            shape.lineTo(topX + 90, WATER_Y + RAY_DEPTH);
            shape.closePath();
            g.fill(shape);
        }
    }

    /** [0] = far waves, [1] = near waves: the surface colour, lightened and see-through. */
    private Color[] colorsFor(ChapterPalette palette) {
        Color[] colors = waveColors.get(palette);
        if (colors == null) {
            Color sea = palette.getSeaTop();
            colors = new Color[] {
                new Color(lighten(sea.getRed(), 0.35), lighten(sea.getGreen(), 0.35), lighten(sea.getBlue(), 0.35), 150),
                new Color(lighten(sea.getRed(), 0.6), lighten(sea.getGreen(), 0.6), lighten(sea.getBlue(), 0.6), 110)
            };
            waveColors.put(palette, colors);
        }
        return colors;
    }

    private static int lighten(int channel, double amount) {
        return (int) (channel + (255 - channel) * amount);
    }
}
