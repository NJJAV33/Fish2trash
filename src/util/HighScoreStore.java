package util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads and writes the high-score file, one line of text per score.
 *
 * It only knows about lines and files. What a line means is up to
 * game.ScoreEntry, so this class works the same for any text.
 */
public final class HighScoreStore {

    private static final String FOLDER = ".fish2trash";
    private static final String FILE = "highscores.txt";

    private final Path file;

    /** @param file where the scores are kept; tests pass a file in a temporary folder */
    public HighScoreStore(Path file) {
        this.file = file;
    }

    /** The store the game uses: a small file in a hidden folder of the player's home folder. */
    public static HighScoreStore inUserHome() {
        return new HighScoreStore(Paths.get(System.getProperty("user.home"), FOLDER, FILE));
    }

    /** The lines of the file. A file that does not exist yet is simply an empty list. */
    public List<String> readLines() throws HighScoreException {
        if (Files.notExists(file)) {
            return new ArrayList<>();
        }
        try {
            return Files.readAllLines(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new HighScoreException("Could not read the high scores from " + file, e);
        }
    }

    /**
     * Replaces the file with these lines. They are written to a second file
     * first, which then takes the place of the old one, so a crash halfway
     * never leaves a half-written score file behind.
     */
    public void writeLines(List<String> lines) throws HighScoreException {
        Path target = file.toAbsolutePath();
        Path draft = target.resolveSibling(target.getFileName() + ".tmp");
        try {
            Files.createDirectories(target.getParent());
            Files.write(draft, lines, StandardCharsets.UTF_8);
            try {
                Files.move(draft, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(draft, target, StandardCopyOption.REPLACE_EXISTING);   // some drives cannot swap in one step
            }
        } catch (IOException e) {
            throw new HighScoreException("Could not save the high scores to " + file, e);
        }
    }
}
