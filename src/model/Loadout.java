package model;

import java.util.ArrayList;
import java.util.List;
import util.GameConstants;

/**
 * The set of tools the crew carries, and which one is on the line right now.
 */
public class Loadout {

    private final List<Equipment> tools = new ArrayList<>();
    private int selected;

    public Loadout(Equipment startingTool) {
        tools.add(startingTool);
        selected = 0;
    }

    /** The gear a new game starts with. */
    public static Loadout standard() {
        return new Loadout(new PlainHook(!GameConstants.EQUIPMENT_ENABLED));
    }

    public Equipment getSelected() {
        return tools.get(selected);
    }
}
