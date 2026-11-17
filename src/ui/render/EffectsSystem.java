package ui.render;

import game.EventType;
import game.GameEvent;
import game.GameListener;
import game.GameView;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.util.EnumMap;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;
import model.Hook;
import util.GameConstants;

/**
 * The short-lived feedback that makes a catch or a mistake feel like
 * something happened: "+10" popping up, a red flash and a screen shake when a
 * fish is hooked, sparks when something slips off, stars when the quota is met.
 * It also adds the small motion around the line: a splash where it goes into
 * the water, a trail of bubbles behind the tool, and a wobble of the catch.
 *
 * OBSERVER: it is a GameListener. The game rules know nothing about effects;
 * they only announce events, and this class decides what each one looks like.
 *
 * Like the bubbles, the effects live in a fixed pool of slots that are reused.
 */
public final class EffectsSystem implements GameListener {

    public static final int MAX_EFFECTS = 72;

    private enum Kind { POPUP, SPARK, STAR, DROPLET, RIPPLE, BUBBLE }

    private static final double POPUP_SECONDS = 0.8;
    private static final double POPUP_RISE = 30;        // pixels a popup floats up
    private static final double SPARK_SECONDS = 0.35;
    private static final double SPARK_SPEED = 130;
    private static final int SPARK_COUNT = 8;
    private static final double STAR_SECONDS = 1.1;
    private static final int STAR_COUNT = 24;
    private static final double GRAVITY = 260;          // stars arc back down
    private static final double FLASH_SECONDS = 0.35;
    private static final int FLASH_ALPHA = 90;
    private static final double SHAKE_SECONDS = 0.4;
    private static final double SHAKE_PIXELS = 8;

    // The splash where the line breaks the surface: drops that fly up and fall back, and a ring that spreads.
    private static final int LAUNCH_DROPLETS = 5;
    private static final int DELIVERY_DROPLETS = 8;
    private static final double DROPLET_SECONDS = 0.5;
    private static final double DROPLET_GRAVITY = 520;
    private static final double RIPPLE_SECONDS = 0.6;
    private static final double RIPPLE_START = 5;       // the ring's half width when it appears...
    private static final double RIPPLE_GROWTH = 24;     // ...and how much it gains before it is gone
    private static final double RIPPLE_FLATNESS = 0.22; // seen from the side, a ring on water is a flat oval

    // The trail behind the tool on its way down.
    private static final double TRAIL_GAP_SECONDS = 0.05;   // one bubble this often
    private static final double BUBBLE_SECONDS = 0.55;
    private static final double BUBBLE_RISE = 30;           // pixels per second, upward
    private static final double BUBBLE_RADIUS = 2.5;

    // The wobble of something that was just caught: wide and flat, then tall, then still.
    private static final double SQUASH_SECONDS = 0.35;
    private static final double SQUASH_AMOUNT = 0.3;
    private static final double SQUASH_SPEED = 28;

    private static final Color SPARK_COLOR = new Color(255, 230, 90);
    private static final Color WATER_COLOR = new Color(225, 242, 250);
    private static final BasicStroke THIN = new BasicStroke(1.3f);

    private final Kind[] kind = new Kind[MAX_EFFECTS];
    private final double[] x = new double[MAX_EFFECTS];
    private final double[] y = new double[MAX_EFFECTS];
    private final double[] speedX = new double[MAX_EFFECTS];
    private final double[] speedY = new double[MAX_EFFECTS];
    private final double[] gravity = new double[MAX_EFFECTS];   // how fast it is pulled down; 0 for most
    private final double[] age = new double[MAX_EFFECTS];
    private final double[] life = new double[MAX_EFFECTS];
    private final String[] text = new String[MAX_EFFECTS];
    private final Color[] color = new Color[MAX_EFFECTS];
    private final boolean[] alive = new boolean[MAX_EFFECTS];

    private double flashAge = FLASH_SECONDS;   // "finished", so nothing shows at the start
    private double shakeAge = SHAKE_SECONDS;
    private double squashAge = SQUASH_SECONDS;
    private double untilNextBubble;
    private GameView game;
    private final Random random = new Random(11);   // only decoration, so a fixed seed is fine
    private final Ellipse2D.Double shape = new Ellipse2D.Double();   // one reusable outline for rings and bubbles

    // What to do for each event, looked up instead of a switch.
    private final Map<EventType, Consumer<GameEvent>> reactions = new EnumMap<>(EventType.class);

    public EffectsSystem() {
        reactions.put(EventType.OBJECT_CAUGHT, this::onTrashCaught);
        reactions.put(EventType.HEALTH_LOST, this::onFishHooked);
        reactions.put(EventType.SLIPPED, this::onSlip);
        reactions.put(EventType.QUOTA_REACHED, this::onQuotaReached);
        reactions.put(EventType.HOOK_LAUNCHED, event -> splash(LAUNCH_DROPLETS));
        reactions.put(EventType.DELIVERED, event -> splash(DELIVERY_DROPLETS));
        reactions.put(EventType.FISH_FREED, event -> splash(DELIVERY_DROPLETS));   // the fish goes back in
    }

    /** Clears everything for a new attempt at a level. */
    public void reset(GameView newGame) {
        game = newGame;
        for (int i = 0; i < MAX_EFFECTS; i++) {
            alive[i] = false;
        }
        flashAge = FLASH_SECONDS;
        shakeAge = SHAKE_SECONDS;
        squashAge = SQUASH_SECONDS;
        untilNextBubble = 0;
    }

    @Override
    public void onGameEvent(GameEvent event) {
        Consumer<GameEvent> reaction = reactions.get(event.getType());
        if (reaction != null) {
            reaction.accept(event);   // events without a reaction are simply ignored
        }
    }

    private void onTrashCaught(GameEvent event) {
        addPopup("+" + event.getValue(), event.getX(), event.getY() - 18, UiPalette.GOLD);
        squashAge = 0;
    }

    private void onFishHooked(GameEvent event) {
        addPopup("-" + event.getValue(), event.getX(), event.getY() - 18, UiPalette.DANGER);
        flashAge = 0;
        shakeAge = 0;
    }

    private void onSlip(GameEvent event) {
        for (int i = 0; i < SPARK_COUNT; i++) {
            double angle = 2 * Math.PI * i / SPARK_COUNT;
            add(Kind.SPARK, event.getX(), event.getY(),
                    Math.cos(angle) * SPARK_SPEED, Math.sin(angle) * SPARK_SPEED, 0, SPARK_SECONDS, null, SPARK_COLOR);
        }
    }

    private void onQuotaReached(GameEvent event) {
        double boatX = game.getBoat().getX();
        double deckY = GameConstants.WATER_Y - GameConstants.BOAT_HEIGHT;
        for (int i = 0; i < STAR_COUNT; i++) {
            add(Kind.STAR, boatX, deckY,
                    -180 + random.nextDouble() * 360, -320 + random.nextDouble() * 200,
                    GRAVITY, STAR_SECONDS, null, UiPalette.GOLD);
        }
    }

    /** Water thrown up where the line breaks the surface, under the boat. */
    private void splash(int droplets) {
        double x = game.getBoat().getHook().getAnchorX();
        double y = GameConstants.WATER_Y;
        add(Kind.RIPPLE, x, y, 0, 0, 0, RIPPLE_SECONDS, null, WATER_COLOR);
        for (int i = 0; i < droplets; i++) {
            add(Kind.DROPLET, x, y, -70 + random.nextDouble() * 140, -110 - random.nextDouble() * 90,
                    DROPLET_GRAVITY, DROPLET_SECONDS, null, WATER_COLOR);
        }
    }

    /** While the tool is on its way down, it leaves a bubble behind every few frames. */
    private void trailBubbles(double dt) {
        Hook hook = game.getBoat().getHook();
        if (!hook.isExtending()) {
            untilNextBubble = 0;
            return;
        }
        untilNextBubble -= dt;
        while (untilNextBubble <= 0) {
            untilNextBubble += TRAIL_GAP_SECONDS;
            add(Kind.BUBBLE, hook.getTipX() - 5 + random.nextDouble() * 10, hook.getTipY() - 6,
                    -8 + random.nextDouble() * 16, -BUBBLE_RISE, 0, BUBBLE_SECONDS, null, WATER_COLOR);
        }
    }

    private void addPopup(String label, double atX, double atY, Color popupColor) {
        add(Kind.POPUP, atX, atY, 0, -POPUP_RISE / POPUP_SECONDS, 0, POPUP_SECONDS, label, popupColor);
    }

    /** Puts a new effect in the first free slot. With every slot busy, the effect is skipped. */
    private void add(Kind newKind, double atX, double atY, double vx, double vy, double pullDown,
                     double seconds, String label, Color effectColor) {
        for (int i = 0; i < MAX_EFFECTS; i++) {
            if (!alive[i]) {
                alive[i] = true;
                kind[i] = newKind;
                x[i] = atX;
                y[i] = atY;
                speedX[i] = vx;
                speedY[i] = vy;
                gravity[i] = pullDown;
                age[i] = 0;
                life[i] = seconds;
                text[i] = label;
                color[i] = effectColor;
                return;
            }
        }
    }

    public void update(double dt) {
        flashAge += dt;
        shakeAge += dt;
        squashAge += dt;
        if (game != null) {
            trailBubbles(dt);
        }
        for (int i = 0; i < MAX_EFFECTS; i++) {
            if (!alive[i]) {
                continue;
            }
            age[i] += dt;
            x[i] += speedX[i] * dt;
            y[i] += speedY[i] * dt;
            speedY[i] += gravity[i] * dt;
            if (age[i] >= life[i]) {
                alive[i] = false;   // finished; the slot is free again
            }
        }
    }

    // ---- The screen shake: the caller shifts the whole scene by these amounts ----

    public double getShakeX() {
        return shakeStrength() * Math.sin(shakeAge * 90);
    }

    public double getShakeY() {
        return shakeStrength() * Math.cos(shakeAge * 70);
    }

    /** Starts at SHAKE_PIXELS and dies away to nothing. */
    private double shakeStrength() {
        if (shakeAge >= SHAKE_SECONDS) {
            return 0;
        }
        return SHAKE_PIXELS * (1 - shakeAge / SHAKE_SECONDS);
    }

    // ---- The wobble of a fresh catch: the renderer stretches the caught object by this ----

    /**
     * How much wider than normal the caught object is drawn right now: above
     * 0 it is wide and flat, below 0 tall and thin, and 0 means no change.
     */
    public double getCatchSquash() {
        if (squashAge >= SQUASH_SECONDS) {
            return 0;
        }
        double fadingOut = 1 - squashAge / SQUASH_SECONDS;
        return SQUASH_AMOUNT * fadingOut * Math.cos(squashAge * SQUASH_SPEED);
    }

    // ---- Drawing ----

    /** Popups, sparks, stars, splashes and bubbles. Drawn in the world, so they shake with it. */
    public void paint(Graphics2D g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setFont(Fonts.HEADING);
        g2.setStroke(THIN);
        for (int i = 0; i < MAX_EFFECTS; i++) {
            if (!alive[i]) {
                continue;
            }
            float fade = (float) Math.max(0, 1 - age[i] / life[i]);   // every effect fades out
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, fade));
            g2.setColor(color[i]);
            int px = (int) x[i];
            int py = (int) y[i];
            switch (kind[i]) {
                case POPUP:
                    // A dark copy underneath keeps the number readable on any water.
                    g2.setColor(UiPalette.SHADE);
                    TextDraw.centered(g2, text[i], px + 2, py + 2);
                    g2.setColor(color[i]);
                    TextDraw.centered(g2, text[i], px, py);
                    break;
                case SPARK:
                    g2.fillOval(px - 2, py - 2, 5, 5);
                    break;
                case STAR:
                    // A small diamond
                    g2.fillPolygon(new int[] {px, px + 5, px, px - 5}, new int[] {py - 6, py, py + 6, py}, 4);
                    break;
                case DROPLET:
                    g2.fillOval(px - 1, py - 1, 3, 3);
                    break;
                case RIPPLE:
                    // A ring that spreads over the surface.
                    double halfWidth = RIPPLE_START + RIPPLE_GROWTH * age[i] / life[i];
                    shape.setFrame(x[i] - halfWidth, y[i] - halfWidth * RIPPLE_FLATNESS,
                            2 * halfWidth, 2 * halfWidth * RIPPLE_FLATNESS);
                    g2.draw(shape);
                    break;
                case BUBBLE:
                    shape.setFrame(x[i] - BUBBLE_RADIUS, y[i] - BUBBLE_RADIUS, 2 * BUBBLE_RADIUS, 2 * BUBBLE_RADIUS);
                    g2.draw(shape);
                    break;
            }
        }
        g2.dispose();
    }

    /** The red flash after hooking a fish. Drawn over the whole screen, without the shake. */
    public void paintFlash(Graphics2D g) {
        if (flashAge >= FLASH_SECONDS) {
            return;
        }
        int alpha = (int) (FLASH_ALPHA * (1 - flashAge / FLASH_SECONDS));
        Color danger = UiPalette.DANGER;
        g.setColor(new Color(danger.getRed(), danger.getGreen(), danger.getBlue(), alpha));
        g.fillRect(0, 0, GameConstants.WORLD_WIDTH, GameConstants.WORLD_HEIGHT);
    }

    /** How many popups, sparks and stars are on screen right now. */
    public int getActiveCount() {
        int count = 0;
        for (boolean effectAlive : alive) {
            if (effectAlive) {
                count++;
            }
        }
        return count;
    }
}
