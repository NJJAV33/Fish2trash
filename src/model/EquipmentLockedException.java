package model;

import util.StoryText;

/**
 * Thrown when the player asks for a tool the crew does not have yet.
 *
 * It is a checked exception: whoever calls Loadout.select() is forced by the
 * compiler to decide what happens in that case.
 */
public class EquipmentLockedException extends Exception {

    private static final long serialVersionUID = 1L;

    private final EquipmentType type;

    public EquipmentLockedException(EquipmentType type) {
        super(String.format(StoryText.TOOL_LOCKED, type.getDisplayName(), type.getUnlockChapter()));
        this.type = type;
    }

    /** The tool that was asked for. */
    public EquipmentType getType() {
        return type;
    }
}
