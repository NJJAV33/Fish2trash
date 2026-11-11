package ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.Timer;
import game.GameManager;
import model.Boat;
import model.Hook;
import model.OceanObject;
import model.Player;
import util.GameConstants;

/**
 * The game screen: draws everything and turns key presses into game actions.
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

    private final GameManager manager = new GameManager();
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

        manager.update(dt);   // ignored by the manager once the game is over
        repaint();
    }

    /**
     * Key bindings work whenever the window is focused, unlike KeyListener,
     * which needs this exact panel to have keyboard focus.
     */
    private void setupKeyBindings() {
        InputMap inputs = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actions = getActionMap();

        inputs.put(KeyStroke.getKeyStroke("SPACE"), "launchHook");
        actions.put("launchHook", new AbstractAction() {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
                manager.launchHook();
            }
        });

        inputs.put(KeyStroke.getKeyStroke("R"), "restart");
        actions.put("restart", new AbstractAction() {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
                manager.restart();
            }
        });
    }

    // ---- Drawing ----

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawBackground(g2);
        drawBoat(g2);
        for (OceanObject object : manager.getObjects()) {
            object.draw(g2);   // POLYMORPHISM: each object draws itself its own way
        }
        drawHook(g2);
        drawHud(g2);

        if (!manager.isPlaying()) {
            drawEndScreen(g2);
        }
    }

    private void drawBackground(Graphics2D g) {
        int w = GameConstants.WORLD_WIDTH;
        int h = GameConstants.WORLD_HEIGHT;
        int waterY = GameConstants.WATER_Y;

        g.setPaint(new GradientPaint(0, 0, new Color(150, 205, 245), 0, waterY, new Color(205, 235, 255)));
        g.fillRect(0, 0, w, waterY);

        g.setColor(new Color(255, 220, 90));       // sun
        g.fillOval(w - 110, 20, 60, 60);

        g.setPaint(new GradientPaint(0, waterY, new Color(35, 125, 195), 0, h, new Color(10, 40, 90)));
        g.fillRect(0, waterY, w, h - waterY);

        g.setColor(new Color(194, 170, 120));      // sea floor
        g.fillRect(0, h - 14, w, 14);
    }

    private void drawBoat(Graphics2D g) {
        Boat boat = manager.getBoat();
        int cx = (int) boat.getX();
        int waterline = (int) boat.getY();
        int half = GameConstants.BOAT_WIDTH / 2;

        // Cabin
        g.setColor(new Color(240, 240, 235));
        g.fillRect(cx - 25, waterline - GameConstants.BOAT_HEIGHT - 22, 40, 24);
        g.setColor(new Color(110, 170, 210));
        g.fillRect(cx - 18, waterline - GameConstants.BOAT_HEIGHT - 16, 12, 10);

        // Hull: a trapezoid that sits slightly in the water
        int[] hullX = {cx - half, cx + half, cx + half - 18, cx - half + 18};
        int[] hullY = {waterline - GameConstants.BOAT_HEIGHT, waterline - GameConstants.BOAT_HEIGHT, waterline + 4, waterline + 4};
        g.setColor(new Color(170, 60, 45));
        g.fillPolygon(hullX, hullY, 4);
        g.setColor(new Color(110, 35, 25));
        g.drawPolygon(hullX, hullY, 4);
    }

    private void drawHook(Graphics2D g) {
        Hook hook = manager.getBoat().getHook();
        int ax = (int) hook.getAnchorX();
        int ay = (int) hook.getAnchorY();
        int tx = (int) hook.getTipX();
        int ty = (int) hook.getTipY();

        g.setColor(new Color(40, 40, 40));
        g.setStroke(new BasicStroke(2));
        g.drawLine(ax, ay, tx, ty);                // rope

        g.setColor(new Color(200, 200, 205));
        g.setStroke(new BasicStroke(3));
        g.drawArc(tx - 7, ty - 3, 14, 14, 180, 200);  // hook curve
        g.fillOval(tx - 3, ty - 3, 6, 6);
        g.setStroke(new BasicStroke(1));
    }

    private void drawHud(Graphics2D g) {
        Player player = manager.getPlayer();
        int secondsLeft = (int) Math.ceil(manager.getTimeLeft());

        g.setColor(new Color(0, 0, 0, 120));
        g.fillRoundRect(10, 10, 200, 92, 12, 12);

        g.setColor(Color.WHITE);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        g.drawString("Score: " + player.getScore() + " / " + GameConstants.QUOTA_CH1, 22, 36);
        g.drawString("Health: " + player.getHealth(), 22, 62);
        g.drawString("Time: " + secondsLeft + "s", 22, 88);

        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        g.drawString("SPACE: launch hook    R: restart", 12, GameConstants.WORLD_HEIGHT - 22);
    }

    private void drawEndScreen(Graphics2D g) {
        boolean won = manager.getState() == GameManager.State.WON;

        g.setColor(new Color(0, 0, 0, 160));
        g.fillRect(0, 0, GameConstants.WORLD_WIDTH, GameConstants.WORLD_HEIGHT);

        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 56));
        g.setColor(won ? new Color(120, 230, 120) : new Color(240, 90, 90));
        drawCentered(g, won ? "YOU WIN!" : "GAME OVER", 250);

        g.setColor(Color.WHITE);
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 24));
        drawCentered(g, manager.getEndMessage(), 300);
        drawCentered(g, "Final score: " + manager.getPlayer().getScore(), 340);
        drawCentered(g, "Press R to restart", 400);
    }

    private void drawCentered(Graphics2D g, String text, int y) {
        FontMetrics metrics = g.getFontMetrics();
        int x = (GameConstants.WORLD_WIDTH - metrics.stringWidth(text)) / 2;
        g.drawString(text, x, y);
    }
}
