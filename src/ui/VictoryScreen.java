package ui;

import game.GameSession;
import game.GameState;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import ui.render.ChapterPalette;
import ui.render.Fonts;
import ui.render.SceneRenderer;
import ui.render.StoryBoxPainter;
import ui.render.TextDraw;
import ui.render.UiPalette;
import util.GameConstants;
import util.StoryText;

/**
 * The ending: the reef has recovered. First the last story pages, then a
 * short report of the whole run.
 */
public final class VictoryScreen implements Screen {

    private static final int CENTER_X = GameConstants.WORLD_WIDTH / 2;
    private static final int LABEL_X = 230;
    private static final int VALUE_X = 570;

    private final ScreenManager screens;
    private final SceneRenderer backdrop;
    private final StoryBoxPainter box = new StoryBoxPainter();
    private final TextPager pager = new TextPager();
    private boolean showingReport;
    private double time;

    public VictoryScreen(ScreenManager screens, SceneRenderer backdrop) {
        this.screens = screens;
        this.backdrop = backdrop;
    }

    @Override
    public void onEnter() {
        pager.start(new String[] {StoryText.CH3_OUTRO, StoryText.END_P1, StoryText.END_P2, StoryText.END_P3});
        showingReport = false;
        time = 0;
    }

    @Override
    public void update(double dt) {
        time += dt;
        pager.update(dt);
    }

    @Override
    public void keyPressed(int keyCode) {
        if (keyCode != KeyEvent.VK_ENTER && keyCode != KeyEvent.VK_SPACE) {
            return;
        }
        if (showingReport) {
            screens.go(GameState.MENU);
        } else if (pager.advance()) {
            showingReport = true;   // the story is told; show the numbers
        }
    }

    @Override
    public void paint(Graphics2D g) {
        backdrop.paintBackdrop(g, ChapterPalette.REEF);

        g.setFont(Fonts.BANNER);
        g.setColor(UiPalette.SHADE);
        TextDraw.centered(g, StoryText.END_TITLE, CENTER_X + 2, 82);
        g.setColor(UiPalette.GOLD);
        TextDraw.centered(g, StoryText.END_TITLE, CENTER_X, 80);

        if (showingReport) {
            drawReport(g);
        } else {
            String prompt = pager.isPageComplete() ? StoryText.NEXT_PROMPT : null;
            box.paint(g, pager.getVisibleText(), pager.getFullText(), prompt, time);
        }
    }

    private void drawReport(Graphics2D g) {
        GameSession session = screens.getSession();

        g.setColor(UiPalette.PANEL_SOLID);
        g.fillRoundRect(170, 150, 460, 330, 18, 18);
        g.setColor(UiPalette.PANEL_EDGE);
        g.drawRoundRect(170, 150, 460, 330, 18, 18);

        g.setFont(Fonts.HEADING);
        g.setColor(UiPalette.ACCENT);
        TextDraw.centered(g, StoryText.RECAP_TITLE, CENTER_X, 205);

        drawRow(g, StoryText.RECAP_TRASH, session.getTotalTrash(), 270);
        drawRow(g, StoryText.RECAP_FISH, session.getTotalFishHits(), 310);
        drawRow(g, StoryText.RECAP_CHAPTERS, session.getChaptersCleared(), 350);
        drawRow(g, StoryText.RECAP_SCORE, session.getTotalPoints(), 390);

        g.setFont(Fonts.LABEL);
        g.setColor(UiPalette.ACCENT);
        TextDraw.centered(g, StoryText.BACK_PROMPT, CENTER_X, 450);
    }

    private void drawRow(Graphics2D g, String label, int value, int y) {
        g.setFont(Fonts.SUBHEADING);
        g.setColor(UiPalette.DIM_TEXT);
        g.drawString(label, LABEL_X, y);
        g.setColor(UiPalette.TEXT);
        TextDraw.rightAligned(g, String.valueOf(value), VALUE_X, y);
    }
}
