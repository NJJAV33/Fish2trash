import game.GameState;
import java.awt.Graphics2D;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.util.List;
import ui.GamePanel;
import util.GameConstants;
import util.SoundEffect;
import util.SoundState;

/**
 * Walks through the screens the way a player would, by sending key presses
 * and time to a real GamePanel (without opening a window), and checks that
 * each key leads to the right screen and that every screen can be drawn.
 */
public final class ScreenFlowTest {

    private static final double STEP = 0.05;
    private static final double SCREEN_CHANGE = 0.7;   // a fade out and back in, with some slack

    private static GamePanel panel;
    private static RecordingAudioPlayer audio;   // records sounds instead of playing them
    private static long clock;   // pretend time stamps for key events, in milliseconds

    private ScreenFlowTest() {
        // only static methods
    }

    public static void run() {
        menuOpensHowToAndComesBack();
        storyPagesLeadIntoTheGame();
        pauseMenuWorks();
        losingTheWindowFocusPauses();
        runningOutOfTimeGetsYouFiredAndYouCanRetry();
        endingTheRunReturnsToTheMenu();
        menusClickAndTheMKeyMutes();
        toolKeysReachTheGame();
        theHKeyBringsTheLastTipBack();
    }

    private static void menusClickAndTheMKeyMutes() {
        startGame();
        press(KeyEvent.VK_DOWN);
        press(KeyEvent.VK_UP);
        press(KeyEvent.VK_ENTER);
        Check.assertEquals("moving and choosing in the menu is heard",
                List.of(SoundEffect.MENU_MOVE, SoundEffect.MENU_MOVE, SoundEffect.MENU_SELECT), audio.getPlayed());

        Check.assertEquals("sound starts on", SoundState.ON, panel.getSoundState());
        press(KeyEvent.VK_M);
        Check.assertEquals("M mutes", SoundState.MUTED, panel.getSoundState());
        press(KeyEvent.VK_M);
        Check.assertEquals("M again unmutes", SoundState.ON, panel.getSoundState());

        // The pause menu's second item switches the sound too.
        startChapterOne();
        press(KeyEvent.VK_P);
        press(KeyEvent.VK_DOWN);
        press(KeyEvent.VK_ENTER);
        Check.assertEquals("the pause menu can mute", SoundState.MUTED, panel.getSoundState());
        Check.assertTrue("and the game stays paused", panel.isPaused());
        draw();
        press(KeyEvent.VK_ENTER);
        Check.assertEquals("choosing it again unmutes", SoundState.ON, panel.getSoundState());
    }

    private static void toolKeysReachTheGame() {
        startChapterOne();
        int before = audio.getPlayed().size();
        press(KeyEvent.VK_1);
        Check.assertEquals("picking the tool already on the line is silent", before, audio.getPlayed().size());

        press(KeyEvent.VK_2);   // the magnet only arrives in chapter 2
        Check.assertEquals("asking for a locked tool is refused out loud",
                SoundEffect.WRONG_TOOL, audio.getPlayed().get(audio.getPlayed().size() - 1));
        draw();   // the "arrives in chapter 2" message is on screen now
        expect("the chapter goes on", GameState.PLAYING);
    }

    private static void theHKeyBringsTheLastTipBack() {
        startChapterOne();
        press(KeyEvent.VK_H);
        Check.assertTrue("H does nothing before the first tip", !audio.getPlayed().contains(SoundEffect.HINT));

        play(2.0);   // the first tip appears one second into chapter 1
        int tipsHeard = count(SoundEffect.HINT);
        Check.assertEquals("the first tip chimes", 1, tipsHeard);
        play(5.0);   // long enough for it to fade away
        press(KeyEvent.VK_H);
        Check.assertEquals("H shows it again, with its chime", tipsHeard + 1, count(SoundEffect.HINT));
        draw();

        // By second 15: the tip about fish (second 8), and the idle hint
        // for twelve seconds without dropping the line.
        play(8.0);
        Check.assertEquals("the next tip and the idle hint chime too", tipsHeard + 3, count(SoundEffect.HINT));
        expect("the chapter goes on", GameState.PLAYING);
    }

    private static int count(SoundEffect effect) {
        int count = 0;
        for (SoundEffect played : audio.getPlayed()) {
            if (played == effect) {
                count++;
            }
        }
        return count;
    }

    private static void menuOpensHowToAndComesBack() {
        startGame();
        expect("the game opens on the menu", GameState.MENU);

        press(KeyEvent.VK_DOWN);
        press(KeyEvent.VK_ENTER);
        play(SCREEN_CHANGE);
        expect("second menu item opens How to Play", GameState.HOW_TO);

        press(KeyEvent.VK_ENTER);
        play(SCREEN_CHANGE);
        expect("ENTER returns to the menu", GameState.MENU);
    }

    private static void storyPagesLeadIntoTheGame() {
        startGame();
        press(KeyEvent.VK_ENTER);
        play(SCREEN_CHANGE);
        expect("Start opens the story", GameState.STORY);

        // The first ENTER only finishes the page that is still being typed out.
        // Holding the key down must count as that one press, however many
        // repeats the keyboard sends.
        holdWithRepeats(KeyEvent.VK_ENTER);
        play(0.2);
        expect("one held ENTER does not leave the story", GameState.STORY);

        int presses = 1;
        while (panel.getState() == GameState.STORY && presses < 20) {
            press(KeyEvent.VK_ENTER);
            play(0.3);
            presses++;
        }
        play(SCREEN_CHANGE);
        expect("after the last page the chapter starts", GameState.PLAYING);
        Check.assertEquals("three pages take two ENTERs each", 6, presses);
    }

    private static void pauseMenuWorks() {
        startChapterOne();

        press(KeyEvent.VK_P);
        Check.assertTrue("P pauses", panel.isPaused());
        draw();
        press(KeyEvent.VK_P);
        Check.assertTrue("P again resumes", !panel.isPaused());

        press(KeyEvent.VK_ESCAPE);
        Check.assertTrue("ESC pauses too", panel.isPaused());
        press(KeyEvent.VK_ENTER);   // "Resume" is the first item
        Check.assertTrue("Resume resumes", !panel.isPaused());

        press(KeyEvent.VK_P);
        press(KeyEvent.VK_DOWN);
        press(KeyEvent.VK_DOWN);
        press(KeyEvent.VK_ENTER);   // "Restart chapter" (third item, after Resume and Sound)
        play(SCREEN_CHANGE);
        expect("Restart chapter goes back into the game", GameState.PLAYING);
        Check.assertTrue("and it is not paused", !panel.isPaused());

        press(KeyEvent.VK_P);
        press(KeyEvent.VK_UP);      // up from the first item wraps to the last
        press(KeyEvent.VK_ENTER);   // "Quit to menu"
        play(SCREEN_CHANGE);
        expect("Quit to menu goes to the menu", GameState.MENU);
    }

    private static void losingTheWindowFocusPauses() {
        startChapterOne();
        for (FocusListener listener : panel.getFocusListeners()) {
            listener.focusLost(new FocusEvent(panel, FocusEvent.FOCUS_LOST));
        }
        Check.assertTrue("the game pauses when the window loses focus", panel.isPaused());
    }

    private static void runningOutOfTimeGetsYouFiredAndYouCanRetry() {
        startChapterOne();
        play(GameConstants.TIME_CH1_S + 3);   // do nothing until the clock runs out
        expect("time up leads to the fired screen", GameState.GAME_OVER);

        press(KeyEvent.VK_ENTER);
        play(SCREEN_CHANGE);
        expect("ENTER retries: back to the chapter's story", GameState.STORY);

        press(KeyEvent.VK_ESCAPE);
        play(SCREEN_CHANGE);
        expect("ESC skips the story into the game", GameState.PLAYING);
    }

    private static void endingTheRunReturnsToTheMenu() {
        startChapterOne();
        play(GameConstants.TIME_CH1_S + 3);
        expect("fired again", GameState.GAME_OVER);

        press(KeyEvent.VK_ESCAPE);
        play(SCREEN_CHANGE);
        expect("ESC ends the run", GameState.MENU);

        // A new run starts cleanly from the first story page.
        press(KeyEvent.VK_ENTER);
        play(SCREEN_CHANGE);
        expect("Start works again after a run ended", GameState.STORY);
    }

    // ---- helpers ----

    private static void startGame() {
        audio = new RecordingAudioPlayer();
        panel = new GamePanel(audio);
        panel.setSize(GameConstants.WORLD_WIDTH, GameConstants.WORLD_HEIGHT);
        clock = 1000;
        play(0.1);
    }

    /** Menu -> Start -> skip the story -> playing chapter 1. */
    private static void startChapterOne() {
        startGame();
        press(KeyEvent.VK_ENTER);
        play(SCREEN_CHANGE);
        press(KeyEvent.VK_ESCAPE);
        play(SCREEN_CHANGE);
        expect("chapter 1 is being played", GameState.PLAYING);
    }

    private static void play(double seconds) {
        for (double t = 0; t < seconds; t += STEP) {
            panel.advance(STEP);
            clock += (long) (STEP * 1000);
        }
    }

    /** A quick tap: the key goes down and comes up again. */
    private static void press(int keyCode) {
        clock += 100;
        KeyEvent down = new KeyEvent(panel, KeyEvent.KEY_PRESSED, clock, 0, keyCode, KeyEvent.CHAR_UNDEFINED);
        clock += 60;
        KeyEvent up = new KeyEvent(panel, KeyEvent.KEY_RELEASED, clock, 0, keyCode, KeyEvent.CHAR_UNDEFINED);
        for (KeyListener listener : panel.getKeyListeners()) {
            listener.keyPressed(down);
            listener.keyReleased(up);
        }
    }

    /**
     * A key held down for a while. Keyboards repeat a held key in two ways:
     * some systems send press, press, press; others send release + press
     * pairs with the same time stamp. Both are sent here.
     */
    private static void holdWithRepeats(int keyCode) {
        clock += 100;
        for (KeyListener listener : panel.getKeyListeners()) {
            listener.keyPressed(keyEvent(KeyEvent.KEY_PRESSED, keyCode));
            for (int i = 0; i < 5; i++) {
                clock += 30;
                listener.keyPressed(keyEvent(KeyEvent.KEY_PRESSED, keyCode));
            }
            for (int i = 0; i < 5; i++) {
                clock += 30;
                listener.keyReleased(keyEvent(KeyEvent.KEY_RELEASED, keyCode));
                listener.keyPressed(keyEvent(KeyEvent.KEY_PRESSED, keyCode));
            }
            clock += 30;
            listener.keyReleased(keyEvent(KeyEvent.KEY_RELEASED, keyCode));
        }
    }

    private static KeyEvent keyEvent(int type, int keyCode) {
        return new KeyEvent(panel, type, clock, 0, keyCode, KeyEvent.CHAR_UNDEFINED);
    }

    /** Checks which screen is showing, and that it can be drawn. */
    private static void expect(String what, GameState state) {
        Check.assertEquals(what, state, panel.getState());
        draw();
    }

    private static void draw() {
        BufferedImage image = new BufferedImage(
                GameConstants.WORLD_WIDTH, GameConstants.WORLD_HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        panel.paint(g);
        g.dispose();
    }
}
