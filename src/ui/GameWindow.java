package ui;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
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
    private Rectangle windowedBounds;
    private int windowedExtendedState;

    public GameWindow(GamePanel panel) {
        this.panel = panel;
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(panel);
        frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentMoved(ComponentEvent e) {
                rememberWindowedBounds();
            }

            @Override
            public void componentResized(ComponentEvent e) {
                rememberWindowedBounds();
            }
        });

        // F11 works whatever has the keyboard focus inside the window.
        panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(FULLSCREEN_KEY), FULLSCREEN_ACTION);
        panel.getActionMap().put(FULLSCREEN_ACTION, new ToggleFullscreen());
    }

    /** Opens the window in the middle of the screen and starts the game. */
    public void open() {
        frame.addNotify();
        panel.setPreferredSize(startingSize());
        frame.pack();                       // size the window to fit the panel
        frame.setLocationRelativeTo(null);  // center on screen
        Rectangle workArea = usableWorkArea(frame.getGraphicsConfiguration());
        frame.setBounds(clampToWorkArea(frame.getBounds(), workArea));
        windowedBounds = frame.getBounds();
        frame.setVisible(true);
        panel.start();
    }

    private void rememberWindowedBounds() {
        if (!frame.isUndecorated() && frame.isDisplayable()
                && (frame.getExtendedState() & Frame.MAXIMIZED_BOTH) == 0) {
            windowedBounds = frame.getBounds();
        }
    }

    /**
     * The biggest whole-number enlargement of the game (1x, 2x, 3x...) that
     * fits inside the work area's target share including window decorations,
     * shrinking proportionally below 1x when the screen itself is smaller.
     */
    private Dimension startingSize() {
        Rectangle workArea = usableWorkArea(frame.getGraphicsConfiguration());
        return startingSizeFor(workArea.getSize(), frame.getInsets());
    }

    /** Computes the initial content size for the given usable screen size and frame insets. */
    public static Dimension startingSizeFor(Dimension usableWorkArea, Insets decorations) {
        int insetX = Math.max(0, decorations.left) + Math.max(0, decorations.right);
        int insetY = Math.max(0, decorations.top) + Math.max(0, decorations.bottom);
        int availW = Math.max(1, usableWorkArea.width - insetX);
        int availH = Math.max(1, usableWorkArea.height - insetY);
        double shareW = Math.min(availW, usableWorkArea.width * SCREEN_SHARE - insetX);
        double shareH = Math.min(availH, usableWorkArea.height * SCREEN_SHARE - insetY);

        int intScale = (int) Math.floor(Math.min(
                shareW / GameConstants.WORLD_WIDTH, shareH / GameConstants.WORLD_HEIGHT));
        if (intScale >= 1) {
            return new Dimension(GameConstants.WORLD_WIDTH * intScale, GameConstants.WORLD_HEIGHT * intScale);
        }
        if (GameConstants.WORLD_WIDTH <= availW && GameConstants.WORLD_HEIGHT <= availH) {
            return new Dimension(GameConstants.WORLD_WIDTH, GameConstants.WORLD_HEIGHT);
        }
        double fit = Math.min((double) availW / GameConstants.WORLD_WIDTH,
                (double) availH / GameConstants.WORLD_HEIGHT);
        return new Dimension(
                Math.max(1, (int) Math.floor(GameConstants.WORLD_WIDTH * fit)),
                Math.max(1, (int) Math.floor(GameConstants.WORLD_HEIGHT * fit)));
    }

    /** Fits and moves bounds so the entire rectangle stays inside the given work area. */
    public static Rectangle clampToWorkArea(Rectangle bounds, Rectangle workArea) {
        int width = Math.max(1, Math.min(bounds.width, workArea.width));
        int height = Math.max(1, Math.min(bounds.height, workArea.height));
        int minX = workArea.x;
        int maxX = workArea.x + Math.max(0, workArea.width - width);
        int minY = workArea.y;
        int maxY = workArea.y + Math.max(0, workArea.height - height);
        int x = Math.max(minX, Math.min(bounds.x, maxX));
        int y = Math.max(minY, Math.min(bounds.y, maxY));
        return new Rectangle(x, y, width, height);
    }

    private static Rectangle usableWorkArea(GraphicsConfiguration config) {
        Rectangle bounds = config.getBounds();
        Insets bars = Toolkit.getDefaultToolkit().getScreenInsets(config);
        int width = Math.max(1, bounds.width - bars.left - bars.right);
        int height = Math.max(1, bounds.height - bars.top - bars.bottom);
        return new Rectangle(bounds.x + bars.left, bounds.y + bars.top, width, height);
    }

    private static Rectangle bestWorkAreaFor(Rectangle saved, GraphicsConfiguration fallback) {
        Rectangle bestArea = usableWorkArea(fallback);
        if (saved == null) {
            return bestArea;
        }
        long bestOverlap = 0;
        for (GraphicsDevice device : GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()) {
            Rectangle area = usableWorkArea(device.getDefaultConfiguration());
            Rectangle hit = saved.intersection(area);
            if (!hit.isEmpty()) {
                long overlap = (long) hit.width * hit.height;
                if (overlap > bestOverlap) {
                    bestOverlap = overlap;
                    bestArea = area;
                }
            }
        }
        return bestArea;
    }

    /** F11: full screen if the window is a normal one, back to a window if it is full screen. */
    @SuppressWarnings("serial")   // Swing actions are Serializable by inheritance; this one is never saved
    private final class ToggleFullscreen extends AbstractAction {

        @Override
        public void actionPerformed(ActionEvent e) {
            GraphicsDevice device = frame.getGraphicsConfiguration().getDevice();
            boolean goFull = device.getFullScreenWindow() != frame;
            if (goFull) {
                windowedExtendedState = frame.getExtendedState();
                if ((windowedExtendedState & Frame.MAXIMIZED_BOTH) == 0) {
                    windowedBounds = frame.getBounds();
                }
            }

            // A window's border can only be changed while the window is not on screen.
            frame.dispose();
            frame.setUndecorated(goFull);
            device.setFullScreenWindow(goFull ? frame : null);
            if (!goFull) {
                Rectangle work = bestWorkAreaFor(windowedBounds, frame.getGraphicsConfiguration());
                if (windowedBounds != null) {
                    frame.setBounds(clampToWorkArea(windowedBounds, work));
                } else {
                    frame.pack();
                    frame.setLocationRelativeTo(null);
                    frame.setBounds(clampToWorkArea(frame.getBounds(), work));
                }
                if ((windowedExtendedState & Frame.MAXIMIZED_BOTH) != 0) {
                    frame.setExtendedState(windowedExtendedState);
                }
            }
            frame.setVisible(true);
            panel.requestFocusInWindow();
        }
    }
}
