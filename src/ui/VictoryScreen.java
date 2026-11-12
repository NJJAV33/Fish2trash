package ui;

import game.GameSession;
import game.GameState;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import ui.render.Fonts;
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

    // The recovered ocean: bright, warm and clear.
    private static final Color SKY_TOP = new Color(127, 211, 245);
    private static final Color SKY_BOTTOM = new Color(255, 241, 201);
    private static final Color SEA_TOP = new Color(47, 182, 230);
    private static final Color SEA_BOTTOM = new Color(11, 95, 163);
    private static final Color SAND = new Color(242, 223, 160);
    private static final Color SUN = new Color(255, 209, 102);
    private static final Color[] CORAL = {
        new Color(255, 111, 145), new Color(255, 150, 113), new Color(255, 199, 95),
        new Color(195, 74, 155), new Color(79, 209, 197)
    };

    private final ScreenManager screens;
    private final StoryBoxPainter box = new StoryBoxPainter();
    private final TextPager pager = new TextPager();
    private boolean showingReport;
    private double time;

    public VictoryScreen(ScreenManager screens) {
        this.screens = screens;
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
        drawRecoveredOcean(g);

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

    private void drawRecoveredOcean(Graphics2D g) {
        int w = GameConstants.WORLD_WIDTH;
        int waterY = GameConstants.WATER_Y;
        int seabedY = GameConstants.SEABED_Y;

        g.setPaint(new GradientPaint(0, 0, SKY_TOP, 0, waterY, SKY_BOTTOM));
        g.fillRect(0, 0, w, waterY);
        g.setColor(SUN);
        g.fillOval(w - 120, 30, 70, 70);
        g.setPaint(new GradientPaint(0, waterY, SEA_TOP, 0, seabedY, SEA_BOTTOM));
        g.fillRect(0, waterY, w, seabedY - waterY);
        g.setColor(SAND);
        g.fillRect(0, seabedY, w, GameConstants.WORLD_HEIGHT - seabedY);

        // A row of coral along the sea floor, cycling through the coral colours.
        for (int i = 0; i < 16; i++) {
            int x = 20 + i * 50;
            int height = 26 + (i * 37) % 30;   // varied, but the same every frame
            g.setColor(CORAL[i % CORAL.length]);
            g.fillRoundRect(x, seabedY - height, 16, height, 12, 12);
            g.fillRoundRect(x - 12, seabedY - height / 2, 12, height / 2, 10, 10);
            g.fillRoundRect(x + 16, seabedY - height * 2 / 3, 12, height * 2 / 3, 10, 10);
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
