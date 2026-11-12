package ui.render;

import game.EventType;
import game.GameEvent;
import game.GameListener;
import game.GameView;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.EnumMap;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;
import util.GameConstants;

/**
 * The short-lived feedback that makes a catch or a mistake feel like
 * something happened: "+10" popping up, a red flash and a screen shake when a
 * fish is hooked, sparks when something slips off, stars when the quota is met.
 *
 * OBSERVER: it is a GameListener. The game rules know nothing about effects;
 * they only announce events, and this class decides what each one looks like.
 *
 * Like the bubbles, the effects live in a fixed pool of slots that are reused.
 */
public final class EffectsSystem implements GameListener {

    public static final int MAX_EFFECTS = 48;

    private enum Kind { POPUP, SPARK, STAR }

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

    private static final Color SPARK_COLOR = new Color(255, 230, 90);

    private final Kind[] kind = new Kind[MAX_EFFECTS];
    private final double[] x = new double[MAX_EFFECTS];
    private final double[] y = new double[MAX_EFFECTS];
    private final double[] speedX = new double[MAX_EFFECTS];
    private final double[] speedY = new double[MAX_EFFECTS];
    private final double[] age = new double[MAX_EFFECTS];
    private final double[] life = new double[MAX_EFFECTS];
    private final String[] text = new String[MAX_EFFECTS];
    private final Color[] color = new Color[MAX_EFFECTS];
    private final boolean[] alive = new boolean[MAX_EFFECTS];

    private double flashAge = FLASH_SECONDS;   // "finished", so nothing shows at the start
    private double shakeAge = SHAKE_SECONDS;
    private GameView game;
    private final Random random = new Random(11);   // only decoration, so a fixed seed is fine

    // What to do for each event, looked up instead of a switch.
    private final Map<EventType, Consumer<GameEvent>> reactions = new EnumMap<>(EventType.class);

    public EffectsSystem() {
        reactions.put(EventType.OBJECT_CAUGHT, this::onTrashCaught);
        reactions.put(EventType.HEALTH_LOST, this::onFishHooked);
        reactions.put(EventType.SLIPPED, this::onSlip);
        reactions.put(EventType.QUOTA_REACHED, this::onQuotaReached);
    }

    /** Clears everything for a new chapter attempt. */
    public void reset(GameView newGame) {
        game = newGame;
        for (int i = 0; i < MAX_EFFECTS; i++) {
            alive[i] = false;
        }
        flashAge = FLASH_SECONDS;
        shakeAge = SHAKE_SECONDS;
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
                    Math.cos(angle) * SPARK_SPEED, Math.sin(angle) * SPARK_SPEED, SPARK_SECONDS, null, SPARK_COLOR);
        }
    }

    private void onQuotaReached(GameEvent event) {
        double boatX = game.getBoat().getX();
        double deckY = GameConstants.WATER_Y - GameConstants.BOAT_HEIGHT;
        for (int i = 0; i < STAR_COUNT; i++) {
            add(Kind.STAR, boatX, deckY,
                    -180 + random.nextDouble() * 360, -320 + random.nextDouble() * 200,
                    STAR_SECONDS, null, UiPalette.GOLD);
        }
    }

    private void addPopup(String label, double atX, double atY, Color popupColor) {
        add(Kind.POPUP, atX, atY, 0, -POPUP_RISE / POPUP_SECONDS, POPUP_SECONDS, label, popupColor);
    }

    /** Puts a new effect in the first free slot. With every slot busy, the effect is skipped. */
    private void add(Kind newKind, double atX, double atY, double vx, double vy,
                     double seconds, String label, Color effectColor) {
        for (int i = 0; i < MAX_EFFECTS; i++) {
            if (!alive[i]) {
                alive[i] = true;
                kind[i] = newKind;
                x[i] = atX;
                y[i] = atY;
                speedX[i] = vx;
                speedY[i] = vy;
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
        for (int i = 0; i < MAX_EFFECTS; i++) {
            if (!alive[i]) {
                continue;
            }
            age[i] += dt;
            x[i] += speedX[i] * dt;
            y[i] += speedY[i] * dt;
            if (kind[i] == Kind.STAR) {
                speedY[i] += GRAVITY * dt;
            }
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

    // ---- Drawing ----

    /** Popups, sparks and stars. Drawn in the world, so they shake with it. */
    public void paint(Graphics2D g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setFont(Fonts.HEADING);
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
