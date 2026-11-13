package model;

import java.util.EnumMap;
import java.util.Map;
import util.GameConstants;

/**
 * A pair of blades on the line. It cuts ghost nets free and brings them up.
 * Bottles and cans slip off it, and its blades still hurt any fish they touch.
 */
public class NetCutter extends Equipment {

    // One outcome per kind of object, looked up instead of an if/else chain.
    private final Map<ObjectKind, CatchOutcome> outcomes = new EnumMap<>(ObjectKind.class);

    public NetCutter() {
        super(EquipmentType.NET_CUTTER, GameConstants.RADIUS_CUTTER);
        for (ObjectKind kind : ObjectKind.values()) {
            // Trash it cannot hold slips off; wildlife gets caught on the blades.
            outcomes.put(kind, kind.isTrash() ? CatchOutcome.SLIP : CatchOutcome.CATCH);
        }
        outcomes.put(ObjectKind.FISHING_NET, CatchOutcome.CATCH);
    }

    @Override
    protected CatchOutcome outcomeFor(ObjectKind kind) {
        return outcomes.get(kind);
    }
}
