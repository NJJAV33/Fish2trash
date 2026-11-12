package ui;

import game.ChapterStatus;
import game.GameManager;
import game.GameSession;
import game.GameState;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import ui.render.SceneRenderer;
import util.GameConstants;

/**
 * The screen where a chapter is played. Each time it is entered it starts a
 * fresh attempt at the session's current chapter.
 */
public final class GameScreen implements Screen {

    private final ScreenManager screens;
    private final InputState input;
    private final SceneRenderer renderer = new SceneRenderer();
    private GameManager game;

    public GameScreen(ScreenManager screens, InputState input) {
        this.screens = screens;
        this.input = input;
    }

    @Override
    public void onEnter() {
        game = screens.getSession().startChapter();
    }

    @Override
    public void update(double dt) {
        game.setBoatDirection(input.getDirection());
        game.update(dt);   // ignored by the manager once the chapter is over
    }

    @Override
    public void keyPressed(int keyCode) {
        if (game.getStatus().isOver()) {
            if (keyCode == KeyEvent.VK_ENTER) {
                continueAfterChapter();
            }
        } else if (keyCode == KeyEvent.VK_SPACE) {
            game.spaceAction();
        }
    }

    /** Records the result, then plays the next chapter (or the same one again after a fail). */
    private void continueAfterChapter() {
        GameSession session = screens.getSession();
        session.onChapterEnded(game);
        if (session.isFinished()) {
            screens.startNewRun();
        }
        screens.go(GameState.PLAYING);
    }

    @Override
    public void paint(Graphics2D g) {
        renderer.paint(g, game);
        drawHud(g);
        if (game.getStatus().isOver()) {
            drawEndOverlay(g);
        }
    }

    private void drawHud(Graphics2D g) {
        int secondsLeft = (int) Math.ceil(game.getTimeLeft());

        g.setColor(new Color(0, 0, 0, 120));
        g.fillRoundRect(10, 10, 300, 92, 12, 12);

        g.setColor(Color.WHITE);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        g.drawString(game.getChapter().getTitle(), 22, 32);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        g.drawString("Score: " + game.getScore() + " / " + game.getQuota(), 22, 56);
        g.drawString("Health: " + game.getHealth() + " / " + game.getMaxHealth()
                + "     Time: " + secondsLeft + "s", 22, 82);

        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        g.drawString("A / D: move boat    SPACE: drop or recall hook", 12, GameConstants.WORLD_HEIGHT - 22);
    }

    private void drawEndOverlay(Graphics2D g) {
        boolean won = game.getStatus() == ChapterStatus.CLEARED;

        g.setColor(new Color(0, 0, 0, 160));
        g.fillRect(0, 0, GameConstants.WORLD_WIDTH, GameConstants.WORLD_HEIGHT);

        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 48));
        g.setColor(won ? new Color(120, 230, 120) : new Color(240, 90, 90));
        drawCentered(g, won ? "CHAPTER CLEARED" : "CHAPTER FAILED", 260);

        g.setColor(Color.WHITE);
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 24));
        drawCentered(g, won ? "ENTER: next chapter" : "ENTER: try again", 330);
    }

    private void drawCentered(Graphics2D g, String text, int y) {
        FontMetrics metrics = g.getFontMetrics();
        g.drawString(text, (GameConstants.WORLD_WIDTH - metrics.stringWidth(text)) / 2, y);
    }
}
