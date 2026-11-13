package ui;

import game.GameSession;
import game.GameState;
import game.ScoreEntry;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.time.LocalDate;
import ui.render.ChapterPalette;
import ui.render.Fonts;
import ui.render.SceneRenderer;
import ui.render.TextDraw;
import ui.render.UiPalette;
import util.GameConstants;
import util.SoundEffect;
import util.StoryText;

/**
 * Shown when a run that just ended made the high-score list: the player
 * types a name and the run is written onto the list.
 */
public final class NameEntryScreen implements Screen {

    private static final int CENTER_X = GameConstants.WORLD_WIDTH / 2;
    private static final int FIELD_X = 240;
    private static final int FIELD_Y = 318;
    private static final int FIELD_WIDTH = 320;
    private static final int FIELD_HEIGHT = 44;
    private static final double CARET_BLINK_SECONDS = 0.5;

    private final ScreenManager screens;
    private final SceneRenderer backdrop;
    private final StringBuilder name = new StringBuilder();
    private double time;

    public NameEntryScreen(ScreenManager screens, SceneRenderer backdrop) {
        this.screens = screens;
        this.backdrop = backdrop;
    }

    @Override
    public void onEnter() {
        name.setLength(0);
        time = 0;
        screens.getAudio().play(SoundEffect.HIGH_SCORE);
    }

    @Override
    public void update(double dt) {
        time += dt;
    }

    @Override
    public boolean isTakingText() {
        return true;
    }

    @Override
    public void keyTyped(char character) {
        if (ScoreEntry.isNameCharacter(character) && name.length() < GameConstants.HIGHSCORE_NAME_MAX_LEN) {
            name.append(Character.toUpperCase(character));
        }
    }

    @Override
    public void keyPressed(int keyCode) {
        if (keyCode == KeyEvent.VK_BACK_SPACE && name.length() > 0) {
            name.setLength(name.length() - 1);
        } else if (keyCode == KeyEvent.VK_ENTER) {
            // An empty field becomes the default name inside ScoreEntry.
            screens.getHighScores().add(ScoreEntry.forRun(name.toString(), screens.getSession(), LocalDate.now()));
            screens.getAudio().play(SoundEffect.MENU_SELECT);
            screens.go(GameState.HIGH_SCORES);
        }
    }

    @Override
    public void paint(Graphics2D g) {
        GameSession session = screens.getSession();
        // The recovered reef after a finished story, the menu's sea after a run that ended early.
        backdrop.paintBackdrop(g, session.isFinished() ? ChapterPalette.REEF : ChapterPalette.MENU);
        g.setColor(UiPalette.LIGHT_SHADE);
        g.fillRect(0, 0, GameConstants.WORLD_WIDTH, GameConstants.WORLD_HEIGHT);

        g.setColor(UiPalette.PANEL_SOLID);
        g.fillRoundRect(170, 130, 460, 330, 18, 18);
        g.setColor(UiPalette.PANEL_EDGE);
        g.drawRoundRect(170, 130, 460, 330, 18, 18);

        g.setFont(Fonts.HEADING);
        g.setColor(UiPalette.GOLD);
        TextDraw.centered(g, StoryText.NAME_TITLE, CENTER_X, 185);

        g.setFont(Fonts.LABEL);
        g.setColor(UiPalette.DIM_TEXT);
        TextDraw.centered(g, StoryText.RECAP_SCORE, CENTER_X, 220);
        g.setFont(Fonts.BANNER);
        g.setColor(UiPalette.TEXT);
        TextDraw.centered(g, String.valueOf(session.getRunScore()), CENTER_X, 264);

        g.setFont(Fonts.LABEL);
        g.setColor(UiPalette.DIM_TEXT);
        TextDraw.centered(g, StoryText.NAME_LABEL, CENTER_X, FIELD_Y - 10);
        drawField(g);

        g.setFont(Fonts.LABEL);
        g.setColor(UiPalette.ACCENT);
        TextDraw.centered(g, String.format(StoryText.NAME_PROMPT, GameConstants.HIGHSCORE_NAME_MAX_LEN),
                CENTER_X, 425);
    }

    /** The text field. While it is empty it shows the default name in grey. */
    private void drawField(Graphics2D g) {
        g.setColor(UiPalette.PANEL);
        g.fillRoundRect(FIELD_X, FIELD_Y, FIELD_WIDTH, FIELD_HEIGHT, 10, 10);
        g.setColor(UiPalette.ACCENT);
        g.drawRoundRect(FIELD_X, FIELD_Y, FIELD_WIDTH, FIELD_HEIGHT, 10, 10);

        g.setFont(Fonts.TIMER);
        int baseline = FIELD_Y + 31;
        int textWidth = g.getFontMetrics().stringWidth(name.toString());
        int textX = CENTER_X - textWidth / 2;
        if (name.length() == 0) {
            g.setColor(UiPalette.BAR_TRACK);
            TextDraw.centered(g, StoryText.PLAYER_NAME, CENTER_X, baseline);
        } else {
            g.setColor(UiPalette.TEXT);
            g.drawString(name.toString(), textX, baseline);
        }

        boolean caretOn = (int) (time / CARET_BLINK_SECONDS) % 2 == 0;
        if (caretOn) {
            g.setColor(UiPalette.TEXT);
            int caretX = name.length() == 0 ? FIELD_X + 14 : textX + textWidth + 3;
            g.fillRect(caretX, FIELD_Y + 9, 2, FIELD_HEIGHT - 18);
        }
    }
}
