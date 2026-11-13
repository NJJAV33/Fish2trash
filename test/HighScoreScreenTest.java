import game.GameManager;
import game.GameSession;
import game.GameState;
import game.HighScoreTable;
import game.ScoreEntry;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Collections;
import ui.HighScoreScreen;
import ui.MainMenu;
import ui.NameEntryScreen;
import ui.ScreenManager;
import ui.render.SceneRenderer;
import util.GameConstants;
import util.HighScoreStore;
import util.SoundEffect;
import util.StoryText;

/**
 * Checks the end of a run on screen: when a name is asked for, what can be
 * typed, and that the run ends up on the high-score list and in the file.
 */
public final class HighScoreScreenTest {

    private static final double STEP = 0.05;
    private static final double SCREEN_CHANGE = 0.7;   // a fade out and back in, with some slack

    private static RecordingAudioPlayer audio;
    private static Path file;
    private static HighScoreTable table;
    private static ScreenManager screens;

    private HighScoreScreenTest() {
        // only static methods
    }

    public static void run() {
        aRunWithoutAScoreSkipsTheNameEntry();
        aRunWithAScoreAsksForAName();
        theTypedNameGoesOnTheList();
        anEmptyNameBecomesTheDefault();
        theListCanBeDrawnEmptyFullAndUnsaved();
    }

    private static void aRunWithoutAScoreSkipsTheNameEntry() {
        start();
        GameManager game = screens.getSession().startChapter();
        while (game.isPlaying()) {
            game.update(STEP);   // do nothing until the clock runs out
        }
        screens.getSession().onChapterEnded(game);
        Check.assertEquals("a run that scored nothing goes straight on",
                GameState.MENU, screens.afterRun(GameState.MENU));
    }

    private static void aRunWithAScoreAsksForAName() {
        start();
        clearChapterOne();
        Check.assertEquals("a run with a score on an empty list asks for a name",
                GameState.NAME_ENTRY, screens.afterRun(GameState.MENU));
    }

    private static void theTypedNameGoesOnTheList() {
        start();
        clearChapterOne();
        screens.start(GameState.NAME_ENTRY);
        draw();   // the empty field shows the default name in grey
        Check.assertEquals("the name entry opens with its own sound", SoundEffect.HIGH_SCORE, audio.getPlayed().get(0));

        // Fifteen characters: a letter that is also the sound key, a character
        // that is not allowed, and more letters than a name may have.
        for (char character : "mark|abcdefghij".toCharArray()) {
            if (character == 'm') {
                screens.keyPressed(KeyEvent.VK_M);   // a real keyboard sends the key and the letter
            }
            screens.keyTyped(character);
        }
        Check.assertTrue("typing an M does not switch the sound off", !audio.isMuted());
        screens.keyPressed(KeyEvent.VK_BACK_SPACE);
        screens.keyPressed(KeyEvent.VK_BACK_SPACE);
        screens.keyTyped('z');
        draw();

        screens.keyPressed(KeyEvent.VK_ENTER);
        play(SCREEN_CHANGE);
        Check.assertEquals("ENTER saves and shows the list", GameState.HIGH_SCORES, screens.getState());
        draw();   // the list with the new run picked out

        Check.assertEquals("one run on the list", 1, table.getEntries().size());
        ScoreEntry entry = table.getEntries().get(0);
        // MARK + ABCDEFGH is the twelve-letter limit; two are deleted, a Z is added.
        Check.assertEquals("capital letters, no forbidden character, cut at the limit", "MARKABCDEFZ", entry.getName());
        Check.assertEquals("with the score of the run", screens.getSession().getRunScore(), entry.getScore());
        Check.assertEquals("its chapters", 1, entry.getChaptersCleared());
        Check.assertEquals("its badges", screens.getSession().getBadges(), entry.getBadges());
        Check.assertEquals("and today's date", LocalDate.now(), entry.getDate());
        Check.assertTrue("it is the latest entry", table.getLatest() == entry);
        Check.assertTrue("and it is in the file", table.isSaved() && Files.exists(file));

        screens.keyPressed(KeyEvent.VK_M);
        Check.assertTrue("on the list M is the sound key again", audio.isMuted());
        screens.keyPressed(KeyEvent.VK_M);
        screens.keyPressed(KeyEvent.VK_ENTER);
        play(SCREEN_CHANGE);
        Check.assertEquals("ENTER leaves the list for the menu", GameState.MENU, screens.getState());
    }

    private static void anEmptyNameBecomesTheDefault() {
        start();
        clearChapterOne();
        screens.start(GameState.NAME_ENTRY);
        screens.keyTyped('x');
        screens.keyPressed(KeyEvent.VK_BACK_SPACE);
        screens.keyPressed(KeyEvent.VK_BACK_SPACE);   // one more than there is to delete
        screens.keyPressed(KeyEvent.VK_ENTER);
        play(SCREEN_CHANGE);
        Check.assertEquals("an empty field saves the default name",
                StoryText.PLAYER_NAME, table.getEntries().get(0).getName());
    }

    private static void theListCanBeDrawnEmptyFullAndUnsaved() {
        start();
        screens.start(GameState.HIGH_SCORES);
        draw();   // "No scores yet"

        for (int n = 1; n <= GameConstants.HIGHSCORE_MAX_ENTRIES + 2; n++) {
            table.add(new ScoreEntry("PLAYER " + n, n * 750, n % 4, Collections.emptyList(), n % 3, LocalDate.now()));
        }
        draw();   // ten rows

        // A list whose file cannot be read shows a note that it is not saved.
        Path folder = TempScores.newFile();
        folder.toFile().mkdir();
        PrintStream real = System.err;
        System.setErr(new PrintStream(new ByteArrayOutputStream()));   // the "could not read" message is expected here
        try {
            table = new HighScoreTable(new HighScoreStore(folder));
        } finally {
            System.setErr(real);
        }
        screens = newScreens();
        screens.start(GameState.HIGH_SCORES);
        Check.assertTrue("the list knows it is not on disk", !table.isSaved());
        draw();
        folder.toFile().delete();
    }

    // ---- helpers ----

    /** A new game on an empty high-score list, with a run started. */
    private static void start() {
        audio = new RecordingAudioPlayer();
        file = TempScores.newFile();
        table = new HighScoreTable(new HighScoreStore(file));
        screens = newScreens();
        screens.startNewRun();
    }

    private static ScreenManager newScreens() {
        ScreenManager manager = new ScreenManager(audio, table);
        SceneRenderer renderer = new SceneRenderer();
        manager.register(GameState.MENU, new MainMenu(manager, renderer));
        manager.register(GameState.NAME_ENTRY, new NameEntryScreen(manager, renderer));
        manager.register(GameState.HIGH_SCORES, new HighScoreScreen(manager, renderer));
        return manager;
    }

    private static void clearChapterOne() {
        GameSession session = screens.getSession();
        GameManager game = session.startChapter();
        GameScript.clearChapter(game);
        session.onChapterEnded(game);
    }

    private static void play(double seconds) {
        for (double t = 0; t < seconds; t += STEP) {
            screens.update(STEP);
        }
    }

    private static void draw() {
        BufferedImage image = new BufferedImage(
                GameConstants.WORLD_WIDTH, GameConstants.WORLD_HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        screens.paint(g);
        g.dispose();
    }
}
