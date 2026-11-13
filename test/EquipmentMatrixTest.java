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
        //                         bottle  can    net    small  big    puffer barracuda
        checkRow(EquipmentType.HOOK,       CATCH, SLIP,  SLIP,  CATCH, CATCH, CATCH, CATCH);
        checkRow(EquipmentType.MAGNET,     PASS,  CATCH, PASS,  PASS,  PASS,  PASS,  PASS);
        checkRow(EquipmentType.NET_CUTTER, SLIP,  SLIP,  CATCH, CATCH, CATCH, CATCH, CATCH);

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
