import model.Barracuda;
import model.BigFish;
import model.Hook;
import model.HookSensor;
import model.Loadout;
import model.OceanObject;
import model.Player;
import model.Pufferfish;
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
    }

    private static void damageDependsOnTheFish() {
        Check.assertEquals("big fish costs 1 health", 4, healthAfterHooking(new BigFish(100, 300, 50)));
        Check.assertEquals("pufferfish costs 2 health", 3, healthAfterHooking(new Pufferfish(100, 300, 50)));
        Check.assertEquals("barracuda costs 2 health", 3, healthAfterHooking(new Barracuda(100, 300, 50)));
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
        private final boolean lineOut;

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
    }
}
