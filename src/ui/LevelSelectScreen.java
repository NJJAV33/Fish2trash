package ui;

import game.GameState;
import game.LevelProgress;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import model.LevelData;
import ui.render.ChapterPalette;
import ui.render.Fonts;
import ui.render.SceneRenderer;
import ui.render.TextDraw;
import ui.render.UiPalette;
import util.GameConstants;
import util.SoundEffect;
import util.StoryText;

/**
 * The level select: all fifteen levels in a grid, one row per chapter.
 * Cleared levels and the next one after them can be picked; picking one
 * starts a run there that goes on through the story to the ending. Levels
 * further on stay locked until the one before them is cleared.
 */
public final class LevelSelectScreen implements Screen {

    private static final int CENTER_X = GameConstants.WORLD_WIDTH / 2;
    private static final int PER_ROW = LevelData.LEVELS_PER_CHAPTER;
    private static final int TILE_WIDTH = 110;
    private static final int TILE_HEIGHT = 72;
    private static final int TILE_GAP = 14;
    private static final int FIRST_TILE_X = CENTER_X - (PER_ROW * TILE_WIDTH + (PER_ROW - 1) * TILE_GAP) / 2;
    private static final int FIRST_ROW_Y = 120;
    private static final int ROW_SPACING = 125;

    private static final Color TILE = new Color(255, 255, 255, 28);
    private static final Color TILE_CHOSEN = new Color(79, 209, 197, 60);
    private static final Color TILE_LOCKED = new Color(0, 0, 0, 70);
    private static final Color LOCKED_TEXT = new Color(184, 196, 204, 110);
    private static final BasicStroke EDGE = new BasicStroke(1);
    private static final BasicStroke CHOSEN_EDGE = new BasicStroke(3);
    private static final BasicStroke MARK = new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);

    private final ScreenManager screens;
    private final SceneRenderer backdrop;
    private int chosen = 1;   // the level number under the highlight, 1 to 15

    public LevelSelectScreen(ScreenManager screens, SceneRenderer backdrop) {
        this.screens = screens;
        this.backdrop = backdrop;
    }

    /** Starts on the level to play next: the one after the furthest cleared. */
    @Override
    public void onEnter() {
        chosen = Math.min(screens.getProgress().getFurthestCleared() + 1, LevelData.LEVEL_COUNT);
    }

    @Override
    public void keyPressed(int keyCode) {
        if (keyCode == KeyEvent.VK_LEFT || keyCode == KeyEvent.VK_A) {
            move(-1);
        } else if (keyCode == KeyEvent.VK_RIGHT || keyCode == KeyEvent.VK_D) {
            move(1);
        } else if (keyCode == KeyEvent.VK_UP || keyCode == KeyEvent.VK_W) {
            move(-PER_ROW);
        } else if (keyCode == KeyEvent.VK_DOWN || keyCode == KeyEvent.VK_S) {
            move(PER_ROW);
        } else if (keyCode == KeyEvent.VK_ENTER || keyCode == KeyEvent.VK_SPACE) {
            play();
        } else if (keyCode == KeyEvent.VK_ESCAPE) {
            screens.go(GameState.MENU);
        }
    }

    /** Moves the highlight, staying inside the grid. */
    private void move(int step) {
        int target = chosen + step;
        if (target >= 1 && target <= LevelData.LEVEL_COUNT) {
            chosen = target;
            screens.getAudio().play(SoundEffect.MENU_MOVE);
        }
    }

    private void play() {
        if (!screens.getProgress().isUnlocked(chosen)) {
            screens.getAudio().play(SoundEffect.WRONG_TOOL);   // the same "no" as a wrong tool
            return;
        }
        screens.getAudio().play(SoundEffect.MENU_SELECT);
        screens.startRunAt(chosen);
        screens.go(GameState.STORY);
    }

    @Override
    public void paint(Graphics2D g) {
        LevelData selected = LevelData.byNumber(chosen);
        backdrop.paintBackdrop(g, ChapterPalette.forChapter(selected.getChapter()));
        g.setColor(UiPalette.LIGHT_SHADE);
        g.fillRect(0, 0, GameConstants.WORLD_WIDTH, GameConstants.WORLD_HEIGHT);
        g.setColor(UiPalette.PANEL_SOLID);
        g.fillRoundRect(50, 36, 700, 520, 18, 18);
        g.setColor(UiPalette.PANEL_EDGE);
        g.drawRoundRect(50, 36, 700, 520, 18, 18);

        g.setFont(Fonts.BANNER);
        g.setColor(UiPalette.ACCENT);
        TextDraw.centered(g, StoryText.LEVELS_TITLE, CENTER_X, 90);

        LevelProgress progress = screens.getProgress();
        for (int chapter = 1; chapter <= LevelData.CHAPTER_COUNT; chapter++) {
            int rowY = FIRST_ROW_Y + (chapter - 1) * ROW_SPACING;
            g.setFont(Fonts.LABEL);
            g.setColor(progress.isUnlocked(LevelData.of(chapter, 1).getNumber()) ? UiPalette.TEXT : LOCKED_TEXT);
            g.drawString(StoryText.chapterTitle(chapter), FIRST_TILE_X, rowY + 18);
            for (int level = 1; level <= PER_ROW; level++) {
                int x = FIRST_TILE_X + (level - 1) * (TILE_WIDTH + TILE_GAP);
                paintTile(g, LevelData.of(chapter, level), progress, x, rowY + 28);
            }
        }

        // What the highlighted level is, or why it cannot be picked yet.
        g.setFont(Fonts.SUBHEADING);
        g.setColor(UiPalette.TEXT);
        TextDraw.centered(g, StoryText.levelTitle(selected.getChapter(), selected.getLevel()), CENTER_X, 506);
        g.setFont(Fonts.BODY);
        if (progress.isUnlocked(chosen)) {
            g.setColor(UiPalette.DIM_TEXT);
            TextDraw.centered(g, String.format(StoryText.LEVELS_DETAILS, selected.getQuota(),
                    (int) selected.getTimeLimit(), selected.getStartHealth()), CENTER_X, 532);
        } else {
            g.setColor(UiPalette.DANGER);
            TextDraw.centered(g, StoryText.LEVELS_LOCKED, CENTER_X, 532);
        }

        g.setFont(Fonts.SMALL);
        g.setColor(UiPalette.DIM_TEXT);
        TextDraw.centered(g, StoryText.LEVELS_HINT, CENTER_X, 580);
    }

    /** One level's tile: its number and name, a tick once cleared, a padlock while locked. */
    private void paintTile(Graphics2D g, LevelData level, LevelProgress progress, int x, int y) {
        int number = level.getNumber();
        boolean unlocked = progress.isUnlocked(number);
        boolean isChosen = number == chosen;

        g.setColor(isChosen ? TILE_CHOSEN : unlocked ? TILE : TILE_LOCKED);
        g.fillRoundRect(x, y, TILE_WIDTH, TILE_HEIGHT, 12, 12);
        g.setColor(isChosen ? UiPalette.ACCENT : UiPalette.PANEL_EDGE);
        g.setStroke(isChosen ? CHOSEN_EDGE : EDGE);
        g.drawRoundRect(x, y, TILE_WIDTH, TILE_HEIGHT, 12, 12);
        g.setStroke(EDGE);

        int middle = x + TILE_WIDTH / 2;
        g.setFont(Fonts.HEADING);
        g.setColor(unlocked ? UiPalette.TEXT : LOCKED_TEXT);
        TextDraw.centered(g, level.getChapter() + "-" + level.getLevel(), middle, y + 34);

        if (!unlocked) {
            paintPadlock(g, middle, y + 46);
            return;
        }
        g.setFont(Fonts.SMALL);
        g.setColor(UiPalette.DIM_TEXT);
        TextDraw.centered(g, StoryText.levelName(level.getChapter(), level.getLevel()), middle, y + 58);
        if (progress.isCleared(number)) {
            paintTick(g, x + TILE_WIDTH - 18, y + 10);
        } else {
            // A small gold badge sitting on the tile's top edge: the level to play next.
            g.setColor(UiPalette.GOLD);
            g.fillRoundRect(x + TILE_WIDTH - 40, y - 8, 34, 16, 8, 8);
            g.setFont(Fonts.LABEL);
            g.setColor(UiPalette.INK);
            TextDraw.centered(g, StoryText.LEVELS_NEW, x + TILE_WIDTH - 23, y + 5);
        }
    }

    private void paintTick(Graphics2D g, int x, int y) {
        g.setColor(UiPalette.GOOD);
        g.setStroke(MARK);
        g.drawPolyline(new int[] {x, x + 4, x + 11}, new int[] {y + 4, y + 8, y - 1}, 3);
        g.setStroke(EDGE);
    }

    /** A small padlock: a shackle over a body, top middle at (x, y). */
    private void paintPadlock(Graphics2D g, int x, int y) {
        g.setColor(LOCKED_TEXT);
        g.setStroke(MARK);
        g.drawArc(x - 5, y, 10, 12, 0, 180);
        g.setStroke(EDGE);
        g.fillRoundRect(x - 8, y + 6, 16, 12, 4, 4);
    }
}
