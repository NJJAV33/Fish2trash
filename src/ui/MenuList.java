package ui;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import ui.render.Fonts;
import ui.render.TextDraw;
import ui.render.UiPalette;
import util.AudioPlayer;
import util.SoundEffect;

/**
 * A list of choices the player moves through with UP / DOWN and picks with
 * ENTER. Each choice carries the action to run, so there is no switch over
 * the selected index. The main menu and the pause menu both use it.
 */
final class MenuList {

    private final AudioPlayer audio;
    private final List<Supplier<String>> labels = new ArrayList<>();
    private final List<Runnable> actions = new ArrayList<>();
    private int selected;

    MenuList(AudioPlayer audio) {
        this.audio = audio;
    }

    void add(String label, Runnable action) {
        add(() -> label, action);
    }

    /** A choice whose text is asked for each time it is drawn, so it can change (like "Sound: on"). */
    void add(Supplier<String> label, Runnable action) {
        labels.add(label);
        actions.add(action);
    }

    /** Puts the highlight back on the first choice. */
    void reset() {
        selected = 0;
    }

    void keyPressed(int keyCode) {
        int count = labels.size();
        if (keyCode == KeyEvent.VK_UP || keyCode == KeyEvent.VK_W) {
            selected = (selected - 1 + count) % count;   // wraps from the top to the bottom
            audio.play(SoundEffect.MENU_MOVE);
        } else if (keyCode == KeyEvent.VK_DOWN || keyCode == KeyEvent.VK_S) {
            selected = (selected + 1) % count;
            audio.play(SoundEffect.MENU_MOVE);
        } else if (keyCode == KeyEvent.VK_ENTER || keyCode == KeyEvent.VK_SPACE) {
            audio.play(SoundEffect.MENU_SELECT);
            actions.get(selected).run();
        }
    }

    void paint(Graphics2D g, int centerX, int firstBaselineY, int spacing) {
        g.setFont(Fonts.HEADING);
        for (int i = 0; i < labels.size(); i++) {
            int y = firstBaselineY + i * spacing;
            boolean chosen = (i == selected);
            g.setColor(chosen ? UiPalette.ACCENT : UiPalette.TEXT);
            String label = labels.get(i).get();
            TextDraw.centered(g, label, centerX, y);
            if (chosen) {
                // A small arrow to the left of the chosen line.
                int left = centerX - g.getFontMetrics().stringWidth(label) / 2 - 26;
                g.fillPolygon(new int[] {left, left + 12, left}, new int[] {y - 18, y - 9, y}, 3);
            }
        }
    }
}
