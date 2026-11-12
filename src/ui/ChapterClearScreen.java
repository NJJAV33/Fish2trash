package ui;

import game.ChapterResult;
import game.GameState;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import ui.render.ChapterPalette;
import ui.render.Fonts;
import ui.render.SceneRenderer;
import ui.render.TextDraw;
import ui.render.UiPalette;
import util.GameConstants;
import util.SoundEffect;
import util.StoryText;

/** The report card after a cleared chapter. */
public final class ChapterClearScreen implements Screen {

    private static final int CENTER_X = GameConstants.WORLD_WIDTH / 2;
    private static final int LABEL_X = 230;
    private static final int VALUE_X = 570;
    private static final double CLEAN_UP_SECONDS = 2.0;   // how long the water takes to clear

    private final ScreenManager screens;
    private final SceneRenderer backdrop;
    private ChapterResult result;
    private double time;

    public ChapterClearScreen(ScreenManager screens, SceneRenderer backdrop) {
        this.screens = screens;
        this.backdrop = backdrop;
    }

    @Override
    public void onEnter() {
        result = screens.getSession().getLastResult();
        time = 0;
    }

    @Override
    public void update(double dt) {
        time += dt;
    }

    @Override
    public void keyPressed(int keyCode) {
        if (keyCode == KeyEvent.VK_ENTER || keyCode == KeyEvent.VK_SPACE) {
            // After the last chapter comes the ending; otherwise the next chapter's story.
            screens.getAudio().play(SoundEffect.MENU_SELECT);
            screens.go(screens.getSession().isFinished() ? GameState.ENDING : GameState.STORY);
        }
    }

    @Override
    public void paint(Graphics2D g) {
        // The sea visibly clears up behind the report card.
        ChapterPalette before = ChapterPalette.forChapter(result.getChapter());
        backdrop.paintBackdropBlend(g, before, before.getCleaned(), time / CLEAN_UP_SECONDS);
        g.setColor(UiPalette.LIGHT_SHADE);
        g.fillRect(0, 0, GameConstants.WORLD_WIDTH, GameConstants.WORLD_HEIGHT);

        g.setColor(UiPalette.PANEL_SOLID);
        g.fillRoundRect(150, 90, 500, 420, 18, 18);
        g.setColor(UiPalette.PANEL_EDGE);
        g.drawRoundRect(150, 90, 500, 420, 18, 18);

        g.setFont(Fonts.BANNER);
        g.setColor(UiPalette.GOOD);
        TextDraw.centered(g, StoryText.CLEAR_TITLE, CENTER_X, 160);
        g.setFont(Fonts.LABEL);
        g.setColor(UiPalette.DIM_TEXT);
        TextDraw.centered(g, StoryText.chapterTitle(result.getChapter()), CENTER_X, 190);

        drawRow(g, StoryText.CLEAR_POINTS, String.valueOf(result.getPoints()), 260);
        drawRow(g, StoryText.CLEAR_TIME, (int) result.getTimeLeft() + " s", 300);
        drawRow(g, StoryText.CLEAR_HEALTH, result.getHealthLeft() + " / " + result.getMaxHealth(), 340);
        drawRow(g, StoryText.CLEAR_FISH, String.valueOf(result.getFishHits()), 380);

        g.setFont(Fonts.LABEL);
        g.setColor(UiPalette.ACCENT);
        TextDraw.centered(g, StoryText.CONTINUE_PROMPT, CENTER_X, 480);
    }

    private void drawRow(Graphics2D g, String label, String value, int y) {
        g.setFont(Fonts.SUBHEADING);
        g.setColor(UiPalette.DIM_TEXT);
        g.drawString(label, LABEL_X, y);
        g.setColor(UiPalette.TEXT);
        TextDraw.rightAligned(g, value, VALUE_X, y);
    }
}
