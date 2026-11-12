package ui;

import game.GameState;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.util.EnumMap;
import java.util.Map;
import ui.render.Fonts;
import ui.render.TextDraw;
import ui.render.UiPalette;
import util.AudioPlayer;
import util.GameConstants;
import util.SoundState;
import util.StoryText;

/** Drawn on top of the frozen game while it is paused. */
public final class PauseOverlay implements Screen {

    private static final int CENTER_X = GameConstants.WORLD_WIDTH / 2;

    private final ScreenManager screens;
    private final MenuList menu;
    // The text of the sound choice for each state, looked up when the menu is drawn.
    private final Map<SoundState, String> soundLabels = new EnumMap<>(SoundState.class);

    public PauseOverlay(ScreenManager screens) {
        this.screens = screens;
        AudioPlayer audio = screens.getAudio();
        soundLabels.put(SoundState.ON, StoryText.PAUSE_SOUND_ON);
        soundLabels.put(SoundState.MUTED, StoryText.PAUSE_SOUND_OFF);
        soundLabels.put(SoundState.UNAVAILABLE, StoryText.PAUSE_SOUND_NONE);

        menu = new MenuList(audio);
        menu.add(StoryText.PAUSE_RESUME, screens::resume);
        menu.add(() -> soundLabels.get(audio.getState()), () -> audio.setMuted(!audio.isMuted()));
        // Entering the playing screen again starts a fresh attempt at the chapter.
        menu.add(StoryText.PAUSE_RESTART, () -> screens.go(GameState.PLAYING));
        menu.add(StoryText.PAUSE_QUIT, () -> screens.go(GameState.MENU));
    }

    @Override
    public void onEnter() {
        menu.reset();
    }

    @Override
    public void keyPressed(int keyCode) {
        if (keyCode == KeyEvent.VK_P || keyCode == KeyEvent.VK_ESCAPE) {
            screens.resume();
        } else {
            menu.keyPressed(keyCode);
        }
    }

    @Override
    public void paint(Graphics2D g) {
        g.setColor(UiPalette.SHADE);
        g.fillRect(0, 0, GameConstants.WORLD_WIDTH, GameConstants.WORLD_HEIGHT);

        g.setFont(Fonts.BANNER);
        g.setColor(UiPalette.TEXT);
        TextDraw.centered(g, StoryText.PAUSE_TITLE, CENTER_X, 220);

        menu.paint(g, CENTER_X, 290, 46);
    }
}
