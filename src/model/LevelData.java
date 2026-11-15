package model;

import java.util.List;
import util.GameConstants;
import util.StoryText;

/**
 * The numbers that define one level: quota, time, health, what is in the
 * water and which tips appear. The levels are plain data, so changing the
 * game's difficulty means editing the tables below, not the game logic.
 *
 * The story has three chapters of five levels each. Inside a chapter every
 * level is harder than the one before, and the last one asks for the
 * chapter's full quota.
 *
 * Trash is limited: every rule says how many pieces the level holds
 * (supply). From chapter 2 on, no single kind of trash is enough to fill the
 * quota, so the player has to change tools.
 */
public final class LevelData {

    public static final int CHAPTER_COUNT = 3;
    public static final int LEVELS_PER_CHAPTER = 5;
    public static final int LEVEL_COUNT = CHAPTER_COUNT * LEVELS_PER_CHAPTER;

    private final int chapter;           // 1 to 3
    private final int level;             // 1 to 5 inside the chapter
    private final int quota;
    private final double timeLimit;      // seconds
    private final int startHealth;
    private final ObjectKind primaryKind;   // the trash this chapter is about
    private final List<SpawnRule> spawnRules;
    private final List<TutorialPrompt> prompts;

    private LevelData(int chapter, int level, int quota, double timeLimit, int startHealth,
                      ObjectKind primaryKind, List<SpawnRule> spawnRules, List<TutorialPrompt> prompts) {
        this.chapter = chapter;
        this.level = level;
        this.quota = quota;
        this.timeLimit = timeLimit;
        this.startHealth = startHealth;
        this.primaryKind = primaryKind;
        this.spawnRules = spawnRules;
        this.prompts = prompts;
    }

    /** Returns the data for one level, for example of(2, 3) for chapter 2, level 3. */
    public static LevelData of(int chapter, int level) {
        if (level < 1 || level > LEVELS_PER_CHAPTER) {
            throw new IllegalArgumentException("There is no level " + chapter + "-" + level);
        }
        switch (chapter) {
            case 1:
                return plasticShallows(level);
            case 2:
                return oldPierWreck(level);
            case 3:
                return ghostNetDeep(level);
            default:
                throw new IllegalArgumentException("There is no chapter " + chapter);
        }
    }

    /** Returns a level by its place in the whole story: 1 is level 1-1, 15 is level 3-5. */
    public static LevelData byNumber(int number) {
        if (number < 1 || number > LEVEL_COUNT) {
            throw new IllegalArgumentException("There is no level number " + number);
        }
        return of((number - 1) / LEVELS_PER_CHAPTER + 1, (number - 1) % LEVELS_PER_CHAPTER + 1);
    }

    // ---------------------------------------------------------------------
    // Chapter 1: bottles and the Hook. Fish get more, faster and bigger.
    // ---------------------------------------------------------------------

    private static LevelData plasticShallows(int level) {
        ObjectKind bottle = ObjectKind.PLASTIC_BOTTLE;
        int health = GameConstants.HEALTH_CH1;
        switch (level) {
            case 1:   // Slow bottles and two slow fish: learn to aim.
                return new LevelData(1, 1, 40, 50, health, bottle,
                        List.of(
                                SpawnRule.of(bottle).count(4, 4).supply(7).min(3).respawn(2.5, 4)
                                        .speed(15, 30).y(200, 380).build(),
                                // One slow fish first, so the "don't hook fish" tip has something to point at.
                                SpawnRule.of(ObjectKind.SMALL_FISH).speed(30, 30).y(190, 190).start(6).build(),
                                SpawnRule.of(ObjectKind.SMALL_FISH).speed(35, 35).y(260, 260).start(12).build()),
                        List.of(
                                TutorialPrompt.at(1, StoryText.TUT_AIM),
                                TutorialPrompt.at(8, StoryText.TUT_FISH),
                                TutorialPrompt.at(16, StoryText.TUT_QUOTA)));
            case 2:   // Faster bottles, fish in pairs.
                return new LevelData(1, 2, 60, 60, health, bottle,
                        List.of(
                                SpawnRule.of(bottle).count(4, 4).supply(10).min(3).respawn(2, 3.5)
                                        .speed(30, 60).y(190, 400).build(),
                                SpawnRule.of(ObjectKind.SMALL_FISH).count(2, 2).speed(40, 70).y(175, 260).start(4).build(),
                                SpawnRule.of(ObjectKind.SMALL_FISH).count(2, 2).speed(50, 80).y(200, 310).start(12).build()),
                        List.of(TutorialPrompt.at(2, StoryText.TUT_RECALL)));
            case 3:   // The first big, fast fish.
                return new LevelData(1, 3, 70, 70, health, bottle,
                        List.of(
                                SpawnRule.of(bottle).count(4, 4).supply(11).min(3).respawn(2, 3.5)
                                        .speed(35, 70).y(190, 420).build(),
                                SpawnRule.of(ObjectKind.SMALL_FISH).count(3, 3).speed(45, 80).y(175, 290).start(4).build(),
                                SpawnRule.of(ObjectKind.BIG_FISH).speed(80, 110).y(200, 330).start(14).build()),
                        List.of(TutorialPrompt.at(15, StoryText.TUT_BIGFISH)));
            case 4:   // Bottles lie deep, under a school of fish near the surface.
                return new LevelData(1, 4, 80, 80, health, bottle,
                        List.of(
                                SpawnRule.of(bottle).count(4, 4).supply(12).min(3).respawn(2.5, 4)
                                        .speed(25, 50).y(300, 520).build(),
                                SpawnRule.of(ObjectKind.SMALL_FISH).count(3, 3).speed(40, 80).x(60, 380).y(165, 260)
                                        .start(3).build(),
                                SpawnRule.of(ObjectKind.SMALL_FISH).speed(45, 85).x(420, 740).y(175, 285)
                                        .start(8).build(),
                                SpawnRule.of(ObjectKind.BIG_FISH).speed(90, 120).y(185, 290).start(20).build()),
                        List.of(TutorialPrompt.at(1, StoryText.TUT_DEEP)));
            default:  // After the storm: fast bottles and seven fish.
                return new LevelData(1, 5, GameConstants.QUOTA_CH1, 95, health, bottle,
                        List.of(
                                SpawnRule.of(bottle).count(5, 5).supply(15).min(3).respawn(1.5, 3)
                                        .speed(50, 95).y(180, 430).build(),
                                SpawnRule.of(ObjectKind.SMALL_FISH).count(3, 3).speed(60, 100).y(170, 290).start(2).build(),
                                SpawnRule.of(ObjectKind.SMALL_FISH).count(2, 2).speed(70, 110).y(200, 300).start(14).build(),
                                SpawnRule.of(ObjectKind.BIG_FISH).speed(100, 150).y(180, 320).start(8).build(),
                                SpawnRule.of(ObjectKind.BIG_FISH).speed(120, 160).y(200, 340).start(30).build()),
                        List.of(TutorialPrompt.at(1, StoryText.TUT_FAST)));
        }
    }

    // ---------------------------------------------------------------------
    // Chapter 2: cans and the Magnet. There are never enough cans, so the
    // Hook is still needed for bottles. Stingrays arrive in level 3.
    // ---------------------------------------------------------------------

    private static LevelData oldPierWreck(int level) {
        ObjectKind can = ObjectKind.CAN;
        ObjectKind bottle = ObjectKind.PLASTIC_BOTTLE;
        int health = GameConstants.HEALTH_CH2;
        switch (level) {
            case 1:   // Four cans at the wreck are worth 80 of the 100 points.
                return new LevelData(2, 1, 100, 65, health, can,
                        List.of(
                                SpawnRule.of(can).count(4, 4).speed(0, 15).x(440, 760).y(430, 540).build(),
                                SpawnRule.of(bottle).count(3, 3).supply(6).min(2).respawn(4, 7)
                                        .speed(30, 55).y(170, 320).build(),
                                SpawnRule.of(ObjectKind.SMALL_FISH).count(3, 3).speed(45, 80).y(170, 300).start(5).build(),
                                SpawnRule.of(ObjectKind.BIG_FISH).count(2, 2).speed(70, 110).y(180, 320).start(15).build()),
                        List.of(
                                TutorialPrompt.aboutGear(1, StoryText.TUT_MAGNET, StoryText.TUT_MAGNET_ALT),
                                TutorialPrompt.aboutGear(9, StoryText.TUT_MAGNET_FISH, StoryText.TUT_BIGFISH)));
            case 2:   // Loose cans and bottles in the same water: constant tool changes.
                return new LevelData(2, 2, 120, 70, health, can,
                        List.of(
                                SpawnRule.of(can).count(4, 4).speed(35, 60).y(230, 400).build(),
                                SpawnRule.of(bottle).count(4, 4).supply(8).min(3).respawn(2.5, 4)
                                        .speed(35, 70).y(170, 300).build(),
                                SpawnRule.of(ObjectKind.SMALL_FISH).count(3, 3).speed(50, 90).y(170, 300).start(3).build(),
                                SpawnRule.of(ObjectKind.SMALL_FISH).count(2, 2).speed(50, 90).y(190, 320).start(18).build(),
                                SpawnRule.of(ObjectKind.BIG_FISH).count(2, 2).speed(90, 140).y(180, 340).start(10).build()),
                        List.of(TutorialPrompt.aboutGear(1, StoryText.TUT_SWITCH, StoryText.TUT_MAGNET_ALT)));
            case 3:   // The first stingray: the Magnet wakes it.
                return new LevelData(2, 3, 150, 85, health, can,
                        List.of(
                                SpawnRule.of(can).count(2, 2).speed(0, 15).x(440, 760).y(430, 540).build(),
                                SpawnRule.of(can).count(4, 4).speed(35, 65).y(220, 380).build(),
                                SpawnRule.of(bottle).count(4, 4).supply(8).min(3).respawn(3, 5)
                                        .speed(35, 70).y(170, 310).build(),
                                SpawnRule.of(ObjectKind.SMALL_FISH).count(3, 3).speed(55, 90).y(170, 300).start(3).build(),
                                SpawnRule.of(ObjectKind.SMALL_FISH).speed(55, 90).y(190, 320).start(20).build(),
                                SpawnRule.of(ObjectKind.BIG_FISH).count(2, 2).speed(90, 150).y(180, 340).start(12).build(),
                                SpawnRule.of(ObjectKind.STINGRAY).speed(30, 40).x(60, 380).y(540, 556).start(10).build()),
                        List.of(TutorialPrompt.aboutGear(11, StoryText.TUT_RAY, StoryText.TUT_RAY_ALT)));
            case 4:   // Two rays, two very fast big fish, cans arriving in two waves.
                return new LevelData(2, 4, 170, 95, health, can,
                        List.of(
                                SpawnRule.of(can).count(2, 2).speed(0, 15).x(440, 760).y(430, 540).build(),
                                SpawnRule.of(can).count(3, 3).speed(35, 70).y(210, 380).build(),
                                SpawnRule.of(can).count(2, 2).speed(35, 70).y(210, 380).start(20).build(),
                                SpawnRule.of(bottle).count(4, 4).supply(9).min(3).respawn(3, 5)
                                        .speed(40, 75).y(170, 300).build(),
                                SpawnRule.of(ObjectKind.SMALL_FISH).count(3, 3).speed(60, 95).y(170, 290).start(3).build(),
                                SpawnRule.of(ObjectKind.BIG_FISH).count(2, 2).speed(110, 180).y(180, 340).start(8).build(),
                                SpawnRule.of(ObjectKind.STINGRAY).speed(30, 45).y(540, 556).start(10).build(),
                                SpawnRule.of(ObjectKind.STINGRAY).speed(35, 50).y(540, 556).start(35).build()),
                        List.of(TutorialPrompt.aboutGear(11, StoryText.TUT_RAY_SPARKS, StoryText.TUT_RAY_ALT)));
            default:  // Everything at once: nine cans in three waves, nine fish.
                return new LevelData(2, 5, GameConstants.QUOTA_CH2, 115, health, can,
                        List.of(
                                SpawnRule.of(can).count(2, 2).speed(0, 15).x(440, 760).y(430, 540).build(),
                                SpawnRule.of(can).count(4, 4).speed(40, 80).y(200, 360).build(),
                                SpawnRule.of(can).count(3, 3).speed(40, 80).y(200, 360).start(25).build(),
                                SpawnRule.of(bottle).count(3, 3).supply(8).min(2).respawn(3, 5)
                                        .speed(45, 80).y(170, 290).build(),
                                SpawnRule.of(ObjectKind.SMALL_FISH).count(3, 3).speed(65, 100).y(170, 290).start(2).build(),
                                SpawnRule.of(ObjectKind.SMALL_FISH).speed(65, 100).y(190, 310).start(20).build(),
                                SpawnRule.of(ObjectKind.BIG_FISH).count(2, 2).speed(120, 180).y(180, 340).start(6).build(),
                                SpawnRule.of(ObjectKind.BIG_FISH).speed(140, 190).y(200, 330).start(45).build(),
                                SpawnRule.of(ObjectKind.STINGRAY).speed(30, 45).y(540, 556).start(8).build(),
                                SpawnRule.of(ObjectKind.STINGRAY).speed(35, 50).y(540, 556).start(40).build()),
                        List.of());
        }
    }

    // ---------------------------------------------------------------------
    // Chapter 3: ghost nets and the Net Cutter, among dangerous fish.
    // Nets alone never fill the quota: cans and bottles are needed too.
    // ---------------------------------------------------------------------

    private static LevelData ghostNetDeep(int level) {
        ObjectKind net = ObjectKind.FISHING_NET;
        ObjectKind can = ObjectKind.CAN;
        ObjectKind bottle = ObjectKind.PLASTIC_BOTTLE;
        int health = GameConstants.HEALTH_CH3;
        switch (level) {
            case 1:   // Three nets and the first pufferfish.
                return new LevelData(3, 1, 200, 75, health, net,
                        List.of(
                                SpawnRule.of(net).count(3, 3).speed(10, 30).y(330, 540).build(),
                                SpawnRule.of(can).count(2, 2).supply(4).min(1).respawn(5, 8)
                                        .speed(30, 60).y(220, 400).build(),
                                SpawnRule.of(bottle).count(2, 2).supply(4).min(1).respawn(5, 8)
                                        .speed(30, 60).y(170, 290).start(10).build(),
                                SpawnRule.of(ObjectKind.SMALL_FISH).count(2, 2).speed(50, 90).y(170, 290).start(3).build(),
                                SpawnRule.of(ObjectKind.SMALL_FISH).count(2, 2).speed(50, 90).y(190, 300).start(20).build(),
                                SpawnRule.of(ObjectKind.PUFFERFISH).speed(40, 60).y(260, 360).start(10).build(),
                                SpawnRule.of(ObjectKind.PUFFERFISH).speed(40, 65).y(300, 420).start(30).build(),
                                SpawnRule.of(ObjectKind.BIG_FISH).speed(90, 130).y(180, 300).start(15).build()),
                        List.of(
                                TutorialPrompt.aboutGear(1, StoryText.TUT_CUTTER, StoryText.TUT_CUTTER_ALT),
                                TutorialPrompt.at(11, StoryText.TUT_PUFFER),
                                TutorialPrompt.aboutGear(22, StoryText.TUT_CUTTER_FISH, StoryText.TUT_BIGFISH)));
            case 2:   // The first barracuda.
                return new LevelData(3, 2, 240, 100, health, net,
                        List.of(
                                SpawnRule.of(net).count(3, 3).speed(10, 30).y(330, 540).build(),
                                SpawnRule.of(can).count(4, 4).supply(7).min(3).respawn(4, 6)
                                        .speed(35, 70).y(210, 450).build(),
                                SpawnRule.of(bottle).count(2, 2).supply(4).min(1).respawn(6, 9)
                                        .speed(30, 60).y(170, 290).start(12).build(),
                                SpawnRule.of(ObjectKind.SMALL_FISH).count(3, 3).speed(50, 90).y(170, 300).start(3).build(),
                                SpawnRule.of(ObjectKind.SMALL_FISH).speed(50, 90).y(190, 320).start(35).build(),
                                SpawnRule.of(ObjectKind.PUFFERFISH).count(2, 2).speed(50, 80).y(250, 420).start(8).build(),
                                SpawnRule.of(ObjectKind.BARRACUDA).speed(100, 130).y(200, 420).start(15).build(),
                                SpawnRule.of(ObjectKind.BIG_FISH).speed(100, 150).y(180, 320).start(25).build()),
                        List.of(TutorialPrompt.at(16, StoryText.TUT_BARRACUDA)));
            case 3:   // Four nets and three cans are worth 260: bottles are needed, so all three tools.
                return new LevelData(3, 3, 280, 110, health, net,
                        List.of(
                                SpawnRule.of(net).count(4, 4).speed(10, 30).y(330, 540).build(),
                                SpawnRule.of(can).count(3, 3).speed(35, 70).y(200, 440).build(),
                                SpawnRule.of(bottle).count(3, 3).supply(11).min(2).respawn(3, 5)
                                        .speed(35, 70).y(170, 300).build(),
                                SpawnRule.of(ObjectKind.SMALL_FISH).count(3, 3).speed(55, 95).y(170, 300).start(3).build(),
                                SpawnRule.of(ObjectKind.SMALL_FISH).speed(55, 95).y(190, 320).start(30).build(),
                                SpawnRule.of(ObjectKind.PUFFERFISH).count(2, 2).speed(50, 85).y(250, 430).start(6).build(),
                                SpawnRule.of(ObjectKind.BARRACUDA).speed(100, 130).y(200, 440).start(12).build(),
                                SpawnRule.of(ObjectKind.BIG_FISH).speed(110, 170).y(180, 320).start(8).build()),
                        List.of(TutorialPrompt.aboutGear(1, StoryText.TUT_THREE, StoryText.TUT_CUTTER_ALT)));
            case 4:   // Two barracuda, a ray in the deep, and a net that arrives late.
                return new LevelData(3, 4, 320, 140, health, net,
                        List.of(
                                SpawnRule.of(net).count(4, 4).speed(10, 30).y(330, 540).build(),
                                SpawnRule.of(net).speed(10, 30).y(330, 540).start(30).build(),
                                SpawnRule.of(can).count(3, 3).speed(35, 70).y(200, 440).build(),
                                SpawnRule.of(bottle).count(2, 2).supply(11).min(1).respawn(4, 7)
                                        .speed(35, 70).y(170, 300).build(),
                                SpawnRule.of(ObjectKind.SMALL_FISH).count(3, 3).speed(60, 95).y(170, 300).start(3).build(),
                                SpawnRule.of(ObjectKind.PUFFERFISH).count(2, 2).speed(55, 90).y(250, 440).start(5).build(),
                                SpawnRule.of(ObjectKind.BARRACUDA).speed(110, 140).y(200, 440).start(10).build(),
                                SpawnRule.of(ObjectKind.BARRACUDA).speed(110, 140).y(200, 440).start(45).build(),
                                SpawnRule.of(ObjectKind.STINGRAY).speed(30, 45).y(540, 556).start(12).build(),
                                SpawnRule.of(ObjectKind.BIG_FISH).speed(120, 180).y(180, 320).start(8).build()),
                        List.of());
            default:  // The last nets: six in two waves, ten fish, the second barracuda arriving late.
                return new LevelData(3, 5, GameConstants.QUOTA_CH3, 150, health, net,
                        List.of(
                                SpawnRule.of(net).count(4, 4).speed(10, 30).y(330, 540).build(),
                                SpawnRule.of(net).count(2, 2).speed(10, 30).y(330, 540).start(45).build(),
                                SpawnRule.of(can).count(3, 3).speed(35, 75).y(200, 440).build(),
                                SpawnRule.of(bottle).count(3, 3).supply(10).min(2).respawn(4, 7)
                                        .speed(35, 75).y(170, 300).build(),
                                SpawnRule.of(ObjectKind.SMALL_FISH).count(3, 3).speed(65, 100).y(170, 300).start(2).build(),
                                SpawnRule.of(ObjectKind.PUFFERFISH).count(2, 2).speed(55, 90).y(250, 440).start(5).build(),
                                SpawnRule.of(ObjectKind.PUFFERFISH).speed(55, 90).y(250, 440).start(30).build(),
                                SpawnRule.of(ObjectKind.BARRACUDA).speed(110, 140).y(200, 440).start(8).build(),
                                SpawnRule.of(ObjectKind.BARRACUDA).speed(120, 150).y(200, 440).start(50).build(),
                                SpawnRule.of(ObjectKind.STINGRAY).speed(30, 45).y(540, 556).start(12).build(),
                                SpawnRule.of(ObjectKind.BIG_FISH).speed(130, 190).y(180, 320).start(10).build()),
                        List.of());
        }
    }

    /** The chapter this level belongs to: 1, 2 or 3. */
    public int getChapter() {
        return chapter;
    }

    /** The level's place inside its chapter: 1 to 5. */
    public int getLevel() {
        return level;
    }

    /** The level's place in the whole story: 1 for level 1-1, up to 15 for level 3-5. */
    public int getNumber() {
        return (chapter - 1) * LEVELS_PER_CHAPTER + level;
    }

    /** True for the last level of a chapter, where the chapter's badge is decided. */
    public boolean isChapterFinale() {
        return level == LEVELS_PER_CHAPTER;
    }

    /** For example "LEVEL 2-3: THE RAY WAKES". */
    public String getTitle() {
        return StoryText.levelTitle(chapter, level);
    }

    public int getQuota() {
        return quota;
    }

    public double getTimeLimit() {
        return timeLimit;
    }

    public int getStartHealth() {
        return startHealth;
    }

    public ObjectKind getPrimaryKind() {
        return primaryKind;
    }

    public List<SpawnRule> getSpawnRules() {
        return spawnRules;
    }

    public List<TutorialPrompt> getPrompts() {
        return prompts;
    }

    @Override
    public String toString() {
        return getTitle() + " (quota " + quota + ", " + timeLimit + " s, " + startHealth + " health)";
    }
}
