package ui;

import game.GameState;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.util.List;
import ui.render.ChapterPalette;
import ui.render.Fonts;
import ui.render.SceneRenderer;
import ui.render.TextDraw;
import ui.render.UiPalette;
import util.GameConstants;
import util.SoundEffect;
import util.StoryText;

/**
 * The title screen: the boat sits in calm water while the player chooses to
 * start, pick a level, read the controls, see the high scores, set the
 * difficulty, or quit.
 */
public final class MainMenu implements Screen {

    private static final int CENTER_X = GameConstants.WORLD_WIDTH / 2;
    private static final int BOAT_X = 635;

    private final ScreenManager screens;
    private final SceneRenderer backdrop;
    private final MenuList menu;

    public MainMenu(ScreenManager screens, SceneRenderer backdrop) {
        this.screens = screens;
        this.backdrop = backdrop;
        this.menu = new MenuList(screens.getAudio());
        menu.add(StoryText.MENU_START, this::startStory);
        menu.add(StoryText.MENU_LEVELS, () -> screens.go(GameState.LEVEL_SELECT));
        menu.add(StoryText.MENU_HOW_TO, () -> screens.go(GameState.HOW_TO));
        menu.add(StoryText.MENU_SCORES, () -> screens.go(GameState.HIGH_SCORES));
        menu.add(
                () -> String.format(StoryText.MENU_DIFFICULTY, screens.getDifficulty().getDisplayName()),
                () -> screens.cycleDifficulty(1),
                () -> screens.cycleDifficulty(-1),
                () -> screens.cycleDifficulty(1));
        menu.add(StoryText.MENU_QUIT, () -> System.exit(0));
    }

    /** A new run from the very start opens with the animation, then the story pages. */
    private void startStory() {
        screens.startNewRun();
        screens.go(GameState.INTRO);
    }

    @Override
    public void onEnter() {
        menu.reset();
    }

    @Override
    public void keyPressed(int keyCode) {
        if (keyCode == KeyEvent.VK_D || keyCode == KeyEvent.VK_RIGHT) {
            screens.cycleDifficulty(1);
            screens.getAudio().play(SoundEffect.MENU_MOVE);
            return;
        }
        if (keyCode == KeyEvent.VK_LEFT) {
            screens.cycleDifficulty(-1);
            screens.getAudio().play(SoundEffect.MENU_MOVE);
            return;
        }
        menu.keyPressed(keyCode);
    }

    @Override
    public String getAccessibleName() {
        return "Main Menu";
    }

    @Override
    public String getAccessibleDescription() {
        return "Selected: " + menu.getSelectedLabel() + ". Difficulty: " + screens.getDifficulty().getDisplayName();
    }

    @Override
    public List<String> getAccessibleChoices() {
        return menu.getLabels();
    }

    @Override
    public int getAccessibleSelectedIndex() {
        return menu.getSelectedIndex();
    }

    @Override
    public void activateAccessibleChoice(int index) {
        menu.selectAndActivate(index);
    }

    @Override
    public void paint(Graphics2D g) {
        backdrop.paintBackdropWithBoat(g, ChapterPalette.MENU, BOAT_X);
        g.setColor(UiPalette.LIGHT_SHADE);
        g.fillRect(0, 0, GameConstants.WORLD_WIDTH, GameConstants.WORLD_HEIGHT);

        g.setFont(Fonts.TITLE);
        g.setColor(UiPalette.SHADE);
        TextDraw.centered(g, StoryText.GAME_TITLE, CENTER_X + 3, 223);   // drop shadow
        g.setColor(UiPalette.TEXT);
        TextDraw.centered(g, StoryText.GAME_TITLE, CENTER_X, 220);

        g.setFont(Fonts.BODY);
        g.setColor(UiPalette.ACCENT);
        TextDraw.centered(g, StoryText.TAGLINE, CENTER_X, 255);

        menu.paint(g, CENTER_X, 304, 38);

        g.setFont(Fonts.SMALL);
        g.setColor(UiPalette.DIM_TEXT);
        TextDraw.centered(g, StoryText.MENU_HINT, CENTER_X, 550);
        TextDraw.centered(g, StoryText.CREDITS, CENTER_X, 575);
    }
}
