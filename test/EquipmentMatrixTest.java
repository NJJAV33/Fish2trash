import java.util.EnumMap;
import java.util.Map;
import model.CatchOutcome;
import model.Equipment;
import model.EquipmentType;
import model.ObjectKind;
import util.GameConstants;

/**
 * Checks the whole tool table: what each of the three tools does to each of
 * the seven kinds of object. Every cell is written out below, so a change to
 * a tool that was not meant shows up here.
 */
public final class EquipmentMatrixTest {

    private static final CatchOutcome CATCH = CatchOutcome.CATCH;
    private static final CatchOutcome SLIP = CatchOutcome.SLIP;
    private static final CatchOutcome PASS = CatchOutcome.PASS;

    private EquipmentMatrixTest() {
        // only static methods
    }

    public static void run() {
        // Every tool touches every fish (CATCH on a fish means: it is hit, and health is lost).
        //                         bottle  can    net    small  big    puffer barracuda ray
        checkRow(EquipmentType.HOOK,       CATCH, SLIP,  SLIP,  CATCH, CATCH, CATCH, CATCH, CATCH);
        checkRow(EquipmentType.MAGNET,     PASS,  CATCH, PASS,  CATCH, CATCH, CATCH, CATCH, CATCH);
        checkRow(EquipmentType.NET_CUTTER, SLIP,  SLIP,  CATCH, CATCH, CATCH, CATCH, CATCH, CATCH);

        noToolIsSafeForFish();
        onlyTheMagnetIsMagnetic();

        everyKindOfTrashHasExactlyOneTool();
        toolsHaveTheirOwnSizeAndName();
    }

    /** expected: one outcome per ObjectKind, in the order the kinds are declared. */
    private static void checkRow(EquipmentType type, CatchOutcome... expected) {
        ObjectKind[] kinds = ObjectKind.values();
        Check.assertEquals("one expected outcome per kind for " + type, kinds.length, expected.length);
        Equipment tool = type.create();
        for (int i = 0; i < kinds.length; i++) {
            Check.assertEquals(type + " on " + kinds[i], expected[i], tool.attempt(kinds[i].create(100, 300, 20)));
        }
    }

    private static void noToolIsSafeForFish() {
        for (EquipmentType type : EquipmentType.values()) {
            for (ObjectKind kind : ObjectKind.values()) {
                if (!kind.isTrash()) {
                    Check.assertEquals("the " + type + " cannot pass through a " + kind,
                            CATCH, type.create().attempt(kind.create(100, 300, 20)));
                }
            }
        }
        // The Net Cutter's blades: every fish costs at least double. The other tools change nothing.
        Check.assertEquals("hook on a small fish", GameConstants.DAMAGE_FISH,
                EquipmentType.HOOK.create().fishDamage(GameConstants.DAMAGE_FISH));
        Check.assertEquals("magnet on a small fish", GameConstants.DAMAGE_FISH,
                EquipmentType.MAGNET.create().fishDamage(GameConstants.DAMAGE_FISH));
        Check.assertEquals("net cutter on a small fish", GameConstants.CUTTER_FISH_DAMAGE,
                EquipmentType.NET_CUTTER.create().fishDamage(GameConstants.DAMAGE_FISH));
        Check.assertEquals("net cutter on a dangerous fish costs no more than it already did",
                GameConstants.DAMAGE_DANGEROUS,
                EquipmentType.NET_CUTTER.create().fishDamage(GameConstants.DAMAGE_DANGEROUS));
    }

    private static void onlyTheMagnetIsMagnetic() {
        for (EquipmentType type : EquipmentType.values()) {
            Check.assertEquals(type + " magnetic", type == EquipmentType.MAGNET, type.create().isMagnetic());
        }
    }

    private static void everyKindOfTrashHasExactlyOneTool() {
        for (ObjectKind kind : ObjectKind.values()) {
            if (!kind.isTrash()) {
                continue;
            }
            int catchers = 0;
            for (EquipmentType type : EquipmentType.values()) {
                if (type.create().attempt(kind.create(100, 300, 20)) == CATCH) {
                    catchers++;
                }
            }
            Check.assertEquals("tools that can lift " + kind, 1, catchers);
        }
    }

    private static void toolsHaveTheirOwnSizeAndName() {
        Map<EquipmentType, Double> sizes = new EnumMap<>(EquipmentType.class);
        sizes.put(EquipmentType.HOOK, GameConstants.RADIUS_HOOK);
        sizes.put(EquipmentType.MAGNET, GameConstants.RADIUS_MAGNET);
        sizes.put(EquipmentType.NET_CUTTER, GameConstants.RADIUS_CUTTER);
        for (EquipmentType type : EquipmentType.values()) {
            Equipment tool = type.create();
            Check.assertEquals(type + " size", sizes.get(type).doubleValue(), tool.getTipRadius(), 1e-9);
            Check.assertEquals(type + " knows its type", type, tool.getType());
            Check.assertEquals(type + " name", type.getDisplayName(), tool.toString());
        }
    }
}
