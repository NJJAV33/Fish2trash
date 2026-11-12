import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import ui.GamePanel;
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
        JFrame frame = new JFrame("FISH2TRASH");
        GamePanel panel = new GamePanel(new SynthAudioPlayer());

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(panel);
        frame.pack();                       // size the window to fit the panel
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);  // center on screen
        frame.setVisible(true);

        panel.start();
    }
}
