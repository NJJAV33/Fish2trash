import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * The rules of the game: what is in the ocean, what the hook touches, the
 * countdown, and whether the player has won or lost.
 *
 * It contains no drawing and no keyboard code - GamePanel handles those.
 */
public class GameManager {

    public enum State { PLAYING, WON, LOST }

    public static final int WORLD_WIDTH = 800;
    public static final int WORLD_HEIGHT = 600;
    public static final int WATER_Y = 130;        // y position of the sea surface

    public static final int QUOTA = 50;
    public static final int STARTING_HEALTH = 3;
    public static final double GAME_SECONDS = 60;

    private static final int TRASH_COUNT = 6;
    private static final int FISH_COUNT = 4;

    private final Random random = new Random();

    // POLYMORPHISM: one list holds both OceanTrash and Fish, because both
    // are OceanObjects. The manager never needs to know which is which.
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
        player = new Player(STARTING_HEALTH);
        boat = new Boat(WORLD_WIDTH / 2.0, WATER_Y);   // new boat = new hook too

        objects.clear();
        for (int i = 0; i < TRASH_COUNT; i++) {
            objects.add(createTrash());
        }
        for (int i = 0; i < FISH_COUNT; i++) {
            objects.add(createFish());
        }

        timeLeft = GAME_SECONDS;
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
            object.update(dt, WORLD_WIDTH);
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
            // Skipping caught objects means nothing can be collected twice.
            if (!object.isCaught()
                    && object.collidesWith(hook.getTipX(), hook.getTipY(), Hook.TIP_RADIUS)) {
                object.markCaught();

                // POLYMORPHISM: the same call does different things -
                // OceanTrash adds points, Fish removes health.
                object.onCaught(player);

                hook.attach(object);
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
            objects.add(createRandomObject());   // keep the ocean stocked
        }
    }

    private void checkEndConditions() {
        if (player.getScore() >= QUOTA) {
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

    private OceanObject createRandomObject() {
        // 60% trash, 40% fish
        return (random.nextDouble() < 0.6) ? createTrash() : createFish();
    }

    private OceanTrash createTrash() {
        return new OceanTrash(randomX(), randomY(), randomSpeed(20, 45));
    }

    private Fish createFish() {
        return new Fish(randomX(), randomY(), randomSpeed(60, 120));
    }

    private double randomX() {
        return 40 + random.nextDouble() * (WORLD_WIDTH - 80);
    }

    private double randomY() {
        return WATER_Y + 80 + random.nextDouble() * (WORLD_HEIGHT - WATER_Y - 140);
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
