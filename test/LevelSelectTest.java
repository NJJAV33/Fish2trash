import game.GameManager;
import game.GameSession;
import game.LevelProgress;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;
import model.LevelData;
import ui.LevelSelectScreen;
import ui.ScreenManager;
import ui.render.SceneRenderer;
import util.GameConstants;
import util.ProgressStore;
import util.SoundEffect;

/**
 * The saved progress and the level select: which levels open up, that they
 * stay open after a restart, and that picking one starts a run there.
 */
public final class LevelSelectTest {

    private LevelSelectTest() {
    }

    public static void run() {
        onlyTheFirstLevelIsOpenAtFirst();
        aClearedLevelOpensTheNextAndIsRemembered();
        progressNeverGoesBack();
        aDamagedFileOpensOnlyTheFirstLevel();
        progressIsKeptWhenItCannotBeSaved();
        aRunCanStartAtAnyLevel();
        clearingALevelInTheGameSavesIt();
        theLevelSelectStartsAnOpenLevel();
        aLockedLevelCannotBePicked();
    }

    private static void onlyTheFirstLevelIsOpenAtFirst() {
        LevelProgress progress = TempScores.newProgress();
        Check.assertEquals("nothing cleared yet", 0, progress.getFurthestCleared());
        Check.assertTrue("level 1-1 is open", progress.isUnlocked(1));
        Check.assertTrue("level 1-2 is locked", !progress.isUnlocked(2));
        Check.assertTrue("level 1-1 is not cleared yet", !progress.isCleared(1));
    }

    private static void aClearedLevelOpensTheNextAndIsRemembered() {
        Path file = TempScores.newFile("progress.txt");
        LevelProgress progress = new LevelProgress(new ProgressStore(file));
        progress.recordCleared(1);
        Check.assertTrue("level 1-1 counts as cleared", progress.isCleared(1));
        Check.assertTrue("level 1-2 opens", progress.isUnlocked(2));
        Check.assertTrue("level 1-3 is still locked", !progress.isUnlocked(3));

        LevelProgress afterRestart = new LevelProgress(new ProgressStore(file));
        Check.assertEquals("the progress is read back after a restart", 1, afterRestart.getFurthestCleared());

        afterRestart.recordCleared(LevelData.LEVEL_COUNT);
        Check.assertTrue("after the last level every level is open", afterRestart.isUnlocked(LevelData.LEVEL_COUNT));
        Check.assertTrue("and there is no level 16", !afterRestart.isUnlocked(LevelData.LEVEL_COUNT + 1));
    }

    private static void progressNeverGoesBack() {
        LevelProgress progress = TempScores.newProgress();
        progress.recordCleared(5);
        progress.recordCleared(2);   // replaying an earlier level
        Check.assertEquals("replaying an earlier level keeps the furthest one", 5, progress.getFurthestCleared());
    }

    private static void aDamagedFileOpensOnlyTheFirstLevel() {
        Check.assertEquals("text that is not a number", 0, furthestReadFrom("banana"));
        Check.assertEquals("a negative number", 0, furthestReadFrom("-3"));
        Check.assertEquals("a number past the last level", LevelData.LEVEL_COUNT, furthestReadFrom("99"));
    }

    private static int furthestReadFrom(String content) {
        try {
            Path file = TempScores.newFile("progress.txt");
            Files.writeString(file, content);
            return new LevelProgress(new ProgressStore(file)).getFurthestCleared();
        } catch (java.io.IOException e) {
            throw new java.io.UncheckedIOException(e);
        }
    }

    private static void progressIsKeptWhenItCannotBeSaved() {
        try {
            // The "folder" the file should go in is really a file, so nothing can be saved there.
            Path blocker = TempScores.newFile("blocker");
            Files.writeString(blocker, "not a folder");
            ProgressStore store = new ProgressStore(blocker.resolve("progress.txt"));
            Check.assertTrue("saving reports the failure", !store.write(3));
            LevelProgress progress = new LevelProgress(store);
            progress.recordCleared(1);
            Check.assertTrue("the level still opens for this session", progress.isUnlocked(2));
        } catch (java.io.IOException e) {
            throw new java.io.UncheckedIOException(e);
        }
    }

    private static void aRunCanStartAtAnyLevel() {
        GameSession session = new GameSession(new Random(1), 9);
        Check.assertEquals("the run starts at level 2-4", 9, session.getLevelData().getNumber());
        GameScript.clearCleanly(session);
        Check.assertEquals("and goes on to the next level", 10, session.getLevelData().getNumber());
        boolean refused = false;
        try {
            new GameSession(new Random(1), LevelData.LEVEL_COUNT + 1);
        } catch (IllegalArgumentException e) {
            refused = true;
        }
        Check.assertTrue("there is no level 16 to start at", refused);
    }

    private static void clearingALevelInTheGameSavesIt() {
        LevelProgress progress = TempScores.newProgress();
        ScreenManager screens = new ScreenManager(new RecordingAudioPlayer(), TempScores.newTable(), progress);
        screens.startNewRun();

        GameManager failed = screens.getSession().startLevel();
        while (failed.isPlaying()) {
            failed.update(1);   // let the clock run out
        }
        screens.endLevel(failed);
        Check.assertEquals("a failed level opens nothing", 0, progress.getFurthestCleared());

        GameManager game = screens.getSession().startLevel();
        GameScript.clearLevel(game);
        screens.endLevel(game);
        Check.assertEquals("a cleared level is saved", 1, progress.getFurthestCleared());
    }

    private static void theLevelSelectStartsAnOpenLevel() {
        LevelProgress progress = TempScores.newProgress();
        for (int level = 1; level <= 8; level++) {
            progress.recordCleared(level);
        }
        RecordingAudioPlayer audio = new RecordingAudioPlayer();
        ScreenManager screens = new ScreenManager(audio, TempScores.newTable(), progress);
        LevelSelectScreen select = new LevelSelectScreen(screens, new SceneRenderer());
        select.onEnter();
        draw(select);
        select.keyPressed(KeyEvent.VK_ENTER);   // the highlight starts on the next level to play: 2-4
        Check.assertEquals("the run starts at level 2-4", 9, screens.getSession().getLevelData().getNumber());

        select.onEnter();
        select.keyPressed(KeyEvent.VK_UP);      // from 2-4 up to 1-4
        select.keyPressed(KeyEvent.VK_LEFT);    // and left to 1-3
        draw(select);
        select.keyPressed(KeyEvent.VK_ENTER);
        Check.assertEquals("a cleared level can be replayed", 3, screens.getSession().getLevelData().getNumber());
        Check.assertTrue("picking is heard", audio.getPlayed().contains(SoundEffect.MENU_SELECT));
    }

    private static void aLockedLevelCannotBePicked() {
        RecordingAudioPlayer audio = new RecordingAudioPlayer();
        ScreenManager screens = new ScreenManager(audio, TempScores.newTable(), TempScores.newProgress());
        LevelSelectScreen select = new LevelSelectScreen(screens, new SceneRenderer());
        select.onEnter();
        select.keyPressed(KeyEvent.VK_RIGHT);   // from 1-1 to 1-2, still locked
        draw(select);
        select.keyPressed(KeyEvent.VK_ENTER);
        Check.assertTrue("no run starts", screens.getSession() == null);
        Check.assertTrue("and a no-sound plays", audio.getPlayed().contains(SoundEffect.WRONG_TOOL));

        for (int i = 0; i < 20; i++) {
            select.keyPressed(KeyEvent.VK_DOWN);   // the highlight stops at the bottom row
            select.keyPressed(KeyEvent.VK_RIGHT);
        }
        draw(select);
        Check.assertTrue("the highlight never leaves the grid", screens.getSession() == null);
    }

    private static void draw(LevelSelectScreen screen) {
        BufferedImage image = new BufferedImage(
                GameConstants.WORLD_WIDTH, GameConstants.WORLD_HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        screen.paint(g);
        g.dispose();
    }
}
