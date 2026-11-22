package ui;

import game.Badge;
import game.LevelResult;
import game.GameState;
import game.ScoreCalculator;
import model.LevelData;
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

/**
 * The report card after a cleared level. The last level of a chapter also
 * shows the chapter's badge; the others show how far into the chapter the
 * player is.
 */
public final class LevelClearScreen implements Screen {

    private static final int CENTER_X = GameConstants.WORLD_WIDTH / 2;
    private static final int LABEL_X = 230;
    private static final int VALUE_X = 570;
    public static final int FIRST_ROW_Y = 166;
    public static final int ROW_SPACING = 28;
    private static final double COUNT_SECONDS = 0.7;         // how long the numbers take to count up
    private static final int BADGE_Y = 382;
    private static final double BADGE_RADIUS = 34;
    private static final double BADGE_DELAY_SECONDS = 0.8;   // the medal arrives a moment after the numbers
    private static final double BADGE_POP_SECONDS = 0.4;
    private static final double SPARKLE_SECONDS = 0.7;
    private static final int SPARKLES = 10;
    private static final int PIP_Y = 374;                    // the row of level dots
    private static final int PIP_RADIUS = 9;
    private static final int PIP_SPACING = 34;
    private static final double PIP_BADGE_RADIUS = 16;

    private final ScreenManager screens;
    private final SceneRenderer backdrop;
    private final BadgePainter badgePainter = new BadgePainter();
    private LevelResult result;
    private Badge badge;
    private boolean badgeDecided;   // only the last level of a chapter awards (or misses) the badge
    private boolean badgeEarned;
    private boolean badgeAnnounced;
    private double time;

    public LevelClearScreen(ScreenManager screens, SceneRenderer backdrop) {
        this.screens = screens;
        this.backdrop = backdrop;
    }

    @Override
    public void onEnter() {
        result = screens.getSession().getLastResult();
        badge = Badge.forChapter(result.getChapter());
        badgeDecided = result.isChapterFinale();
        badgeEarned = badgeDecided && screens.getSession().hasBadge(badge);
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
            // After the last level comes the ending; otherwise the next level's story.
            screens.getAudio().play(SoundEffect.MENU_SELECT);
            screens.go(screens.getSession().isFinished() ? GameState.ENDING : GameState.STORY);
        }
    }

    @Override
    public void paint(Graphics2D g) {
        // The sea cleared up while the level was played; the report shows how clean it is now.
        backdrop.paintBackdrop(g, StoryScreen.lookOf(result.getChapter(), result.getLevel()));
        g.setColor(UiPalette.LIGHT_SHADE);
        g.fillRect(0, 0, GameConstants.WORLD_WIDTH, GameConstants.WORLD_HEIGHT);

        g.setColor(UiPalette.PANEL_SOLID);
        g.fillRoundRect(150, 56, 500, 488, 18, 18);
        g.setColor(UiPalette.PANEL_EDGE);
        g.drawRoundRect(150, 56, 500, 488, 18, 18);

        g.setFont(Fonts.BANNER);
        g.setColor(UiPalette.GOOD);
        TextDraw.centered(g, StoryText.CLEAR_TITLE, CENTER_X, 108);
        g.setFont(Fonts.LABEL);
        g.setColor(UiPalette.DIM_TEXT);
        TextDraw.centered(g, StoryText.levelTitle(result.getChapter(), result.getLevel()), CENTER_X, 132);

        String diffText = result.getDifficulty().getDisplayName();
        if (result.getDifficulty().getScoreMultiplier() > 1.0) {
            diffText += " (" + result.getDifficulty().getMultiplierLabel() + ")";
        }
        String comboText = result.getBestCombo() >= 2
                ? "x" + result.getBestCombo() + " (+" + counted(result.getComboBonus()) + ")"
                : "-";

        // The numbers count up from zero when the report appears.
        drawRow(g, StoryText.CLEAR_DIFFICULTY, diffText, FIRST_ROW_Y);
        drawRow(g, StoryText.CLEAR_POINTS, String.valueOf(counted(result.getPoints())), FIRST_ROW_Y + ROW_SPACING);
        drawRow(g, StoryText.CLEAR_TIME, counted((int) result.getTimeLeft()) + " s", FIRST_ROW_Y + 2 * ROW_SPACING);
        drawRow(g, StoryText.CLEAR_HEALTH, counted(result.getHealthLeft()) + " / " + result.getMaxHealth(),
                FIRST_ROW_Y + 3 * ROW_SPACING);
        drawRow(g, StoryText.CLEAR_COMBO, comboText, FIRST_ROW_Y + 4 * ROW_SPACING);
        g.setColor(UiPalette.PANEL_EDGE);
        g.drawLine(LABEL_X, 290, VALUE_X, 290);
        drawRow(g, StoryText.CLEAR_SCORE, String.valueOf(counted(ScoreCalculator.levelScore(result))), 318);

        if (badgeDecided) {
            drawBadge(g);
        } else {
            drawProgress(g);
        }

        g.setFont(Fonts.LABEL);
        g.setColor(UiPalette.ACCENT);
        TextDraw.centered(g, StoryText.CONTINUE_PROMPT, CENTER_X, 528);
    }

    /** The part of a number that has been counted so far: 0 at first, the full number after a moment. */
    private int counted(int value) {
        return (int) Math.round(value * Mathx.easeOut(Mathx.clamp(time / COUNT_SECONDS, 0, 1)));
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
        TextDraw.centered(g, badgeEarned ? StoryText.BADGE_EARNED_LABEL : StoryText.BADGE_MISSED_LABEL, CENTER_X, 446);
        g.setFont(Fonts.SUBHEADING);
        g.setColor(badgeEarned ? UiPalette.TEXT : UiPalette.DIM_TEXT);
        TextDraw.centered(g, badge.getDisplayName(), CENTER_X, 472);
        g.setFont(Fonts.SMALL);
        g.setColor(UiPalette.DIM_TEXT);
        TextDraw.centered(g, badgeEarned ? badge.getEarnedText() : badge.getLockedText(), CENTER_X, 492);
    }

    /**
     * Before the last level of a chapter: five dots, one per level, filled
     * for the ones already cleared, and the badge that is still to win.
     */
    private void drawProgress(Graphics2D g) {
        int levels = LevelData.LEVELS_PER_CHAPTER;
        for (int i = 0; i < levels; i++) {
            int x = CENTER_X + (i - levels / 2) * PIP_SPACING;
            if (i < result.getLevel()) {
                g.setColor(UiPalette.GOOD);
                g.fillOval(x - PIP_RADIUS, PIP_Y - PIP_RADIUS, 2 * PIP_RADIUS, 2 * PIP_RADIUS);
            }
            g.setColor(UiPalette.PANEL_EDGE);
            g.drawOval(x - PIP_RADIUS, PIP_Y - PIP_RADIUS, 2 * PIP_RADIUS, 2 * PIP_RADIUS);
        }
        g.setFont(Fonts.SUBHEADING);
        g.setColor(UiPalette.TEXT);
        TextDraw.centered(g, String.format(StoryText.CLEAR_PROGRESS, result.getLevel(), levels), CENTER_X, 414);

        badgePainter.paint(g, badge, CENTER_X - 150, 464, PIP_BADGE_RADIUS, false);
        g.setFont(Fonts.SMALL);
        g.setColor(UiPalette.DIM_TEXT);
        g.drawString(badge.getDisplayName() + ": " + badge.getLockedText(), CENTER_X - 126, 460);
        g.drawString(StoryText.CLEAR_BADGE_AHEAD, CENTER_X - 126, 476);
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

    @Override
    public String getAccessibleName() {
        return StoryText.CLEAR_TITLE;
    }

    @Override
    public String getAccessibleDescription() {
        if (result == null) {
            return "";
        }
        String text = StoryText.levelTitle(result.getChapter(), result.getLevel())
                + ", Level score " + ScoreCalculator.levelScore(result)
                + ", Time left " + (int) result.getTimeLeft() + " s"
                + ", Health " + result.getHealthLeft() + " of " + result.getMaxHealth();
        if (badgeDecided && badge != null) {
            text += ", " + (badgeEarned ? "Earned badge: " : "Missed badge: ") + badge.getDisplayName();
        }
        return text;
    }
}
