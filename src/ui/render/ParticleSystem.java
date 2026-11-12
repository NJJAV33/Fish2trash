package ui.render;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Random;
import util.GameConstants;

/**
 * Bubbles that rise from the sea floor.
 *
 * It is an "object pool": there is room for a fixed number of bubbles in
 * arrays made once. A bubble that reaches the surface frees its slot for the
 * next one, so no memory is created or thrown away while the game runs.
 */
public final class ParticleSystem {

    public static final int MAX_BUBBLES = 40;

    private static final Color FILL = new Color(255, 255, 255, 45);
    private static final Color EDGE = new Color(255, 255, 255, 120);
    private static final double MIN_SPAWN_GAP = 0.25;   // seconds between new bubbles
    private static final double MAX_SPAWN_GAP = 0.6;

    private final double[] x = new double[MAX_BUBBLES];
    private final double[] y = new double[MAX_BUBBLES];
    private final double[] radius = new double[MAX_BUBBLES];
    private final double[] speed = new double[MAX_BUBBLES];     // pixels per second, upward
    private final double[] wobble = new double[MAX_BUBBLES];    // each bubble sways on its own rhythm
    private final boolean[] alive = new boolean[MAX_BUBBLES];
    private final Random random = new Random(7);   // only decoration, so a fixed seed is fine
    private double untilNextBubble;

    public void update(double dt) {
        untilNextBubble -= dt;
        if (untilNextBubble <= 0) {
            spawn();
            untilNextBubble = MIN_SPAWN_GAP + random.nextDouble() * (MAX_SPAWN_GAP - MIN_SPAWN_GAP);
        }
        for (int i = 0; i < MAX_BUBBLES; i++) {
            if (!alive[i]) {
                continue;
            }
            y[i] -= speed[i] * dt;
            x[i] += Math.sin(wobble[i] + y[i] * 0.05) * 12 * dt;
            if (y[i] < GameConstants.WATER_Y + radius[i]) {
                alive[i] = false;   // popped at the surface; the slot is free again
            }
        }
    }

    /** Starts a bubble in the first free slot. If all slots are in use, nothing happens. */
    private void spawn() {
        for (int i = 0; i < MAX_BUBBLES; i++) {
            if (!alive[i]) {
                alive[i] = true;
                x[i] = 20 + random.nextDouble() * (GameConstants.WORLD_WIDTH - 40);
                y[i] = GameConstants.SEABED_Y;
                radius[i] = 2 + random.nextDouble() * 3;
                speed[i] = 30 + random.nextDouble() * 40;
                wobble[i] = random.nextDouble() * Math.PI * 2;
                return;
            }
        }
    }

    public void paint(Graphics2D g) {
        for (int i = 0; i < MAX_BUBBLES; i++) {
            if (alive[i]) {
                int size = (int) (radius[i] * 2);
                int left = (int) (x[i] - radius[i]);
                int top = (int) (y[i] - radius[i]);
                g.setColor(FILL);
                g.fillOval(left, top, size, size);
                g.setColor(EDGE);
                g.drawOval(left, top, size, size);
            }
        }
    }

    /** How many bubbles are in the water right now. */
    public int getActiveCount() {
        int count = 0;
        for (boolean bubbleAlive : alive) {
            if (bubbleAlive) {
                count++;
            }
        }
        return count;
    }
}
