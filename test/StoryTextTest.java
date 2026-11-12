import model.ChapterData;
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

    private StoryTextTest() {
        // only static methods
    }

    public static void run() {
        checkPage("opening", StoryText.CRAWL, STORY_LINES, STORY_LINE_LENGTH);

        for (int chapter = 1; chapter <= ChapterData.CHAPTER_COUNT; chapter++) {
            String name = "chapter " + chapter;
            Check.assertTrue(name + " title fits", StoryText.chapterTitle(chapter).length() <= TITLE_LENGTH);

            String[] intro = StoryText.introPages(chapter);
            Check.assertTrue(name + " has intro pages", intro.length >= 1);
            for (int i = 0; i < intro.length; i++) {
                checkPage(name + " intro page " + (i + 1), intro[i], STORY_LINES, STORY_LINE_LENGTH);
            }
            checkPage(name + " outro", StoryText.outro(chapter), STORY_LINES, STORY_LINE_LENGTH);
            checkPage(name + " fail (time)", StoryText.failText(chapter, true), FAIL_LINES, FAIL_LINE_LENGTH);
            checkPage(name + " fail (health)", StoryText.failText(chapter, false), FAIL_LINES, FAIL_LINE_LENGTH);
            Check.assertTrue(name + " has different texts for the two ways to fail",
                    !StoryText.failText(chapter, true).equals(StoryText.failText(chapter, false)));
        }

        checkPage("ending page 1", StoryText.END_P1, STORY_LINES, STORY_LINE_LENGTH);
        checkPage("ending page 2", StoryText.END_P2, STORY_LINES, STORY_LINE_LENGTH);
        checkPage("ending page 3", StoryText.END_P3, STORY_LINES, STORY_LINE_LENGTH);
        checkPage("retry line", StoryText.FAIL_RETRY, 1, FAIL_LINE_LENGTH);

        String[] tips = {
            StoryText.TUT_AIM, StoryText.TUT_FISH, StoryText.TUT_QUOTA,
            StoryText.TUT_MAGNET, StoryText.TUT_MAGNET_ALT, StoryText.TUT_BIGFISH,
            StoryText.TUT_CUTTER, StoryText.TUT_CUTTER_ALT, StoryText.TUT_PUFFER, StoryText.TUT_BARRACUDA
        };
        for (String tip : tips) {
            checkPage("tip \"" + tip + "\"", tip, 1, TIP_LENGTH);
        }

        // The quota in the chapter 1 tip must be the real quota.
        Check.assertTrue("the quota tip names the real chapter 1 quota",
                StoryText.TUT_QUOTA.contains(String.valueOf(ChapterData.forChapter(1).getQuota())));
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
