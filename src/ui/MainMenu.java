package ui;

import game.GameState;
import java.awt.Graphics2D;
import ui.render.ChapterPalette;
import ui.render.Fonts;
import ui.render.SceneRenderer;
import ui.render.TextDraw;
import ui.render.UiPalette;
import util.GameConstants;
import util.StoryText;

/** The title screen: start the story, read how to play, or quit. */
public final class MainMenu implements Screen {

    private static final int CENTER_X = GameConstants.WORLD_WIDTH / 2;
    private static final int BOAT_X = 170;   // the boat idles to the left of the title

    private final ScreenManager screens;
    private final SceneRenderer backdrop;
    private final MenuList menu = new MenuList();

    public MainMenu(ScreenManager screens, SceneRenderer backdrop) {
        this.screens = screens;
        this.backdrop = backdrop;
        menu.add(StoryText.MENU_START, this::startStory);
        menu.add(StoryText.MENU_HOW_TO, () -> screens.go(GameState.HOW_TO));
        menu.add(StoryText.MENU_QUIT, () -> System.exit(0));
    }

    private void startStory() {
        screens.startNewRun();
        screens.go(GameState.STORY);
    }

    @Override
    public void onEnter() {
        menu.reset();
    }

    @Override
    public void keyPressed(int keyCode) {
        menu.keyPressed(keyCode);
    }

    @Override
    public void paint(Graphics2D g) {
        backdrop.paintBackdropWithBoat(g, ChapterPalette.MENU, BOAT_X);
        g.setColor(UiPalette.LIGHT_SHADE);
        g.fillRect(0, 0, GameConstants.WORLD_WIDTH, GameConstants.WORLD_HEIGHT);

        g.setFont(Fonts.TITLE);
        g.setColor(UiPalette.SHADE);
        TextDraw.centered(g, StoryText.GAME_TITLE, CENTER_X + 3, 253);   // drop shadow
        g.setColor(UiPalette.TEXT);
        TextDraw.centered(g, StoryText.GAME_TITLE, CENTER_X, 250);

        g.setFont(Fonts.BODY);
        g.setColor(UiPalette.ACCENT);
        TextDraw.centered(g, StoryText.TAGLINE, CENTER_X, 285);

        menu.paint(g, CENTER_X, 360, 46);

        g.setFont(Fonts.SMALL);
        g.setColor(UiPalette.DIM_TEXT);
        TextDraw.centered(g, StoryText.MENU_HINT, CENTER_X, 545);
        TextDraw.centered(g, StoryText.CREDITS, CENTER_X, 572);
    }
}
