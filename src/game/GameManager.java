package game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;
import model.Boat;
import model.CatchResolver;
import model.Difficulty;
import model.Equipment;
import model.EquipmentLockedException;
import model.EquipmentType;
import model.Hook;
import model.Level;
import model.LevelData;
import model.Loadout;
import model.ObjectView;
import model.OceanEventType;
import model.OceanObject;
import model.Player;
import model.Sea;
import model.SpawnRule;
import model.TimeLimit;
import util.GameConstants;

/**
 * The rules of one attempt at a level: what the hook touches, the countdown,
 * combo streaks, mid-game ocean events, and whether the player has won or lost.
 *
 * It contains no drawing and no keyboard code - the ui package handles those.
 * A new attempt is simply a new GameManager.
 */
public final class GameManager implements GameView, CatchResolver {

    private final LevelData levelData;
    private final Difficulty difficulty;
    private final Level level;
    private final List<GameListener> listeners = new ArrayList<>();
    private final SessionStats stats = new SessionStats();
    // Objects currently flashing or sparking a warning, so each alert fires once per episode.
    private final Set<ObjectView> activeAlerts = Collections.newSetFromMap(new IdentityHashMap<>());
    private final HintEngine hints = new HintEngine();

    private final Player player;
    private final Boat boat;
    private final TimeLimit timeLimit;
    private final int quota;
    private int boatDirection;
    private LevelStatus status;
    private boolean quotaReached;
    private int lastAnnouncedSecond;   // the last "seconds left" that was announced as a low-time tick

    private int combo;
    private int bestCombo;
    private int comboBonus;

    // True once the current launch has touched something or was recalled.
    // A launch that comes back with this still false was a miss.
    private boolean launchResolved;

    /**
     * Starts an attempt on Easy difficulty.
     *
     * @param levelData the level to play
     * @param loadout the tools the crew has
     * @param random  the random source; a test can pass a fixed seed to get
     *                the same game every time
     */
    public GameManager(LevelData levelData, Loadout loadout, Random random) {
        this(levelData, loadout, random, Difficulty.EASY);
    }

    /** Starts an attempt at the chosen difficulty. */
    public GameManager(LevelData levelData, Loadout loadout, Random random, Difficulty difficulty) {
        this.levelData = levelData;
        this.difficulty = difficulty;
        level = new Level(levelData, random, difficulty, true);
        player = new Player(level.getStartHealth());
        boat = new Boat(GameConstants.WORLD_WIDTH / 2.0, GameConstants.WATER_Y, loadout);
        timeLimit = new TimeLimit(level.getTimeLimitSeconds());
        quota = level.getQuota();
        status = LevelStatus.RUNNING;
        addListener(stats);   // the stats count events like any other listener

        // Run the level for zero seconds, so the water is stocked from the first frame.
        level.update(0, boat.getHook());
    }

    /** Registers someone who wants to hear about game events. */
    public void addListener(GameListener listener) {
        listeners.add(listener);
    }

    private void emit(GameEvent event) {
        for (GameListener listener : listeners) {
            listener.onGameEvent(event);
        }
    }

    /**
     * The Space key: drops the hook if it is swinging, or pulls it back early
     * if it is still on its way out.
     */
    public void spaceAction() {
        if (status != LevelStatus.RUNNING) {
            return;
        }
        Hook hook = boat.getHook();
        if (hook.isSwinging()) {
            hook.launch();
            launchResolved = false;
            emit(GameEvent.of(EventType.HOOK_LAUNCHED));
        } else if (hook.isExtending()) {
            hook.recall();
            launchResolved = true;
            emit(GameEvent.of(EventType.HOOK_RECALLED));
        }
    }

    /**
     * The player asks for a tool. Tools are changed at the boat, so the
     * request is refused while the line is out, right after another change,
     * or when the crew does not have that tool yet.
     */
    public void selectTool(EquipmentType type) {
        Hook hook = boat.getHook();
        Loadout loadout = hook.getLoadout();
        if (status != LevelStatus.RUNNING || type == loadout.getSelectedType()) {
            return;
        }
        try {
            if (hook.isSwinging() && loadout.select(type)) {
                emit(GameEvent.withText(EventType.TOOL_SWITCHED, type.getDisplayName()));
            } else {
                emit(GameEvent.of(EventType.SWAP_DENIED));
            }
        } catch (EquipmentLockedException e) {
            // The exception's message says when the tool arrives.
            emit(GameEvent.withText(EventType.SWAP_DENIED, e.getMessage()));
        }
    }

    /** Steps to the next (1) or previous (-1) tool the crew has. */
    public void cycleTool(int direction) {
        selectTool(boat.getHook().getLoadout().next(direction));
    }

    /** Which way the player is steering: -1 left, 0 not at all, 1 right. */
    public void setBoatDirection(int direction) {
        boatDirection = direction;
    }

    /** Advances the game by dt seconds. Does nothing once the level is over. */
    public void update(double dt) {
        if (status != LevelStatus.RUNNING) {
            return;
        }

        timeLimit.tick(dt);   // does nothing once the clock is frozen
        if (timeLimit.isTimeUp()) {
            fail(LevelStatus.FAILED_TIME);
            return;
        }
        announceLowTime();

        boat.moveBy(boatDirection, dt);
        boat.rock(level.getSea().getTilt());   // the swell tips the boat and throws the swing off
        Hook hook = boat.getHook();
        boolean lineWasOut = hook.isLineOut();
        hook.update(dt);
        hook.getLoadout().tick(dt);
        level.update(dt, hook);
        announceOceanEvents();
        updateHookSpeedScale();
        announceAlerts();
        for (String tip : level.pollTutorialPrompts()) {
            emit(GameEvent.withText(EventType.TUTORIAL_PROMPT, tip));
        }

        checkHookCollision(hook);
        if (lineWasOut && hook.isSwinging()) {
            onHookReturned(hook);
        }
        giveHintIfDue(dt);
    }

    private void announceOceanEvents() {
        OceanEventType started = level.pollStartedEvent();
        if (started != OceanEventType.NONE) {
            emit(GameEvent.withValueAndText(EventType.OCEAN_EVENT_STARTED,
                    started.ordinal(), started.getAnnouncement()));
        }
        OceanEventType ended = level.pollEndedEvent();
        if (ended != OceanEventType.NONE) {
            emit(GameEvent.withValueAndText(EventType.OCEAN_EVENT_ENDED,
                    ended.ordinal(), ended.getLabel()));
        }
    }

    private void updateHookSpeedScale() {
        double eventScale = level.getActiveEvent().getHookSpeedScale();
        double flowScale = combo >= GameConstants.HOT_STREAK_MIN ? GameConstants.HOT_STREAK_SPEED_MULT : 1.0;
        boat.getHook().setSpeedScale(eventScale * flowScale);
    }

    /** The hint engine watches the stats; when it has something to say, it is announced like any event. */
    private void giveHintIfDue(double dt) {
        stats.tick(dt);
        String hint = hints.update(dt, stats, this);
        if (status == LevelStatus.RUNNING && !hint.isEmpty()) {
            emit(GameEvent.withText(EventType.HINT_SHOWN, hint));
        }
    }

    /** Announces each object that just began flashing or sparking a warning. */
    private void announceAlerts() {
        List<OceanObject> objects = level.getObjects();
        activeAlerts.removeIf(alerted -> !objects.contains(alerted) || alerted.getAlert() < 1.0);
        for (OceanObject object : objects) {
            if (object.getAlert() >= 1.0 && activeAlerts.add(object)) {
                emit(GameEvent.about(EventType.HAZARD_ALERT, object, 0));
            }
        }
    }

    /** In the last seconds, announces each new second once, so the screen and sound can warn. */
    private void announceLowTime() {
        int secondsLeft = (int) Math.ceil(timeLimit.getRemaining());
        if (!timeLimit.isFrozen() && secondsLeft <= GameConstants.LOW_TIME_S && secondsLeft != lastAnnouncedSecond) {
            lastAnnouncedSecond = secondsLeft;
            emit(GameEvent.withValue(EventType.LOW_TIME_TICK, secondsLeft));
        }
    }

    private void checkHookCollision(Hook hook) {
        // Only a hook that is shooting out and still empty can grab something.
        if (!hook.isExtending() || hook.getCaughtObject() != null) {
            return;
        }

        // POLYMORPHISM: the list holds bottles, cans, nets and fish, because
        // all of them are OceanObjects. The manager never asks which is which.
        Equipment tool = hook.getLoadout().getSelected();
        for (OceanObject object : level.getObjects()) {
            if (object.isWhollyOutsideWater()) {
                continue;
            }
            if (object.collidesWith(hook.getTipX(), hook.getTipY(), tool.getTipRadius())) {
                // The tool decides the outcome (catch, slip or pass), and the
                // outcome calls back onCatch, onSlip or onPass below.
                tool.attempt(object).resolve(this, object);
                if (!hook.isExtending()) {
                    return;   // this throw is over; a pass keeps flying
                }
            }
        }
    }

    @Override
    public void onCatch(OceanObject object) {
        int scoreBefore = player.getScore();
        int healthBefore = player.getHealth();

        // POLYMORPHISM: the same call does different things - trash adds
        // points and rides the line up, a fish costs health and then the
        // tool decides what becomes of it.
        object.onCaught(player, boat.getHook());
        launchResolved = true;

        // The manager looks at what changed instead of asking what the object is.
        if (player.getScore() > scoreBefore) {
            combo++;
            bestCombo = Math.max(bestCombo, combo);
            emit(GameEvent.about(EventType.OBJECT_CAUGHT, object, player.getScore() - scoreBefore));
            if (combo >= 2) {
                int stepBonus = Math.min(GameConstants.COMBO_MAX_SCORE_BONUS,
                        (combo - 1) * GameConstants.COMBO_SCORE_STEP);
                comboBonus += stepBonus;
                emit(GameEvent.about(EventType.COMBO_GAINED, object, combo, "+" + stepBonus));
            }
            updateHookSpeedScale();
        }
        if (player.getHealth() < healthBefore) {
            combo = 0;
            updateHookSpeedScale();
            emit(GameEvent.about(EventType.HEALTH_LOST, object, healthBefore - player.getHealth()));
        }

        if (!player.isAlive()) {
            fail(LevelStatus.FAILED_HEALTH);
        } else if (!quotaReached && player.getScore() >= quota) {
            // Quota met: stop the clock so the catch on the line cannot be
            // lost to it. The level is won when that catch reaches the boat.
            quotaReached = true;
            timeLimit.freeze();
            emit(GameEvent.of(EventType.QUOTA_REACHED));
        }
    }

    @Override
    public void onSlip(OceanObject object) {
        // Wrong tool: nothing is gained or lost except time and streak.
        object.grantImmunity(GameConstants.SLIP_IMMUNITY_S);
        boat.getHook().beginRetractEmpty();
        launchResolved = true;
        combo = 0;
        updateHookSpeedScale();
        emit(GameEvent.about(EventType.SLIPPED, object, 0));
    }

    @Override
    public void onPass(OceanObject object) {
        // The tool ignores this object, so the throw simply carries on.
    }

    /** The hook is back at the boat: unload the catch, let a fish go, or count a miss. */
    private void onHookReturned(Hook hook) {
        OceanObject carried = hook.getCaughtObject();
        if (carried == null) {
            if (!launchResolved) {
                combo = 0;
                updateHookSpeedScale();
                emit(GameEvent.of(EventType.MISSED));
            }
            return;
        }
        hook.clearCaughtObject();
        // POLYMORPHISM again: trash stays aboard, a snagged fish goes back into the sea.
        if (carried.reachBoat()) {
            level.remove(carried);   // the level decides what appears in its place
            emit(GameEvent.about(EventType.DELIVERED, carried, 0));
            if (quotaReached) {
                status = LevelStatus.CLEARED;
                emit(GameEvent.of(EventType.LEVEL_CLEARED));
            }
        } else {
            emit(GameEvent.about(EventType.FISH_FREED, carried, 0));
        }
    }

    private void fail(LevelStatus reason) {
        status = reason;
        emit(GameEvent.of(EventType.LEVEL_FAILED));
    }

    // ---- Reading the game (GameView) ----

    @Override
    public LevelData getLevelData() {
        return levelData;
    }

    @Override
    public Difficulty getDifficulty() {
        return difficulty;
    }

    @Override
    public Boat getBoat() {
        return boat;
    }

    /** The objects in the water, as a read-only list. */
    @Override
    public List<OceanObject> getObjects() {
        return level.getObjects();
    }

    public List<SpawnRule> getSpawnRules() {
        return level.getSpawnRules();
    }

    @Override
    public Sea getSea() {
        return level.getSea();
    }

    @Override
    public int getScore() {
        return player.getScore();
    }

    @Override
    public int getQuota() {
        return quota;
    }

    @Override
    public int getHealth() {
        return player.getHealth();
    }

    @Override
    public int getMaxHealth() {
        return player.getMaxHealth();
    }

    @Override
    public double getTimeLeft() {
        return timeLimit.getRemaining();
    }

    @Override
    public double getTimeTotal() {
        return timeLimit.getTotal();
    }

    @Override
    public boolean isTimeFrozen() {
        return timeLimit.isFrozen();
    }

    @Override
    public LevelStatus getStatus() {
        return status;
    }

    @Override
    public int getCombo() {
        return combo;
    }

    @Override
    public int getBestCombo() {
        return bestCombo;
    }

    @Override
    public int getComboBonus() {
        return comboBonus;
    }

    @Override
    public OceanEventType getActiveEvent() {
        return level.getActiveEvent();
    }

    @Override
    public double getActiveEventFraction() {
        return level.getActiveEventFraction();
    }

    @Override
    public EquipmentType getSelectedTool() {
        return boat.getHook().getLoadout().getSelectedType();
    }

    @Override
    public boolean isToolUnlocked(EquipmentType type) {
        return boat.getHook().getLoadout().isUnlocked(type);
    }

    @Override
    public double getSwapCooldownFraction() {
        return boat.getHook().getLoadout().getCooldownFraction();
    }

    public boolean isPlaying() {
        return status == LevelStatus.RUNNING;
    }

    public Player getPlayer() {
        return player;
    }

    public SessionStats getStats() {
        return stats;
    }
}
