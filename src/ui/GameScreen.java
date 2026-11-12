package ui;

import game.ChapterResult;
import game.EventType;
import game.GameEvent;
import game.GameManager;
import game.GameState;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import model.EquipmentType;
import ui.render.EffectsSystem;
import ui.render.HudPainter;
import ui.render.SceneRenderer;
import util.GameConstants;
import util.SoundEffect;

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
    private final HudPainter hud;
    private final EffectsSystem effects = new EffectsSystem();
    private final AudioEventListener sounds;
    private final PauseOverlay pauseOverlay;
    private GameManager game;
    private String tip;          // the last tip or hint, or null before the first one
    private double tipAge;       // seconds since it appeared
    private double time;         // seconds on this screen, drives HUD animation
    private double secondsSinceEnd;
    private boolean leaving;

    public GameScreen(ScreenManager screens, InputState input, SceneRenderer renderer) {
        this.screens = screens;
        this.input = input;
        this.renderer = renderer;
        this.hud = new HudPainter(renderer.getPainters());
        this.pauseOverlay = new PauseOverlay(screens);
        this.sounds = new AudioEventListener(screens.getAudio());
    }

    @Override
    public void onEnter() {
        game = screens.getSession().startChapter();
        game.addListener(this::onGameEvent);   // this screen listens for messages to show
        effects.reset(game);
        game.addListener(effects);             // the effects listen for catches and hits
        game.addListener(sounds);              // and so do the sounds
        tip = null;
        time = 0;
        secondsSinceEnd = 0;
        leaving = false;
    }

    /** OBSERVER: the game announces events; this screen reacts to the ones it shows. */
    private void onGameEvent(GameEvent event) {
        // A tip, a hint, or the reason a tool could not be picked. All appear in the same toast.
        boolean carriesMessage = event.getType() == EventType.TUTORIAL_PROMPT
                || event.getType() == EventType.HINT_SHOWN
                || event.getType() == EventType.SWAP_DENIED;
        if (carriesMessage && !event.getText().isEmpty()) {
            tip = event.getText();
            tipAge = 0;
        }
    }

    @Override
    public void update(double dt) {
        time += dt;
        tipAge += dt;
        game.setBoatDirection(input.getDirection());
        game.update(dt);   // ignored by the manager once the chapter is over
        effects.update(dt);

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
        } else if (keyCode == KeyEvent.VK_H) {
            showLastTipAgain();
        } else if (GameConstants.EQUIPMENT_ENABLED) {
            toolKey(keyCode);
        }
    }

    /** H brings the last tip or hint back, for a player who missed it. */
    private void showLastTipAgain() {
        if (tip != null) {
            tipAge = 0;
            screens.getAudio().play(SoundEffect.HINT);
        }
    }

    /** 1, 2 and 3 pick a tool directly; Q and E step to the previous and next one. */
    private void toolKey(int keyCode) {
        EquipmentType[] tools = EquipmentType.values();
        int number = keyCode - KeyEvent.VK_1;   // 0 for the 1 key, 1 for the 2 key, ...
        if (number >= 0 && number < tools.length) {
            game.selectTool(tools[number]);
        } else if (keyCode == KeyEvent.VK_Q) {
            game.cycleTool(-1);
        } else if (keyCode == KeyEvent.VK_E) {
            game.cycleTool(1);
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
        // The world can shake; the HUD on top of it stays still.
        Graphics2D world = (Graphics2D) g.create();
        world.translate(effects.getShakeX(), effects.getShakeY());
        renderer.paint(world, game);
        effects.paint(world);
        world.dispose();
        effects.paintFlash(g);

        hud.paint(g, game, time, screens.getAudio().getState());
        hud.paintToast(g, tip, tipAge);
    }
}
