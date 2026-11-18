package util;

/**
 * Every piece of text the player reads, in one place, so the story can be
 * edited (or translated) without touching the game code.
 *
 * "\n" starts a new line on screen. Story pages are at most 5 lines.
 */
public final class StoryText {

    private StoryText() {
        // only constants, never created
    }

    // ---- Title and menu ----
    public static final String GAME_TITLE = "FISH2TRASH";
    public static final String TAGLINE = "Hook the trash. Spare the fish. Save the reef.";
    public static final String CREDITS = "Made by team LangoyLaban for CIS 2103, University of San Carlos.";
    public static final String MENU_START = "Start";
    public static final String MENU_LEVELS = "Select Level";
    public static final String LEVELS_TITLE = "SELECT LEVEL";
    public static final String LEVELS_HINT = "ARROWS: choose     ENTER: play from here     ESC: back";
    public static final String LEVELS_LOCKED = "Locked. Clear the level before it to open this one.";
    public static final String LEVELS_DETAILS = "Quota %d points     %d s     %d health";
    public static final String LEVELS_NEW = "NEW";
    public static final String MENU_HOW_TO = "How to Play";
    public static final String MENU_SCORES = "High Scores";
    public static final String MENU_QUIT = "Quit";
    public static final String MENU_HINT = "UP / DOWN: choose     ENTER: select     F11: full screen";

    // ---- How to play ----
    public static final String HOW_TO_TITLE = "HOW TO PLAY";
    public static final String HOW_TO_TEXT =
            "A / D or arrow keys   Move the boat (while the hook swings)\n"
            + "SPACE                 Drop the hook\n"
            + "SPACE again           Pull the hook back early\n"
            + "1 2 3  or  Q / E      Change tool (while the hook swings)\n"
            + "H                     Show the last tip again\n"
            + "P or ESC              Pause\n"
            + "M                     Sound on or off\n"
            + "F11                   Full screen on or off\n"
            + "\n"
            + "Each kind of trash needs its own tool:\n"
            + "Hook - bottle 10.   Magnet - can 20.   Net Cutter - net 50.\n"
            + "A fish costs health. The Hook hauls it up, the Magnet stuns it,\n"
            + "the Net Cutter kills it (2 health). Rays chase the Magnet.\n"
            + "Waves rock the boat and shift the swing. The current carries\n"
            + "trash and fish across the screen: the arrows show which way.\n"
            + "Fill the quota before time or health runs out.";
    public static final String BACK_PROMPT = "ENTER: back to menu";

    // ---- Tools ----
    public static final String TOOL_HOOK = "Hook";
    public static final String TOOL_MAGNET = "Magnet";
    public static final String TOOL_CUTTER = "Net Cutter";
    /** Filled in with the tool's name and the chapter it arrives in. */
    public static final String TOOL_LOCKED = "The %s arrives in chapter %d.";
    /** The label of a tool slot the crew does not have yet. Filled in with the chapter. */
    public static final String TOOL_LOCKED_SHORT = "Chapter %d";
    public static final String GEAR_TITLE = "NEW GEAR";
    public static final String GEAR_MAGNET =
            "Santos hands Nico a heavy red salvage magnet.\n"
            + "Cans stick to it. Bottles and nets do not.\n"
            + "It still knocks fish, and rays can feel it.\n"
            + "Press 1 for the Hook, 2 for the Magnet.\n"
            + "Tools change only while the line swings.";
    public static final String GEAR_CUTTER =
            "Dolor unlocks the last box on deck: the Net Cutter.\n"
            + "It cuts ghost nets free. Cans and bottles slip off.\n"
            + "Careful: any fish its blades touch costs 2 health.\n"
            + "Press 1 Hook, 2 Magnet, 3 Net Cutter.\n"
            + "Tools change only while the line swings.";

    // ---- Opening ----
    public static final String CRAWL =
            "The sea around Bantay Reef is choking on our trash.\n"
            + "Bottles, cans and nets drift where fish once played.\n"
            + "Captain Dolor's crew is the reef's last hope.\n"
            + "Nico Abella just joined. The first shift starts at dawn.\n"
            + "Fill the quota. Spare the fish. Bring the reef back.";

    // ---- The three chapters: a title and the page that opens each one ----
    private static final String[] CHAPTER_TITLES = {
        "CHAPTER 1: PLASTIC SHALLOWS",
        "CHAPTER 2: OLD PIER WRECK",
        "CHAPTER 3: GHOST NET DEEP"
    };
    private static final String[] CHAPTER_OPENINGS = {
        "Bottles bob in the warm shallows off Bantay Reef.\n"
        + "The Hook lifts bottles. Leave the fish alone.\n"
        + "Dolor: \"Tide waits for no one. Neither do I.\"\n"
        + "Nico: \"Okay, okay. I got this.\"",

        "Past the shallows lie an old pier and a sunken boat.\n"
        + "Rusty cans lie in heaps, too heavy for the Hook.\n"
        + "Dr. Santos unpacks a new tool from her lab.\n"
        + "Santos: \"Cans are metal. Metal is easy.\"",

        "The seabed drops into cold, dark water.\n"
        + "Lost fishing nets drift here, still trapping fish.\n"
        + "Nico: \"Lola fished near here. She never said this.\"\n"
        + "Dolor: \"She would not have. Let's go.\""
    };

    // ---- The fifteen levels. Each is one shift: a name, a page before it and a page after it. ----
    // The first index is the chapter, the second the level inside it.
    private static final String[][] LEVEL_NAMES = {
        {"First Shift", "Faster Tide", "Big Shadows", "Under the School", "Storm Drain"},
        {"Cans at the Wreck", "Two Tools", "The Ray Wakes", "Two Rays", "All at Once"},
        {"Ghost Nets", "Silver Teeth", "Three Tools", "The Long Dark", "The Last Net"}
    };

    private static final String[][] LEVEL_INTROS = {
        {
            "Dawn on M/B Tala. The water is flat and grey.\n"
            + "Migo: \"Ay, boss! Your first drop. Don't be shy.\"\n"
            + "Nico: \"Aim. Breathe. Drop. Easy.\"\n"
            + "Quota: 40 points. Lift bottles with the Hook.\n"
            + "Careful. A few small fish live here too.",

            "Rain fell in the night. The bottles drift faster now.\n"
            + "Dolor: \"More fish today, rookie. Watch your line.\"\n"
            + "Quota: 60 points. Fast bottles, more small fish.\n"
            + "Time your drop. A hit on a fish costs health.",

            "Something large slides under the boat.\n"
            + "Santos: \"Giant trevally! Fast, and very curious.\"\n"
            + "Nico: \"Fast and curious. Like Migo.\"\n"
            + "Quota: 70 points. Big, fast fish arrive. Aim with care.",

            "The bottles have sunk deeper. Fish gather near the top.\n"
            + "Dolor: \"Steer the boat right over the bottle, rookie.\"\n"
            + "Nico: \"Right over it. Got it.\"\n"
            + "Quota: 80 points. Fish school near the surface.\n"
            + "Steer first, then drop.",

            "After the storm, the drain pours plastic into the bay.\n"
            + "Nico: \"So much. Where does it all come from?\"\n"
            + "Dolor: \"The streets, rookie. Rain carries it here.\"\n"
            + "Quota: 100 points. Fast bottles and many fish.\n"
            + "Last shift in the shallows."
        },
        {
            "Cans lie in heaps around the wreck. Use the Magnet.\n"
            + "Migo: \"Cans stick to it like me to free food, boss!\"\n"
            + "Dolor: \"Mind the fish, Nico. The Magnet hits them too.\"\n"
            + "Quota: 100 points. Each can is worth 20.\n"
            + "There are not enough cans. Hook some bottles too.",

            "Bottles and cans float over the same spot today.\n"
            + "Dolor: \"Cans need the Magnet. Bottles need the Hook.\"\n"
            + "Nico: \"Switch tools. Switch tools. Okay, okay.\"\n"
            + "Quota: 120 points. The wrong tool lets trash slip off.\n"
            + "Change tools while the line swings.",

            "A stingray rests on the sand beside the wreck.\n"
            + "Santos: \"Rays feel magnets. The Magnet will wake it.\"\n"
            + "Nico: \"So the Magnet is a doorbell. Great.\"\n"
            + "Quota: 150 points. A ray hit costs 2 health.\n"
            + "With the Hook on the line, the ray stays calm.",

            "Two rays now, and big fish patrol the wreck.\n"
            + "Loose cans drift on the current.\n"
            + "Dolor: \"Hook when a ray is close. Magnet when clear.\"\n"
            + "Nico: \"Hook near rays. Magnet when clear. Okay.\"\n"
            + "Quota: 170 points. Stay calm and switch fast.",

            "The last heap lies under the wreck's old mast.\n"
            + "Dolor: \"Cans, bottles, rays, fish. All of it today.\"\n"
            + "Nico: \"Okay, okay. I got this. I think.\"\n"
            + "Quota: 200 points. Think before every drop."
        },
        {
            "Pale nets hang in the dark like old curtains.\n"
            + "Dolor: \"Ghost nets. They keep catching fish for years.\"\n"
            + "Quota: 200 points. Use the Net Cutter on nets.\n"
            + "Watch for the pufferfish. It swells near the line.\n"
            + "A pufferfish costs 2 health.",

            "Silver flashes in the dark. Barracuda.\n"
            + "Santos: \"It flashes, then charges. Barracuda are fast.\"\n"
            + "Dolor: \"When it flashes, pull the line back.\"\n"
            + "Quota: 240 points. A charge costs 2 health.",

            "Nets, cans and bottles. All in one cold shift.\n"
            + "Dolor: \"Hook: bottles. Magnet: cans. Cutter: nets.\"\n"
            + "Nico: \"One, two, three. One, two, three.\"\n"
            + "Quota: 280 points. The Net Cutter costs 2 per fish.\n"
            + "Cut only when the lane is clear.",

            "Everyone is tired. The deck lamps flicker.\n"
            + "Dolor: \"Two barracuda. A ray too. No mistakes.\"\n"
            + "Migo: \"Boss, my hands hurt. But we keep going.\"\n"
            + "Quota: 320 points. The deep is dark and crowded.",

            "The last ghost nets are huge. They cover the deep reef.\n"
            + "Dolor: \"This is it, Nico. Tide waits for no one.\"\n"
            + "Nico: \"Okay, okay. I got this.\"\n"
            + "Quota: 350 points. All three tools. Everything."
        }
    };

    private static final String[][] LEVEL_OUTROS = {
        {
            "Forty points. Nico lets out a long breath.\n"
            + "Migo: \"Salamat, boss! You did it!\"\n"
            + "Dolor: \"Not bad, rookie. Same time tomorrow.\"",

            "A cloud of tiny fish flickers over the sand.\n"
            + "Nico: \"They came back. Did you see that?\"\n"
            + "Dolor: \"I saw. Eat something. Tomorrow is bigger.\"",

            "The trevally circle once, then drift away unhurt.\n"
            + "Santos: \"Fish remember boats. They trust you more now.\"\n"
            + "Migo: \"Boss, tomorrow the bottles hide deeper.\"",

            "Seagrass sways where the bottles were piled.\n"
            + "Migo: \"Look, boss! Green! Real green!\"\n"
            + "Dark clouds gather in the east. A storm is coming.",

            "Quota met! Sunlight reaches the seagrass again.\n"
            + "Small fish dart back in. Something hums below.\n"
            + "The reef: \"still here.\"\n"
            + "Nico: \"Lola's reef used to look like this...\"\n"
            + "Dolor: \"Good shift. Now, the wreck.\""
        },
        {
            "The first rusty heap is gone. Clean sand shows.\n"
            + "Santos: \"Fan worms! They only settle in clean water.\"\n"
            + "Migo: \"Tomorrow cans and bottles mix. Two tools, boss.\"",

            "Nico switches faster now. Migo claps.\n"
            + "Migo: \"Ay! Boss is a juggler now!\"\n"
            + "A wide, flat shadow slides under the wreck.\n"
            + "Dolor: \"Something lives there. Go slow tomorrow.\"",

            "The ray glides off and settles in a far corner.\n"
            + "Santos: \"It was only curious. Nobody got hurt.\"\n"
            + "Migo: \"Ay, boss. Tomorrow there are two.\"",

            "The big fish circle back, calm and unhurt.\n"
            + "Santos: \"The rays are settling. This place feels safe.\"\n"
            + "Dolor: \"Everything at once tomorrow. Sleep, Nico.\"",

            "Quota met! The wreck is clean at last.\n"
            + "Coral peeks out where the cans once sat.\n"
            + "Santos: \"Nico, look at the colors!\"\n"
            + "The reef: \"still here.\"\n"
            + "Dolor: \"Ghost nets next. The worst of it.\""
        },
        {
            "The cut net sinks away. A trapped fish swims free.\n"
            + "Nico: \"Did you see that? It just took off!\"\n"
            + "Santos: \"Pufferfish swell to look big. It is a bluff.\"\n"
            + "Migo: \"Boss, tomorrow something with teeth.\"",

            "The barracuda lose interest and drift off.\n"
            + "Nico's hands shake. Nico grins anyway.\n"
            + "Nico: \"I did not enjoy that. But I did it.\"\n"
            + "Dolor: \"Tomorrow: nets, cans, bottles. All three tools.\"",

            "A sea fan opens on the rock where nets once hung.\n"
            + "Santos: \"Sea fans grow very slowly. This one is old.\"\n"
            + "Migo: \"The reef is waking up, boss!\"\n"
            + "Dolor: \"Tomorrow is harder. Rest.\"",

            "New nets drift in from the open sea tonight.\n"
            + "Nico: \"It keeps coming back. What if it never ends?\"\n"
            + "Dolor: \"Then we keep going. One net at a time.\"\n"
            + "The biggest nets wait in the deepest hole.",

            "The last ghost net falls away into the dark.\n"
            + "Warm light fills the deep. A turtle glides past.\n"
            + "Nico: \"Lola, you should see this.\"\n"
            + "Dolor: \"Good shift, Nico. The best one yet.\"\n"
            + "The reef: \"still here. salamat.\""
        }
    };
    /** Filled in with the chapter, the level inside it, and the level's name. */
    public static final String LEVEL_TITLE = "LEVEL %d-%d: %s";
    /** The short form for the bar at the top of the playing screen. */
    public static final String LEVEL_LABEL = "%d-%d  %s";

    // ---- Tips shown while playing ----
    public static final String TUT_AIM = "Time the swing, then press SPACE to drop the hook.";
    public static final String TUT_FISH = "Fish are not trash! Each one you hook costs 1 health.";
    public static final String TUT_QUOTA = "Fill the quota before time or health runs out.";
    public static final String TUT_RECALL = "SPACE again pulls the line back early.";
    public static final String TUT_CURRENT = "The arrows show the current. Trash drifts that way.";
    public static final String TUT_SWELL = "Waves rock the boat and shift the swing. Watch the hook.";
    public static final String TUT_BIGFISH = "Big fish are fast. Wait for a gap before you drop.";
    public static final String TUT_DEEP = "Deep bottles: steer the boat right over them first.";
    public static final String TUT_FAST = "Fast bottles: drop a little ahead of them.";
    public static final String TUT_MAGNET = "The Magnet is on: cans stick to it. 1 = Hook for bottles.";
    public static final String TUT_MAGNET_ALT = "Cans are worth 20 points. Hook them!";
    public static final String TUT_MAGNET_FISH = "The Magnet stuns fish and costs health. Check the lane.";
    public static final String TUT_SWITCH = "Cans and bottles mixed? Switch tools before you drop.";
    public static final String TUT_RAY = "Rays feel the Magnet. Use the Hook when one is close.";
    public static final String TUT_RAY_ALT = "A ray rests on the sand. Hooking it costs 2 health.";
    public static final String TUT_RAY_SPARKS = "Sparks on a ray? Switch to the Hook, or steer away.";
    public static final String TUT_CUTTER = "The Net Cutter is on: it frees nets. 1 Hook, 2 Magnet.";
    public static final String TUT_CUTTER_ALT = "Nets are worth 50 points. Hook them!";
    public static final String TUT_CUTTER_FISH = "Net Cutter blades kill fish: 2 health. Cut in clear lanes.";
    public static final String TUT_PUFFER = "Pufferfish swell up near the hook. Steer clear!";
    public static final String TUT_BARRACUDA = "Barracuda flash, then charge. Dodge or recall!";
    public static final String TUT_THREE = "Hook: bottles. Magnet: cans. Net Cutter: nets.";

    // ---- Hints after repeated mistakes ----
    public static final String HINT_FISH_STREAK = "Those are fish! Lift only trash. Watch the shapes.";
    public static final String HINT_SLIP = "Wrong tool! Hook = bottles, Magnet = cans, Cutter = nets.";
    public static final String HINT_GEAR_PLASTIC = "Plastic bottles: use the Hook (1).";
    public static final String HINT_GEAR_CAN = "Metal cans: use the Magnet (2).";
    public static final String HINT_GEAR_NET = "Fishing nets: use the Net Cutter (3).";
    public static final String HINT_IDLE = "Still there? Drop the line. Trash won't wait.";
    public static final String HINT_LOW_TIME = "Time is low. Go for the big pieces!";
    public static final String HINT_LOW_HEALTH = "Last health! Skip anything that swims.";

    // ---- Chapter cleared ----
    public static final String CLEAR_TITLE = "LEVEL CLEARED";
    public static final String CLEAR_POINTS = "Points";
    public static final String CLEAR_TIME = "Time left";
    public static final String CLEAR_HEALTH = "Health left";
    public static final String CLEAR_FISH = "Fish hooked";
    public static final String CLEAR_SCORE = "Level score";
    public static final String CONTINUE_PROMPT = "ENTER: continue";
    /** Filled in with the level inside the chapter and the number of levels a chapter has. */
    public static final String CLEAR_PROGRESS = "Level %d of %d in this chapter";
    public static final String CLEAR_BADGE_AHEAD = "The chapter badge is decided in its last level.";

    // ---- Badges: a name, the line shown when earned, and how to earn it ----
    public static final String BADGE_1_NAME = "Eco Rookie";
    public static final String BADGE_1_EARNED = "Cleared the shallows. Welcome aboard!";
    public static final String BADGE_1_LOCKED = "Clear level 1-5.";
    public static final String BADGE_2_NAME = "Sorter";
    public static final String BADGE_2_EARNED = "Right gear for every piece of trash.";
    public static final String BADGE_2_LOCKED = "Clear level 2-5 with few mistakes.";
    public static final String BADGE_3_NAME = "Champion";
    public static final String BADGE_3_EARNED = "Freed the deep from its ghost nets.";
    public static final String BADGE_3_LOCKED = "Clear level 3-5 fast, with health to spare.";
    public static final String BADGE_EARNED_LABEL = "BADGE EARNED";
    public static final String BADGE_MISSED_LABEL = "BADGE MISSED";

    // ---- Fired ----
    public static final String FAIL_TITLE = "YOU'RE FIRED!";
    public static final String FAIL_SUB = "...for now.";
    public static final String FAIL_STAMP = "FIRED";
    public static final String CH1_FAIL_TIME =
            "The tide went out before the quota was met.\n"
            + "Dolor: \"You're off the crew, Nico. But the\n"
            + "shallows are still waiting.\"";
    public static final String CH1_FAIL_HEALTH =
            "Too many fish hooked. Nico is worn out.\n"
            + "Dolor: \"You're fired. Rest, then try again.\n"
            + "The reef is patient.\"";
    public static final String CH2_FAIL_TIME =
            "The sun set on the wreck, quota unfinished.\n"
            + "Dolor: \"You're fired, Nico. The cans will\n"
            + "still be here. So will I.\"";
    public static final String CH2_FAIL_HEALTH =
            "The big fish wore Nico down.\n"
            + "Dolor: \"You're fired. Learn their rhythm,\n"
            + "then come back stronger.\"";
    public static final String CH3_FAIL_TIME =
            "The deep took your time, and the nets won.\n"
            + "Dolor: \"You're fired, Nico. But the ghost nets\n"
            + "haven't won. Not yet.\"";
    public static final String CH3_FAIL_HEALTH =
            "The dark water wore Nico out.\n"
            + "Dolor: \"You're fired. Rest your hands.\n"
            + "The ocean will wait for you.\"";
    public static final String FAIL_RETRY = "Santos: \"One more shift, Nico? The reef waits.\"";
    public static final String FAIL_PROMPT = "ENTER: retry level     ESC: end run";

    // ---- Ending ----
    public static final String END_TITLE = "THE REEF RETURNS";
    public static final String END_P1 =
            "Dawn again at Bantay Reef. Nico leans on the rail.\n"
            + "The water is clear, almost glass. Below the boat\n"
            + "coral glows, fish school in the light, and a sea\n"
            + "turtle glides past without a bag to chase.";
    public static final String END_P2 =
            "Santos counts new coral and laughs out loud.\n"
            + "Migo: \"We did this? We actually did this!\"\n"
            + "Dolor: \"We did. But one crew is not enough.\n"
            + "Bin your trash. Tell your friends.\"";
    public static final String END_P3 =
            "Nico thinks of Lola, who fished here long ago.\n"
            + "\"One bottle at a time,\" Nico says.\n"
            + "The reef: \"salamat, nico. still here.\"\n"
            + "The crew heads home. Tomorrow, another shift.";
    public static final String RECAP_TITLE = "SHIFT REPORT";
    public static final String RECAP_TRASH = "Trash collected";
    public static final String RECAP_FISH = "Fish hooked";
    public static final String RECAP_LEVELS = "Levels cleared";
    public static final String RECAP_SCORE = "Run score";
    public static final String RECAP_BADGES = "Badges";

    // ---- High scores ----
    public static final String PLAYER_NAME = "NICO";   // used when no name is typed
    public static final String NAME_TITLE = "NEW HIGH SCORE";
    public static final String NAME_LABEL = "Your name";
    public static final String NAME_PROMPT = "Type a name, up to %d letters.     ENTER: save";
    public static final String SCORES_TITLE = "HIGH SCORES";
    public static final String SCORES_RANK = "#";
    public static final String SCORES_NAME = "NAME";
    public static final String SCORES_SCORE = "SCORE";
    public static final String SCORES_LEVELS = "LEVELS";
    public static final String SCORES_BADGES = "BADGES";
    public static final String SCORES_DATE = "DATE";
    public static final String SCORES_EMPTY = "No scores yet. Finish a run to get on the list.";
    public static final String SCORES_NOT_SAVED = "The scores could not be saved. They last until the game closes.";

    // ---- Pause ----
    public static final String PAUSE_TITLE = "PAUSED";
    public static final String PAUSE_RESUME = "Resume";
    public static final String PAUSE_RESTART = "Restart level";
    public static final String PAUSE_QUIT = "Quit to menu";
    public static final String PAUSE_SOUND_ON = "Sound: on";
    public static final String PAUSE_SOUND_OFF = "Sound: off";
    public static final String PAUSE_SOUND_NONE = "Sound: not available";

    // ---- Small prompts ----
    public static final String NEXT_PROMPT = "ENTER";
    public static final String SKIP_PROMPT = "ESC: skip";
    public static final String PLAY_HINT = "A / D: move    SPACE: drop or recall    1 2 3: tool    H: last tip    P: pause    M: sound";
    public static final String HUD_OBJECTIVE = "Hook trash, spare the fish";
    public static final String HUD_SCORE = "SCORE";
    public static final String HUD_CURRENT = "CURRENT";
    public static final String HUD_CALM = "NO CURRENT";

    // What Captain Dolor says when a level is lost, looked up by chapter (see failText below).
    private static final String[] GEAR_PAGES = {"", GEAR_MAGNET, GEAR_CUTTER};   // chapter 1 has no new tool
    private static final String[] FAIL_TIME = {CH1_FAIL_TIME, CH2_FAIL_TIME, CH3_FAIL_TIME};
    private static final String[] FAIL_HEALTH = {CH1_FAIL_HEALTH, CH2_FAIL_HEALTH, CH3_FAIL_HEALTH};

    /** The title of chapter 1, 2 or 3. */
    public static String chapterTitle(int chapter) {
        return CHAPTER_TITLES[chapter - 1];
    }

    /** The page that opens chapter 1, 2 or 3, shown before its first level. */
    public static String chapterOpening(int chapter) {
        return CHAPTER_OPENINGS[chapter - 1];
    }

    /** The name of a level, for example "Big Shadows" for chapter 1, level 3. */
    public static String levelName(int chapter, int level) {
        return LEVEL_NAMES[chapter - 1][level - 1];
    }

    /** The full title of a level, for example "LEVEL 1-3: BIG SHADOWS". */
    public static String levelTitle(int chapter, int level) {
        return String.format(LEVEL_TITLE, chapter, level, levelName(chapter, level).toUpperCase());
    }

    /** The short title of a level for the playing screen, for example "1-3  Big Shadows". */
    public static String levelLabel(int chapter, int level) {
        return String.format(LEVEL_LABEL, chapter, level, levelName(chapter, level));
    }

    /** The page shown before a level starts. */
    public static String levelIntro(int chapter, int level) {
        return LEVEL_INTROS[chapter - 1][level - 1];
    }

    /** The page shown after a level is cleared. */
    public static String levelOutro(int chapter, int level) {
        return LEVEL_OUTROS[chapter - 1][level - 1];
    }

    /** The page about the tool the crew receives in chapter 1, 2 or 3. Empty if there is none. */
    public static String gearPage(int chapter) {
        return GEAR_PAGES[chapter - 1];
    }

    /** What Captain Dolor says when a level of the given chapter is lost. */
    public static String failText(int chapter, boolean outOfTime) {
        return outOfTime ? FAIL_TIME[chapter - 1] : FAIL_HEALTH[chapter - 1];
    }
}
