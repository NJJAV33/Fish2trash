import game.EventType;
import game.GameEvent;
import game.GameManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import model.Can;
import model.CatchOutcome;
import model.CatchResolver;
import model.ChapterData;
import model.Equipment;
import model.EquipmentType;
import model.Loadout;
import model.ObjectKind;
import model.OceanObject;
import model.PlainHook;
import model.PlasticBottle;

/** Checks what the hook does to each kind of object, and how a slip plays out. */
public final class EquipmentTest {

    private EquipmentTest() {
        // only static methods
    }

    public static void run() {
        universalHookCatchesEverything();
        strictHookOnlyHoldsPlastic();
        anObjectThatGotAwayIsPassedThrough();
        eachOutcomeCallsItsOwnResolverMethod();
        aSlipCostsNothingButTheThrow();
    }

    private static void universalHookCatchesEverything() {
        Equipment hook = new PlainHook(true);
        for (ObjectKind kind : ObjectKind.values()) {
            Check.assertEquals("universal hook on " + kind,
                    CatchOutcome.CATCH, hook.attempt(kind.create(100, 300, 20)));
        }
    }

    private static void strictHookOnlyHoldsPlastic() {
        Equipment hook = new PlainHook(false);
        Check.assertEquals("hook on bottle", CatchOutcome.CATCH, hook.attempt(ObjectKind.PLASTIC_BOTTLE.create(0, 0, 0)));
        Check.assertEquals("hook on can", CatchOutcome.SLIP, hook.attempt(ObjectKind.CAN.create(0, 0, 0)));
        Check.assertEquals("hook on net", CatchOutcome.SLIP, hook.attempt(ObjectKind.FISHING_NET.create(0, 0, 0)));
        Check.assertEquals("hook on fish", CatchOutcome.CATCH, hook.attempt(ObjectKind.SMALL_FISH.create(0, 0, 0)));
    }

    private static void anObjectThatGotAwayIsPassedThrough() {
        Equipment hook = new PlainHook(true);
        OceanObject bottle = new PlasticBottle(100, 300, 20);
        bottle.grantImmunity(1);
        Check.assertEquals("immune object", CatchOutcome.PASS, hook.attempt(bottle));
        bottle.update(1.1, 800);
        Check.assertEquals("immunity runs out", CatchOutcome.CATCH, hook.attempt(bottle));
        bottle.markCaught();
        Check.assertEquals("already caught object", CatchOutcome.PASS, hook.attempt(bottle));
    }

    private static void eachOutcomeCallsItsOwnResolverMethod() {
        List<String> calls = new ArrayList<>();
        CatchResolver recorder = new CatchResolver() {
            @Override
            public void onCatch(OceanObject object) {
                calls.add("catch");
            }

            @Override
            public void onSlip(OceanObject object) {
                calls.add("slip");
            }

            @Override
            public void onPass(OceanObject object) {
                calls.add("pass");
            }
        };
        OceanObject can = new Can(100, 300, 20);
        CatchOutcome.CATCH.resolve(recorder, can);
        CatchOutcome.SLIP.resolve(recorder, can);
        CatchOutcome.PASS.resolve(recorder, can);
        Check.assertEquals("resolver calls", List.of("catch", "slip", "pass"), calls);
    }

    private static void aSlipCostsNothingButTheThrow() {
        GameManager game = new GameManager(ChapterData.forChapter(2), Loadout.forChapter(2), new Random(7));
        game.selectTool(EquipmentType.HOOK);   // the wrong tool for a can
        List<GameEvent> events = GameScript.record(game);
        OceanObject slippery = firstCanOrNet(game);

        GameScript.hookOnto(game, slippery);

        Check.assertEquals("events for a slip", List.of(EventType.HOOK_LAUNCHED, EventType.SLIPPED), GameScript.types(events));
        Check.assertEquals("a slip gives no points", 0, game.getPlayer().getScore());
        Check.assertEquals("a slip costs no health", game.getPlayer().getMaxHealth(), game.getPlayer().getHealth());
        Check.assertTrue("the object stays in the water", game.getObjects().contains(slippery));
        Check.assertTrue("the hook came back empty", game.getBoat().getHook().getCaughtObject() == null);
    }

    private static OceanObject firstCanOrNet(GameManager game) {
        for (OceanObject object : game.getObjects()) {
            if (object.getKind() == ObjectKind.CAN || object.getKind() == ObjectKind.FISHING_NET) {
                return object;
            }
        }
        throw new IllegalStateException("chapter 2 should start with cans");
    }
}
