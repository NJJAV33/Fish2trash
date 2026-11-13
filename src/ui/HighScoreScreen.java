package ui;

import game.Badge;
import game.GameState;
import game.HighScoreTable;
import game.ScoreEntry;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.util.List;
import model.ChapterData;
import ui.render.BadgePainter;
import ui.render.ChapterPalette;
import ui.render.Fonts;
import ui.render.SceneRenderer;
import ui.render.TextDraw;
import ui.render.UiPalette;
import util.GameConstants;
import util.SoundEffect;
import util.StoryText;

/** The high-score list: the ten best runs, with their chapters, badges and date. */
public final class HighScoreScreen implements Screen {

    private static final int CENTER_X = GameConstants.WORLD_WIDTH / 2;
    private static final int FIRST_ROW_Y = 185;
    private static final int ROW_HEIGHT = 32;
    private static final int RANK_RIGHT = 150;      // the columns, from left to right
    private static final int NAME_X = 170;
    private static final int SCORE_RIGHT = 400;
    private static final int CHAPTERS_X = 450;
    private static final int BADGES_X = 522;
    private static final int BADGE_SPACING = 22;
    private static final int BADGE_RADIUS = 8;
    private static final int DATE_RIGHT = 676;
    private static final Color[] TOP_THREE = {UiPalette.GOLD, UiPalette.SILVER, UiPalette.BRONZE};

    private final ScreenManager screens;
    private final SceneRenderer backdrop;
    private final BadgePainter badgePainter = new BadgePainter();

    public HighScoreScreen(ScreenManager screens, SceneRenderer backdrop) {
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
        HighScoreTable table = screens.getHighScores();
        List<ScoreEntry> entries = table.getEntries();

        backdrop.paintBackdrop(g, ChapterPalette.MENU);
        g.setColor(UiPalette.PANEL_SOLID);
        g.fillRoundRect(100, 60, 600, 480, 18, 18);
        g.setColor(UiPalette.PANEL_EDGE);
        g.drawRoundRect(100, 60, 600, 480, 18, 18);

        g.setFont(Fonts.HEADING);
        g.setColor(UiPalette.ACCENT);
        TextDraw.centered(g, StoryText.SCORES_TITLE, CENTER_X, 112);

        if (entries.isEmpty()) {
            g.setFont(Fonts.BODY);
            g.setColor(UiPalette.DIM_TEXT);
            TextDraw.centered(g, StoryText.SCORES_EMPTY, CENTER_X, 300);
        } else {
            drawHeader(g);
            for (int i = 0; i < entries.size(); i++) {
                drawRow(g, i, entries.get(i), entries.get(i) == table.getLatest());
            }
        }

        if (!table.isSaved()) {
            g.setFont(Fonts.SMALL);
            g.setColor(UiPalette.DANGER);
            TextDraw.centered(g, StoryText.SCORES_NOT_SAVED, CENTER_X, 500);
        }
        g.setFont(Fonts.LABEL);
        g.setColor(UiPalette.ACCENT);
        TextDraw.centered(g, StoryText.BACK_PROMPT, CENTER_X, 522);
    }

    private void drawHeader(Graphics2D g) {
        int y = FIRST_ROW_Y - 30;
        g.setFont(Fonts.LABEL);
        g.setColor(UiPalette.DIM_TEXT);
        TextDraw.rightAligned(g, StoryText.SCORES_RANK, RANK_RIGHT, y);
        g.drawString(StoryText.SCORES_NAME, NAME_X, y);
        TextDraw.rightAligned(g, StoryText.SCORES_SCORE, SCORE_RIGHT, y);
        TextDraw.centered(g, StoryText.SCORES_CHAPTERS, CHAPTERS_X, y);
        TextDraw.centered(g, StoryText.SCORES_BADGES, BADGES_X + BADGE_SPACING, y);
        TextDraw.rightAligned(g, StoryText.SCORES_DATE, DATE_RIGHT, y);
    }

    /** One line of the list. The run that was just added is picked out with a lighter bar. */
    private void drawRow(Graphics2D g, int index, ScoreEntry entry, boolean latest) {
        int y = FIRST_ROW_Y + index * ROW_HEIGHT;
        if (latest) {
            g.setColor(UiPalette.BAR_TRACK);
            g.fillRoundRect(116, y - 21, 568, ROW_HEIGHT - 4, 8, 8);
        }

        g.setFont(Fonts.SCORE);
        g.setColor(index < TOP_THREE.length ? TOP_THREE[index] : UiPalette.DIM_TEXT);
        TextDraw.rightAligned(g, String.valueOf(index + 1), RANK_RIGHT, y);
        g.setColor(latest ? UiPalette.ACCENT : UiPalette.TEXT);
        g.drawString(entry.getName(), NAME_X, y);
        g.setColor(UiPalette.TEXT);
        TextDraw.rightAligned(g, String.valueOf(entry.getScore()), SCORE_RIGHT, y);

        g.setFont(Fonts.MONO);
        g.setColor(UiPalette.DIM_TEXT);
        TextDraw.centered(g, entry.getChaptersCleared() + " / " + ChapterData.CHAPTER_COUNT, CHAPTERS_X, y);
        TextDraw.rightAligned(g, entry.getDate().toString(), DATE_RIGHT, y);

        Badge[] badges = Badge.values();
        for (int i = 0; i < badges.length; i++) {
            badgePainter.paint(g, badges[i], BADGES_X + i * BADGE_SPACING, y - 8, BADGE_RADIUS,
                    entry.getBadges().contains(badges[i]));
        }
    }
}
