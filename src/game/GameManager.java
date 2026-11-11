package game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import model.Boat;
import model.Hook;
import model.ObjectKind;
import model.OceanObject;
import model.Player;
import util.GameConstants;

/**
 * The rules of the game: what is in the ocean, what the hook touches, the
 * countdown, and whether the player has won or lost.
 *
 * It contains no drawing and no keyboard code - GamePanel handles those.
 */
public final class GameManager {

    public enum State { PLAYING, WON, LOST }

    // Which trash appears: a kind listed more often is more common.
    private static final ObjectKind[] TRASH_MIX = {
        ObjectKind.PLASTIC_BOTTLE, ObjectKind.PLASTIC_BOTTLE, ObjectKind.PLASTIC_BOTTLE,
        ObjectKind.CAN, ObjectKind.CAN, ObjectKind.FISHING_NET
    };

    private final Random random = new Random();

    // POLYMORPHISM: one list holds bottles, cans, nets and fish, because all
    // of them are OceanObjects. The manager never needs to know which is which.
    private final List<OceanObject> objects = new ArrayList<>();

    private Player player;
    private Boat boat;
    private double timeLeft;
    private State state;
    private String endMessage;

    public GameManager() {
        restart();
    }

    /** Puts every piece of game state back to the start. */
    public void restart() {
        player = new Player(GameConstants.HEALTH_CH1);
        boat = new Boat(GameConstants.WORLD_WIDTH / 2.0, GameConstants.WATER_Y);   // new boat = new hook too

        objects.clear();
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

    public void launchHook() {
        if (state == State.PLAYING) {
            boat.getHook().launch();
        }
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

        Hook hook = boat.getHook();
        hook.update(dt);
        for (OceanObject object : objects) {
            object.update(dt, GameConstants.WORLD_WIDTH);
        }

        checkHookCollision(hook);
        removeDeliveredObject(hook);
    }

    private void checkHookCollision(Hook hook) {
        // Only a hook that is shooting out and still empty can grab something.
        if (!hook.isExtending() || hook.getCaughtObject() != null) {
            return;
        }

        for (OceanObject object : objects) {
            // isCatchable() is false for caught or just-released objects,
            // so nothing can be collected twice.
            if (object.isCatchable()
                    && object.collidesWith(hook.getTipX(), hook.getTipY(), GameConstants.RADIUS_HOOK)) {
                // POLYMORPHISM: the same call does different things -
                // trash adds points and rides the hook, a fish costs health
                // and swims away.
                object.onCaught(player, hook);
                checkEndConditions();
                return;   // one object per launch
            }
        }
    }

    /** Once the hook is back at the boat, the caught object leaves the ocean. */
    private void removeDeliveredObject(Hook hook) {
        OceanObject carried = hook.getCaughtObject();
        if (carried != null && hook.isSwinging()) {
            objects.remove(carried);
            hook.clearCaughtObject();
            objects.add(createTrash());   // keep the ocean stocked
        }
    }

    private void checkEndConditions() {
        if (player.getScore() >= GameConstants.QUOTA_CH1) {
            endGame(State.WON, "Quota reached!");
        } else if (!player.isAlive()) {
            endGame(State.LOST, "You caught too many fish!");
        }
    }

    private void endGame(State result, String message) {
        state = result;
        endMessage = message;
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

    // ---- Read-only access for GamePanel ----

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
