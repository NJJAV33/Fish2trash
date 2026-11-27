package ui;

import game.GameState;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import ui.render.IntroPainter;
import ui.render.SceneRenderer;
import util.SoundEffect;
import util.StoryText;

/**
 * The opening animation: six short scenes that show what the story is
 * about before chapter 1 begins. It plays once, when a new run starts from
 * the menu, and then hands over to the story pages.
 *
 * ENTER or SPACE jumps to the next scene. ESC skips the whole story, as it
 * does on the story pages, and starts the first level.
 */
public final class IntroScreen implements Screen {

    private static final IntroScene[] SCENES = IntroScene.values();

    private final ScreenManager screens;
    private final IntroPainter painter;
    private int sceneIndex;
    private double sceneTime;   // seconds since the current scene began
    private boolean leaving;    // the next screen has been asked for; the opening stands still

    public IntroScreen(ScreenManager screens, SceneRenderer renderer) {
        this.screens = screens;
        this.painter = new IntroPainter(renderer);
    }

    @Override
    public void onEnter() {
        sceneIndex = 0;
        sceneTime = 0;
        leaving = false;
    }

    @Override
    public void update(double dt) {
        if (leaving) {
            return;
        }
        double before = sceneTime;
        sceneTime += dt;
        getScene().playSounds(screens.getAudio(), before, sceneTime);
        if (sceneTime >= getScene().getSeconds()) {
            nextScene();
        }
    }

    @Override
    public void keyPressed(int keyCode) {
        if (leaving) {
            return;
        }
        if (keyCode == KeyEvent.VK_ENTER || keyCode == KeyEvent.VK_SPACE) {
            screens.getAudio().play(SoundEffect.MENU_MOVE);
            nextScene();
        } else if (keyCode == KeyEvent.VK_ESCAPE) {
            leave(GameState.PLAYING);
        }
    }

    /** Moves on to the next scene; after the last one, the story pages follow. */
    private void nextScene() {
        if (sceneIndex == SCENES.length - 1) {
            leave(GameState.STORY);
            return;
        }
        sceneIndex++;
        sceneTime = 0;
        screens.notifyAccessibility();
    }

    private void leave(GameState target) {
        leaving = true;
        screens.go(target);
    }

    @Override
    public void paint(Graphics2D g) {
        IntroScene scene = getScene();
        scene.paint(painter, g, sceneTime);
        painter.paintFrame(g, scene.getCaption(), sceneIndex, SCENES.length, sceneTime, scene.getSeconds(),
                sceneIndex == SCENES.length - 1);
    }

    /** The scene being shown. */
    public IntroScene getScene() {
        return SCENES[sceneIndex];
    }

    @Override
    public String getAccessibleName() {
        return StoryText.INTRO_NAME;
    }

    @Override
    public String getAccessibleDescription() {
        String caption = getScene().getCaption();
        return caption.isEmpty() ? StoryText.GAME_TITLE + ". " + StoryText.chapterTitle(1) : caption;
    }
}
