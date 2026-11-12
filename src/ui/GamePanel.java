package ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.EnumMap;
import java.util.Map;
import java.util.Random;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.Timer;
import game.ChapterStatus;
import game.GameManager;
import model.ChapterData;
import model.Loadout;
import model.Player;
import ui.render.SceneRenderer;
import util.GameConstants;

/**
 * The game screen: shows the scene and HUD and turns key presses into game actions.
 *
 * Updating and painting are kept apart: the timer calls manager.update()
 * to change the game, and paintComponent() only reads the game to draw it.
 */
// Swing panels are Serializable by inheritance, but this game never saves one,
// so the "field is not serializable" warning does not apply here.
@SuppressWarnings("serial")
public final class GamePanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private static final int FRAME_DELAY_MS = 16;      // about 60 frames per second
    private static final double MAX_STEP_SECONDS = 0.05;

    // The line shown on the end screen for each way a chapter can end.
    private static final Map<ChapterStatus, String> END_MESSAGES = new EnumMap<>(ChapterStatus.class);
    static {
        END_MESSAGES.put(ChapterStatus.CLEARED, "Quota reached!");
        END_MESSAGES.put(ChapterStatus.FAILED_TIME, "Time's up!");
        END_MESSAGES.put(ChapterStatus.FAILED_HEALTH, "You caught too many fish!");
    }

    private GameManager manager = new GameManager(ChapterData.forChapter(1), Loadout.standard(), new Random());
    private final SceneRenderer renderer = new SceneRenderer();
    private final InputState input = new InputState();
    private final Timer timer;
    private long lastTickNanos;

    public GamePanel() {
        setPreferredSize(new Dimension(GameConstants.WORLD_WIDTH, GameConstants.WORLD_HEIGHT));
        setupKeyBindings();
        timer = new Timer(FRAME_DELAY_MS, e -> tick());
    }

    public void start() {
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

        manager.setBoatDirection(input.getDirection());
        manager.update(dt);   // ignored by the manager once the game is over
        repaint();
    }

    /**
     * Key bindings work whenever the window is focused, unlike KeyListener,
     * which needs this exact panel to have keyboard focus.
     */
    private void setupKeyBindings() {
        bindKey(KeyEvent.VK_SPACE, false, () -> manager.spaceAction());
        // Restarting is just starting a brand new game.
        bindKey(KeyEvent.VK_R, false, () -> manager = new GameManager(ChapterData.forChapter(1), Loadout.standard(), new Random()));

        // Movement keys are tracked on press AND release, so holding works.
        bindKey(KeyEvent.VK_A, false, () -> input.setLeftHeld(true));
        bindKey(KeyEvent.VK_A, true, () -> input.setLeftHeld(false));
        bindKey(KeyEvent.VK_LEFT, false, () -> input.setLeftHeld(true));
        bindKey(KeyEvent.VK_LEFT, true, () -> input.setLeftHeld(false));
        bindKey(KeyEvent.VK_D, false, () -> input.setRightHeld(true));
        bindKey(KeyEvent.VK_D, true, () -> input.setRightHeld(false));
        bindKey(KeyEvent.VK_RIGHT, false, () -> input.setRightHeld(true));
        bindKey(KeyEvent.VK_RIGHT, true, () -> input.setRightHeld(false));
    }

    /** Runs the action when the key is pressed (or released, if onRelease is true). */
    private void bindKey(int keyCode, boolean onRelease, Runnable action) {
        String name = (onRelease ? "released " : "pressed ") + keyCode;
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(keyCode, 0, onRelease), name);
        getActionMap().put(name, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                action.run();
            }
        });
    }

    // ---- Drawing ----

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        renderer.paint(g2, manager);
        drawHud(g2);

        if (!manager.isPlaying()) {
            drawEndScreen(g2);
        }
    }

    private void drawHud(Graphics2D g) {
        Player player = manager.getPlayer();
        int secondsLeft = (int) Math.ceil(manager.getTimeLeft());

        g.setColor(new Color(0, 0, 0, 120));
        g.fillRoundRect(10, 10, 200, 92, 12, 12);

        g.setColor(Color.WHITE);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        g.drawString("Score: " + player.getScore() + " / " + manager.getQuota(), 22, 36);
        g.drawString("Health: " + player.getHealth() + " / " + player.getMaxHealth(), 22, 62);
        g.drawString("Time: " + secondsLeft + "s", 22, 88);

        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        g.drawString("A / D: move boat    SPACE: drop or recall hook    R: restart", 12, GameConstants.WORLD_HEIGHT - 22);
    }

    private void drawEndScreen(Graphics2D g) {
        boolean won = manager.getStatus() == ChapterStatus.CLEARED;

        g.setColor(new Color(0, 0, 0, 160));
        g.fillRect(0, 0, GameConstants.WORLD_WIDTH, GameConstants.WORLD_HEIGHT);

        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 56));
        g.setColor(won ? new Color(120, 230, 120) : new Color(240, 90, 90));
        drawCentered(g, won ? "YOU WIN!" : "GAME OVER", 250);

        g.setColor(Color.WHITE);
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 24));
        drawCentered(g, END_MESSAGES.get(manager.getStatus()), 300);
        drawCentered(g, "Final score: " + manager.getPlayer().getScore(), 340);
        drawCentered(g, "Press R to restart", 400);
    }

    private void drawCentered(Graphics2D g, String text, int y) {
        FontMetrics metrics = g.getFontMetrics();
        int x = (GameConstants.WORLD_WIDTH - metrics.stringWidth(text)) / 2;
        g.drawString(text, x, y);
    }
}
