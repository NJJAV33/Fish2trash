import model.OceanObject;
import model.SmallFish;
import util.GameConstants;

/** Checks the behaviour every ocean object shares: movement, bouncing and collision. */
public final class OceanObjectTest {

    private static final double TINY = 1e-9;

    private OceanObjectTest() {
        // only static methods
    }

    public static void run() {
        movesAtItsSpeed();
        bouncesOffBothEdgesAndTurnsAround();
        aCaughtObjectStaysWhereTheHookPutsIt();
        collisionIsCircleAgainstCircle();
    }

    private static void movesAtItsSpeed() {
        OceanObject fish = new SmallFish(100, 300, 50);
        fish.update(2, GameConstants.WORLD_WIDTH);
        Check.assertEquals("x after 2 s at 50 px/s", 200.0, fish.getX(), TINY);
        Check.assertEquals("y does not change", 300.0, fish.getY(), TINY);
        Check.assertEquals("faces right when swimming right", 1, fish.getFacing());
    }

    private static void bouncesOffBothEdgesAndTurnsAround() {
        double r = GameConstants.RADIUS_SMALL_FISH;

        OceanObject goingRight = new SmallFish(790, 300, 50);
        goingRight.update(1, GameConstants.WORLD_WIDTH);
        Check.assertEquals("stops at the right edge", GameConstants.WORLD_WIDTH - r, goingRight.getX(), TINY);
        Check.assertEquals("turns to face left", -1, goingRight.getFacing());
        goingRight.update(1, GameConstants.WORLD_WIDTH);
        Check.assertTrue("swims back left", goingRight.getX() < GameConstants.WORLD_WIDTH - r);

        OceanObject goingLeft = new SmallFish(10, 300, -50);
        Check.assertEquals("faces left when swimming left", -1, goingLeft.getFacing());
        goingLeft.update(1, GameConstants.WORLD_WIDTH);
        Check.assertEquals("stops at the left edge", r, goingLeft.getX(), TINY);
        Check.assertEquals("turns to face right", 1, goingLeft.getFacing());
    }

    private static void aCaughtObjectStaysWhereTheHookPutsIt() {
        OceanObject fish = new SmallFish(100, 300, 50);
        fish.markCaught();
        fish.moveTo(400, 200);
        fish.update(1, GameConstants.WORLD_WIDTH);
        Check.assertEquals("a caught object does not swim", 400.0, fish.getX(), TINY);
    }

    private static void collisionIsCircleAgainstCircle() {
        double r = GameConstants.RADIUS_SMALL_FISH;
        OceanObject fish = new SmallFish(100, 100, 0);

        Check.assertTrue("overlapping circles collide", fish.collidesWith(100 + r, 100, 5));
        Check.assertTrue("circles that just touch collide", fish.collidesWith(100 + r + 5, 100, 5));
        Check.assertTrue("circles apart do not collide", !fish.collidesWith(100 + r + 5.5, 100, 5));
        // Diagonal: a 3-4-5 triangle puts the centres exactly "reach" apart,
        // so half a pixel closer overlaps and one pixel further does not.
        double reach = r + 5;
        Check.assertTrue("diagonal overlap collides",
                fish.collidesWith(100 + reach * 0.6 - 0.5, 100 + reach * 0.8 - 0.5, 5));
        Check.assertTrue("diagonal gap does not collide",
                !fish.collidesWith(100 + reach * 0.6 + 1, 100 + reach * 0.8 + 1, 5));
    }
}
