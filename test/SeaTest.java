import game.EventType;
import game.GameEvent;
import game.GameManager;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import model.Boat;
import model.Can;
import model.Condition;
import model.EquipmentType;
import model.Hook;
import model.HookSensor;
import model.Level;
import model.LevelData;
import model.Loadout;
import model.ObjectKind;
import model.OceanObject;
import model.PlasticBottle;
import model.Player;
import model.Pufferfish;
import model.Sea;
import model.SmallFish;
import util.GameConstants;

/**
 * Checks what makes the sea alive: the swell that rocks the boat, the
 * current that carries things along, objects coming in from the sides and
 * leaving again, fish that change their mind, and what each tool does to a
 * fish.
 */
public final class SeaTest {

    private static final double STEP = 0.016;
    private static final double TINY = 1e-9;
    private static final int W = GameConstants.WORLD_WIDTH;

    private SeaTest() {
        // only static methods
    }

    public static void run() {
        aCalmSeaDoesNothing();
        theSwellRocksTheBoatBothWays();
        theCurrentTurnsAndIsWeakerDeepDown();
        theCurrentCarriesTrashMoreThanFish();
        theRockingBoatThrowsTheSwingOff();
        theSeaGetsRougherTowardTheEndOfAChapter();
        laterArrivalsComeInFromTheSides();
        whatLeavesTheScreenComesBack();
        aFishTurnsAroundSmoothly();
        aFishChangesDepthNearItsHome();
        fishNearTheEdgeAndTrashDoNotChangeCourse();
        theHookHaulsAFishUpAndTheCrewLetsItGo();
        theMagnetKnocksAFishOut();
        theNetCutterKillsAFish();
        theMagnetPullsCansButNotBottles();
    }

    // ---- the sea itself ----

    private static void aCalmSeaDoesNothing() {
        Check.assertEquals("no tilt", 0.0, Sea.CALM.getTilt(), TINY);
        Check.assertEquals("no current", 0.0, Sea.CALM.getCurrentAt(200), TINY);
        Check.assertEquals("level 1-1 has no swell", 0.0, LevelData.of(1, 1).getSwellDegrees(), TINY);
        Check.assertEquals("and no current", 0.0, LevelData.of(1, 1).getCurrentSpeed(), TINY);
    }

    private static void theSwellRocksTheBoatBothWays() {
        Sea sea = new Sea(8, 0, 0);
        double most = Math.toRadians(8);
        double left = 0;
        double right = 0;
        for (double t = 0; t < 30; t += STEP) {
            sea.update(STEP);
            left = Math.min(left, sea.getTilt());
            right = Math.max(right, sea.getTilt());
        }
        Check.assertTrue("it tips to the right", right > most * 0.7);
        Check.assertTrue("and to the left", left < -most * 0.7);
        Check.assertTrue("never further than the level says", right <= most + TINY && left >= -most - TINY);
    }

    private static void theCurrentTurnsAndIsWeakerDeepDown() {
        Sea sea = new Sea(0, 30, 0);
        boolean flowedRight = false;
        boolean flowedLeft = false;
        boolean alwaysWeakerBelow = true;
        boolean neverTooFast = true;
        for (double t = 0; t < GameConstants.CURRENT_PERIOD_S; t += STEP) {
            sea.update(STEP);
            double top = sea.getCurrentAt(GameConstants.WATER_Y);
            double floor = sea.getCurrentAt(GameConstants.SEABED_Y);
            flowedRight = flowedRight || top > 20;
            flowedLeft = flowedLeft || top < -20;
            neverTooFast = neverTooFast && Math.abs(top) <= 30 + TINY;
            alwaysWeakerBelow = alwaysWeakerBelow && Math.abs(floor) <= Math.abs(top) * 0.11 + TINY
                    && Math.abs(sea.getCurrentAt(350)) <= Math.abs(top) + TINY;
        }
        Check.assertTrue("the current flows to the right for a while", flowedRight);
        Check.assertTrue("then turns and flows to the left", flowedLeft);
        Check.assertTrue("never faster than the level says", neverTooFast);
        Check.assertTrue("near the sea floor little of it is left", alwaysWeakerBelow);
    }

    private static void theCurrentCarriesTrashMoreThanFish() {
        Sea sea = new Sea(0, 30, Math.PI / 2);   // starts at full strength, flowing right
        OceanObject bottle = new PlasticBottle(400, GameConstants.WATER_Y, 0);
        OceanObject fish = new SmallFish(400, GameConstants.WATER_Y, 0);
        OceanObject deepBottle = new PlasticBottle(400, GameConstants.SEABED_Y, 0);
        bottle.update(1, sea);
        fish.update(1, sea);
        deepBottle.update(1, sea);
        Check.assertEquals("a bottle at the surface goes with the whole current", 430.0, bottle.getX(), 0.5);
        Check.assertEquals("its speed says so", 30.0, bottle.getSpeedX(), 0.5);
        Check.assertEquals("a fish swims against it", 400 + 30 * GameConstants.FISH_CURRENT_SHARE, fish.getX(), 0.5);
        Check.assertEquals("a bottle on the sea floor hardly moves",
                400 + 30 * GameConstants.CURRENT_FLOOR_SHARE, deepBottle.getX(), 0.5);
        Check.assertEquals("the current does not turn a fish around", 1, fish.getFacing());
    }

    private static void theRockingBoatThrowsTheSwingOff() {
        Boat level = new Boat(400, GameConstants.WATER_Y, Loadout.forChapter(1));
        Boat tipped = new Boat(400, GameConstants.WATER_Y, Loadout.forChapter(1));
        tipped.rock(Math.toRadians(10));
        level.getHook().update(0.3);
        tipped.getHook().update(0.3);
        Check.assertEquals("the boat remembers its tilt", Math.toRadians(10), tipped.getTilt(), TINY);
        Check.assertEquals("the whole swing leans with the boat",
                level.getHook().getAngle() + Math.toRadians(10), tipped.getHook().getAngle(), TINY);

        tipped.getHook().launch();
        double atLaunch = tipped.getHook().getAngle();
        tipped.rock(Math.toRadians(-10));
        tipped.getHook().update(0.3);
        Check.assertEquals("a line that is out keeps its angle", atLaunch, tipped.getHook().getAngle(), TINY);

        GameManager game = new GameManager(LevelData.of(1, 5), Loadout.forChapter(1), new Random(3));
        double most = 0;
        for (int i = 0; i < 600; i++) {
            game.update(STEP);
            most = Math.max(most, Math.abs(game.getBoat().getTilt()));
        }
        Check.assertTrue("in a rough level the game rocks the boat", most > Math.toRadians(5));
        Check.assertEquals("by what the sea says", game.getSea().getTilt(), game.getBoat().getTilt(), 0.02);
    }

    private static void theSeaGetsRougherTowardTheEndOfAChapter() {
        for (int chapter = 1; chapter <= LevelData.CHAPTER_COUNT; chapter++) {
            for (int place = 2; place <= LevelData.LEVELS_PER_CHAPTER; place++) {
                LevelData before = LevelData.of(chapter, place - 1);
                LevelData level = LevelData.of(chapter, place);
                String name = "level " + chapter + "-" + place;
                Check.assertTrue(name + " has at least the swell of the one before",
                        level.getSwellDegrees() >= before.getSwellDegrees());
                Check.assertTrue(name + " has at least the current of the one before",
                        level.getCurrentSpeed() >= before.getCurrentSpeed());
                Check.assertTrue(name + ": the swing still passes straight down", level.getSwellDegrees() < 20);
            }
        }
    }

    // ---- coming and going ----

    private static void laterArrivalsComeInFromTheSides() {
        for (int number = 1; number <= LevelData.LEVEL_COUNT; number++) {
            LevelData data = LevelData.byNumber(number);
            Level level = new Level(data, new Random(number));
            level.update(0, RESTING_HOOK);
            Set<OceanObject> seen = new HashSet<>(level.getObjects());
            boolean startedInView = true;
            for (OceanObject object : seen) {
                startedInView = startedInView && object.getX() >= 0 && object.getX() <= W;
            }
            boolean allFromOutside = true;
            boolean allHeadingIn = true;
            int arrivals = 0;
            for (double t = 0; t < 45; t += STEP) {
                level.update(STEP, RESTING_HOOK);
                for (OceanObject object : level.getObjects()) {
                    if (seen.add(object)) {   // true the first time we meet it
                        arrivals++;
                        boolean left = object.getX() < 0;
                        allFromOutside = allFromOutside && (left || object.getX() > W);
                        allHeadingIn = allHeadingIn && (left ? object.getSpeedX() > 0 : object.getSpeedX() < 0);
                    }
                }
            }
            String name = "level " + data.getChapter() + "-" + data.getLevel();
            Check.assertTrue(name + ": at the start the water is already stocked", startedInView);
            Check.assertTrue(name + ": something arrives later", arrivals > 0);
            Check.assertTrue(name + ": every later arrival starts outside the screen", allFromOutside);
            Check.assertTrue(name + ": and heads in", allHeadingIn);
        }
    }

    private static void whatLeavesTheScreenComesBack() {
        Level level = new Level(LevelData.of(1, 2), new Random(4));
        level.update(0, RESTING_HOOK);
        Check.assertEquals("four bottles at the start", 4, count(level, ObjectKind.PLASTIC_BOTTLE));
        OceanObject leaver = firstOf(level, ObjectKind.PLASTIC_BOTTLE);
        leaver.moveTo(W + GameConstants.EXIT_OUTSIDE + 1, leaver.getY());   // as if it had drifted out
        level.update(STEP, RESTING_HOOK);
        Check.assertTrue("a bottle far outside the screen is taken out", !level.getObjects().contains(leaver));
        Check.assertEquals("so one is missing", 3, count(level, ObjectKind.PLASTIC_BOTTLE));

        boolean cameBack = false;
        for (double t = 0; t < GameConstants.RETURN_MAX_S + 0.1 && !cameBack; t += STEP) {
            level.update(STEP, RESTING_HOOK);
            cameBack = count(level, ObjectKind.PLASTIC_BOTTLE) == 4;
        }
        Check.assertTrue("another bottle comes in within a few seconds", cameBack);

        // Over a whole level the water keeps what the table says, however much comes and goes.
        boolean neverTooMany = true;
        int fewest = 4;
        for (double t = 0; t < 60; t += STEP) {
            level.update(STEP, RESTING_HOOK);
            int bottles = count(level, ObjectKind.PLASTIC_BOTTLE);
            neverTooMany = neverTooMany && bottles <= 4;
            fewest = Math.min(fewest, bottles);
        }
        Check.assertTrue("never more bottles than the table allows", neverTooMany);
        Check.assertTrue("and never none at all: " + fewest, fewest >= 1);
    }

    // ---- fish with a mind of their own ----

    private static void aFishTurnsAroundSmoothly() {
        OceanObject fish = new SmallFish(400, 300, 60);
        fish.changeCourse(0.2);   // a low roll: turn around
        fish.update(STEP, Sea.CALM);
        Check.assertTrue("it does not flip at once: first it slows", fish.getSpeedX() > 0 && fish.getSpeedX() < 60);
        for (int i = 0; i < 60; i++) {
            fish.update(STEP, Sea.CALM);
        }
        Check.assertEquals("a second later it swims the other way at its usual speed", -60.0, fish.getSpeedX(), TINY);
        Check.assertEquals("and faces that way", -1, fish.getFacing());
        Check.assertEquals("at the same depth", 300.0, fish.getY(), TINY);
    }

    private static void aFishChangesDepthNearItsHome() {
        OceanObject fish = new SmallFish(400, 300, 0);
        fish.changeCourse(1.0);   // the highest roll: as deep as it roams
        fish.update(STEP, Sea.CALM);
        Check.assertTrue("it does not jump there", fish.getY() < 302);
        for (int i = 0; i < 200; i++) {
            fish.update(STEP, Sea.CALM);
        }
        Check.assertEquals("it settles below its home depth", 300 + GameConstants.FISH_DEPTH_ROAM, fish.getY(), TINY);
        fish.changeCourse(0.5);   // the lowest roll of the depth half: as high as it roams
        for (int i = 0; i < 400; i++) {
            fish.update(STEP, Sea.CALM);
        }
        Check.assertEquals("or above it, measured from home", 300 - GameConstants.FISH_DEPTH_ROAM, fish.getY(), TINY);

        OceanObject puffer = new Pufferfish(400, 300, 50);
        puffer.changeCourse(1.0);
        for (int i = 0; i < 200; i++) {
            puffer.update(STEP, Sea.CALM);
        }
        Check.assertTrue("a pufferfish only turns around; its weave is its depth", puffer.getSpeedX() < 0
                && Math.abs(puffer.getY() - 300) <= GameConstants.PUFFER_WEAVE_AMPLITUDE + TINY);
    }

    private static void fishNearTheEdgeAndTrashDoNotChangeCourse() {
        OceanObject arriving = new SmallFish(-GameConstants.ENTRY_OUTSIDE, 300, 60);
        arriving.changeCourse(0.2);
        arriving.update(1, Sea.CALM);
        Check.assertEquals("a fish that is just coming in keeps coming", 60.0, arriving.getSpeedX(), TINY);

        OceanObject bottle = new PlasticBottle(400, 300, 30);
        bottle.changeCourse(0.2);
        bottle.update(1, Sea.CALM);
        Check.assertEquals("trash has no mind to change", 430.0, bottle.getX(), TINY);
    }

    // ---- what each tool does to a fish ----

    private static void theHookHaulsAFishUpAndTheCrewLetsItGo() {
        GameManager game = new GameManager(LevelData.of(1, 2), Loadout.forChapter(1), new Random(5));
        List<GameEvent> events = GameScript.record(game);
        OceanObject fish = GameScript.firstCatchable(game, false);
        Hook hook = game.getBoat().getHook();

        GameScript.clearTheWater(game);
        game.spaceAction();
        boolean rodeTheLine = false;
        for (int i = 0; i < 2000 && hook.isLineOut(); i++) {
            if (hook.isExtending()) {
                fish.moveTo(hook.getTipX(), hook.getTipY() + 10);   // keep it right under the tip
            }
            game.update(STEP);
            rodeTheLine = rodeTheLine || (hook.getCaughtObject() == fish && fish.isCaught()
                    && Math.abs(fish.getY() - hook.getTipY()) < TINY);
        }
        Check.assertTrue("the hooked fish rides the line up", rodeTheLine);
        Check.assertEquals("it costs 1 health", game.getMaxHealth() - 1, game.getHealth());
        Check.assertEquals("launched, hurt, let go", List.of(EventType.HOOK_LAUNCHED, EventType.HEALTH_LOST,
                EventType.FISH_FREED), GameScript.types(events));
        Check.assertTrue("it is back in the sea, not aboard", game.getObjects().contains(fish) && !fish.isCaught());
        Check.assertTrue("near the boat", fish.getY() < GameConstants.WATER_Y + GameConstants.HOOK_REST + 5);
        Check.assertEquals("unhurt", Condition.NORMAL, fish.getCondition());
        Check.assertTrue("and it darts off faster than it usually swims",
                Math.abs(fish.getSpeedX()) > 60);

        double depthAtBoat = fish.getY();
        for (int i = 0; i < 120; i++) {
            fish.update(STEP, Sea.CALM);
        }
        Check.assertTrue("then it swims back down toward its own depth", fish.getY() > depthAtBoat + 30);
    }

    private static void theMagnetKnocksAFishOut() {
        OceanObject fish = new SmallFish(300, 300, 50);
        Player player = new Player(4);
        Hook hook = hookWith(EquipmentType.MAGNET);
        fish.onCaught(player, hook);

        Check.assertEquals("it costs 1 health", 3, player.getHealth());
        Check.assertEquals("the fish is dazed", Condition.DAZED, fish.getCondition());
        Check.assertTrue("the Magnet comes back empty", hook.getCaughtObject() == null && hook.isRetracting());
        Check.assertTrue("a dazed fish cannot be touched", !fish.isCatchable());
        fish.update(GameConstants.DAZE_S - 0.1, Sea.CALM);
        Check.assertEquals("it hangs where it was", 300.0, fish.getX(), TINY);
        Check.assertEquals("still dazed", Condition.DAZED, fish.getCondition());

        fish.update(0.2, Sea.CALM);
        Check.assertEquals("then it comes round", Condition.NORMAL, fish.getCondition());
        for (int i = 0; i < 80; i++) {
            fish.update(STEP, Sea.CALM);
        }
        Check.assertEquals("and swims off the other way", -50.0, fish.getSpeedX(), TINY);
        Check.assertTrue("it can be touched again", fish.isCatchable());
    }

    private static void theNetCutterKillsAFish() {
        OceanObject fish = new SmallFish(300, 300, 50);
        Player player = new Player(5);
        Hook hook = hookWith(EquipmentType.NET_CUTTER);
        fish.onCaught(player, hook);

        Check.assertEquals("it costs 2 health", 3, player.getHealth());
        Check.assertEquals("the fish is lifeless", Condition.LIFELESS, fish.getCondition());
        Check.assertTrue("the cutter comes back empty", hook.getCaughtObject() == null && hook.isRetracting());
        Check.assertTrue("it can never be touched again", !fish.isCatchable());

        fish.update(1, Sea.CALM);
        Check.assertEquals("it floats up", 300 - GameConstants.LIFELESS_RISE_SPEED, fish.getY(), TINY);
        Check.assertEquals("and no longer swims", 300.0, fish.getX(), TINY);
        fish.changeCourse(0.2);
        for (int i = 0; i < 250; i++) {
            fish.update(STEP, Sea.CALM);
        }
        Check.assertEquals("up to the surface", GameConstants.WATER_Y + GameConstants.RADIUS_SMALL_FISH, fish.getY(), TINY);
        Check.assertTrue("where it fades", fish.getOpacity() < 1 || fish.isGone());
        fish.update(GameConstants.LIFELESS_FADE_S, Sea.CALM);
        Check.assertTrue("and is gone, so the level lets another fish come", fish.isGone());
        Check.assertEquals("faded away", 0.0, fish.getOpacity(), TINY);
    }

    private static void theMagnetPullsCansButNotBottles() {
        FakeLine magnet = new FakeLine(400, 300, true, true);
        OceanObject near = new Can(440, 300, 0);
        OceanObject far = new Can(400 + GameConstants.MAGNET_PULL_RANGE + 20, 300, 0);
        OceanObject bottle = new PlasticBottle(440, 300, 0);
        for (OceanObject object : List.of(near, far, bottle)) {
            object.sense(magnet);
            object.update(0.2, Sea.CALM);
        }
        Check.assertEquals("a can close to the Magnet slides toward it",
                440 - GameConstants.MAGNET_PULL_SPEED * 0.2, near.getX(), TINY);
        Check.assertEquals("a can further off does not", 400 + GameConstants.MAGNET_PULL_RANGE + 20, far.getX(), TINY);
        Check.assertEquals("a bottle is not metal", 440.0, bottle.getX(), TINY);

        near.sense(magnet);
        near.update(5, Sea.CALM);
        Check.assertEquals("it stops at the Magnet, not past it", 400.0, near.getX(), TINY);

        OceanObject other = new Can(440, 300, 0);
        other.sense(new FakeLine(400, 300, true, false));
        other.update(0.2, Sea.CALM);
        Check.assertEquals("the Hook pulls nothing", 440.0, other.getX(), TINY);
        other.sense(new FakeLine(400, 300, false, true));
        other.update(0.2, Sea.CALM);
        Check.assertEquals("nor does a Magnet that is still at the boat", 440.0, other.getX(), TINY);

        other.sense(new FakeLine(400, 300, true, false, true));
        other.update(0.2, Sea.CALM);
        Check.assertEquals("nor does a Magnet that is already coming back up", 440.0, other.getX(), TINY);
    }

    // ---- helpers ----

    /** A launched hook with the given tool on the line. */
    private static Hook hookWith(EquipmentType tool) {
        Loadout loadout = Loadout.forChapter(3);
        try {
            loadout.select(tool);
        } catch (model.EquipmentLockedException e) {
            throw new IllegalStateException(e);
        }
        Hook hook = new Hook(400, GameConstants.WATER_Y, loadout);
        hook.launch();
        return hook;
    }

    private static int count(Level level, ObjectKind kind) {
        int count = 0;
        for (OceanObject object : level.getObjects()) {
            if (object.getKind() == kind) {
                count++;
            }
        }
        return count;
    }

    private static OceanObject firstOf(Level level, ObjectKind kind) {
        for (OceanObject object : level.getObjects()) {
            if (object.getKind() == kind) {
                return object;
            }
        }
        throw new IllegalStateException("no " + kind);
    }

    private static final HookSensor RESTING_HOOK = new FakeLine(
            W / 2.0, GameConstants.WATER_Y + GameConstants.HOOK_REST, false, false);

    /** A stand-in for the line: a tip at a fixed place, out or not, extending or not, magnetic or not. */
    private static final class FakeLine implements HookSensor {

        private final double tipX;
        private final double tipY;
        private final boolean lineOut;
        private final boolean extending;
        private final boolean magnetic;

        private FakeLine(double tipX, double tipY, boolean lineOut, boolean magnetic) {
            this(tipX, tipY, lineOut, lineOut, magnetic);
        }

        private FakeLine(double tipX, double tipY, boolean lineOut, boolean extending, boolean magnetic) {
            this.tipX = tipX;
            this.tipY = tipY;
            this.lineOut = lineOut;
            this.extending = extending;
            this.magnetic = magnetic;
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
        public boolean isExtending() {
            return extending;
        }

        @Override
        public boolean isMagnetic() {
            return magnetic;
        }
    }
}
