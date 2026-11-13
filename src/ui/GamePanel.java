package ui;

import game.GameState;
import game.HighScoreTable;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.JPanel;
import javax.swing.Timer;
import ui.render.SceneRenderer;
import util.AudioPlayer;
import util.GameConstants;
import util.SoundState;

/**
 * The window's content: it runs the frame timer, receives the keyboard, and
 * hands both to the ScreenManager. It does not know any game rules.
 *
 * Updating and painting are kept apart: the timer updates the current screen,
 * and paintComponent() only draws it.
 */
// Swing panels are Serializable by inheritance, but this game never saves one,
// so the "field is not serializable" warning does not apply here.
@SuppressWarnings("serial")
public final class GamePanel extends JPanel {

    private static final int FRAME_DELAY_MS = 16;      // about 60 frames per second
    private static final double MAX_STEP_SECONDS = 0.05;

    private final InputState input = new InputState();
    private final AudioPlayer audio;
    private final ScreenManager screens;
    private final SceneRenderer renderer = new SceneRenderer();
    private final GameScreen gameScreen;
    private final Timer timer;
    private long lastTickNanos;

    /**
     * @param audio      how the game makes sound; tests pass a silent player
     * @param highScores the high-score list; tests pass one kept in a temporary folder
     */
    public GamePanel(AudioPlayer audio, HighScoreTable highScores) {
        this.audio = audio;
        this.screens = new ScreenManager(audio, highScores);
        setPreferredSize(new Dimension(GameConstants.WORLD_WIDTH, GameConstants.WORLD_HEIGHT));
        setBackground(Color.BLACK);   // shows at the edges when the scene shakes
        setFocusable(true);   // a panel only receives keys if it can have the focus
        addKeyListener(new KeyHandler());
        addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                // Another window took the keyboard: no key is "still held",
                // and a chapter in progress should not run unattended.
                input.releaseAll();
                if (screens.getState() == GameState.PLAYING) {
                    gameScreen.pause();
                }
            }
        });

        // One screen per state. They share one renderer for their backdrops.
        gameScreen = new GameScreen(screens, input, renderer);
        screens.register(GameState.MENU, new MainMenu(screens, renderer));
        screens.register(GameState.HOW_TO, new HowToScreen(screens, renderer));
        screens.register(GameState.STORY, new StoryScreen(screens, renderer));
        screens.register(GameState.PLAYING, gameScreen);
        screens.register(GameState.CHAPTER_CLEAR, new ChapterClearScreen(screens, renderer));
        screens.register(GameState.GAME_OVER, new GameOverScreen(screens, renderer));
        screens.register(GameState.ENDING, new VictoryScreen(screens, renderer));
        screens.register(GameState.NAME_ENTRY, new NameEntryScreen(screens, renderer));
        screens.register(GameState.HIGH_SCORES, new HighScoreScreen(screens, renderer));
        screens.start(GameState.MENU);

        timer = new Timer(FRAME_DELAY_MS, e -> tick());
    }

    public void start() {
        requestFocusInWindow();
        lastTickNanos = System.nanoTime();
        timer.start();
    }

    /** Runs every frame on the event-dispatch thread. */
    private void tick() {
        // Use the real time between frames (dt) so movement and the countdown
        // run at the same speed even if a frame arrives late.
        long now = System.nanoTime();
        double dt = (now - lastTickNanos) / 1_000_000_000.0;
        lastTickNanos = now;
        dt = Math.min(dt, MAX_STEP_SECONDS);   // avoid a huge jump after a pause

        advance(dt);
        repaint();
    }

    /** Moves the game forward by dt seconds. The timer calls this; tests can call it directly. */
    public void advance(double dt) {
        renderer.update(dt);   // water and bubbles keep moving on every screen, even when paused
        screens.update(dt);
    }

    /** Which screen is showing. */
    public GameState getState() {
        return screens.getState();
    }

    /** Whether sound is on, muted or not available. */
    public SoundState getSoundState() {
        return audio.getState();
    }

    /** True while the pause menu is up. */
    public boolean isPaused() {
        return screens.isPaused();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        screens.paint(g2);
    }

    /** Turns raw keyboard events into held keys and fresh key presses. */
    private final class KeyHandler extends KeyAdapter {

        @Override
        public void keyPressed(KeyEvent e) {
            boolean fresh = input.press(e.getKeyCode(), e.getWhen());
            if (!fresh) {
                return;   // repeats of a held key are not passed on
            }
            screens.keyPressed(e.getKeyCode());
        }

        @Override
        public void keyReleased(KeyEvent e) {
            input.release(e.getKeyCode(), e.getWhen());
        }

        @Override
        public void keyTyped(KeyEvent e) {
            screens.keyTyped(e.getKeyChar());
        }
    }
}
