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
    public static final String MENU_HOW_TO = "How to Play";
    public static final String MENU_SCORES = "High Scores";
    public static final String MENU_QUIT = "Quit";
    public static final String MENU_HINT = "UP / DOWN: choose     ENTER: select";

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
            + "\n"
            + "Each kind of trash needs its own tool:\n"
            + "Hook - bottle 10.   Magnet - can 20.   Net Cutter - net 50.\n"
            + "Hooking a fish costs health. Some fish cost double.\n"
            + "Fill the quota before the time runs out.\n"
            + "Run out of time or health and you are fired.";
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
            "Santos hands Nico a heavy red horseshoe.\n"
            + "The Magnet. It is on the line already.\n"
            + "Cans stick to it. Fish and plastic do not.\n"
            + "Press 1 for the Hook, 2 for the Magnet.\n"
            + "Tools change only while the hook swings.";
    public static final String GEAR_CUTTER =
            "Dolor unlocks the last box on deck.\n"
            + "The Net Cutter. It is on the line already.\n"
            + "It cuts ghost nets free. Cans and bottles slip off.\n"
            + "Careful: its blades still hurt fish.\n"
            + "Press 1 Hook, 2 Magnet, 3 Net Cutter.";

    // ---- Opening ----
    public static final String CRAWL =
            "The sea around Bantay Reef is choking on our trash.\n"
            + "Bottles, cans and nets drift where fish once played.\n"
            + "Captain Dolor's crew is the reef's last hope.\n"
            + "Nico Abella just joined. The first shift starts at dawn.\n"
            + "Fill the quota. Spare the fish. Bring the reef back.";

    // ---- Chapter 1 ----
    public static final String CH1_TITLE = "CHAPTER 1: PLASTIC SHALLOWS";
    public static final String CH1_INTRO_1 =
            "Dawn at Bantay Reef. Nico Abella's first shift.\n"
            + "Bottles bob in the shallows. Plastic never goes\n"
            + "away: it breaks into bits that fish eat.\n"
            + "Dolor: \"Hook what floats, rookie. Leave the fish.\"";
    public static final String CH1_INTRO_2 =
            "QUOTA: 100 points of plastic.\n"
            + "Fill it before time or health runs out.\n"
            + "Every fish you hook costs 1 health.\n"
            + "Migo: \"Easy, boss. I'll cheer from the deck!\"\n"
            + "Nico: \"Okay, okay. I got this.\"";
    public static final String CH1_OUTRO =
            "Quota met! The shallows are clear again.\n"
            + "Sunlight reaches the seagrass for the first time\n"
            + "in years. Small fish dart back in.\n"
            + "Nico: \"Lola's reef used to look like this...\"\n"
            + "Dolor: \"Good shift. Now, the wreck.\"";

    // ---- Chapter 2 ----
    public static final String CH2_TITLE = "CHAPTER 2: OLD PIER WRECK";
    public static final String CH2_INTRO_1 =
            "The Old Pier wreck. Cans and scrap pile up\n"
            + "between the corals and cut the reef.\n"
            + "Santos: \"Metal sinks. The Magnet is your answer.\"\n"
            + "Nico: \"Right gear, right trash. Got it.\"";
    public static final String CH2_INTRO_2 =
            "QUOTA: 200 points. Big fish patrol here.\n"
            + "They are fast. Time your drops carefully.\n"
            + "Migo: \"Magnets ignore fish, boss. Use that!\"";
    public static final String CH2_OUTRO =
            "Quota met! The wreck can breathe again.\n"
            + "Pink and orange coral peek out where cans sat.\n"
            + "Bright fish circle the pier posts.\n"
            + "Santos: \"Nico, look at the colors!\"\n"
            + "Dolor: \"Ghost nets next. The worst of it.\"";

    // ---- Chapter 3 ----
    public static final String CH3_TITLE = "CHAPTER 3: GHOST NET DEEP";
    public static final String CH3_INTRO_1 =
            "Past the drop-off the water turns dark and cold.\n"
            + "Ghost nets drift here, trapping fish and coral\n"
            + "long after the fishers are gone.\n"
            + "Santos: \"Cut them free. Carefully.\"";
    public static final String CH3_INTRO_2 =
            "QUOTA: 350 points. Spiky pufferfish and fast\n"
            + "barracuda lurk here. Each costs 2 health.\n"
            + "Dolor: \"Last shift, Nico. Make it count.\"";
    public static final String CH3_OUTRO =
            "Quota met! The last ghost net falls away.\n"
            + "Warm light fills the deep. Coral glows,\n"
            + "fish school freely, and a turtle glides past.\n"
            + "Nico: \"Lola, you should see this.\"\n"
            + "The reef: \"still here. salamat.\"";

    // ---- Tips shown while playing ----
    public static final String TUT_AIM = "Time the swing, then press SPACE to drop the hook.";
    public static final String TUT_FISH = "Fish are not trash! Each one you hook costs 1 health.";
    public static final String TUT_QUOTA = "Reach 100 points before time or health runs out.";
    public static final String TUT_MAGNET = "The Magnet is on: cans stick to it. 1 = Hook for bottles.";
    public static final String TUT_MAGNET_ALT = "Cans are worth 20 points. Hook them!";
    public static final String TUT_BIGFISH = "Big fish are fast. Wait for a gap before you drop.";
    public static final String TUT_CUTTER = "The Net Cutter is on: it frees nets. 1 Hook, 2 Magnet.";
    public static final String TUT_CUTTER_ALT = "Nets are worth 50 points. Hook them!";
    public static final String TUT_PUFFER = "Pufferfish swell up near the hook. Steer clear!";
    public static final String TUT_BARRACUDA = "Barracuda flash, then charge. Dodge or recall!";

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
    public static final String CLEAR_TITLE = "CHAPTER CLEARED";
    public static final String CLEAR_POINTS = "Points";
    public static final String CLEAR_TIME = "Time left";
    public static final String CLEAR_HEALTH = "Health left";
    public static final String CLEAR_FISH = "Fish hooked";
    public static final String CLEAR_SCORE = "Chapter score";
    public static final String CONTINUE_PROMPT = "ENTER: continue";

    // ---- Badges: a name, the line shown when earned, and how to earn it ----
    public static final String BADGE_1_NAME = "Eco Rookie";
    public static final String BADGE_1_EARNED = "Cleared the shallows. Welcome aboard!";
    public static final String BADGE_1_LOCKED = "Clear Chapter 1.";
    public static final String BADGE_2_NAME = "Sorter";
    public static final String BADGE_2_EARNED = "Right gear for every piece of trash.";
    public static final String BADGE_2_LOCKED = "Clear Chapter 2 with few mistakes.";
    public static final String BADGE_3_NAME = "Champion";
    public static final String BADGE_3_EARNED = "Freed the deep from its ghost nets.";
    public static final String BADGE_3_LOCKED = "Clear fast and keep your health.";
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
    public static final String FAIL_PROMPT = "ENTER: retry chapter     ESC: end run";

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
    public static final String RECAP_CHAPTERS = "Chapters cleared";
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
    public static final String SCORES_CHAPTERS = "CLEARED";
    public static final String SCORES_BADGES = "BADGES";
    public static final String SCORES_DATE = "DATE";
    public static final String SCORES_EMPTY = "No scores yet. Finish a run to get on the list.";
    public static final String SCORES_NOT_SAVED = "The scores could not be saved. They last until the game closes.";

    // ---- Pause ----
    public static final String PAUSE_TITLE = "PAUSED";
    public static final String PAUSE_RESUME = "Resume";
    public static final String PAUSE_RESTART = "Restart chapter";
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

    // The story pages of each chapter, looked up by chapter number (see the methods below).
    private static final String[] TITLES = {CH1_TITLE, CH2_TITLE, CH3_TITLE};
    private static final String[][] INTRO_PAGES = {
        {CH1_INTRO_1, CH1_INTRO_2},
        {CH2_INTRO_1, CH2_INTRO_2},
        {CH3_INTRO_1, CH3_INTRO_2}
    };
    private static final String[] OUTROS = {CH1_OUTRO, CH2_OUTRO, CH3_OUTRO};
    private static final String[] GEAR_PAGES = {"", GEAR_MAGNET, GEAR_CUTTER};   // chapter 1 has no new tool
    private static final String[] FAIL_TIME = {CH1_FAIL_TIME, CH2_FAIL_TIME, CH3_FAIL_TIME};
    private static final String[] FAIL_HEALTH = {CH1_FAIL_HEALTH, CH2_FAIL_HEALTH, CH3_FAIL_HEALTH};

    /** The title of chapter 1, 2 or 3. */
    public static String chapterTitle(int chapter) {
        return TITLES[chapter - 1];
    }

    /** The pages shown before chapter 1, 2 or 3 starts. */
    public static String[] introPages(int chapter) {
        return INTRO_PAGES[chapter - 1].clone();
    }

    /** The page shown after chapter 1, 2 or 3 is cleared. */
    public static String outro(int chapter) {
        return OUTROS[chapter - 1];
    }

    /** The page about the tool the crew receives in chapter 1, 2 or 3. Empty if there is none. */
    public static String gearPage(int chapter) {
        return GEAR_PAGES[chapter - 1];
    }

    /** What Captain Dolor says when a chapter is lost. */
    public static String failText(int chapter, boolean outOfTime) {
        return outOfTime ? FAIL_TIME[chapter - 1] : FAIL_HEALTH[chapter - 1];
    }
}
