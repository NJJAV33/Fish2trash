package ui;

import game.Badge;
import game.GameSession;
import game.GameState;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import ui.render.BadgePainter;
import ui.render.ChapterPalette;
import ui.render.Fonts;
import ui.render.SceneRenderer;
import ui.render.StoryBoxPainter;
import ui.render.TextDraw;
import ui.render.UiPalette;
import util.GameConstants;
import util.Mathx;
import util.SoundEffect;
import util.StoryText;

/**
 * The ending: the reef has recovered. First the last story pages, then a
 * short report of the whole run.
 */
public final class VictoryScreen implements Screen {

    private static final int CENTER_X = GameConstants.WORLD_WIDTH / 2;
    private static final int LABEL_X = 230;
    private static final int VALUE_X = 570;
    private static final int BADGE_RADIUS = 15;
    private static final int BADGE_SPACING = 42;
    private static final int SCHOOL_SIZE = 24;
    private static final double SCHOOL_DELAY_SECONDS = 2.0;
    private static final double SCHOOL_FADE_SECONDS = 1.5;
    private static final Color[] SCHOOL_COLORS = {
        new Color(255, 199, 95), new Color(255, 150, 113), new Color(255, 111, 145), new Color(250, 250, 250)
    };

    private final ScreenManager screens;
    private final SceneRenderer backdrop;
    private final StoryBoxPainter box = new StoryBoxPainter();
    private final BadgePainter badgePainter = new BadgePainter();
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
        screens.getAudio().play(SoundEffect.ENDING);
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
        screens.getAudio().play(SoundEffect.MENU_SELECT);
        if (showingReport) {
            screens.go(screens.afterRun(GameState.HIGH_SCORES));
        } else if (pager.advance()) {
            showingReport = true;   // the story is told; show the numbers
        }
    }

    @Override
    public void paint(Graphics2D g) {
        backdrop.paintBackdrop(g, ChapterPalette.REEF);
        drawFishSchool(g);

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

    /** A school of small fish that swims back into the reef a moment after the ending starts. */
    private void drawFishSchool(Graphics2D g) {
        double arrival = Mathx.clamp((time - SCHOOL_DELAY_SECONDS) / SCHOOL_FADE_SECONDS, 0, 1);
        if (arrival <= 0) {
            return;
        }
        Graphics2D school = (Graphics2D) g.create();
        school.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) arrival));
        for (int i = 0; i < SCHOOL_SIZE; i++) {
            // Each fish has its own speed, depth and colour, worked out from its number.
            double speed = 30 + (i * 7) % 25;
            int x = (int) ((time * speed + i * 37.0) % (GameConstants.WORLD_WIDTH + 80)) - 40;
            int y = 170 + (i * 53) % 190 + (int) (6 * Math.sin(time * 2 + i));
            school.setColor(SCHOOL_COLORS[i % SCHOOL_COLORS.length]);
            school.fillOval(x - 8, y - 4, 16, 8);
            school.fillPolygon(new int[] {x - 7, x - 14, x - 14}, new int[] {y, y - 5, y + 5}, 3);
        }
        school.dispose();
    }

    private void drawReport(Graphics2D g) {
        GameSession session = screens.getSession();

        g.setColor(UiPalette.PANEL_SOLID);
        g.fillRoundRect(170, 150, 460, 330, 18, 18);
        g.setColor(UiPalette.PANEL_EDGE);
        g.drawRoundRect(170, 150, 460, 330, 18, 18);

        g.setFont(Fonts.HEADING);
        g.setColor(UiPalette.ACCENT);
        TextDraw.centered(g, StoryText.RECAP_TITLE, CENTER_X, 200);

        drawRow(g, StoryText.RECAP_TRASH, session.getTotalTrash(), 250);
        drawRow(g, StoryText.RECAP_FISH, session.getTotalFishHits(), 284);
        drawRow(g, StoryText.RECAP_CHAPTERS, session.getChaptersCleared(), 318);
        drawRow(g, StoryText.RECAP_SCORE, session.getRunScore(), 352);

        // The badges of the run: earned ones in colour, the others in grey.
        g.setFont(Fonts.SUBHEADING);
        g.setColor(UiPalette.DIM_TEXT);
        g.drawString(StoryText.RECAP_BADGES, LABEL_X, 396);
        Badge[] badges = Badge.values();
        for (int i = 0; i < badges.length; i++) {
            int x = VALUE_X - BADGE_RADIUS - (badges.length - 1 - i) * BADGE_SPACING;
            badgePainter.paint(g, badges[i], x, 386, BADGE_RADIUS, session.hasBadge(badges[i]));
        }

        g.setFont(Fonts.LABEL);
        g.setColor(UiPalette.ACCENT);
        TextDraw.centered(g, StoryText.CONTINUE_PROMPT, CENTER_X, 450);
    }

    private void drawRow(Graphics2D g, String label, int value, int y) {
        g.setFont(Fonts.SUBHEADING);
        g.setColor(UiPalette.DIM_TEXT);
        g.drawString(label, LABEL_X, y);
        g.setColor(UiPalette.TEXT);
        TextDraw.rightAligned(g, String.valueOf(value), VALUE_X, y);
    }
}
