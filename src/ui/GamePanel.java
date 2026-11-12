package ui;

import game.GameState;
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
import util.GameConstants;

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
    private final ScreenManager screens = new ScreenManager();
    private final GameScreen gameScreen;
    private final Timer timer;
    private long lastTickNanos;

    public GamePanel() {
        setPreferredSize(new Dimension(GameConstants.WORLD_WIDTH, GameConstants.WORLD_HEIGHT));
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
        SceneRenderer renderer = new SceneRenderer();
        gameScreen = new GameScreen(screens, input, renderer);
        screens.register(GameState.MENU, new MainMenu(screens, renderer));
        screens.register(GameState.HOW_TO, new HowToScreen(screens, renderer));
        screens.register(GameState.STORY, new StoryScreen(screens, renderer));
        screens.register(GameState.PLAYING, gameScreen);
        screens.register(GameState.CHAPTER_CLEAR, new ChapterClearScreen(screens, renderer));
        screens.register(GameState.GAME_OVER, new GameOverScreen(screens, renderer));
        screens.register(GameState.ENDING, new VictoryScreen(screens));
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
        screens.update(dt);
    }

    /** Which screen is showing. */
    public GameState getState() {
        return screens.getState();
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
            if (fresh) {
                screens.keyPressed(e.getKeyCode());   // repeats of a held key are not passed on
            }
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
