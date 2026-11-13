import game.Badge;
import game.HighScoreTable;
import game.ScoreEntry;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import util.GameConstants;
import util.HighScoreException;
import util.HighScoreStore;
import util.StoryText;

/**
 * Checks the high-score list: tidy names, the line format of the file, the
 * order and length of the list, and that it survives a restart, a damaged
 * file and a folder that cannot be written.
 */
public final class HighScoreStoreTest {

    private static final LocalDate DAY = LocalDate.of(2026, 11, 13);

    private HighScoreStoreTest() {
        // only static methods
    }

    public static void run() {
        namesAreTidied();
        anEntryBecomesOneLineAndBack();
        damagedLinesAreRefused();
        theBetterRunComesFirst();
        theTableKeepsTheTenBest();
        theListSurvivesARestart();
        damagedLinesInTheFileAreSkipped();
        aFailedSaveKeepsTheListInMemory();
        aFileThatCannotBeReadIsLeftAlone();
    }

    // ---- One entry ----

    private static void namesAreTidied() {
        Check.assertEquals("spaces around a name are dropped", "Matt", ScoreEntry.cleanName("  Matt  "));
        Check.assertEquals("the separator of the score file cannot be part of a name",
                "ab", ScoreEntry.cleanName("a|b"));
        Check.assertEquals("neither can a line break", "ab", ScoreEntry.cleanName("a\nb\r"));
        Check.assertEquals("an empty name becomes the default", StoryText.PLAYER_NAME, ScoreEntry.cleanName("   "));
        Check.assertEquals("so does a name with nothing usable in it", StoryText.PLAYER_NAME, ScoreEntry.cleanName("|!?"));
        Check.assertEquals("a long name is cut", GameConstants.HIGHSCORE_NAME_MAX_LEN,
                ScoreEntry.cleanName("ABCDEFGHIJKLMNOPQRSTUVWXYZ").length());
        Check.assertEquals("spaces, dashes and dots inside a name stay", "Lia S.-T", ScoreEntry.cleanName("Lia S.-T"));
        Check.assertEquals("the entry tidies the name it is given", "ab",
                new ScoreEntry(" a|b ", 10, 1, noBadges(), 0, DAY).getName());
    }

    private static void anEntryBecomesOneLineAndBack() {
        ScoreEntry entry = new ScoreEntry("NICO", 1234, 2, EnumSet.of(Badge.ECO_ROOKIE, Badge.SORTER), 1, DAY);
        Check.assertEquals("the line of the score file", "NICO|1234|2|3|1|2026-11-13", entry.toLine());

        for (ScoreEntry original : Arrays.asList(entry,
                new ScoreEntry("Lia S.", 0, 0, noBadges(), 7, DAY.minusDays(40)),
                new ScoreEntry("X", 98765, 3, EnumSet.allOf(Badge.class), 0, DAY))) {
            try {
                ScoreEntry copy = ScoreEntry.parse(original.toLine());
                Check.assertEquals("name survives", original.getName(), copy.getName());
                Check.assertEquals("score survives", original.getScore(), copy.getScore());
                Check.assertEquals("chapters survive", original.getChaptersCleared(), copy.getChaptersCleared());
                Check.assertEquals("badges survive", original.getBadges(), copy.getBadges());
                Check.assertEquals("retries survive", original.getRetries(), copy.getRetries());
                Check.assertEquals("date survives", original.getDate(), copy.getDate());
            } catch (HighScoreException e) {
                Check.fail("a line the game wrote itself must be readable: " + e.getMessage());
            }
        }
    }

    private static void damagedLinesAreRefused() {
        String[] damaged = {
            "",
            "NICO",
            "NICO|100|1|1|0",                           // a field is missing
            "NICO|100|1|1|0|2026-11-13|extra",
            "NICO|lots|1|1|0|2026-11-13",               // not a number
            "NICO|100|1|1|0|yesterday",                 // not a date
            "NICO|-5|1|1|0|2026-11-13",                 // a negative score
            "NICO|100|1|8|0|2026-11-13",                // more badges than there are
            "NICO|100|1|1|-1|2026-11-13"
        };
        for (String line : damaged) {
            boolean refused = false;
            try {
                ScoreEntry.parse(line);
            } catch (HighScoreException expected) {
                refused = true;
            }
            Check.assertTrue("not a score line: \"" + line + "\"", refused);
        }
    }

    private static void theBetterRunComesFirst() {
        ScoreEntry high = new ScoreEntry("A", 500, 1, noBadges(), 3, DAY);
        ScoreEntry low = new ScoreEntry("B", 400, 3, EnumSet.allOf(Badge.class), 0, DAY.minusDays(9));
        Check.assertTrue("the higher score comes first, whatever else", high.compareTo(low) < 0);
        Check.assertTrue("and the lower one after it", low.compareTo(high) > 0);

        ScoreEntry fewerRetries = new ScoreEntry("C", 500, 1, noBadges(), 1, DAY);
        Check.assertTrue("same score: fewer failed attempts first", fewerRetries.compareTo(high) < 0);

        ScoreEntry older = new ScoreEntry("D", 500, 1, noBadges(), 3, DAY.minusDays(1));
        Check.assertTrue("same score and attempts: the older run first", older.compareTo(high) < 0);
        Check.assertEquals("nothing to tell them apart", 0,
                high.compareTo(new ScoreEntry("E", 500, 2, noBadges(), 3, DAY)));
    }

    // ---- The list ----

    private static void theTableKeepsTheTenBest() {
        HighScoreTable table = TempScores.newTable();
        Check.assertTrue("an empty list takes any real score", table.qualifies(1));
        Check.assertTrue("but never a score of zero", !table.qualifies(0));
        Check.assertTrue("nothing was added yet", table.getLatest() == null);

        // Twelve runs, scores 100 to 1200, added in a jumbled order.
        int[] order = {7, 2, 11, 5, 12, 1, 9, 4, 10, 3, 8, 6};
        for (int n : order) {
            table.add(new ScoreEntry("P" + n, n * 100, 1, noBadges(), 0, DAY));
        }
        List<ScoreEntry> entries = table.getEntries();
        Check.assertEquals("only the ten best stay", GameConstants.HIGHSCORE_MAX_ENTRIES, entries.size());
        Check.assertEquals("the best is on top", 1200, entries.get(0).getScore());
        Check.assertEquals("the tenth is the last", 300, entries.get(entries.size() - 1).getScore());
        for (int i = 1; i < entries.size(); i++) {
            Check.assertTrue("the list goes down", entries.get(i - 1).getScore() >= entries.get(i).getScore());
        }

        Check.assertTrue("a full list refuses a score that only ties the last place", !table.qualifies(300));
        Check.assertTrue("and takes one that beats it", table.qualifies(301));

        ScoreEntry second = new ScoreEntry("NEW", 1150, 2, noBadges(), 0, DAY);
        Check.assertEquals("a new run is told its place", 2, table.add(second));
        Check.assertTrue("and is remembered as the latest", table.getLatest() == second);
        Check.assertEquals("still ten", GameConstants.HIGHSCORE_MAX_ENTRIES, table.getEntries().size());

        ScoreEntry tooLow = new ScoreEntry("LOW", 50, 1, noBadges(), 0, DAY);
        Check.assertEquals("a run that does not make the list gets place 0", 0, table.add(tooLow));
        Check.assertTrue("and the latest stays what it was", table.getLatest() == second);

        boolean refused = false;
        try {
            table.getEntries().clear();
        } catch (UnsupportedOperationException expected) {
            refused = true;
        }
        Check.assertTrue("the list cannot be changed from outside", refused);
    }

    private static void theListSurvivesARestart() {
        Path file = TempScores.newFile();
        HighScoreTable first = new HighScoreTable(new HighScoreStore(file));
        Check.assertTrue("no file yet: an empty list, and that is fine", first.getEntries().isEmpty() && first.isSaved());
        first.add(new ScoreEntry("MIGO", 900, 1, EnumSet.of(Badge.ECO_ROOKIE), 2, DAY));
        first.add(new ScoreEntry("DOLOR", 4200, 3, EnumSet.allOf(Badge.class), 0, DAY));
        first.add(new ScoreEntry("LIA", 2500, 2, EnumSet.of(Badge.ECO_ROOKIE), 1, DAY));
        Check.assertTrue("the list is on disk", first.isSaved() && Files.exists(file));
        Check.assertTrue("the draft file is gone after the save",
                Files.notExists(file.resolveSibling(file.getFileName() + ".tmp")));

        // "Restart": a new table reads the same file.
        HighScoreTable second = new HighScoreTable(new HighScoreStore(file));
        Check.assertEquals("the same list comes back, best first", lines(first), lines(second));
        Check.assertEquals("three entries", 3, second.getEntries().size());
        Check.assertEquals("with the best on top", "DOLOR", second.getEntries().get(0).getName());
        Check.assertTrue("nothing counts as just added after a restart", second.getLatest() == null);
        delete(file);
    }

    private static void damagedLinesInTheFileAreSkipped() {
        Path file = TempScores.newFile();
        write(file, "LIA|2500|2|1|1|2026-11-10", "this is not a score", "",
                "MIGO|900|1|1|2|2026-11-09", "DOLOR|many|3|7|0|2026-11-02", "DOLOR|4200|3|7|0|2026-11-02");

        HighScoreTable table = new HighScoreTable(new HighScoreStore(file));
        Check.assertEquals("the three good lines are kept", 3, table.getEntries().size());
        Check.assertEquals("and sorted", "DOLOR", table.getEntries().get(0).getName());
        Check.assertTrue("a damaged line is not an error for the whole file", table.isSaved());
        delete(file);
    }

    // ---- When the disk says no ----

    private static void aFailedSaveKeepsTheListInMemory() {
        // A folder sits where the draft file has to be written, so every save fails.
        Path file = TempScores.newFile();
        Path blocker = file.resolveSibling(file.getFileName() + ".tmp");
        try {
            Files.createDirectory(blocker);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        blocker.toFile().deleteOnExit();

        HighScoreTable table = new HighScoreTable(new HighScoreStore(file));
        Check.assertTrue("reading is fine: there is simply no file yet", table.isSaved());
        String complaint = errorOutputOf(() -> table.add(new ScoreEntry("NICO", 700, 1, noBadges(), 0, DAY)));

        Check.assertEquals("the run is on the list all the same", 1, table.getEntries().size());
        Check.assertTrue("the list says it is not on disk", !table.isSaved());
        Check.assertTrue("the problem is reported", complaint.contains("Could not save"));
        Check.assertTrue("and no score file was made", Files.notExists(file));
        delete(blocker);
    }

    private static void aFileThatCannotBeReadIsLeftAlone() {
        // A folder sits where the score file should be: it cannot be read as a file.
        Path file = TempScores.newFile();
        try {
            Files.createDirectory(file);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        List<HighScoreTable> made = new ArrayList<>();
        String complaint = errorOutputOf(() -> made.add(new HighScoreTable(new HighScoreStore(file))));
        HighScoreTable table = made.get(0);
        Check.assertTrue("the problem is reported", complaint.contains("Could not read"));
        Check.assertTrue("the game starts with an empty list", table.getEntries().isEmpty());
        Check.assertTrue("which is not on disk", !table.isSaved());

        table.add(new ScoreEntry("NICO", 700, 1, noBadges(), 0, DAY));
        Check.assertEquals("a run can still be added", 1, table.getEntries().size());
        Check.assertTrue("what could not be read is not overwritten", Files.isDirectory(file));
        delete(file);
    }

    // ---- helpers ----

    private static List<Badge> noBadges() {
        return Collections.emptyList();
    }

    private static List<String> lines(HighScoreTable table) {
        List<String> lines = new ArrayList<>();
        for (ScoreEntry entry : table.getEntries()) {
            lines.add(entry.toLine());
        }
        return lines;
    }

    private static void write(Path file, String... lines) {
        try {
            Files.write(file, Arrays.asList(lines), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static void delete(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** Runs the action and returns what it printed on the error stream, instead of showing it. */
    private static String errorOutputOf(Runnable action) {
        PrintStream real = System.err;
        ByteArrayOutputStream caught = new ByteArrayOutputStream();
        System.setErr(new PrintStream(caught, true));
        try {
            action.run();
        } finally {
            System.setErr(real);
        }
        return caught.toString();
    }
}
