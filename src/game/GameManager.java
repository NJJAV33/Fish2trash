package game;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import model.Boat;
import model.CatchResolver;
import model.ChapterData;
import model.Equipment;
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
public final class GameManager implements CatchResolver {

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

        boat.moveBy(boatDirection, dt);
        Hook hook = boat.getHook();
        boolean lineWasOut = hook.isLineOut();
        hook.update(dt);
        level.update(dt, hook, player.getScore());
        for (String tip : level.pollTutorialPrompts()) {
            emit(GameEvent.withText(EventType.TUTORIAL_PROMPT, tip));
        }

        checkHookCollision(hook);
        if (lineWasOut && hook.isSwinging()) {
            onHookReturned(hook);
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

    // ---- Read-only access for the ui ----

    public ChapterData getChapter() {
        return chapter;
    }

    public Player getPlayer() {
        return player;
    }

    public Boat getBoat() {
        return boat;
    }

    /** The objects in the water, as a read-only list. */
    public List<OceanObject> getObjects() {
        return level.getObjects();
    }

    public double getTimeLeft() {
        return timeLimit.getRemaining();
    }

    public double getTimeTotal() {
        return timeLimit.getTotal();
    }

    public int getQuota() {
        return quota;
    }

    public SessionStats getStats() {
        return stats;
    }

    public ChapterStatus getStatus() {
        return status;
    }

    public boolean isPlaying() {
        return status == ChapterStatus.RUNNING;
    }
}
