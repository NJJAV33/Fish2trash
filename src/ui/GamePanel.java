package ui;

import game.GameState;
import game.HighScoreTable;
import game.LevelProgress;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import javax.accessibility.Accessible;
import javax.accessibility.AccessibleAction;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import javax.accessibility.AccessibleSelection;
import javax.accessibility.AccessibleState;
import javax.accessibility.AccessibleStateSet;
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
 *
 * The game is always laid out for 800 x 600. If the panel is bigger, the
 * whole picture is enlarged to fit, with black bars where the shape of the
 * panel does not match.
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

    // Each frame is drawn into this image first and then copied to the
    // screen in one go. Drawing many smooth-edged shapes straight onto the
    // screen is several times slower.
    private BufferedImage frame;
    private AccessibleGamePanel accessiblePanel;

    /**
     * @param audio      how the game makes sound; tests pass a silent player
     * @param highScores the high-score list; tests pass one kept in a temporary folder
     * @param progress   the levels cleared so far; tests pass one kept in a temporary folder
     */
    public GamePanel(AudioPlayer audio, HighScoreTable highScores, LevelProgress progress) {
        this.audio = audio;
        this.screens = new ScreenManager(audio, highScores, progress);
        this.screens.setAccessibilityListener(this::notifyAccessibility);
        setPreferredSize(new Dimension(GameConstants.WORLD_WIDTH, GameConstants.WORLD_HEIGHT));
        setBackground(Color.BLACK);   // the bars beside the picture when the window has another shape
        setOpaque(true);              // every pixel is painted here, so nothing behind needs painting
        setFocusable(true);   // a panel only receives keys if it can have the focus
        setFocusTraversalKeysEnabled(false);
        addKeyListener(new KeyHandler());
        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                lastTickNanos = System.nanoTime();
            }

            @Override
            public void focusLost(FocusEvent e) {
                // Another window took the keyboard: no key is "still held",
                // and a level in progress should not run unattended.
                input.releaseAll();
                if (screens.getState() == GameState.PLAYING || screens.getNext() == GameState.PLAYING) {
                    gameScreen.pause();
                }
            }
        });

        // One screen per state. They share one renderer for their backdrops.
        gameScreen = new GameScreen(screens, input, renderer);
        screens.register(GameState.MENU, new MainMenu(screens, renderer));
        screens.register(GameState.LEVEL_SELECT, new LevelSelectScreen(screens, renderer));
        screens.register(GameState.HOW_TO, new HowToScreen(screens, renderer));
        screens.register(GameState.STORY, new StoryScreen(screens, renderer));
        screens.register(GameState.PLAYING, gameScreen);
        screens.register(GameState.LEVEL_CLEAR, new LevelClearScreen(screens, renderer));
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

        if (screens.isPaused()) {
            advance(Math.min(dt, MAX_STEP_SECONDS));
            repaint();
            return;
        }

        while (dt > 1e-6) {
            double step = Math.min(dt, MAX_STEP_SECONDS);
            advance(step);
            dt -= step;
            if (screens.isPaused()) {
                break;
            }
        }
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
        // On a high-density display one panel unit is more than one screen
        // pixel. The frame is made in real pixels so it is sharp there too.
        double density = ((Graphics2D) g).getTransform().getScaleX();
        int width = (int) Math.ceil(getWidth() * density);
        int height = (int) Math.ceil(getHeight() * density);
        if (frame == null || frame.getWidth() != width || frame.getHeight() != height) {
            frame = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        }

        // Enlarge the 800 x 600 picture to fit and put it in the middle.
        // Everything is drawn as shapes, so it stays sharp at any size.
        double scale = Math.min((double) width / GameConstants.WORLD_WIDTH,
                (double) height / GameConstants.WORLD_HEIGHT);
        long left = Math.round((width - GameConstants.WORLD_WIDTH * scale) / 2);
        long top = Math.round((height - GameConstants.WORLD_HEIGHT * scale) / 2);

        Graphics2D g2 = frame.createGraphics();
        if (left > 0 || top > 0) {
            g2.setColor(getBackground());   // the bars beside the picture
            g2.fillRect(0, 0, width, height);
        }
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.translate(left, top);
        g2.scale(scale, scale);
        g2.clipRect(0, 0, GameConstants.WORLD_WIDTH, GameConstants.WORLD_HEIGHT);
        screens.paint(g2);
        g2.dispose();

        if (density == 1) {
            g.drawImage(frame, 0, 0, null);   // a plain copy
        } else {
            g.drawImage(frame, 0, 0, getWidth(), getHeight(), null);
        }
    }

    /** How much the 800 x 600 picture is enlarged to fit the panel: the largest size that fits whole. */
    public double getViewScale() {
        return Math.min((double) getWidth() / GameConstants.WORLD_WIDTH,
                (double) getHeight() / GameConstants.WORLD_HEIGHT);
    }

    /** Turns raw keyboard events into held keys and fresh key presses. */
    private final class KeyHandler extends KeyAdapter {

        @Override
        public void keyPressed(KeyEvent e) {
            boolean fresh = input.press(e.getKeyCode(), e.getWhen());
            boolean textBackspaceRepeat = e.getKeyCode() == KeyEvent.VK_BACK_SPACE && screens.isTakingText();
            if (!fresh && !textBackspaceRepeat) {
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

    @Override
    public AccessibleContext getAccessibleContext() {
        if (accessiblePanel == null) {
            accessiblePanel = new AccessibleGamePanel();
            accessibleContext = accessiblePanel;
        }
        return accessiblePanel;
    }

    private void notifyAccessibility() {
        if (accessiblePanel != null) {
            accessiblePanel.notifyChange();
        }
    }

    /** Exposes menus, selections, and live HUD status to assistive technologies. */
    @SuppressWarnings("serial")
    private final class AccessibleGamePanel extends AccessibleJPanel
            implements AccessibleSelection, AccessibleAction {

        private String lastName;
        private String lastDescription;
        private int lastSelected = -1;

        private AccessibleGamePanel() {
            lastName = getAccessibleName();
            lastDescription = getAccessibleDescription();
            lastSelected = screens.getAccessibleSelectedIndex();
        }

        private void notifyChange() {
            String name = getAccessibleName();
            if (!Objects.equals(name, lastName)) {
                firePropertyChange(ACCESSIBLE_NAME_PROPERTY, lastName, name);
                lastName = name;
            }
            String description = getAccessibleDescription();
            if (!Objects.equals(description, lastDescription)) {
                firePropertyChange(ACCESSIBLE_DESCRIPTION_PROPERTY, lastDescription, description);
                lastDescription = description;
            }
            int selected = screens.getAccessibleSelectedIndex();
            if (selected != lastSelected) {
                lastSelected = selected;
                firePropertyChange(ACCESSIBLE_SELECTION_PROPERTY, null, getAccessibleSelection(0));
            }
        }

        @Override
        public String getAccessibleName() {
            String screenName = screens.getAccessibleName();
            return screenName.isEmpty() ? "Fish2Trash" : "Fish2Trash: " + screenName;
        }

        @Override
        public String getAccessibleDescription() {
            return screens.getAccessibleDescription();
        }

        @Override
        public AccessibleRole getAccessibleRole() {
            return AccessibleRole.PANEL;
        }

        @Override
        public int getAccessibleChildrenCount() {
            return screens.getAccessibleChoices().size();
        }

        @Override
        public Accessible getAccessibleChild(int i) {
            List<String> choices = screens.getAccessibleChoices();
            if (i < 0 || i >= choices.size()) {
                return null;
            }
            return new AccessibleChoice(i);
        }

        @Override
        public AccessibleSelection getAccessibleSelection() {
            return this;
        }

        @Override
        public int getAccessibleSelectionCount() {
            int selected = screens.getAccessibleSelectedIndex();
            return selected >= 0 && selected < getAccessibleChildrenCount() ? 1 : 0;
        }

        @Override
        public Accessible getAccessibleSelection(int i) {
            int selected = screens.getAccessibleSelectedIndex();
            if (i == 0 && selected >= 0 && selected < getAccessibleChildrenCount()) {
                return new AccessibleChoice(selected);
            }
            return null;
        }

        @Override
        public boolean isAccessibleChildSelected(int i) {
            return i >= 0 && i == screens.getAccessibleSelectedIndex();
        }

        @Override
        public void addAccessibleSelection(int i) {
            screens.activateAccessibleChoice(i);
        }

        @Override
        public void removeAccessibleSelection(int i) {
        }

        @Override
        public void clearAccessibleSelection() {
        }

        @Override
        public void selectAllAccessibleSelection() {
        }

        @Override
        public AccessibleAction getAccessibleAction() {
            return this;
        }

        @Override
        public int getAccessibleActionCount() {
            return screens.getAccessibleChoices().isEmpty() ? 1 : 2;
        }

        @Override
        public String getAccessibleActionDescription(int i) {
            if (i == 0) {
                return "Read status";
            }
            if (i == 1 && !screens.getAccessibleChoices().isEmpty()) {
                return "Activate selected";
            }
            return null;
        }

        @Override
        public boolean doAccessibleAction(int i) {
            if (i == 0) {
                firePropertyChange(ACCESSIBLE_DESCRIPTION_PROPERTY, null, getAccessibleDescription());
                return true;
            }
            if (i == 1) {
                int selected = screens.getAccessibleSelectedIndex();
                if (selected >= 0) {
                    screens.activateAccessibleChoice(selected);
                    return true;
                }
            }
            return false;
        }
    }

    /** One selectable menu entry or level tile exposed to assistive technology. */
    private final class AccessibleChoice extends AccessibleContext implements Accessible, AccessibleAction {

        private final int index;

        private AccessibleChoice(int index) {
            this.index = index;
        }

        @Override
        public AccessibleContext getAccessibleContext() {
            return this;
        }

        @Override
        public String getAccessibleName() {
            List<String> choices = screens.getAccessibleChoices();
            return index >= 0 && index < choices.size() ? choices.get(index) : "";
        }

        @Override
        public AccessibleRole getAccessibleRole() {
            return AccessibleRole.MENU_ITEM;
        }

        @Override
        public Accessible getAccessibleParent() {
            return GamePanel.this;
        }

        @Override
        public int getAccessibleIndexInParent() {
            return index;
        }

        @Override
        public int getAccessibleChildrenCount() {
            return 0;
        }

        @Override
        public Accessible getAccessibleChild(int i) {
            return null;
        }

        @Override
        public Locale getLocale() {
            return GamePanel.this.getLocale();
        }

        @Override
        public AccessibleStateSet getAccessibleStateSet() {
            AccessibleStateSet states = new AccessibleStateSet();
            states.add(AccessibleState.ENABLED);
            states.add(AccessibleState.FOCUSABLE);
            states.add(AccessibleState.VISIBLE);
            states.add(AccessibleState.SHOWING);
            states.add(AccessibleState.SELECTABLE);
            if (index == screens.getAccessibleSelectedIndex()) {
                states.add(AccessibleState.SELECTED);
                states.add(AccessibleState.FOCUSED);
            }
            return states;
        }

        @Override
        public AccessibleAction getAccessibleAction() {
            return this;
        }

        @Override
        public int getAccessibleActionCount() {
            return 1;
        }

        @Override
        public String getAccessibleActionDescription(int i) {
            return i == 0 ? "Activate " + getAccessibleName() : null;
        }

        @Override
        public boolean doAccessibleAction(int i) {
            if (i != 0) {
                return false;
            }
            screens.activateAccessibleChoice(index);
            return true;
        }
    }
}
