package ui.render;

import game.ChapterStatus;
import game.GameView;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Graphics2D;
import model.EquipmentType;
import util.GameConstants;
import util.Mathx;
import util.SoundState;
import util.StoryText;

/**
 * Draws the heads-up display over the game: chapter name, quota bar, hearts,
 * timer and score along the top, the tools along the bottom, and tips in a
 * toast above them.
 * It reads everything from the GameView and changes nothing.
 */
public final class HudPainter {

    private static final int BAR_X = 230;
    private static final int BAR_Y = 18;
    private static final int BAR_WIDTH = 220;
    private static final int BAR_HEIGHT = 14;
    private static final int HEARTS_X = 470;
    private static final int HEART_SPACING = 26;

    // The tool bar is kept low and flat, so it hides as little of the deep water as possible.
    private static final int TOOL_Y = 553;
    private static final int TOOL_WIDTH = 124;
    private static final int TOOL_HEIGHT = 30;
    private static final int TOOL_GAP = 8;
    private static final double TOOL_ICON_SCALE = 0.55;
    private static final BasicStroke THIN = new BasicStroke(1);
    private static final BasicStroke SELECTED_EDGE = new BasicStroke(2);

    private static final double TOAST_SLIDE_SECONDS = 0.25;
    private static final double TOAST_FADE_SECONDS = 0.4;
    public static final double TOAST_SECONDS = 4.0;   // how long a tip stays, in total

    private final PainterRegistry painters;   // draws the tool icons

    public HudPainter(PainterRegistry painters) {
        this.painters = painters;
    }

    /**
     * @param time  seconds, used to make the timer pulse when it runs low
     * @param sound whether sound is on, shown as a small speaker
     */
    public void paint(Graphics2D g, GameView game, double time, SoundState sound) {
        g.setColor(UiPalette.PANEL);
        g.fillRoundRect(8, 8, 784, 52, 14, 14);

        g.setFont(Fonts.LABEL);
        g.setColor(UiPalette.ACCENT);
        g.drawString(game.getChapter().getTitle(), 20, 30);
        g.setFont(Fonts.SMALL);
        g.setColor(UiPalette.DIM_TEXT);
        g.drawString(StoryText.HUD_OBJECTIVE, 20, 48);

        drawQuotaBar(g, game);
        drawHearts(g, game);
        drawTimer(g, game, time);

        g.setFont(Fonts.SMALL);
        g.setColor(UiPalette.DIM_TEXT);
        TextDraw.centered(g, StoryText.HUD_SCORE, 747, 26);
        g.setFont(Fonts.SUBHEADING);
        g.setColor(UiPalette.TEXT);
        TextDraw.centered(g, String.valueOf(game.getScore()), 747, 48);

        drawSpeaker(g, sound);
        if (GameConstants.EQUIPMENT_ENABLED) {
            drawToolBar(g, game, time);
        }

        // The key reminder sits on a dark strip, so it reads on any sea floor.
        g.setColor(UiPalette.PANEL);
        g.fillRect(0, GameConstants.SEABED_Y, GameConstants.WORLD_WIDTH,
                GameConstants.WORLD_HEIGHT - GameConstants.SEABED_Y);
        g.setFont(Fonts.SMALL);
        g.setColor(UiPalette.DIM_TEXT);
        TextDraw.centered(g, StoryText.PLAY_HINT, GameConstants.WORLD_WIDTH / 2, 597);
    }

    /** A small speaker under the HUD bar: with sound waves when on, crossed out when not. */
    private void drawSpeaker(Graphics2D g, SoundState sound) {
        boolean on = (sound == SoundState.ON);
        int x = 762;
        int y = 68;

        g.setColor(UiPalette.PANEL);
        g.fillRoundRect(x - 6, y - 4, 34, 26, 10, 10);
        g.setColor(on ? UiPalette.TEXT : UiPalette.DIM_TEXT);
        g.fillRect(x, y + 6, 5, 7);
        g.fillPolygon(new int[] {x + 5, x + 12, x + 12, x + 5}, new int[] {y + 6, y + 1, y + 18, y + 13}, 4);
        if (on) {
            g.drawArc(x + 10, y + 5, 8, 9, -60, 120);
            g.drawArc(x + 10, y + 1, 14, 17, -60, 120);
        } else {
            g.drawLine(x + 15, y + 5, x + 22, y + 13);
            g.drawLine(x + 22, y + 5, x + 15, y + 13);
        }
    }

    /** One slot per tool along the bottom: its key, its picture and its name. */
    private void drawToolBar(Graphics2D g, GameView game, double time) {
        EquipmentType[] tools = EquipmentType.values();
        int totalWidth = tools.length * TOOL_WIDTH + (tools.length - 1) * TOOL_GAP;
        int left = (GameConstants.WORLD_WIDTH - totalWidth) / 2;
        for (int i = 0; i < tools.length; i++) {
            drawToolSlot(g, game, tools[i], i + 1, left + i * (TOOL_WIDTH + TOOL_GAP), time);
        }
    }

    private void drawToolSlot(Graphics2D g, GameView game, EquipmentType tool, int keyNumber, int x, double time) {
        boolean selected = (tool == game.getSelectedTool());
        boolean unlocked = game.isToolUnlocked(tool);

        g.setColor(selected ? UiPalette.PANEL_SOLID : UiPalette.PANEL);
        g.fillRoundRect(x, TOOL_Y, TOOL_WIDTH, TOOL_HEIGHT, 12, 12);
        if (unlocked && !selected && game.getSwapCooldownFraction() > 0) {
            // Right after a change the other tools are greyed out; the shade shrinks as the wait runs down.
            int waiting = (int) (TOOL_WIDTH * game.getSwapCooldownFraction());
            g.setColor(UiPalette.SHADE);
            g.fillRoundRect(x, TOOL_Y, waiting, TOOL_HEIGHT, 12, 12);
        }
        g.setColor(selected ? UiPalette.ACCENT : UiPalette.PANEL_EDGE);
        g.setStroke(selected ? SELECTED_EDGE : THIN);
        g.drawRoundRect(x, TOOL_Y, TOOL_WIDTH, TOOL_HEIGHT, 12, 12);
        g.setStroke(THIN);

        Graphics2D icon = (Graphics2D) g.create();
        icon.translate(x + 30, TOOL_Y + TOOL_HEIGHT / 2 + 1);
        icon.scale(TOOL_ICON_SCALE, TOOL_ICON_SCALE);
        if (!unlocked) {
            icon.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.35f));
        }
        painters.paintTool(icon, tool, time, false);
        icon.dispose();

        g.setFont(Fonts.SMALL);
        g.setColor(UiPalette.DIM_TEXT);
        g.drawString(String.valueOf(keyNumber), x + 8, TOOL_Y + 19);
        g.setFont(selected ? Fonts.LABEL : Fonts.SMALL);
        g.setColor(selected ? UiPalette.TEXT : UiPalette.DIM_TEXT);
        String label = unlocked ? tool.getDisplayName()
                : String.format(StoryText.TOOL_LOCKED_SHORT, tool.getUnlockChapter());
        g.drawString(label, x + 46, TOOL_Y + 20);
    }

    private void drawQuotaBar(Graphics2D g, GameView game) {
        double filled = Mathx.clamp((double) game.getScore() / game.getQuota(), 0, 1);

        g.setColor(UiPalette.BAR_TRACK);
        g.fillRoundRect(BAR_X, BAR_Y, BAR_WIDTH, BAR_HEIGHT, 8, 8);
        g.setColor(filled >= 1 ? UiPalette.GOLD : UiPalette.ACCENT);
        g.fillRoundRect(BAR_X, BAR_Y, (int) (BAR_WIDTH * filled), BAR_HEIGHT, 8, 8);

        // Quarter marks, so progress is readable at a glance.
        g.setColor(UiPalette.PANEL);
        for (int quarter = 1; quarter <= 3; quarter++) {
            int x = BAR_X + BAR_WIDTH * quarter / 4;
            g.drawLine(x, BAR_Y, x, BAR_Y + BAR_HEIGHT - 1);
        }

        g.setFont(Fonts.SMALL);
        g.setColor(UiPalette.TEXT);
        TextDraw.centered(g, game.getScore() + " / " + game.getQuota(), BAR_X + BAR_WIDTH / 2, 50);
    }

    private void drawHearts(Graphics2D g, GameView game) {
        for (int i = 0; i < game.getMaxHealth(); i++) {
            g.setColor(i < game.getHealth() ? UiPalette.HEART : UiPalette.HEART_EMPTY);
            drawHeart(g, HEARTS_X + i * HEART_SPACING, 24);
        }
    }

    /** A heart from two circles and a triangle, 20 pixels wide. */
    private void drawHeart(Graphics2D g, int x, int y) {
        g.fillOval(x, y, 11, 11);
        g.fillOval(x + 9, y, 11, 11);
        g.fillPolygon(new int[] {x, x + 20, x + 10}, new int[] {y + 7, y + 7, y + 20}, 3);
    }

    private void drawTimer(Graphics2D g, GameView game, double time) {
        int secondsLeft = (int) Math.ceil(game.getTimeLeft());
        String text = String.format("%d:%02d", secondsLeft / 60, secondsLeft % 60);

        g.setFont(Fonts.TIMER);
        if (secondsLeft <= GameConstants.LOW_TIME_S && game.getStatus() == ChapterStatus.RUNNING) {
            // Running low: red, and pulsing between dim and bright.
            float pulse = (float) (0.65 + 0.35 * Math.sin(time * 8));
            Graphics2D pulsing = (Graphics2D) g.create();
            pulsing.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, pulse));
            pulsing.setColor(UiPalette.TIMER_WARNING);
            TextDraw.centered(pulsing, text, 655, 43);
            pulsing.dispose();
        } else {
            g.setColor(UiPalette.TEXT);
            TextDraw.centered(g, text, 655, 43);
        }
    }

    /**
     * Draws a tip near the bottom of the screen. It slides up into place,
     * stays, then fades out.
     *
     * @param age seconds since the tip appeared
     */
    public void paintToast(Graphics2D g, String text, double age) {
        if (text == null || age >= TOAST_SECONDS) {
            return;
        }
        double slideIn = Mathx.clamp(age / TOAST_SLIDE_SECONDS, 0, 1);
        double fadeOut = Mathx.clamp((TOAST_SECONDS - age) / TOAST_FADE_SECONDS, 0, 1);
        int y = (int) Mathx.lerp(484, 464, slideIn);

        Graphics2D toast = (Graphics2D) g.create();
        toast.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) (slideIn * fadeOut)));
        toast.setColor(UiPalette.PANEL_SOLID);
        toast.fillRoundRect(160, y, 480, 36, 16, 16);
        toast.setColor(UiPalette.PANEL_EDGE);
        toast.drawRoundRect(160, y, 480, 36, 16, 16);
        toast.setFont(Fonts.LABEL);
        toast.setColor(UiPalette.TEXT);
        TextDraw.centered(toast, text, GameConstants.WORLD_WIDTH / 2, y + 23);
        toast.dispose();
    }
}
