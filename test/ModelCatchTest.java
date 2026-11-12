import model.Can;
import model.FishingNet;
import model.Hook;
import model.Loadout;
import model.ObjectKind;
import model.OceanObject;
import model.PlasticBottle;
import model.Player;
import model.SmallFish;
import util.GameConstants;

/**
 * Checks what each kind of object does when the hook catches it
 * (the polymorphic onCaught method).
 */
public final class ModelCatchTest {

    private ModelCatchTest() {
        // only static methods
    }

    public static void run() {
        trashGivesPointsAndRidesTheHook();
        fishCostsHealthAndSwimsAway();
        everyKindBuildsItsOwnObject();
    }

    private static void trashGivesPointsAndRidesTheHook() {
        Player player = new Player(3);

        Hook hook = launchedHook();
        OceanObject bottle = new PlasticBottle(100, 300, 20);
        bottle.onCaught(player, hook);
        Check.assertEquals("bottle is worth 10", 10, player.getScore());
        Check.assertTrue("bottle is on the hook", hook.getCaughtObject() == bottle);
        Check.assertTrue("bottle is marked caught", bottle.isCaught());
        Check.assertTrue("a caught bottle cannot be caught again", !bottle.isCatchable());
        Check.assertTrue("hook reels in after a catch", hook.getState() == Hook.State.RETRACTING);

        new Can(100, 300, 20).onCaught(player, launchedHook());
        Check.assertEquals("can adds 20", 30, player.getScore());

        new FishingNet(100, 300, 20).onCaught(player, launchedHook());
        Check.assertEquals("net adds 50", 80, player.getScore());
        Check.assertEquals("trash never costs health", 3, player.getHealth());
    }

    private static void fishCostsHealthAndSwimsAway() {
        Player player = new Player(3);
        Hook hook = launchedHook();
        OceanObject fish = new SmallFish(100, 300, 50);

        fish.onCaught(player, hook);
        Check.assertEquals("fish costs 1 health", 2, player.getHealth());
        Check.assertEquals("fish gives no points", 0, player.getScore());
        Check.assertTrue("fish is not kept on the hook", hook.getCaughtObject() == null);
        Check.assertTrue("hook reels in empty", hook.getState() == Hook.State.RETRACTING);
        Check.assertTrue("fish swims free", !fish.isCaught());
        Check.assertTrue("fish cannot be hooked again right away", !fish.isCatchable());

        fish.update(GameConstants.FISH_RELEASE_IMMUNITY_S + 0.1, GameConstants.WORLD_WIDTH);
        Check.assertTrue("fish can be hooked again later", fish.isCatchable());
    }

    private static void everyKindBuildsItsOwnObject() {
        for (ObjectKind kind : ObjectKind.values()) {
            OceanObject object = kind.create(100, 300, 20);
            Check.assertEquals(kind + " creates its own kind", kind, object.getKind());
        }
        Check.assertTrue("a can is trash", ObjectKind.CAN.isTrash());
        Check.assertTrue("a fish is not trash", !ObjectKind.SMALL_FISH.isTrash());
    }

    private static Hook launchedHook() {
        Hook hook = new Hook(400, GameConstants.WATER_Y, Loadout.standard());
        hook.launch();
        return hook;
    }
}
