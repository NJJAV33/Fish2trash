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
    public static final int CUTTER_FISH_DAMAGE = 2;      // the Net Cutter's blades: every fish it touches costs this much
    public static final double SLIP_IMMUNITY_S = 1.5;   // an object that slipped off can't be touched for this long
    public static final double MAGNET_PULL_RANGE = 70;  // a can this close to the Magnet on its way slides toward it
    public static final double MAGNET_PULL_SPEED = 90;  // pixels per second
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

    // ---- Fish with a mind of their own ----
    public static final double FISH_WHIMS_PER_S = 0.16;       // on average a fish changes its mind every six seconds
    public static final double FISH_TURN_ACCEL = 260;         // pixels per second, per second: turning takes a moment
    public static final double FISH_DEPTH_SPEED = 45;         // pixels per second when it changes depth
    public static final double FISH_DEPTH_ROAM = 45;          // it roams this far above and below its own depth
    public static final double FISH_EDGE_SHY = 70;            // no turning this close to the edge of the screen
    public static final double FISH_FLEE_BOOST = 1.8;         // let go at the boat, it darts off this much faster

    // ---- What each tool does to a fish it touches ----
    public static final double DAZE_S = 3.0;                  // the Magnet knocks a fish out for this long
    public static final double LIFELESS_RISE_SPEED = 55;      // a fish cut by the Net Cutter floats up this fast
    public static final double LIFELESS_FADE_S = 1.5;         // and fades away at the surface in this time

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

    // ---- Stingray: rests on the sand until it feels the Magnet above it, then swims up at it ----
    public static final double RADIUS_STINGRAY = 24;
    public static final double STINGRAY_SENSE_RANGE = 220;    // sideways distance at which it notices the Magnet
    public static final double STINGRAY_LOSE_RANGE = 300;     // it gives up when the Magnet is further away than this
    public static final double STINGRAY_WARNING_S = 0.8;      // sparks fly this long before it moves
    public static final double STINGRAY_CHASE_SPEED = 120;    // pixels per second toward the Magnet; the boat is faster
    public static final double STINGRAY_SINK_SPEED = 80;      // going back down to the sand

    // ---- The sea: a swell that rocks the boat and a current that carries everything along ----
    // How strong they are in each level is in model.LevelData.
    public static final double SWELL_PERIOD_S = 4.6;          // seconds for one roll of the boat
    public static final double SWELL_CHOP = 0.45;             // a smaller, faster wave on top makes the roll uneven
    public static final double CURRENT_PERIOD_S = 46;         // the current swings round and back in this time
    public static final double CURRENT_FLOOR_SHARE = 0.1;     // near the sea floor only this much of it is left
    public static final double FISH_CURRENT_SHARE = 0.4;      // fish swim against it, so it moves them less

    // ---- Coming and going: nothing appears in the middle of the screen ----
    public static final double ENTRY_OUTSIDE = 45;            // arrivals start this far outside the screen
    public static final double EXIT_OUTSIDE = 90;             // and whatever gets this far outside is gone
    public static final double ENTRY_MIN_SPEED = 20;          // slow things come in at least this fast
    public static final double RETURN_MIN_S = 0.5;            // after something leaves, another arrives...
    public static final double RETURN_MAX_S = 2.0;            // ...between these many seconds later

    // ---- Chapters: every chapter has five levels (the tables are in model.LevelData) ----
    // The last level of a chapter asks for the chapter's full quota; the earlier ones build up to it.
    public static final int QUOTA_CH1 = 100;
    public static final int QUOTA_CH2 = 200;
    public static final int QUOTA_CH3 = 350;
    public static final int HEALTH_CH1 = 3;
    public static final int HEALTH_CH2 = 4;
    public static final int HEALTH_CH3 = 5;
    public static final int LOW_TIME_S = 10;       // the clock ticks and turns red from here down
    public static final int URGENT_TIME_S = 5;     // and ticks higher from here down

    // ---- Hints after repeated mistakes ----
    public static final double HINT_COOLDOWN_S = 20;       // quiet time after every hint
    public static final int HINT_MAX_PER_LEVEL = 3;
    public static final int HINT_FISH_STREAK = 3;          // fish hooked in a row
    public static final int HINT_SLIP_STREAK = 2;          // wrong-tool touches in a row
    public static final double HINT_IDLE_S = 12;           // seconds without dropping the line
    public static final double HINT_LOW_TIME_S = 15;       // seconds left, with less than half the quota
    public static final int HINT_LOW_HEALTH = 1;

    // ---- Badges: one per chapter, for clearing its last level well ----
    public static final int SORTER_MAX_SLIPS = 1;               // wrong-tool touches allowed
    public static final double SORTER_MIN_ACCURACY = 0.60;      // share of throws that must bring up trash
    public static final double CHAMPION_TIME_FRACTION = 0.40;   // share of the clock that must be left
    public static final int CHAMPION_MAX_HP_LOST = 1;

    // ---- Run score: what a cleared level is worth, and what a failed attempt costs ----
    public static final int SCORE_POINT_MULT = 10;          // per point of trash
    public static final int TIME_BONUS_PER_S = 20;          // per whole second left on the clock
    public static final int HEALTH_BONUS_PER_HP = 100;      // per health left
    public static final int BADGE_BONUS_1 = 300;
    public static final int BADGE_BONUS_2 = 500;
    public static final int BADGE_BONUS_3 = 1000;
    public static final int RETRY_PENALTY_BASE = 500;       // the 1st failed attempt costs 500, the 2nd 1000 more, ...

    // ---- High scores ----
    public static final int HIGHSCORE_MAX_ENTRIES = 10;
    public static final int HIGHSCORE_NAME_MAX_LEN = 12;

    // ---- Spawning (the tables themselves are in model.LevelData) ----
    public static final double SPAWN_MARGIN_X = 40;        // default: keep spawns off the screen edges
    public static final double MIN_SPAWN_Y = 210;          // default spawn depth range
    public static final double MAX_SPAWN_Y = 540;          // the hook cannot reach much deeper
    public static final double SPAWN_FADE_IN_S = 0.5;      // at the start of a level objects fade in and can't be hooked meanwhile
    public static final double SPAWN_MIN_DIST_TRASH = 100; // nothing appears closer to the hook than this
    public static final double SPAWN_MIN_DIST_FISH = 150;
    public static final int SPAWN_PLACEMENT_TRIES = 8;     // attempts to find a spot away from the hook
    public static final double SUPPLY_MARGIN = 1.3;        // every level holds at least 30% more trash than its quota
}
