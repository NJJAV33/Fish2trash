package ui;

import game.ChapterResult;
import game.GameManager;
import game.GameState;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import ui.render.SceneRenderer;
import util.GameConstants;
import util.StoryText;

/**
 * The screen where a chapter is played. Each time it is entered it starts a
 * fresh attempt at the session's current chapter.
 */
public final class GameScreen implements Screen {

    // After a chapter ends, hold the last frame briefly before moving on.
    private static final double END_PAUSE_SECONDS = 0.8;

    private final ScreenManager screens;
    private final InputState input;
    private final SceneRenderer renderer;
    private final PauseOverlay pauseOverlay;
    private GameManager game;
    private double secondsSinceEnd;
    private boolean leaving;

    public GameScreen(ScreenManager screens, InputState input, SceneRenderer renderer) {
        this.screens = screens;
        this.input = input;
        this.renderer = renderer;
        this.pauseOverlay = new PauseOverlay(screens);
    }

    @Override
    public void onEnter() {
        game = screens.getSession().startChapter();
        secondsSinceEnd = 0;
        leaving = false;
    }

    @Override
    public void update(double dt) {
        game.setBoatDirection(input.getDirection());
        game.update(dt);   // ignored by the manager once the chapter is over

        if (game.getStatus().isOver() && !leaving) {
            secondsSinceEnd += dt;
            if (secondsSinceEnd >= END_PAUSE_SECONDS) {
                leave();
            }
        }
    }

    /** Hands the result to the session and moves to the clear or game-over screen. */
    private void leave() {
        leaving = true;
        ChapterResult result = screens.getSession().onChapterEnded(game);
        screens.go(result.isCleared() ? GameState.CHAPTER_CLEAR : GameState.GAME_OVER);
    }

    @Override
    public void keyPressed(int keyCode) {
        if (game.getStatus().isOver()) {
            return;
        }
        if (keyCode == KeyEvent.VK_SPACE) {
            game.spaceAction();
        } else if (keyCode == KeyEvent.VK_P || keyCode == KeyEvent.VK_ESCAPE) {
            pause();
        }
    }

    /** Pauses the chapter. Also called when the window loses focus. */
    public void pause() {
        if (!game.getStatus().isOver()) {
            screens.pause(pauseOverlay);
        }
    }

    @Override
    public void paint(Graphics2D g) {
        renderer.paint(g, game);
        drawHud(g);
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
        g.drawString(StoryText.PLAY_HINT, 12, GameConstants.WORLD_HEIGHT - 22);
    }
}
