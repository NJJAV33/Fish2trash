package game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import model.Boat;
import model.CatchResolver;
import model.Equipment;
import model.Hook;
import model.Loadout;
import model.ObjectKind;
import model.OceanObject;
import model.Player;
import util.GameConstants;

/**
 * The rules of one game: what is in the ocean, what the hook touches, the
 * countdown, and whether the player has won or lost.
 *
 * It contains no drawing and no keyboard code - the ui package handles those.
 * A new game is simply a new GameManager.
 */
public final class GameManager implements CatchResolver {

    public enum State { PLAYING, WON, LOST }

    // Which trash appears: a kind listed more often is more common.
    private static final ObjectKind[] TRASH_MIX = {
        ObjectKind.PLASTIC_BOTTLE, ObjectKind.PLASTIC_BOTTLE, ObjectKind.PLASTIC_BOTTLE,
        ObjectKind.CAN, ObjectKind.CAN, ObjectKind.FISHING_NET
    };

    // The random source is handed in, so a test can pass a fixed seed and
    // get the same game every time.
    private final Random random;

    // POLYMORPHISM: one list holds bottles, cans, nets and fish, because all
    // of them are OceanObjects. The manager never needs to know which is which.
    private final List<OceanObject> objects = new ArrayList<>();
    private final List<GameListener> listeners = new ArrayList<>();

    private final Player player;
    private final Boat boat;
    private double timeLeft;
    private int boatDirection;
    private State state;
    private String endMessage;

    // True once the current launch has touched something or was recalled.
    // A launch that comes back with this still false was a miss.
    private boolean launchResolved;

    public GameManager(Loadout loadout, Random random) {
        this.random = random;
        player = new Player(GameConstants.HEALTH_CH1);
        boat = new Boat(GameConstants.WORLD_WIDTH / 2.0, GameConstants.WATER_Y, loadout);

        for (int i = 0; i < GameConstants.SPAWN_TRASH_COUNT; i++) {
            objects.add(createTrash());
        }
        for (int i = 0; i < GameConstants.SPAWN_FISH_COUNT; i++) {
            objects.add(createFish());
        }

        timeLeft = GameConstants.TIME_CH1_S;
        state = State.PLAYING;
        endMessage = "";
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
        if (state != State.PLAYING) {
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

    /** Advances the game by dt seconds. Does nothing once the game is over. */
    public void update(double dt) {
        if (state != State.PLAYING) {
            return;
        }

        timeLeft -= dt;
        if (timeLeft <= 0) {
            timeLeft = 0;
            endGame(State.LOST, "Time's up!");
            return;
        }

        boat.moveBy(boatDirection, dt);
        Hook hook = boat.getHook();
        boolean lineWasOut = hook.isLineOut();
        hook.update(dt);
        for (OceanObject object : objects) {
            object.update(dt, GameConstants.WORLD_WIDTH);
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

        Equipment tool = hook.getLoadout().getSelected();
        for (OceanObject object : objects) {
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
        checkEndConditions();
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
            objects.remove(carried);
            hook.clearCaughtObject();
            objects.add(createTrash());   // keep the ocean stocked
            emit(GameEvent.about(EventType.DELIVERED, carried, 0));
        } else if (!launchResolved) {
            emit(GameEvent.of(EventType.MISSED));
        }
    }

    private void checkEndConditions() {
        if (player.getScore() >= GameConstants.QUOTA_CH1) {
            emit(GameEvent.of(EventType.QUOTA_REACHED));
            endGame(State.WON, "Quota reached!");
        } else if (!player.isAlive()) {
            endGame(State.LOST, "You caught too many fish!");
        }
    }

    private void endGame(State result, String message) {
        state = result;
        endMessage = message;
        if (result == State.WON) {
            emit(GameEvent.of(EventType.CHAPTER_CLEARED));
        } else {
            emit(GameEvent.withText(EventType.CHAPTER_FAILED, message));
        }
    }

    // ---- Spawning ----

    private OceanObject createTrash() {
        ObjectKind kind = TRASH_MIX[random.nextInt(TRASH_MIX.length)];
        return kind.create(randomX(), randomY(),
                randomSpeed(GameConstants.TRASH_SPEED_MIN, GameConstants.TRASH_SPEED_MAX));
    }

    private OceanObject createFish() {
        return ObjectKind.SMALL_FISH.create(randomX(), randomY(),
                randomSpeed(GameConstants.FISH_SPEED_MIN, GameConstants.FISH_SPEED_MAX));
    }

    private double randomX() {
        return GameConstants.SPAWN_MARGIN_X
                + random.nextDouble() * (GameConstants.WORLD_WIDTH - 2 * GameConstants.SPAWN_MARGIN_X);
    }

    private double randomY() {
        return GameConstants.MIN_SPAWN_Y
                + random.nextDouble() * (GameConstants.MAX_SPAWN_Y - GameConstants.MIN_SPAWN_Y);
    }

    /** A speed between min and max, randomly to the left or right. */
    private double randomSpeed(double min, double max) {
        double speed = min + random.nextDouble() * (max - min);
        return random.nextBoolean() ? speed : -speed;
    }

    // ---- Read-only access for the ui ----

    public Player getPlayer() {
        return player;
    }

    public Boat getBoat() {
        return boat;
    }

    /** Returns a read-only view, so other classes cannot add or remove objects. */
    public List<OceanObject> getObjects() {
        return Collections.unmodifiableList(objects);
    }

    public double getTimeLeft() {
        return timeLeft;
    }

    public State getState() {
        return state;
    }

    public boolean isPlaying() {
        return state == State.PLAYING;
    }

    public String getEndMessage() {
        return endMessage;
    }
}
