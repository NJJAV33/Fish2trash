package util;

/**
 * Every tuning value of the game in one place, so balancing the game never
 * means hunting through the classes.
 *
 * All values are constants: "static" means one shared copy, "final" means it
 * can never change while the game runs.
 */
public final class GameConstants {

    private GameConstants() {
        // only constants, never created
    }

    // ---- World (pixels) ----
    public static final int WORLD_WIDTH = 800;
    public static final int WORLD_HEIGHT = 600;
    public static final int WATER_Y = 130;           // y position of the sea surface
    public static final int SEABED_Y = 586;          // y position of the sea floor

    // ---- Boat ----
    public static final int BOAT_WIDTH = 120;
    public static final int BOAT_HEIGHT = 28;
    public static final double BOAT_SPEED = 200;     // pixels per second
    public static final double BOAT_MIN_X = 60;      // how far left the boat may go
    public static final double BOAT_MAX_X = 740;     // how far right

    // ---- Hook (pixels and pixels per second) ----
    public static final double HOOK_REST = 40;       // rope length while swinging
    public static final double HOOK_MAX = 440;       // furthest the hook can reach
    public static final double HOOK_EXTEND = 380;    // speed going out
    public static final double HOOK_RETRACT = 420;   // speed coming back empty
    public static final double HOOK_CARRY = 260;     // speed coming back loaded
    public static final double SWING_MAX_DEG = 60;   // widest swing angle
    public static final double SWING_SPEED = 1.8;    // how fast it rocks

    // ---- Tools ----
    public static final double RADIUS_HOOK = 12;     // size of each tool for collisions
    public static final double RADIUS_MAGNET = 16;
    public static final double RADIUS_CUTTER = 22;
    public static final double TOOL_SWAP_COOLDOWN_S = 1.0;   // wait between two tool changes
    public static final double SLIP_IMMUNITY_S = 1.5;   // an object that slipped off can't be touched for this long
    // false = one hook catches every kind of trash; true = each kind needs its own tool
    public static final boolean EQUIPMENT_ENABLED = true;

    // ---- Trash: points and size ----
    public static final int POINTS_BOTTLE = 10;
    public static final int POINTS_CAN = 20;
    public static final int POINTS_NET = 50;
    public static final double RADIUS_BOTTLE = 14;
    public static final double RADIUS_CAN = 14;
    public static final double RADIUS_NET = 32;

    // ---- Fish ----
    public static final int DAMAGE_FISH = 1;
    public static final int DAMAGE_DANGEROUS = 2;        // pufferfish and barracuda
    public static final double RADIUS_SMALL_FISH = 14;
    public static final double RADIUS_BIG_FISH = 24;
    public static final double RADIUS_PUFFER = 22;
    public static final double RADIUS_BARRACUDA = 22;
    public static final double FISH_RELEASE_IMMUNITY_S = 1.5;   // a hooked fish can't be hooked again for this long

    // ---- Pufferfish: weaves up and down, swells when the hook is near ----
    public static final double PUFFER_WEAVE_AMPLITUDE = 55;   // pixels above and below its depth
    public static final double PUFFER_WEAVE_PERIOD_S = 3.2;   // seconds for one full up-and-down
    public static final double PUFFER_SENSE_RANGE = 120;      // it swells when the hook tip is this close
    public static final double PUFFER_SWELL = 0.5;            // fully swollen = 50% bigger
    public static final double PUFFER_INFLATE_PER_S = 2.5;    // how fast it swells and shrinks

    // ---- Barracuda: patrols, then flashes and charges at the hook ----
    public static final double BARRACUDA_SENSE_RANGE = 150;   // sideways distance at which it notices the line
    public static final double BARRACUDA_TELEGRAPH_S = 0.6;   // warning flash before the charge
    public static final double BARRACUDA_CHARGE_S = 0.8;
    public static final double BARRACUDA_CHARGE_SPEED = 400;  // pixels per second
    public static final double BARRACUDA_COOLDOWN_S = 3;      // rest before it can charge again

    // ---- Chapters: points needed, seconds allowed, starting health ----
    public static final int QUOTA_CH1 = 100;
    public static final int QUOTA_CH2 = 200;
    public static final int QUOTA_CH3 = 350;
    public static final double TIME_CH1_S = 65;
    public static final double TIME_CH2_S = 90;
    public static final double TIME_CH3_S = 105;
    public static final int HEALTH_CH1 = 3;
    public static final int HEALTH_CH2 = 4;
    public static final int HEALTH_CH3 = 5;
    public static final int LOW_TIME_S = 10;       // the clock ticks and turns red from here down
    public static final int URGENT_TIME_S = 5;     // and ticks higher from here down

    // ---- Spawning (the tables themselves are in model.ChapterData) ----
    public static final double SPAWN_MARGIN_X = 40;        // default: keep spawns off the screen edges
    public static final double MIN_SPAWN_Y = 210;          // default spawn depth range
    public static final double MAX_SPAWN_Y = 540;          // the hook cannot reach much deeper
    public static final double SPAWN_FADE_IN_S = 0.5;      // new objects fade in and can't be hooked meanwhile
    public static final double SPAWN_MIN_DIST_TRASH = 100; // nothing appears closer to the hook than this
    public static final double SPAWN_MIN_DIST_FISH = 150;
    public static final int SPAWN_PLACEMENT_TRIES = 8;     // attempts to find a spot away from the hook
    public static final double SUPPLY_MARGIN = 1.3;        // keep 30% more trash in the water than still needed
}
