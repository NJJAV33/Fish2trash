package game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import util.GameConstants;
import util.HighScoreException;
import util.HighScoreStore;

/**
 * The best runs, highest score first, at most ten of them.
 *
 * The list is loaded from the store when it is created and saved after
 * every new entry. If the file cannot be read or written the game does not
 * stop: the list then only lasts until the game closes, and isSaved() says so.
 */
public final class HighScoreTable {

    private final HighScoreStore store;
    private final List<ScoreEntry> entries = new ArrayList<>();
    private final boolean loaded;      // false: the file could not be read, so it is left alone
    private boolean saved = true;      // false: the last save failed
    private ScoreEntry latest;         // the entry added last in this session, or null

    public HighScoreTable(HighScoreStore store) {
        this.store = store;
        this.loaded = load();
    }

    private boolean load() {
        List<String> lines;
        try {
            lines = store.readLines();
        } catch (HighScoreException e) {
            System.err.println(e.getMessage());
            return false;
        }
        for (String line : lines) {
            try {
                entries.add(ScoreEntry.parse(line));
            } catch (HighScoreException damaged) {
                // One damaged line is skipped; the rest of the list is kept.
            }
        }
        sortAndTrim();
        return true;
    }

    /** True if a run with this score would get onto the list. A score of zero never does. */
    public boolean qualifies(int score) {
        if (score <= 0) {
            return false;
        }
        return entries.size() < GameConstants.HIGHSCORE_MAX_ENTRIES
                || score > entries.get(entries.size() - 1).getScore();
    }

    /**
     * Adds a run to the list and saves the list.
     *
     * @return the place the entry took (1 is the top), or 0 if it did not make the list
     */
    public int add(ScoreEntry entry) {
        entries.add(entry);
        sortAndTrim();
        int rank = entries.indexOf(entry) + 1;
        if (rank > 0) {
            latest = entry;
            save();
        }
        return rank;
    }

    private void sortAndTrim() {
        Collections.sort(entries);   // uses ScoreEntry.compareTo
        while (entries.size() > GameConstants.HIGHSCORE_MAX_ENTRIES) {
            entries.remove(entries.size() - 1);
        }
    }

    private void save() {
        if (!loaded) {
            return;   // never overwrite a file that could not be read
        }
        List<String> lines = new ArrayList<>();
        for (ScoreEntry entry : entries) {
            lines.add(entry.toLine());
        }
        try {
            store.writeLines(lines);
            saved = true;
        } catch (HighScoreException e) {
            System.err.println(e.getMessage());
            saved = false;
        }
    }

    /** The list, best first, as a read-only list. */
    public List<ScoreEntry> getEntries() {
        return Collections.unmodifiableList(entries);
    }

    /** The entry that was added last since the game started, or null. */
    public ScoreEntry getLatest() {
        return latest;
    }

    /** False if the list is not on disk: it could not be read, or the last save failed. */
    public boolean isSaved() {
        return loaded && saved;
    }
}
