import game.Badge;
import java.util.HashSet;
import java.util.Set;
import model.LevelData;
import model.EquipmentType;
import util.StoryText;

/**
 * Checks that every piece of story text fits where the screens draw it:
 * few enough lines, and lines short enough for their box.
 * If a writer makes a line too long, this fails before the game shows it cut off.
 */
public final class StoryTextTest {

    private static final int STORY_LINES = 5;        // the story box holds five lines
    private static final int STORY_LINE_LENGTH = 58;
    private static final int FAIL_LINES = 3;         // the pink slip holds three
    private static final int FAIL_LINE_LENGTH = 48;
    private static final int TIP_LENGTH = 60;        // tips are a single line
    private static final int TITLE_LENGTH = 30;
    private static final int LABEL_LENGTH = 24;       // the level name in the bar at the top
    private static final int BADGE_NAME_LENGTH = 16;
    private static final int BADGE_TEXT_LENGTH = 45; // one small line under the medal

    private StoryTextTest() {
        // only static methods
    }

    public static void run() {
        checkPage("opening", StoryText.CRAWL, STORY_LINES, STORY_LINE_LENGTH);

        for (int chapter = 1; chapter <= LevelData.CHAPTER_COUNT; chapter++) {
            String name = "chapter " + chapter;
            Check.assertTrue(name + " title fits", StoryText.chapterTitle(chapter).length() <= TITLE_LENGTH);
            checkPage(name + " opening", StoryText.chapterOpening(chapter), STORY_LINES, STORY_LINE_LENGTH);
            checkPage(name + " fail (time)", StoryText.failText(chapter, true), FAIL_LINES, FAIL_LINE_LENGTH);
            checkPage(name + " fail (health)", StoryText.failText(chapter, false), FAIL_LINES, FAIL_LINE_LENGTH);
            Check.assertTrue(name + " has different texts for the two ways to fail",
                    !StoryText.failText(chapter, true).equals(StoryText.failText(chapter, false)));
        }

        // Every level has its own name and its own two pages, and the page
        // before it tells the player the real quota.
        Set<String> names = new HashSet<>();
        Set<String> pages = new HashSet<>();
        for (int number = 1; number <= LevelData.LEVEL_COUNT; number++) {
            LevelData level = LevelData.byNumber(number);
            int chapter = level.getChapter();
            int inChapter = level.getLevel();
            String name = "level " + chapter + "-" + inChapter;
            Check.assertTrue(name + " title fits: " + StoryText.levelTitle(chapter, inChapter),
                    StoryText.levelTitle(chapter, inChapter).length() <= TITLE_LENGTH);
            Check.assertTrue(name + " label fits the bar at the top",
                    StoryText.levelLabel(chapter, inChapter).length() <= LABEL_LENGTH);
            checkPage(name + " intro", StoryText.levelIntro(chapter, inChapter), STORY_LINES, STORY_LINE_LENGTH);
            checkPage(name + " outro", StoryText.levelOutro(chapter, inChapter), STORY_LINES, STORY_LINE_LENGTH);
            Check.assertTrue(name + " intro names the real quota of " + level.getQuota(),
                    StoryText.levelIntro(chapter, inChapter).contains("Quota: " + level.getQuota() + " points"));
            names.add(StoryText.levelName(chapter, inChapter));
            pages.add(StoryText.levelIntro(chapter, inChapter));
            pages.add(StoryText.levelOutro(chapter, inChapter));
        }
        Check.assertEquals("every level has its own name", LevelData.LEVEL_COUNT, names.size());
        Check.assertEquals("every level has its own two pages", 2 * LevelData.LEVEL_COUNT, pages.size());

        checkPage("ending page 1", StoryText.END_P1, STORY_LINES, STORY_LINE_LENGTH);
        checkPage("ending page 2", StoryText.END_P2, STORY_LINES, STORY_LINE_LENGTH);
        checkPage("ending page 3", StoryText.END_P3, STORY_LINES, STORY_LINE_LENGTH);
        checkPage("retry line", StoryText.FAIL_RETRY, 1, FAIL_LINE_LENGTH);

        String[] tips = {
            StoryText.TUT_AIM, StoryText.TUT_FISH, StoryText.TUT_QUOTA, StoryText.TUT_RECALL,
            StoryText.TUT_CURRENT, StoryText.TUT_SWELL,
            StoryText.TUT_BIGFISH, StoryText.TUT_DEEP, StoryText.TUT_FAST,
            StoryText.TUT_MAGNET, StoryText.TUT_MAGNET_ALT, StoryText.TUT_MAGNET_FISH, StoryText.TUT_SWITCH,
            StoryText.TUT_RAY, StoryText.TUT_RAY_ALT, StoryText.TUT_RAY_SPARKS,
            StoryText.TUT_CUTTER, StoryText.TUT_CUTTER_ALT, StoryText.TUT_CUTTER_FISH,
            StoryText.TUT_PUFFER, StoryText.TUT_BARRACUDA, StoryText.TUT_THREE,
            StoryText.HINT_FISH_STREAK, StoryText.HINT_SLIP, StoryText.HINT_GEAR_PLASTIC, StoryText.HINT_GEAR_CAN,
            StoryText.HINT_GEAR_NET, StoryText.HINT_IDLE, StoryText.HINT_LOW_TIME, StoryText.HINT_LOW_HEALTH
        };
        for (String tip : tips) {
            checkPage("tip \"" + tip + "\"", tip, 1, TIP_LENGTH);
        }

        // Every tool but the first comes with a page that presents it, and a
        // "not yet" message short enough for the tip box.
        for (EquipmentType tool : EquipmentType.values()) {
            String page = StoryText.gearPage(tool.getUnlockChapter());
            if (tool.getUnlockChapter() == 1) {
                Check.assertTrue("chapter 1 has no new-tool page", page.isEmpty());
            } else {
                checkPage("new-tool page for the " + tool.getDisplayName(), page, STORY_LINES, STORY_LINE_LENGTH);
                Check.assertTrue("the page names the " + tool.getDisplayName(), page.contains(tool.getDisplayName()));
            }
            checkPage("locked message for the " + tool.getDisplayName(),
                    String.format(StoryText.TOOL_LOCKED, tool.getDisplayName(), tool.getUnlockChapter()), 1, TIP_LENGTH);
        }

        for (Badge badge : Badge.values()) {
            String name = "badge " + badge;
            Check.assertTrue(name + " has a short name",
                    !badge.getDisplayName().isEmpty() && badge.getDisplayName().length() <= BADGE_NAME_LENGTH);
            checkPage(name + " earned line", badge.getEarnedText(), 1, BADGE_TEXT_LENGTH);
            checkPage(name + " how-to-earn line", badge.getLockedText(), 1, BADGE_TEXT_LENGTH);
        }

        checkPage("progress line", String.format(StoryText.CLEAR_PROGRESS, 4, LevelData.LEVELS_PER_CHAPTER),
                1, BADGE_TEXT_LENGTH);
        checkPage("badge-ahead line", StoryText.CLEAR_BADGE_AHEAD, 1, FAIL_LINE_LENGTH);
    }

    /** A page fits if it has no more lines than allowed and no line is too long or empty. */
    private static void checkPage(String name, String text, int maxLines, int maxLineLength) {
        String[] lines = text.split("\n", -1);
        Check.assertTrue(name + " has at most " + maxLines + " lines (has " + lines.length + ")",
                lines.length <= maxLines);
        for (String line : lines) {
            Check.assertTrue(name + ": line fits in " + maxLineLength + " letters: \"" + line + "\"",
                    !line.isEmpty() && line.length() <= maxLineLength);
        }
    }
}
