package ui;

import game.GameManager;
import game.GameSession;
import game.GameState;
import game.HighScoreTable;
import game.LevelProgress;
import game.LevelResult;
import game.ScoreEntry;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.time.LocalDate;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import model.Difficulty;
import util.AudioPlayer;
import util.GameConstants;
import util.StoryText;

/**
 * Knows which screen is showing and moves between screens with a short fade.
 * It also holds what every screen needs: the run in progress (the
 * GameSession), the chosen difficulty, the high-score list, how far the
 * player has got (for the level select) and the sound player.
 */
public final class ScreenManager {

    private static final double FADE_SECONDS = 0.25;

    // One screen object per state, looked up by the state - no if/else chain.
    private final Map<GameState, Screen> screens = new EnumMap<>(GameState.class);
    private GameState current;
    private GameState next;     // where a fade-out is heading, or null
    private double darkness;    // 0 = screen fully visible, 1 = fully black
    private Screen overlay;     // drawn on top while the game is paused, or null
    private Screen pendingPause;
    private GameSession session;
    private Difficulty difficulty = Difficulty.EASY;
    private final AudioPlayer audio;
    private final HighScoreTable highScores;
    private final LevelProgress progress;
    private Runnable accessibilityListener;

    public ScreenManager(AudioPlayer audio, HighScoreTable highScores, LevelProgress progress) {
        this.audio = audio;
        this.highScores = highScores;
        this.progress = progress;
    }

    /** The sound player every screen shares. */
    public AudioPlayer getAudio() {
        return audio;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    public void cycleDifficulty(int direction) {
        difficulty = direction < 0 ? difficulty.previous() : difficulty.next();
        notifyAccessibility();
    }

    public void register(GameState state, Screen screen) {
        screens.put(state, screen);
    }

    /** Shows the first screen, without a fade. */
    public void start(GameState first) {
        current = first;
        screens.get(current).onEnter();
        notifyAccessibility();
    }

    /** Fades out, switches to the target screen, and fades back in. */
    public void go(GameState target) {
        if (next == null) {
            next = target;
            overlay = null;
            pendingPause = null;
        }
    }

    /** Counts an abandoned level attempt as a retry and restarts the current level. */
    public void restartLevel() {
        if (session != null) {
            session.recordRetry();
        }
        go(GameState.PLAYING);
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
                if (pendingPause != null) {
                    overlay = pendingPause;
                    overlay.onEnter();
                    pendingPause = null;
                }
                notifyAccessibility();
            }
            return;
        }
        if (darkness > 0) {
            darkness = Math.max(0, darkness - dt / FADE_SECONDS);
        }
        if (pendingPause != null) {
            overlay = pendingPause;
            overlay.onEnter();
            pendingPause = null;
            notifyAccessibility();
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
        if (keyCode == KeyEvent.VK_M && !isTakingText()) {
            audio.setMuted(!audio.isMuted());   // M switches the sound on any screen
            notifyAccessibility();
        }
        if (isFading()) {
            return;   // no input in the middle of a screen change
        }
        if (overlay != null) {
            overlay.keyPressed(keyCode);
        } else {
            screens.get(current).keyPressed(keyCode);
        }
        notifyAccessibility();
    }

    public void keyTyped(char character) {
        if (!isFading() && overlay == null) {
            screens.get(current).keyTyped(character);
            notifyAccessibility();
        }
    }

    /** True while the screen in front is a text field, where M is just a letter. */
    boolean isTakingText() {
        return overlay == null && screens.get(current).isTakingText();
    }

    private boolean isFading() {
        return next != null || darkness > 0;
    }

    // ---- Pause ----

    /** Freezes the current screen and shows the overlay on top of it. */
    public void pause(Screen pauseOverlay) {
        if (overlay == null) {
            if (isFading()) {
                pendingPause = pauseOverlay;
            } else {
                overlay = pauseOverlay;
                overlay.onEnter();
                notifyAccessibility();
            }
        }
    }

    public void resume() {
        overlay = null;
        pendingPause = null;
        notifyAccessibility();
    }

    public boolean isPaused() {
        return overlay != null || pendingPause != null;
    }

    public GameState getState() {
        return current;
    }

    public GameState getNext() {
        return next;
    }

    // ---- The run in progress ----

    /** Starts a new run through the story from level 1-1. */
    public void startNewRun() {
        session = new GameSession(new Random(), 1, difficulty);
    }

    /** Starts a new run at a level picked from the level select. */
    public void startRunAt(int levelNumber) {
        session = new GameSession(new Random(), levelNumber, difficulty);
    }

    /**
     * Records how an attempt at a level ended: in the run, and for a cleared
     * level also in the saved progress, which opens it and the next one in
     * the level select.
     */
    public LevelResult endLevel(GameManager game) {
        int levelNumber = game.getLevelData().getNumber();
        LevelResult result = session.onLevelEnded(game);
        if (result.isCleared()) {
            progress.recordCleared(levelNumber);
        }
        return result;
    }

    public GameSession getSession() {
        return session;
    }

    public HighScoreTable getHighScores() {
        return highScores;
    }

    /** The levels cleared so far, kept between launches. */
    public LevelProgress getProgress() {
        return progress;
    }

    /**
     * Where to go when the run is over: to the name entry if its score
     * makes the high-score list, otherwise to the screen given here.
     */
    public GameState afterRun(GameState otherwise) {
        ScoreEntry candidate = ScoreEntry.forRun(StoryText.PLAYER_NAME, session, LocalDate.now());
        return highScores.qualifies(candidate) ? GameState.NAME_ENTRY : otherwise;
    }

    void setAccessibilityListener(Runnable listener) {
        accessibilityListener = listener;
    }

    void notifyAccessibility() {
        if (accessibilityListener != null) {
            accessibilityListener.run();
        }
    }

    private Screen activeScreen() {
        if (overlay != null) {
            return overlay;
        }
        return current == null ? null : screens.get(current);
    }

    public String getAccessibleName() {
        Screen active = activeScreen();
        return active == null ? "" : active.getAccessibleName();
    }

    public String getAccessibleDescription() {
        Screen active = activeScreen();
        return active == null ? "" : active.getAccessibleDescription();
    }

    public List<String> getAccessibleChoices() {
        Screen active = activeScreen();
        return active == null ? Collections.emptyList() : active.getAccessibleChoices();
    }

    public int getAccessibleSelectedIndex() {
        Screen active = activeScreen();
        return active == null ? -1 : active.getAccessibleSelectedIndex();
    }

    public void activateAccessibleChoice(int index) {
        Screen active = activeScreen();
        if (active != null && !isFading()) {
            active.activateAccessibleChoice(index);
            notifyAccessibility();
        }
    }
}
