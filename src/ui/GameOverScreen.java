package ui;

import game.ChapterResult;
import game.FailCause;
import game.GameState;
import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import ui.render.ChapterPalette;
import ui.render.Fonts;
import ui.render.SceneRenderer;
import ui.render.TextDraw;
import ui.render.UiPalette;
import util.GameConstants;
import util.Mathx;
import util.SoundEffect;
import util.StoryText;

/** "You're fired": the pink slip after a lost chapter, with a retry. */
public final class GameOverScreen implements Screen {

    private static final int CENTER_X = GameConstants.WORLD_WIDTH / 2;
    private static final double STAMP_SECONDS = 0.25;   // how long the stamp takes to slam down
    private static final double SLIP_TILT = Math.toRadians(-3);
    private static final double STAMP_TILT = Math.toRadians(12);
    private static final BasicStroke STAMP_STROKE = new BasicStroke(5);

    private final ScreenManager screens;
    private final SceneRenderer backdrop;
    private ChapterResult result;
    private double time;

    public GameOverScreen(ScreenManager screens, SceneRenderer backdrop) {
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
        if (keyCode == KeyEvent.VK_ENTER || keyCode == KeyEvent.VK_R) {
            screens.getAudio().play(SoundEffect.MENU_SELECT);
            screens.go(GameState.STORY);   // the session is still on the failed chapter
        } else if (keyCode == KeyEvent.VK_ESCAPE) {
            screens.go(GameState.MENU);
        }
    }

    @Override
    public void paint(Graphics2D g) {
        backdrop.paintBackdrop(g, ChapterPalette.forChapter(result.getChapter()));
        g.setColor(UiPalette.SHADE);
        g.fillRect(0, 0, GameConstants.WORLD_WIDTH, GameConstants.WORLD_HEIGHT);
        g.setColor(UiPalette.FIRED_TINT);
        g.fillRect(0, 0, GameConstants.WORLD_WIDTH, GameConstants.WORLD_HEIGHT);

        drawPinkSlip(g);
        drawStamp(g);

        g.setFont(Fonts.SUBHEADING);
        g.setColor(UiPalette.TEXT);
        TextDraw.centered(g, StoryText.FAIL_PROMPT, CENTER_X, 520);
    }

    private void drawPinkSlip(Graphics2D g) {
        Graphics2D slip = (Graphics2D) g.create();
        slip.rotate(SLIP_TILT, CENTER_X, 280);   // slightly crooked, like a note slapped on a desk

        slip.setColor(UiPalette.PAPER);
        slip.fillRect(160, 120, 480, 330);

        slip.setColor(UiPalette.STAMP);
        slip.setFont(Fonts.BANNER);
        TextDraw.centered(slip, StoryText.FAIL_TITLE, CENTER_X, 185);
        slip.setColor(UiPalette.INK);
        slip.setFont(Fonts.LABEL);
        TextDraw.centered(slip, StoryText.FAIL_SUB, CENTER_X, 210);

        boolean outOfTime = result.getFailCause() == FailCause.TIME;
        slip.setFont(Fonts.BODY);
        TextDraw.centeredLines(slip, StoryText.failText(result.getChapter(), outOfTime), CENTER_X, 255, 26);
        slip.setFont(Fonts.LABEL);
        slip.drawString(StoryText.FAIL_RETRY, 190, 343);

        slip.dispose();
    }

    /** The red FIRED stamp starts three times too big and slams down to its real size. */
    private void drawStamp(Graphics2D g) {
        double progress = Mathx.clamp(time / STAMP_SECONDS, 0, 1);
        double scale = Mathx.lerp(3, 1, progress);

        Graphics2D stamp = (Graphics2D) g.create();
        stamp.translate(540, 412);   // the lower right corner of the slip
        stamp.rotate(STAMP_TILT);
        stamp.scale(scale, scale);
        stamp.setColor(UiPalette.STAMP);
        stamp.setStroke(STAMP_STROKE);
        stamp.drawRoundRect(-95, -40, 190, 70, 10, 10);
        stamp.setFont(Fonts.STAMP);
        TextDraw.centered(stamp, StoryText.FAIL_STAMP, 0, 17);
        stamp.dispose();
    }
}
