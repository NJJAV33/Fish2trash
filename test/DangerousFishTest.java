import model.Barracuda;
import model.BigFish;
import model.Hook;
import model.HookSensor;
import model.Loadout;
import model.OceanObject;
import model.Player;
import model.Pufferfish;
import model.Stingray;
import model.SmallFish;
import util.GameConstants;

/** Checks the fish with their own behaviour: big fish, pufferfish and barracuda. */
public final class DangerousFishTest {

    private static final double STEP = 0.016;
    private static final double TINY = 1e-9;

    private DangerousFishTest() {
        // only static methods
    }

    public static void run() {
        damageDependsOnTheFish();
        pufferfishWeavesAroundItsDepth();
        pufferfishSwellsNearTheHook();
        barracudaIgnoresAHookAtRest();
        barracudaWarnsThenCharges();
        stingrayIgnoresEveryToolButTheMagnet();
        stingrayWarnsThenRisesAtTheMagnet();
        stingrayGivesUpWhenTheMagnetComesOff();
        theNetCutterMakesEveryFishCostDouble();
    }

    private static void damageDependsOnTheFish() {
        Check.assertEquals("big fish costs 1 health", 4, healthAfterHooking(new BigFish(100, 300, 50)));
        Check.assertEquals("pufferfish costs 2 health", 3, healthAfterHooking(new Pufferfish(100, 300, 50)));
        Check.assertEquals("barracuda costs 2 health", 3, healthAfterHooking(new Barracuda(100, 300, 50)));
        Check.assertEquals("stingray costs 2 health", 3, healthAfterHooking(new Stingray(100, 550, 30)));
    }

    private static int healthAfterHooking(OceanObject fish) {
        Player player = new Player(5);
        Hook hook = new Hook(400, GameConstants.WATER_Y, Loadout.forChapter(1));
        hook.launch();
        fish.onCaught(player, hook);
        return player.getHealth();
    }

    private static void pufferfishWeavesAroundItsDepth() {
        OceanObject puffer = new Pufferfish(400, 350, 0);
        FakeHook farAway = new FakeHook(0, 0, false);
        double highest = 350;
        double lowest = 350;
        for (double t = 0; t < GameConstants.PUFFER_WEAVE_PERIOD_S; t += STEP) {
            puffer.sense(farAway);
            puffer.update(STEP, GameConstants.WORLD_WIDTH);
            highest = Math.min(highest, puffer.getY());
            lowest = Math.max(lowest, puffer.getY());
        }
        Check.assertEquals("weaves up by the amplitude", 350 - GameConstants.PUFFER_WEAVE_AMPLITUDE, highest, 1.0);
        Check.assertEquals("weaves down by the amplitude", 350 + GameConstants.PUFFER_WEAVE_AMPLITUDE, lowest, 1.0);
        Check.assertEquals("stays calm when the hook is far", 0.0, puffer.getAlert(), TINY);
        Check.assertEquals("calm size", GameConstants.RADIUS_PUFFER, puffer.getRadius(), TINY);
    }

    private static void pufferfishSwellsNearTheHook() {
        OceanObject puffer = new Pufferfish(400, 350, 0);
        FakeHook near = new FakeHook(400, 350 - GameConstants.PUFFER_SENSE_RANGE + 70, true);
        for (double t = 0; t < 1.0; t += STEP) {
            near.tipY = puffer.getY() - 30;   // keep the hook right beside it
            puffer.sense(near);
            puffer.update(STEP, GameConstants.WORLD_WIDTH);
        }
        Check.assertEquals("fully swollen near the hook", 1.0, puffer.getAlert(), TINY);
        Check.assertEquals("swollen size is 50% bigger",
                GameConstants.RADIUS_PUFFER * (1 + GameConstants.PUFFER_SWELL), puffer.getRadius(), TINY);
        Check.assertTrue("a swollen pufferfish is easier to touch",
                puffer.collidesWith(puffer.getX() + GameConstants.RADIUS_PUFFER + 8, puffer.getY(), 1));

        FakeHook gone = new FakeHook(0, 0, false);
        for (double t = 0; t < 1.0; t += STEP) {
            puffer.sense(gone);
            puffer.update(STEP, GameConstants.WORLD_WIDTH);
        }
        Check.assertEquals("shrinks again when the hook is gone", 0.0, puffer.getAlert(), TINY);
    }

    private static void barracudaIgnoresAHookAtRest() {
        OceanObject barracuda = new Barracuda(300, 400, 100);
        FakeHook resting = new FakeHook(320, 170, false);   // close, but the line is not out
        for (double t = 0; t < 1.0; t += STEP) {
            barracuda.sense(resting);
            barracuda.update(STEP, GameConstants.WORLD_WIDTH);
        }
        Check.assertEquals("no warning while the hook is at rest", 0.0, barracuda.getAlert(), TINY);
        Check.assertEquals("keeps patrolling at its own speed", 400.0, barracuda.getX(), 2.0);
    }

    private static void barracudaWarnsThenCharges() {
        OceanObject barracuda = new Barracuda(300, 400, 100);
        FakeHook hook = new FakeHook(380, 250, true);   // line out, 80 px to the side

        // Warning: it stops and flashes.
        barracuda.sense(hook);
        barracuda.update(STEP, GameConstants.WORLD_WIDTH);
        double xAtWarning = barracuda.getX();
        double yAtWarning = barracuda.getY();
        play(barracuda, hook, GameConstants.BARRACUDA_TELEGRAPH_S - 0.1);
        Check.assertEquals("flashes a warning", 1.0, barracuda.getAlert(), TINY);
        Check.assertEquals("holds still during the warning", xAtWarning, barracuda.getX(), TINY);

        // Charge: fast, toward the hook (up and to the right from here).
        play(barracuda, hook, 0.1 + GameConstants.BARRACUDA_CHARGE_S / 2);
        Check.assertTrue("charges toward the hook sideways", barracuda.getX() > xAtWarning + 20);
        Check.assertTrue("charges toward the hook upward", barracuda.getY() < yAtWarning - 20);
        Check.assertEquals("faces the way it charges", 1, barracuda.getFacing());
        play(barracuda, hook, GameConstants.BARRACUDA_CHARGE_S / 2 + 0.05);
        double charged = Math.hypot(barracuda.getX() - xAtWarning, barracuda.getY() - yAtWarning);
        Check.assertEquals("charge distance = speed x time",
                GameConstants.BARRACUDA_CHARGE_SPEED * GameConstants.BARRACUDA_CHARGE_S, charged, 25);

        // Cooldown: calm again, and it cannot be provoked for a while.
        Check.assertEquals("calm after the charge", 0.0, barracuda.getAlert(), TINY);
        play(barracuda, hook, GameConstants.BARRACUDA_COOLDOWN_S - 0.3);
        Check.assertEquals("does not charge again during the cooldown", 0.0, barracuda.getAlert(), TINY);

        // After the cooldown it reacts again.
        hook.tipX = barracuda.getX();
        play(barracuda, hook, 0.5);
        Check.assertEquals("warns again after the cooldown", 1.0, barracuda.getAlert(), TINY);
    }

    private static void stingrayIgnoresEveryToolButTheMagnet() {
        OceanObject ray = new Stingray(400, 550, 0);
        FakeHook hookLine = new FakeHook(400, 450, true);   // right above it, line out, but no magnet
        play(ray, hookLine, 3.0);
        Check.assertEquals("a hook or cutter line leaves it calm", 0.0, ray.getAlert(), TINY);
        Check.assertEquals("and on the sand", 550, ray.getY(), TINY);

        FakeHook farMagnet = new FakeHook(400 + GameConstants.STINGRAY_SENSE_RANGE + 40, 170, false);
        farMagnet.magnetic = true;
        play(ray, farMagnet, 3.0);
        Check.assertEquals("a magnet too far to the side leaves it calm", 0.0, ray.getAlert(), TINY);
    }

    private static void stingrayWarnsThenRisesAtTheMagnet() {
        OceanObject ray = new Stingray(400, 550, 0);
        FakeHook magnet = new FakeHook(460, 170, false);   // the Magnet hangs under the boat; the line is not even out
        magnet.magnetic = true;

        play(ray, magnet, GameConstants.STINGRAY_WARNING_S - 0.1);
        Check.assertEquals("it sparks as a warning", 1.0, ray.getAlert(), TINY);
        Check.assertEquals("and does not move during the warning", 550, ray.getY(), TINY);

        play(ray, magnet, 1.1);
        Check.assertTrue("then it leaves the sand", ray.getY() < 550 - 60);
        Check.assertTrue("toward the magnet", ray.getX() > 400);
        Check.assertEquals("still sparking", 1.0, ray.getAlert(), TINY);

        play(ray, magnet, 5.0);
        Check.assertEquals("it swims all the way up to the magnet", 460, ray.getX(), 2.0);
        Check.assertEquals("and stays there", 170, ray.getY(), 2.0);

        // The boat is faster than the ray: steering away loses it.
        magnet.tipX = 460 + GameConstants.STINGRAY_LOSE_RANGE + 20;
        play(ray, magnet, 0.1);
        Check.assertEquals("out of reach, the sparks stop", 0.0, ray.getAlert(), TINY);
        play(ray, magnet, 6.0);
        Check.assertEquals("and it sinks back to the sand", 550, ray.getY(), TINY);
    }

    private static void stingrayGivesUpWhenTheMagnetComesOff() {
        OceanObject ray = new Stingray(400, 550, 0);
        FakeHook magnet = new FakeHook(400, 170, false);
        magnet.magnetic = true;
        play(ray, magnet, GameConstants.STINGRAY_WARNING_S + 1.0);
        double reached = ray.getY();
        Check.assertTrue("it is on its way up", reached < 550 - 80);

        magnet.magnetic = false;   // the player put the Hook on the line
        play(ray, magnet, 0.1);
        Check.assertEquals("the sparks stop at once", 0.0, ray.getAlert(), TINY);
        play(ray, magnet, 0.5);
        Check.assertTrue("and it sinks again", ray.getY() > reached);

        // A sinking ray ignores the Magnet until it is back on the sand.
        magnet.magnetic = true;
        play(ray, magnet, 0.5);
        Check.assertEquals("the Magnet does not turn it around on the way down", 0.0, ray.getAlert(), TINY);
        play(ray, magnet, 3.0);
        Check.assertEquals("back on the sand, it feels the Magnet again", 1.0, ray.getAlert(), TINY);

        // Changing tools during the warning is always in time: the ray never left the sand.
        OceanObject second = new Stingray(400, 550, 0);
        FakeHook quick = new FakeHook(400, 170, false);
        quick.magnetic = true;
        play(second, quick, GameConstants.STINGRAY_WARNING_S - 0.2);
        quick.magnetic = false;
        play(second, quick, 1.0);
        Check.assertEquals("a quick change of tools keeps the ray on the sand", 550, second.getY(), TINY);
    }

    private static void theNetCutterMakesEveryFishCostDouble() {
        Player player = new Player(5);
        Hook hook = new Hook(400, GameConstants.WATER_Y, Loadout.forChapter(3));   // the Net Cutter is on the line
        hook.launch();
        new SmallFish(100, 300, 50).onCaught(player, hook);
        Check.assertEquals("a small fish costs 2 with the Net Cutter", 3, player.getHealth());
        new Pufferfish(100, 300, 50).onCaught(player, hook);
        Check.assertEquals("a pufferfish still costs 2, not more", 1, player.getHealth());
    }

    private static void play(OceanObject fish, HookSensor hook, double seconds) {
        for (double t = 0; t < seconds - 1e-9; t += STEP) {
            fish.sense(hook);
            fish.update(STEP, GameConstants.WORLD_WIDTH);
        }
    }

    /** A stand-in hook whose position the test sets directly. */
    private static final class FakeHook implements HookSensor {

        private double tipX;
        private double tipY;
        private boolean lineOut;
        private boolean magnetic;   // true = the Magnet is the tool on the line

        private FakeHook(double tipX, double tipY, boolean lineOut) {
            this.tipX = tipX;
            this.tipY = tipY;
            this.lineOut = lineOut;
        }

        @Override
        public double getTipX() {
            return tipX;
        }

        @Override
        public double getTipY() {
            return tipY;
        }

        @Override
        public boolean isLineOut() {
            return lineOut;
        }

        @Override
        public boolean isMagnetic() {
            return magnetic;
        }
    }
}
