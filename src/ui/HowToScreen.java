package ui;

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

/** One page that explains the controls and the rules. */
public final class HowToScreen implements Screen {

    private static final int CENTER_X = GameConstants.WORLD_WIDTH / 2;

    private final ScreenManager screens;
    private final SceneRenderer backdrop;

    public HowToScreen(ScreenManager screens, SceneRenderer backdrop) {
        this.screens = screens;
        this.backdrop = backdrop;
    }

    @Override
    public void keyPressed(int keyCode) {
        if (keyCode == KeyEvent.VK_ENTER || keyCode == KeyEvent.VK_ESCAPE) {
            screens.getAudio().play(SoundEffect.MENU_SELECT);
            screens.go(GameState.MENU);
        }
    }

    @Override
    public void paint(Graphics2D g) {
        backdrop.paintBackdrop(g, ChapterPalette.MENU);
        g.setColor(UiPalette.PANEL_SOLID);
        g.fillRoundRect(90, 70, 620, 460, 18, 18);
        g.setColor(UiPalette.PANEL_EDGE);
        g.drawRoundRect(90, 70, 620, 460, 18, 18);

        g.setFont(Fonts.HEADING);
        g.setColor(UiPalette.ACCENT);
        TextDraw.centered(g, StoryText.HOW_TO_TITLE, CENTER_X, 125);

        g.setFont(Fonts.MONO);
        g.setColor(UiPalette.TEXT);
        TextDraw.lines(g, StoryText.HOW_TO_TEXT, 125, 180, 26);

        g.setFont(Fonts.LABEL);
        g.setColor(UiPalette.ACCENT);
        TextDraw.centered(g, StoryText.BACK_PROMPT, CENTER_X, 500);
    }
}
