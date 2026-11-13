package ui;

import game.Badge;
import game.ChapterResult;
import game.GameState;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import ui.render.BadgePainter;
import ui.render.ChapterPalette;
import ui.render.Fonts;
import ui.render.SceneRenderer;
import ui.render.TextDraw;
import ui.render.UiPalette;
import util.GameConstants;
import util.Mathx;
import util.SoundEffect;
import util.StoryText;

/** The report card after a cleared chapter, with the chapter's badge if it was earned. */
public final class ChapterClearScreen implements Screen {

    private static final int CENTER_X = GameConstants.WORLD_WIDTH / 2;
    private static final int LABEL_X = 230;
    private static final int VALUE_X = 570;
    private static final double CLEAN_UP_SECONDS = 2.0;   // how long the water takes to clear
    private static final int BADGE_Y = 352;
    private static final double BADGE_RADIUS = 34;
    private static final double BADGE_DELAY_SECONDS = 0.8;   // the medal arrives a moment after the numbers
    private static final double BADGE_POP_SECONDS = 0.4;
    private static final double SPARKLE_SECONDS = 0.7;
    private static final int SPARKLES = 10;

    private final ScreenManager screens;
    private final SceneRenderer backdrop;
    private final BadgePainter badgePainter = new BadgePainter();
    private ChapterResult result;
    private Badge badge;
    private boolean badgeEarned;
    private boolean badgeAnnounced;
    private double time;

    public ChapterClearScreen(ScreenManager screens, SceneRenderer backdrop) {
        this.screens = screens;
        this.backdrop = backdrop;
    }

    @Override
    public void onEnter() {
        result = screens.getSession().getLastResult();
        badge = Badge.forChapter(result.getChapter());
        badgeEarned = screens.getSession().hasBadge(badge);
        badgeAnnounced = false;
        time = 0;
    }

    @Override
    public void update(double dt) {
        time += dt;
        if (badgeEarned && !badgeAnnounced && time >= BADGE_DELAY_SECONDS) {
            badgeAnnounced = true;
            screens.getAudio().play(SoundEffect.BADGE);
        }
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
        TextDraw.centered(g, StoryText.CLEAR_TITLE, CENTER_X, 148);
        g.setFont(Fonts.LABEL);
        g.setColor(UiPalette.DIM_TEXT);
        TextDraw.centered(g, StoryText.chapterTitle(result.getChapter()), CENTER_X, 174);

        drawRow(g, StoryText.CLEAR_POINTS, String.valueOf(result.getPoints()), 216);
        drawRow(g, StoryText.CLEAR_TIME, (int) result.getTimeLeft() + " s", 242);
        drawRow(g, StoryText.CLEAR_HEALTH, result.getHealthLeft() + " / " + result.getMaxHealth(), 268);
        drawRow(g, StoryText.CLEAR_FISH, String.valueOf(result.getFishHits()), 294);

        drawBadge(g);

        g.setFont(Fonts.LABEL);
        g.setColor(UiPalette.ACCENT);
        TextDraw.centered(g, StoryText.CONTINUE_PROMPT, CENTER_X, 494);
    }

    /**
     * An earned badge pops in with a burst of sparkles. A missed one is
     * shown in grey from the start, with what it takes to earn it.
     */
    private void drawBadge(Graphics2D g) {
        double pop = badgeEarned ? Mathx.clamp((time - BADGE_DELAY_SECONDS) / BADGE_POP_SECONDS, 0, 1) : 1;
        if (pop <= 0) {
            return;
        }
        double size = badgeEarned ? Mathx.easeOutBack(pop) : 1;
        badgePainter.paint(g, badge, CENTER_X, BADGE_Y, BADGE_RADIUS * size, badgeEarned);
        if (badgeEarned) {
            drawSparkles(g);
        }

        g.setFont(Fonts.LABEL);
        g.setColor(badgeEarned ? UiPalette.GOLD : UiPalette.DIM_TEXT);
        TextDraw.centered(g, badgeEarned ? StoryText.BADGE_EARNED_LABEL : StoryText.BADGE_MISSED_LABEL, CENTER_X, 428);
        g.setFont(Fonts.SUBHEADING);
        g.setColor(badgeEarned ? UiPalette.TEXT : UiPalette.DIM_TEXT);
        TextDraw.centered(g, badge.getDisplayName(), CENTER_X, 449);
        g.setFont(Fonts.SMALL);
        g.setColor(UiPalette.DIM_TEXT);
        TextDraw.centered(g, badgeEarned ? badge.getEarnedText() : badge.getLockedText(), CENTER_X, 467);
    }

    /** Small crosses of light that fly out from the medal and fade. */
    private void drawSparkles(Graphics2D g) {
        double age = (time - BADGE_DELAY_SECONDS) / SPARKLE_SECONDS;
        if (age >= 1) {
            return;
        }
        Graphics2D sparkles = (Graphics2D) g.create();
        sparkles.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) (1 - age)));
        sparkles.setColor(UiPalette.GOLD);
        double distance = BADGE_RADIUS * (1.2 + 1.1 * age);
        int arm = (int) Math.round(6 - 3 * age);
        for (int i = 0; i < SPARKLES; i++) {
            double angle = Math.toRadians(i * 360.0 / SPARKLES + 18);
            int x = (int) Math.round(CENTER_X + distance * Math.cos(angle));
            int y = (int) Math.round(BADGE_Y + distance * Math.sin(angle));
            sparkles.drawLine(x - arm, y, x + arm, y);
            sparkles.drawLine(x, y - arm, x, y + arm);
        }
        sparkles.dispose();
    }

    private void drawRow(Graphics2D g, String label, String value, int y) {
        g.setFont(Fonts.SUBHEADING);
        g.setColor(UiPalette.DIM_TEXT);
        g.drawString(label, LABEL_X, y);
        g.setColor(UiPalette.TEXT);
        TextDraw.rightAligned(g, value, VALUE_X, y);
    }
}
