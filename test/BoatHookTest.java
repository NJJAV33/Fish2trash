import model.Boat;
import model.Hook;
import util.GameConstants;

/** Checks boat steering and the hook's swing, launch and recall rules. */
public final class BoatHookTest {

    private static final double TINY = 1e-6;
    private static final double STEP = 0.016;   // one frame

    private BoatHookTest() {
        // only static methods
    }

    public static void run() {
        boatMovesAndTakesTheHookAlong();
        boatStaysInsideTheScreen();
        boatHoldsStillWhileTheLineIsOut();
        hookFliesStraightAndComesBack();
        hookCanBeRecalledOnTheWayOut();
    }

    private static void boatMovesAndTakesTheHookAlong() {
        Boat boat = new Boat(400, GameConstants.WATER_Y);
        boat.moveBy(1, 0.5);
        Check.assertEquals("boat moved right", 400 + GameConstants.BOAT_SPEED * 0.5, boat.getX(), TINY);
        Check.assertEquals("hook anchor follows the boat", boat.getX(), boat.getHook().getAnchorX(), TINY);
        Check.assertEquals("boat faces right", 1, boat.getFacing());

        boat.moveBy(-1, 0.25);
        Check.assertEquals("boat moved back left", 450.0, boat.getX(), TINY);
        Check.assertEquals("boat faces left", -1, boat.getFacing());

        boat.moveBy(0, 1);
        Check.assertEquals("direction 0 does not move the boat", 450.0, boat.getX(), TINY);
    }

    private static void boatStaysInsideTheScreen() {
        Boat boat = new Boat(400, GameConstants.WATER_Y);
        boat.moveBy(1, 100);
        Check.assertEquals("boat stops at the right limit", GameConstants.BOAT_MAX_X, boat.getX(), TINY);
        boat.moveBy(-1, 100);
        Check.assertEquals("boat stops at the left limit", GameConstants.BOAT_MIN_X, boat.getX(), TINY);
    }

    private static void boatHoldsStillWhileTheLineIsOut() {
        Boat boat = new Boat(400, GameConstants.WATER_Y);
        boat.getHook().launch();
        boat.moveBy(1, 1);
        Check.assertEquals("boat cannot move while the hook is out", 400.0, boat.getX(), TINY);
    }

    private static void hookFliesStraightAndComesBack() {
        Hook hook = new Hook(400, GameConstants.WATER_Y);
        Check.assertTrue("hook starts swinging", hook.isSwinging());
        Check.assertEquals("hook starts at rest length", GameConstants.HOOK_REST, hook.getLength(), TINY);

        hook.update(0.4);   // swing a little away from straight down
        double angleAtLaunch = hook.getAngle();
        Check.assertTrue("swing stays inside the widest angle",
                Math.abs(angleAtLaunch) <= Math.toRadians(GameConstants.SWING_MAX_DEG) + TINY);

        hook.launch();
        Check.assertTrue("hook extends after launch", hook.isExtending());
        Check.assertTrue("line counts as out", hook.isLineOut());
        hook.update(0.5);
        Check.assertEquals("angle is frozen while flying", angleAtLaunch, hook.getAngle(), TINY);

        // The tip is where trigonometry says: anchor + length along the angle.
        Check.assertEquals("tip x", 400 + Math.sin(angleAtLaunch) * hook.getLength(), hook.getTipX(), TINY);
        Check.assertEquals("tip y",
                GameConstants.WATER_Y + Math.cos(angleAtLaunch) * hook.getLength(), hook.getTipY(), TINY);

        boolean reachedMax = false;
        for (int i = 0; i < 2000 && !hook.isSwinging(); i++) {
            hook.update(STEP);
            reachedMax = reachedMax || hook.getLength() >= GameConstants.HOOK_MAX - TINY;
            Check.assertTrue("hook never goes past its maximum", hook.getLength() <= GameConstants.HOOK_MAX + TINY);
        }
        Check.assertTrue("a miss goes out to the maximum distance", reachedMax);
        Check.assertTrue("hook is swinging again after a miss", hook.isSwinging());
        Check.assertTrue("a miss comes back empty", hook.getCaughtObject() == null);
    }

    private static void hookCanBeRecalledOnTheWayOut() {
        Hook hook = new Hook(400, GameConstants.WATER_Y);
        hook.recall();
        Check.assertTrue("recall does nothing while swinging", hook.isSwinging());

        hook.launch();
        hook.update(0.2);
        double lengthAtRecall = hook.getLength();
        hook.recall();
        Check.assertTrue("recall starts reeling in", hook.isRetracting());
        hook.update(STEP);
        Check.assertTrue("hook gets shorter after a recall", hook.getLength() < lengthAtRecall);

        hook.launch();
        Check.assertTrue("cannot launch again until the hook is back", hook.isRetracting());
    }
}
