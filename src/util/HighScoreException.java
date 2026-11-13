package util;

/**
 * Thrown when the high scores cannot be read or saved, or when a line of
 * the score file is damaged.
 *
 * It is a checked exception: the caller is forced by the compiler to decide
 * what happens then. The game carries on without the file.
 */
public class HighScoreException extends Exception {

    private static final long serialVersionUID = 1L;

    public HighScoreException(String message) {
        super(message);
    }

    /** @param cause the lower-level error (usually an IOException) that made this happen */
    public HighScoreException(String message, Throwable cause) {
        super(message, cause);
    }
}
