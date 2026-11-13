package ui;

import game.GameSession;
import game.GameState;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.EnumMap;
import java.util.Map;
import java.util.Random;
import util.AudioPlayer;
import util.GameConstants;

/**
 * Knows which screen is showing and moves between screens with a short fade.
 * It also holds what every screen needs: the run in progress (the
 * GameSession) and the sound player.
 */
public final class ScreenManager {

    private static final double FADE_SECONDS = 0.25;

    // One screen object per state, looked up by the state - no if/else chain.
    private final Map<GameState, Screen> screens = new EnumMap<>(GameState.class);
    private GameState current;
    private GameState next;     // where a fade-out is heading, or null
    private double darkness;    // 0 = screen fully visible, 1 = fully black
    private Screen overlay;     // drawn on top while the game is paused, or null
    private GameSession session;
    private final AudioPlayer audio;

    public ScreenManager(AudioPlayer audio) {
        this.audio = audio;
    }

    /** The sound player every screen shares. */
    public AudioPlayer getAudio() {
        return audio;
    }

    public void register(GameState state, Screen screen) {
        screens.put(state, screen);
    }

    /** Shows the first screen, without a fade. */
    public void start(GameState first) {
        current = first;
        screens.get(current).onEnter();
    }

    /** Fades out, switches to the target screen, and fades back in. */
    public void go(GameState target) {
        if (next == null) {
            next = target;
            overlay = null;
        }
    }

    public void update(double dt) {
        if (next != null) {
            // Fading out. The old screen is frozen until the switch.
            darkness += dt / FADE_SECONDS;
            if (darkness >= 1) {
                darkness = 1;
                screens.get(current).onExit();
                current = next;
                next = null;
                screens.get(current).onEnter();
            }
            return;
        }
        if (darkness > 0) {
            darkness = Math.max(0, darkness - dt / FADE_SECONDS);
        }
        if (overlay != null) {
            overlay.update(dt);   // paused: only the overlay runs
        } else {
            screens.get(current).update(dt);
        }
    }

    public void paint(Graphics2D g) {
        screens.get(current).paint(g);
        if (overlay != null) {
            overlay.paint(g);
        }
        if (darkness > 0) {
            g.setColor(new Color(0f, 0f, 0f, (float) darkness));
            g.fillRect(0, 0, GameConstants.WORLD_WIDTH, GameConstants.WORLD_HEIGHT);
        }
    }

    public void keyPressed(int keyCode) {
        if (isFading()) {
            return;   // no input in the middle of a screen change
        }
        if (overlay != null) {
            overlay.keyPressed(keyCode);
        } else {
            screens.get(current).keyPressed(keyCode);
        }
    }

    public void keyTyped(char character) {
        if (!isFading() && overlay == null) {
            screens.get(current).keyTyped(character);
        }
    }

    private boolean isFading() {
        return next != null || darkness > 0;
    }

    // ---- Pause ----

    /** Freezes the current screen and shows the overlay on top of it. */
    public void pause(Screen pauseOverlay) {
        if (overlay == null && !isFading()) {
            overlay = pauseOverlay;
            overlay.onEnter();
        }
    }

    public void resume() {
        overlay = null;
    }

    public boolean isPaused() {
        return overlay != null;
    }

    public GameState getState() {
        return current;
    }

    // ---- The run in progress ----

    /** Starts a new run through the story from chapter 1. */
    public void startNewRun() {
        session = new GameSession(new Random());
    }

    public GameSession getSession() {
        return session;
    }
}
