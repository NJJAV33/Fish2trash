package game;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import model.Boat;
import model.CatchResolver;
import model.ChapterData;
import model.Equipment;
import model.EquipmentLockedException;
import model.EquipmentType;
import model.Hook;
import model.Level;
import model.Loadout;
import model.OceanObject;
import model.Player;
import model.TimeLimit;
import util.GameConstants;

/**
 * The rules of one chapter attempt: what the hook touches, the countdown, and
 * whether the player has won or lost.
 *
 * It contains no drawing and no keyboard code - the ui package handles those.
 * A new attempt is simply a new GameManager.
 */
public final class GameManager implements GameView, CatchResolver {

    private final ChapterData chapter;
    private final Level level;
    private final List<GameListener> listeners = new ArrayList<>();
    private final SessionStats stats = new SessionStats();

    private final Player player;
    private final Boat boat;
    private final TimeLimit timeLimit;
    private final int quota;
    private int boatDirection;
    private ChapterStatus status;
    private boolean quotaReached;
    private int lastAnnouncedSecond;   // the last "seconds left" that was announced as a low-time tick

    // True once the current launch has touched something or was recalled.
    // A launch that comes back with this still false was a miss.
    private boolean launchResolved;

    /**
     * @param chapter the chapter to play
     * @param loadout the tools the crew has
     * @param random  the random source; a test can pass a fixed seed to get
     *                the same game every time
     */
    public GameManager(ChapterData chapter, Loadout loadout, Random random) {
        this.chapter = chapter;
        level = new Level(chapter, random);
        player = new Player(level.getStartHealth());
        boat = new Boat(GameConstants.WORLD_WIDTH / 2.0, GameConstants.WATER_Y, loadout);
        timeLimit = new TimeLimit(level.getTimeLimitSeconds());
        quota = level.getQuota();
        status = ChapterStatus.RUNNING;
        addListener(stats);   // the stats count events like any other listener

        // Run the level for zero seconds, so the water is stocked from the first frame.
        level.update(0, boat.getHook(), 0);
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
        if (status != ChapterStatus.RUNNING) {
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
        if (status != ChapterStatus.RUNNING || type == loadout.getSelectedType()) {
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

    /** Advances the game by dt seconds. Does nothing once the chapter is over. */
    public void update(double dt) {
        if (status != ChapterStatus.RUNNING) {
            return;
        }

        timeLimit.tick(dt);   // does nothing once the clock is frozen
        if (timeLimit.isTimeUp()) {
            fail(ChapterStatus.FAILED_TIME);
            return;
        }
        announceLowTime();

        boat.moveBy(boatDirection, dt);
        Hook hook = boat.getHook();
        boolean lineWasOut = hook.isLineOut();
        hook.update(dt);
        hook.getLoadout().tick(dt);
        level.update(dt, hook, player.getScore());
        for (String tip : level.pollTutorialPrompts()) {
            emit(GameEvent.withText(EventType.TUTORIAL_PROMPT, tip));
        }

        checkHookCollision(hook);
        if (lineWasOut && hook.isSwinging()) {
            onHookReturned(hook);
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
        // points and rides the hook, a fish costs health and swims away.
        object.onCaught(player, boat.getHook());
        launchResolved = true;

        // The manager looks at what changed instead of asking what the object is.
        if (player.getScore() > scoreBefore) {
            emit(GameEvent.about(EventType.OBJECT_CAUGHT, object, player.getScore() - scoreBefore));
        }
        if (player.getHealth() < healthBefore) {
            emit(GameEvent.about(EventType.HEALTH_LOST, object, healthBefore - player.getHealth()));
        }

        if (!player.isAlive()) {
            fail(ChapterStatus.FAILED_HEALTH);
        } else if (!quotaReached && player.getScore() >= quota) {
            // Quota met: stop the clock so the catch on the line cannot be
            // lost to it. The chapter is won when that catch reaches the boat.
            quotaReached = true;
            timeLimit.freeze();
            emit(GameEvent.of(EventType.QUOTA_REACHED));
        }
    }

    @Override
    public void onSlip(OceanObject object) {
        // Wrong tool: nothing is gained or lost except time.
        object.grantImmunity(GameConstants.SLIP_IMMUNITY_S);
        boat.getHook().beginRetractEmpty();
        launchResolved = true;
        emit(GameEvent.about(EventType.SLIPPED, object, 0));
    }

    @Override
    public void onPass(OceanObject object) {
        // The tool ignores this object, so the throw simply carries on.
    }

    /** The hook is back at the boat: unload the catch, or count a miss. */
    private void onHookReturned(Hook hook) {
        OceanObject carried = hook.getCaughtObject();
        if (carried != null) {
            level.remove(carried);   // the level decides what appears in its place
            hook.clearCaughtObject();
            emit(GameEvent.about(EventType.DELIVERED, carried, 0));
            if (quotaReached) {
                status = ChapterStatus.CLEARED;
                emit(GameEvent.of(EventType.CHAPTER_CLEARED));
            }
        } else if (!launchResolved) {
            emit(GameEvent.of(EventType.MISSED));
        }
    }

    private void fail(ChapterStatus reason) {
        status = reason;
        emit(GameEvent.of(EventType.CHAPTER_FAILED));
    }

    // ---- Reading the game (GameView) ----

    @Override
    public ChapterData getChapter() {
        return chapter;
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
    public ChapterStatus getStatus() {
        return status;
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
        return status == ChapterStatus.RUNNING;
    }

    public Player getPlayer() {
        return player;
    }

    public SessionStats getStats() {
        return stats;
    }
}
