package model;

import java.util.List;
import util.GameConstants;
import util.StoryText;

/**
 * The numbers that define one chapter: quota, time, health, what spawns and
 * which tips appear. The chapters are plain data, so changing the game's
 * difficulty means editing the tables below, not the game logic.
 */
public final class ChapterData {

    public static final int CHAPTER_COUNT = 3;

    private final int number;
    private final String title;
    private final int quota;
    private final double timeLimit;      // seconds
    private final int startHealth;
    private final ObjectKind primaryKind;   // the trash this chapter is about
    private final List<SpawnRule> spawnRules;
    private final List<TutorialPrompt> prompts;

    private ChapterData(int number, String title, int quota, double timeLimit, int startHealth,
                        ObjectKind primaryKind, List<SpawnRule> spawnRules, List<TutorialPrompt> prompts) {
        this.number = number;
        this.title = title;
        this.quota = quota;
        this.timeLimit = timeLimit;
        this.startHealth = startHealth;
        this.primaryKind = primaryKind;
        this.spawnRules = spawnRules;
        this.prompts = prompts;
    }

    /** Returns the data for chapter 1, 2 or 3. */
    public static ChapterData forChapter(int number) {
        switch (number) {
            case 1:
                return plasticShallows();
            case 2:
                return oldPierWreck();
            case 3:
                return ghostNetDeep();
            default:
                throw new IllegalArgumentException("There is no chapter " + number);
        }
    }

    /** Chapter 1: bottles near the surface and a few slow fish. Teaches the basics. */
    private static ChapterData plasticShallows() {
        return new ChapterData(1, StoryText.CH1_TITLE,
                GameConstants.QUOTA_CH1, GameConstants.TIME_CH1_S, GameConstants.HEALTH_CH1,
                ObjectKind.PLASTIC_BOTTLE,
                List.of(
                        SpawnRule.of(ObjectKind.PLASTIC_BOTTLE).count(6, 7).min(4).respawn(1.5, 3)
                                .speed(25, 55).y(170, 420).build(),
                        // One slow fish first, so the "don't hook fish" tip has something to point at.
                        SpawnRule.of(ObjectKind.SMALL_FISH).speed(35, 35).y(200, 200).start(6).build(),
                        SpawnRule.of(ObjectKind.SMALL_FISH).count(3, 3).speed(40, 80).y(170, 380).start(12).build()),
                List.of(
                        TutorialPrompt.at(1, StoryText.TUT_AIM),
                        TutorialPrompt.at(8, StoryText.TUT_FISH),
                        TutorialPrompt.at(16, StoryText.TUT_QUOTA)));
    }

    /** Chapter 2: cans piled up at the wreck on the right, and more fish in the way. */
    private static ChapterData oldPierWreck() {
        return new ChapterData(2, StoryText.CH2_TITLE,
                GameConstants.QUOTA_CH2, GameConstants.TIME_CH2_S, GameConstants.HEALTH_CH2,
                ObjectKind.CAN,
                List.of(
                        SpawnRule.of(ObjectKind.CAN).count(7, 8).min(4).respawn(2, 4)
                                .speed(0, 20).x(440, 760).y(430, 540).build(),
                        SpawnRule.of(ObjectKind.CAN).count(3, 4).respawn(3, 5).speed(35, 70).y(200, 420).build(),
                        SpawnRule.of(ObjectKind.PLASTIC_BOTTLE).count(3, 3).respawn(4, 6)
                                .speed(30, 60).y(160, 380).start(15).build(),
                        SpawnRule.of(ObjectKind.SMALL_FISH).count(2, 2).speed(50, 90).y(160, 380).start(8).build()),
                List.of(
                        TutorialPrompt.aboutGear(1, StoryText.TUT_MAGNET, StoryText.TUT_MAGNET_ALT)));
    }

    /** Chapter 3: big, slow ghost nets deep down. */
    private static ChapterData ghostNetDeep() {
        return new ChapterData(3, StoryText.CH3_TITLE,
                GameConstants.QUOTA_CH3, GameConstants.TIME_CH3_S, GameConstants.HEALTH_CH3,
                ObjectKind.FISHING_NET,
                List.of(
                        SpawnRule.of(ObjectKind.FISHING_NET).count(5, 6).min(3).respawn(5, 8)
                                .speed(10, 30).y(330, 540).build(),
                        SpawnRule.of(ObjectKind.CAN).count(3, 4).respawn(4, 6).speed(30, 60).y(200, 500).start(18).build(),
                        SpawnRule.of(ObjectKind.PLASTIC_BOTTLE).count(3, 3).respawn(4, 6)
                                .speed(30, 60).y(160, 300).start(18).build(),
                        SpawnRule.of(ObjectKind.SMALL_FISH).count(2, 2).speed(50, 90).y(160, 380).build()),
                List.of(
                        TutorialPrompt.aboutGear(1, StoryText.TUT_CUTTER, StoryText.TUT_CUTTER_ALT)));
    }

    public int getNumber() {
        return number;
    }

    public String getTitle() {
        return title;
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
        return title + " (quota " + quota + ", " + timeLimit + " s, " + startHealth + " health)";
    }
}
