package game;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import util.GameConstants;
import util.HighScoreException;
import util.StoryText;

/**
 * One line of the high-score list: who, how many points, and how the run went.
 * Immutable.
 *
 * In the score file an entry is one line of text:
 * name|score|chapters cleared|badges|retries|date
 */
public final class ScoreEntry implements Comparable<ScoreEntry> {

    private static final char SEPARATOR = '|';
    private static final int FIELD_COUNT = 6;

    private final String name;
    private final int score;
    private final int chaptersCleared;
    private final Set<Badge> badges = EnumSet.noneOf(Badge.class);
    private final int retries;
    private final LocalDate date;

    /** @param name as the player typed it; it is tidied by cleanName() */
    public ScoreEntry(String name, int score, int chaptersCleared, Collection<Badge> badges,
            int retries, LocalDate date) {
        this.name = cleanName(name);
        this.score = score;
        this.chaptersCleared = chaptersCleared;
        this.badges.addAll(badges);
        this.retries = retries;
        this.date = date;
    }

    /** The entry for a run that has just ended. */
    public static ScoreEntry forRun(String name, GameSession session, LocalDate date) {
        return new ScoreEntry(name, session.getRunScore(), session.getChaptersCleared(),
                session.getBadges(), session.getRetries(), date);
    }

    /** True for the characters a name may contain. */
    public static boolean isNameCharacter(char character) {
        return Character.isLetterOrDigit(character) || " -_.'".indexOf(character) >= 0;
    }

    /**
     * Makes any text safe to use as a name: only name characters are kept
     * (so it can never break a line of the score file), it is cut to the
     * longest allowed length, and an empty name becomes the default one.
     */
    public static String cleanName(String typed) {
        StringBuilder clean = new StringBuilder();
        for (char character : typed.trim().toCharArray()) {
            if (isNameCharacter(character) && clean.length() < GameConstants.HIGHSCORE_NAME_MAX_LEN) {
                clean.append(character);
            }
        }
        String name = clean.toString().trim();
        return name.isEmpty() ? StoryText.PLAYER_NAME : name;
    }

    /** This entry as one line of the score file. */
    public String toLine() {
        int badgeBits = 0;
        for (Badge badge : badges) {
            badgeBits |= 1 << badge.ordinal();   // one bit per badge: 1, 2 and 4
        }
        return name + SEPARATOR + score + SEPARATOR + chaptersCleared + SEPARATOR + badgeBits
                + SEPARATOR + retries + SEPARATOR + date;
    }

    /**
     * Reads one line of the score file.
     *
     * @throws HighScoreException if the line is not a score: fields missing, or not numbers, or out of range
     */
    public static ScoreEntry parse(String line) throws HighScoreException {
        String[] fields = line.split("\\" + SEPARATOR, -1);
        if (fields.length != FIELD_COUNT) {
            throw new HighScoreException("Not a score line: " + line);
        }
        try {
            int score = Integer.parseInt(fields[1]);
            int chaptersCleared = Integer.parseInt(fields[2]);
            int badgeBits = Integer.parseInt(fields[3]);
            int retries = Integer.parseInt(fields[4]);
            LocalDate date = LocalDate.parse(fields[5]);
            if (score < 0 || chaptersCleared < 0 || retries < 0
                    || badgeBits < 0 || badgeBits >= 1 << Badge.values().length) {
                throw new HighScoreException("A number is out of range in: " + line);
            }
            Set<Badge> badges = EnumSet.noneOf(Badge.class);
            for (Badge badge : Badge.values()) {
                if ((badgeBits & 1 << badge.ordinal()) != 0) {
                    badges.add(badge);
                }
            }
            return new ScoreEntry(fields[0], score, chaptersCleared, badges, retries, date);
        } catch (NumberFormatException | DateTimeParseException e) {
            throw new HighScoreException("Not a score line: " + line, e);
        }
    }

    /**
     * The order of the list: the higher score first; with equal scores the
     * run with fewer failed attempts; then the older one.
     */
    @Override
    public int compareTo(ScoreEntry other) {
        if (score != other.score) {
            return Integer.compare(other.score, score);
        }
        if (retries != other.retries) {
            return Integer.compare(retries, other.retries);
        }
        return date.compareTo(other.date);
    }

    public String getName() {
        return name;
    }

    public int getScore() {
        return score;
    }

    public int getChaptersCleared() {
        return chaptersCleared;
    }

    /** The badges of the run, as a read-only set. */
    public Set<Badge> getBadges() {
        return Collections.unmodifiableSet(badges);
    }

    public int getRetries() {
        return retries;
    }

    public LocalDate getDate() {
        return date;
    }

    @Override
    public String toString() {
        return name + " " + score + " (" + chaptersCleared + " chapters, " + badges.size() + " badges, "
                + retries + " retries, " + date + ")";
    }
}
