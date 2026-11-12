package model;

import java.util.EnumMap;
import java.util.Map;
import util.GameConstants;
import util.Mathx;

/**
 * The tools the crew carries in a chapter, and which one is on the line.
 *
 * Changing tools takes a moment: after every swap there is a short wait
 * before the next one.
 */
public class Loadout {

    private final Map<EquipmentType, Equipment> tools = new EnumMap<>(EquipmentType.class);   // only the unlocked ones
    private EquipmentType selected;
    private double cooldown;   // seconds until the next swap is allowed

    private Loadout(int chapter) {
        for (EquipmentType type : EquipmentType.values()) {
            // With the tools switched off, the crew only ever has the hook.
            boolean inPlay = GameConstants.EQUIPMENT_ENABLED || type == EquipmentType.HOOK;
            if (inPlay && type.getUnlockChapter() <= chapter) {
                tools.put(type, type.create());
            }
        }
        // The newest tool starts on the line: it is the one the chapter is about.
        EquipmentType newest = EquipmentType.newestIn(chapter);
        selected = isUnlocked(newest) ? newest : EquipmentType.HOOK;
    }

    /** The gear the crew has in chapter 1, 2 or 3. */
    public static Loadout forChapter(int chapter) {
        return new Loadout(chapter);
    }

    /** The tool on the line right now. */
    public Equipment getSelected() {
        return tools.get(selected);
    }

    public EquipmentType getSelectedType() {
        return selected;
    }

    public boolean isUnlocked(EquipmentType type) {
        return tools.containsKey(type);
    }

    /** True when the wait after the last swap is over. */
    public boolean canSwap() {
        return cooldown <= 0;
    }

    /**
     * Puts another tool on the line.
     *
     * @return true if the tool changed; false if it was already on the line
     *         or the last swap was too recent
     * @throws EquipmentLockedException if the crew does not have that tool yet
     */
    public boolean select(EquipmentType type) throws EquipmentLockedException {
        if (!isUnlocked(type)) {
            throw new EquipmentLockedException(type);
        }
        if (type == selected || !canSwap()) {
            return false;
        }
        selected = type;
        cooldown = GameConstants.TOOL_SWAP_COOLDOWN_S;
        return true;
    }

    /**
     * The tool one step along from the current one, skipping tools the crew
     * does not have. direction: 1 = next, -1 = previous. It wraps around.
     */
    public EquipmentType next(int direction) {
        EquipmentType[] all = EquipmentType.values();
        int index = selected.ordinal();
        do {
            index = Math.floorMod(index + direction, all.length);
        } while (!isUnlocked(all[index]));   // ends at the latest on the selected tool itself
        return all[index];
    }

    /** Lets dt seconds of the wait after a swap pass. */
    public void tick(double dt) {
        cooldown = Math.max(0, cooldown - dt);
    }

    /** How much of the wait is left: 1 right after a swap, 0 when the next swap is allowed. */
    public double getCooldownFraction() {
        return Mathx.clamp(cooldown / GameConstants.TOOL_SWAP_COOLDOWN_S, 0, 1);
    }
}
