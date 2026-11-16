import game.HighScoreTable;
import game.LevelProgress;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import util.HighScoreStore;
import util.ProgressStore;

/**
 * High-score and progress files for tests, each in its own temporary folder,
 * so a test never touches the real files in the player's home folder. The folders are
 * removed when the tests end.
 */
final class TempScores {

    private TempScores() {
        // only static methods
    }

    /** The path of a score file that does not exist yet, in a new temporary folder. */
    static Path newFile() {
        return newFile("highscores.txt");
    }

    /** The path of a file that does not exist yet, in a new temporary folder. */
    static Path newFile(String name) {
        try {
            Path folder = Files.createTempDirectory("fish2trash-test");
            Path file = folder.resolve(name);
            // Deleted in reverse order when the tests exit: the file first, then its folder.
            folder.toFile().deleteOnExit();
            file.toFile().deleteOnExit();
            return file;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** An empty high-score list on a new temporary file. */
    static HighScoreTable newTable() {
        return new HighScoreTable(new HighScoreStore(newFile()));
    }

    /** No levels cleared yet, saved to a new temporary file. */
    static LevelProgress newProgress() {
        return new LevelProgress(new ProgressStore(newFile("progress.txt")));
    }
}
