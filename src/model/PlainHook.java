package model;

import java.util.EnumMap;
import java.util.Map;
import util.GameConstants;

/**
 * The basic hook. It snags anything it touches - which is why it also hurts
 * fish: a fish it touches is really hooked, and has to be hauled up to the
 * boat and let go there, which costs time as well as health. When gear matters, it only holds on to plastic: cans and nets slip
 * off and need their own tools.
 */
public class PlainHook extends Equipment {

    // One outcome per kind of object, looked up instead of an if/else chain.
    private final Map<ObjectKind, CatchOutcome> outcomes = new EnumMap<>(ObjectKind.class);

    /**
     * @param catchesAllTrash true for a hook that holds every kind of trash
     *                        (used while the other tools are switched off)
     */
    public PlainHook(boolean catchesAllTrash) {
        super(EquipmentType.HOOK, GameConstants.RADIUS_HOOK);
        for (ObjectKind kind : ObjectKind.values()) {
            outcomes.put(kind, CatchOutcome.CATCH);
        }
        if (!catchesAllTrash) {
            outcomes.put(ObjectKind.CAN, CatchOutcome.SLIP);
            outcomes.put(ObjectKind.FISHING_NET, CatchOutcome.SLIP);
        }
    }

    @Override
    protected CatchOutcome outcomeFor(ObjectKind kind) {
        return outcomes.get(kind);
    }

    /** The barb holds: the fish rides the line up to the boat. */
    @Override
    public void strike(Fish fish, Hook hook) {
        fish.snag(hook);
    }
}
