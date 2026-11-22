package ui;

import game.LevelResult;
import game.EventType;
import game.GameEvent;
import game.GameManager;
import game.GameState;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import model.EquipmentType;
import model.OceanEventType;
import ui.render.EffectsSystem;
import ui.render.HudPainter;
import ui.render.SceneRenderer;
import util.GameConstants;
import util.SoundEffect;
import util.StoryText;

/**
 * The screen where a level is played. Each time it is entered it starts a
 * fresh attempt at the session's current level.
 */
public final class GameScreen implements Screen {

    // After a level ends, hold the last frame briefly before moving on.
    private static final double END_PAUSE_SECONDS = 0.8;
    private static final double BAR_CATCH_UP = 6;   // how fast the quota bar slides to a new score

    private final ScreenManager screens;
    private final InputState input;
    private final SceneRenderer renderer;
    private final HudPainter hud;
    private final EffectsSystem effects = new EffectsSystem();
    private final AudioEventListener sounds;
    private final PauseOverlay pauseOverlay;
    private GameManager game;
    private String toast;        // message currently shown in the toast
    private double toastAge;     // seconds since it appeared
    private String lastTip;      // the last recallable tip or hint, never overwritten by swap-denied notices
    private double time;         // seconds on this screen, drives HUD animation
    private double shownScore;   // the score the quota bar shows; it slides toward the real one
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
        game = screens.getSession().startLevel();
        game.addListener(this::onGameEvent);   // this screen listens for messages to show
        effects.reset(game);
        game.addListener(effects);             // the effects listen for catches and hits
        game.addListener(sounds);              // and so do the sounds
        toast = null;
        toastAge = 0;
        lastTip = null;
        time = 0;
        shownScore = 0;
        secondsSinceEnd = 0;
        leaving = false;
    }

    /** OBSERVER: the game announces events; this screen reacts to the ones it shows. */
    private void onGameEvent(GameEvent event) {
        boolean isTipOrHint = event.getType() == EventType.TUTORIAL_PROMPT
                || event.getType() == EventType.HINT_SHOWN;
        if ((isTipOrHint || event.getType() == EventType.SWAP_DENIED) && !event.getText().isEmpty()) {
            toast = event.getText();
            toastAge = 0;
            if (isTipOrHint) {
                lastTip = toast;
            }
            screens.notifyAccessibility();
        } else if (event.getType() == EventType.OBJECT_CAUGHT
                || event.getType() == EventType.HEALTH_LOST
                || event.getType() == EventType.OCEAN_EVENT_STARTED
                || event.getType() == EventType.TOOL_SWITCHED
                || event.getType() == EventType.QUOTA_REACHED) {
            screens.notifyAccessibility();
        }
    }

    @Override
    public void update(double dt) {
        time += dt;
        toastAge += dt;
        game.setBoatDirection(input.getDirection());
        game.update(dt);   // ignored by the manager once the level is over
        effects.update(dt);
        // The quota bar slides to the new score instead of jumping: fast at first, then easing in.
        shownScore += (game.getScore() - shownScore) * Math.min(1, BAR_CATCH_UP * dt);

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
        LevelResult result = screens.endLevel(game);
        screens.go(result.isCleared() ? GameState.LEVEL_CLEAR : GameState.GAME_OVER);
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
        if (lastTip != null) {
            toast = lastTip;
            toastAge = 0;
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

    /** Pauses the level. Also called when the window loses focus. */
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
        renderer.paint(world, game, effects.getCatchSquash());
        effects.paint(world);
        world.dispose();
        effects.paintFlash(g);

        hud.paint(g, game, time, screens.getAudio().getState(), shownScore);
        hud.paintToast(g, toast, toastAge);
    }

    @Override
    public String getAccessibleName() {
        return game == null ? "Playing" : game.getLevelData().getTitle();
    }

    @Override
    public String getAccessibleDescription() {
        if (game == null) {
            return "";
        }
        int secondsLeft = Math.max(0, (int) Math.floor(game.getTimeLeft()));
        StringBuilder status = new StringBuilder();
        status.append(StoryText.levelLabel(game.getLevelData().getChapter(), game.getLevelData().getLevel()))
                .append(", Difficulty ").append(game.getDifficulty().getDisplayName())
                .append(", Score ").append(game.getScore()).append(" of ").append(game.getQuota())
                .append(", Health ").append(game.getHealth()).append(" of ").append(game.getMaxHealth())
                .append(", Time ").append(String.format("%d:%02d", secondsLeft / 60, secondsLeft % 60))
                .append(", Tool ").append(game.getSelectedTool().getDisplayName());
        if (game.getCombo() >= 2) {
            status.append(", Streak x").append(game.getCombo());
        }
        if (game.getActiveEvent() != OceanEventType.NONE && game.getActiveEventFraction() > 0) {
            status.append(", Event ").append(game.getActiveEvent().getAnnouncement());
        }
        if (toast != null && toastAge < HudPainter.TOAST_SECONDS) {
            status.append(". Tip: ").append(toast);
        }
        return status.toString();
    }
}
