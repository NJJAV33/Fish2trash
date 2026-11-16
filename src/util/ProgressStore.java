package util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * Remembers between launches how far the player has got: the number of the
 * furthest level cleared (0 to 15), as one line of text in a small file.
 *
 * Losing this file only locks the level select again, so problems are
 * reported on the error output and never stop the game.
 */
public final class ProgressStore {

    private static final String FOLDER = ".fish2trash";
    private static final String FILE = "progress.txt";

    private final Path file;

    /** @param file where the progress is kept; tests pass a file in a temporary folder */
    public ProgressStore(Path file) {
        this.file = file;
    }

    /** The store the game uses: next to the high scores, in the player's home folder. */
    public static ProgressStore inUserHome() {
        return new ProgressStore(Paths.get(System.getProperty("user.home"), FOLDER, FILE));
    }

    /** The furthest level cleared, or 0 if nothing is saved yet or the file cannot be read. */
    public int read() {
        if (Files.notExists(file)) {
            return 0;
        }
        try {
            return Integer.parseInt(new String(Files.readAllBytes(file), StandardCharsets.UTF_8).trim());
        } catch (IOException | NumberFormatException e) {
            System.err.println("Could not read the saved progress from " + file + ": " + e.getMessage());
            return 0;
        }
    }

    /**
     * Saves the furthest level cleared. Like the high scores, it is written
     * to a second file first, which then takes the place of the old one.
     *
     * @return false if it could not be saved
     */
    public boolean write(int furthestCleared) {
        Path target = file.toAbsolutePath();
        Path draft = target.resolveSibling(target.getFileName() + ".tmp");
        try {
            Files.createDirectories(target.getParent());
            Files.write(draft, (furthestCleared + System.lineSeparator()).getBytes(StandardCharsets.UTF_8));
            try {
                Files.move(draft, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(draft, target, StandardCopyOption.REPLACE_EXISTING);   // some drives cannot swap in one step
            }
            return true;
        } catch (IOException e) {
            System.err.println("Could not save the progress to " + file + ": " + e.getMessage());
            return false;
        }
    }
}
