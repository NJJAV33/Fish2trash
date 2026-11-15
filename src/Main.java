import javax.swing.SwingUtilities;
import game.HighScoreTable;
import ui.GamePanel;
import ui.GameWindow;
import util.HighScoreStore;
import util.SynthAudioPlayer;

/**
 * Entry point of FISH2TRASH. Its only job is to open the game window.
 */
public class Main {

    public static void main(String[] args) {
        // Swing is not thread-safe, so every window must be created on
        // Swing's event-dispatch thread (EDT). invokeLater schedules it there.
        SwingUtilities.invokeLater(() -> createAndShowWindow());
    }

    private static void createAndShowWindow() {
        HighScoreTable highScores = new HighScoreTable(HighScoreStore.inUserHome());
        GamePanel panel = new GamePanel(new SynthAudioPlayer(), highScores);
        new GameWindow(panel).open();
    }
}
