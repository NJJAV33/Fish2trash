package ui;

import game.GameState;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import ui.render.Fonts;
import ui.render.TextDraw;
import ui.render.UiPalette;
import util.GameConstants;
import util.StoryText;

/** Drawn on top of the frozen game while it is paused. */
public final class PauseOverlay implements Screen {

    private static final int CENTER_X = GameConstants.WORLD_WIDTH / 2;

    private final ScreenManager screens;
    private final MenuList menu = new MenuList();

    public PauseOverlay(ScreenManager screens) {
        this.screens = screens;
        menu.add(StoryText.PAUSE_RESUME, screens::resume);
        // Entering the playing screen again starts a fresh attempt at the chapter.
        menu.add(StoryText.PAUSE_RESTART, () -> screens.go(GameState.PLAYING));
        menu.add(StoryText.PAUSE_QUIT, () -> screens.go(GameState.MENU));
    }

    @Override
    public void onEnter() {
        menu.reset();
    }

    @Override
    public void keyPressed(int keyCode) {
        if (keyCode == KeyEvent.VK_P || keyCode == KeyEvent.VK_ESCAPE) {
            screens.resume();
        } else {
            menu.keyPressed(keyCode);
        }
    }

    @Override
    public void paint(Graphics2D g) {
        g.setColor(UiPalette.SHADE);
        g.fillRect(0, 0, GameConstants.WORLD_WIDTH, GameConstants.WORLD_HEIGHT);

        g.setFont(Fonts.BANNER);
        g.setColor(UiPalette.TEXT);
        TextDraw.centered(g, StoryText.PAUSE_TITLE, CENTER_X, 220);

        menu.paint(g, CENTER_X, 300, 46);
    }
}
