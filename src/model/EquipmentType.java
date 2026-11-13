package model;

import util.GameConstants;
import util.StoryText;

/**
 * The tools the crew can put on the line.
 *
 * Each type knows the chapter in which the crew receives it and how to build
 * its tool (create), so no other class needs a switch over tool types.
 */
public enum EquipmentType {

    HOOK(1, StoryText.TOOL_HOOK) {
        @Override
        public Equipment create() {
            // With the other tools switched off, the hook has to hold every kind of trash.
            return new PlainHook(!GameConstants.EQUIPMENT_ENABLED);
        }
    },
    MAGNET(2, StoryText.TOOL_MAGNET) {
        @Override
        public Equipment create() {
            return new Magnet();
        }
    },
    NET_CUTTER(3, StoryText.TOOL_CUTTER) {
        @Override
        public Equipment create() {
            return new NetCutter();
        }
    };

    private final int unlockChapter;
    private final String displayName;

    EquipmentType(int unlockChapter, String displayName) {
        this.unlockChapter = unlockChapter;
        this.displayName = displayName;
    }

    /** Builds a new tool of this type. */
    public abstract Equipment create();

    /** The chapter from which the crew has this tool. */
    public int getUnlockChapter() {
        return unlockChapter;
    }

    public String getDisplayName() {
        return displayName;
    }

    /** The newest tool the crew has in the given chapter. */
    public static EquipmentType newestIn(int chapter) {
        EquipmentType newest = HOOK;
        for (EquipmentType type : values()) {
            if (type.unlockChapter <= chapter) {
                newest = type;   // the types are listed in the order they are handed out
            }
        }
        return newest;
    }
}
