package ui;

import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.KeyStroke;
import util.GameConstants;
import util.StoryText;

/**
 * The window around the game. It picks a starting size that suits the
 * screen, lets the player resize it, and switches to full screen and back
 * with F11.
 *
 * The game itself is always 800 x 600 inside: the GamePanel scales its
 * picture to whatever size this window gives it.
 */
public final class GameWindow {

    private static final String FULLSCREEN_KEY = "released F11";   // on release, so holding the key switches once
    private static final String FULLSCREEN_ACTION = "toggleFullscreen";
    private static final double SCREEN_SHARE = 0.8;   // the window starts at most this much of the screen

    private final JFrame frame = new JFrame(StoryText.GAME_TITLE);
    private final GamePanel panel;

    public GameWindow(GamePanel panel) {
        this.panel = panel;
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(panel);

        // F11 works whatever has the keyboard focus inside the window.
        panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(FULLSCREEN_KEY), FULLSCREEN_ACTION);
        panel.getActionMap().put(FULLSCREEN_ACTION, new ToggleFullscreen());
    }

    /** Opens the window in the middle of the screen and starts the game. */
    public void open() {
        panel.setPreferredSize(startingSize());
        frame.pack();                       // size the window to fit the panel
        frame.setLocationRelativeTo(null);  // center on screen
        frame.setVisible(true);
        panel.start();
    }

    /**
     * The biggest whole-number enlargement of the game (1x, 2x, 3x...) that
     * still leaves room around the window. Whole numbers keep the picture
     * sharpest; the player can drag the window to any other size.
     */
    private Dimension startingSize() {
        GraphicsConfiguration screen = frame.getGraphicsConfiguration();
        Rectangle bounds = screen.getBounds();
        Insets bars = Toolkit.getDefaultToolkit().getScreenInsets(screen);   // task bars and docks
        double roomX = (bounds.width - bars.left - bars.right) * SCREEN_SHARE;
        double roomY = (bounds.height - bars.top - bars.bottom) * SCREEN_SHARE;
        int scale = (int) Math.max(1, Math.min(roomX / GameConstants.WORLD_WIDTH, roomY / GameConstants.WORLD_HEIGHT));
        return new Dimension(GameConstants.WORLD_WIDTH * scale, GameConstants.WORLD_HEIGHT * scale);
    }

    /** F11: full screen if the window is a normal one, back to a window if it is full screen. */
    @SuppressWarnings("serial")   // Swing actions are Serializable by inheritance; this one is never saved
    private final class ToggleFullscreen extends AbstractAction {

        @Override
        public void actionPerformed(ActionEvent e) {
            GraphicsDevice device = frame.getGraphicsConfiguration().getDevice();
            boolean goFull = device.getFullScreenWindow() != frame;

            // A window's border can only be changed while the window is not on screen.
            frame.dispose();
            frame.setUndecorated(goFull);
            device.setFullScreenWindow(goFull ? frame : null);
            if (!goFull) {
                frame.pack();
                frame.setLocationRelativeTo(null);
            }
            frame.setVisible(true);
            panel.requestFocusInWindow();
        }
    }
}
