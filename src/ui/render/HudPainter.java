package ui.render;

import game.LevelStatus;
import game.GameView;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import model.Difficulty;
import model.EquipmentType;
import model.OceanEventType;
import model.LevelData;
import util.GameConstants;
import util.Mathx;
import util.SoundState;
import util.StoryText;

/**
 * Draws the heads-up display over the game: level name, quota bar, hearts,
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
    private static final BasicStroke CURRENT_STROKE = new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    private static final double CURRENT_SLACK = 0.3;   // below a tenth of full strength no arrowhead shows

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
    public void paint(Graphics2D g, GameView game, double time, SoundState sound, double shownScore) {
        g.setColor(UiPalette.PANEL);
        g.fillRoundRect(8, 8, 784, 52, 14, 14);

        g.setFont(Fonts.LABEL);
        g.setColor(UiPalette.ACCENT);
        LevelData level = game.getLevelData();
        g.drawString(StoryText.levelLabel(level.getChapter(), level.getLevel()), 20, 30);
        g.setFont(Fonts.SMALL);
        g.setColor(UiPalette.DIM_TEXT);
        g.drawString(StoryText.HUD_OBJECTIVE, 20, 48);

        drawQuotaBar(g, game, shownScore);
        drawHearts(g, game);
        drawTimer(g, game, time);

        g.setFont(Fonts.SMALL);
        g.setColor(UiPalette.DIM_TEXT);
        TextDraw.centered(g, StoryText.HUD_SCORE, 747, 26);
        g.setFont(Fonts.SUBHEADING);
        g.setColor(UiPalette.TEXT);
        TextDraw.centered(g, String.valueOf(game.getScore()), 747, 48);

        drawDifficultyBadge(g, game.getDifficulty());
        drawComboBadge(g, game.getCombo(), time);
        drawOceanEventBanner(g, game.getActiveEvent(), game.getActiveEventFraction(), time);
        drawSpeaker(g, sound);
        drawCurrent(g, game.getSea().getCurrent(), game.getSea().getStrength());
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

    private void drawDifficultyBadge(Graphics2D g, Difficulty difficulty) {
        int x = 10;
        int y = 64;
        int width = 104;
        Color badgeColor = difficulty == Difficulty.HARD ? UiPalette.DANGER
                : difficulty == Difficulty.MEDIUM ? UiPalette.GOLD : UiPalette.ACCENT;
        g.setColor(UiPalette.PANEL);
        g.fillRoundRect(x, y, width, 26, 10, 10);
        g.setColor(badgeColor);
        g.drawRoundRect(x, y, width, 26, 10, 10);
        g.setFont(Fonts.LABEL);
        String label = difficulty.getDisplayName().toUpperCase();
        if (difficulty.getScoreMultiplier() > 1.0) {
            label = label + " " + difficulty.getMultiplierLabel();
        }
        TextDraw.centered(g, label, x + width / 2, y + 18);
    }

    private void drawComboBadge(Graphics2D g, int combo, double time) {
        if (combo < 2) {
            return;
        }
        int x = 122;
        int y = 64;
        int width = 132;
        boolean flow = combo >= GameConstants.HOT_STREAK_MIN;
        g.setColor(UiPalette.PANEL);
        g.fillRoundRect(x, y, width, 26, 10, 10);
        Color accent = flow ? UiPalette.ACCENT : UiPalette.GOLD;
        if (flow) {
            float pulse = (float) (0.65 + 0.35 * Math.sin(time * 7));
            g.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), (int) (255 * pulse)));
        } else {
            g.setColor(accent);
        }
        g.drawRoundRect(x, y, width, 26, 10, 10);
        g.setFont(Fonts.LABEL);
        String label = flow
                ? String.format(StoryText.HUD_FLOW, combo)
                : String.format(StoryText.HUD_COMBO, combo);
        TextDraw.centered(g, label, x + width / 2, y + 18);
    }

    private void drawOceanEventBanner(Graphics2D g, OceanEventType event, double fraction, double time) {
        if (event == OceanEventType.NONE || fraction <= 0) {
            return;
        }
        int x = 264;
        int y = 64;
        int width = 356;
        int height = 28;
        Color accent = (event == OceanEventType.SQUALL || event == OceanEventType.FISH_FRENZY)
                ? UiPalette.DANGER : UiPalette.ACCENT;
        g.setColor(UiPalette.PANEL_SOLID);
        g.fillRoundRect(x, y, width, height, 10, 10);
        float pulse = (float) (0.7 + 0.3 * Math.sin(time * 6));
        g.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), (int) (255 * pulse)));
        g.drawRoundRect(x, y, width, height, 10, 10);

        g.setFont(Fonts.LABEL);
        g.setColor(UiPalette.TEXT);
        TextDraw.centered(g, event.getAnnouncement(), x + width / 2, y + 17);

        int barX = x + 12;
        int barY = y + 22;
        int barWidth = width - 24;
        g.setColor(UiPalette.BAR_TRACK);
        g.fillRoundRect(barX, barY, barWidth, 3, 2, 2);
        g.setColor(accent);
        g.fillRoundRect(barX, barY, (int) Math.round(barWidth * fraction), 3, 2, 2);
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

    /**
     * Which way the current flows and how hard, next to the speaker: up to
     * three arrowheads pointing with the flow. Trash and fish drift that way.
     *
     * @param current  the current right now, in pixels per second; positive flows right
     * @param strength the fastest it gets in this level
     */
    private void drawCurrent(Graphics2D g, double current, double strength) {
        int x = 632;
        int y = 64;
        g.setColor(UiPalette.PANEL);
        g.fillRoundRect(x, y, 118, 26, 10, 10);
        g.setFont(Fonts.SMALL);
        if (strength <= 0) {
            g.setColor(UiPalette.DIM_TEXT);
            TextDraw.centered(g, StoryText.HUD_CALM, x + 59, y + 18);
            return;
        }
        g.setColor(UiPalette.DIM_TEXT);
        g.drawString(StoryText.HUD_CURRENT, x + 8, y + 18);

        // One arrowhead for a weak current, three for the strongest this level has.
        int heads = (int) Math.ceil(Math.abs(current) / strength * 3 - CURRENT_SLACK);
        int direction = (current < 0) ? -1 : 1;
        int middle = x + 92;
        g.setColor(UiPalette.ACCENT);
        g.setStroke(CURRENT_STROKE);
        for (int i = 0; i < heads; i++) {
            int tip = middle + direction * (i * 8 - (heads - 1) * 4 + 3);
            g.drawLine(tip - direction * 5, y + 8, tip, y + 13);
            g.drawLine(tip, y + 13, tip - direction * 5, y + 18);
        }
        g.setStroke(THIN);
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

    private void drawQuotaBar(Graphics2D g, GameView game, double shownScore) {
        double filled = Mathx.clamp(shownScore / game.getQuota(), 0, 1);

        g.setColor(UiPalette.BAR_TRACK);
        g.fillRoundRect(BAR_X, BAR_Y, BAR_WIDTH, BAR_HEIGHT, 8, 8);
        g.setColor(game.getScore() >= game.getQuota() ? UiPalette.GOLD : UiPalette.ACCENT);
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
        if (secondsLeft <= GameConstants.LOW_TIME_S && game.getStatus() == LevelStatus.RUNNING) {
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
