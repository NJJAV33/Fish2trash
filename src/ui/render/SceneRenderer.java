package ui.render;

import game.GameView;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import model.Boat;
import model.Hook;
import model.ObjectView;
import util.GameConstants;

/**
 * Draws the game world: the background of the chapter, the boat, the ocean
 * objects and the hook.
 *
 * It only reads the game. All drawing lives here in the ui package, so the
 * model classes stay free of any graphics code.
 */
public final class SceneRenderer {

    private static final Color CABIN = new Color(240, 240, 235);
    private static final Color WINDOW = new Color(110, 170, 210);
    private static final Color HULL = new Color(170, 60, 45);
    private static final Color HULL_OUTLINE = new Color(110, 35, 25);
    private static final Color ROPE = new Color(40, 40, 40);
    private static final Color HOOK_METAL = new Color(200, 200, 205);

    private static final BasicStroke THIN = new BasicStroke(1);
    private static final BasicStroke ROPE_STROKE = new BasicStroke(2);
    private static final BasicStroke HOOK_STROKE = new BasicStroke(3);

    private final BackgroundPainter background = new BackgroundPainter();
    private final PainterRegistry painters = new PainterRegistry();

    public void paint(Graphics2D g, GameView game) {
        background.paint(g, ChapterPalette.forChapter(game.getChapter().getNumber()));
        drawBoat(g, game.getBoat());
        for (ObjectView object : game.getObjects()) {
            painters.paint(g, object);   // the registry picks the painter for this kind
        }
        drawHook(g, game.getBoat().getHook());
    }

    /** The scene without boat or objects. Menus and story screens use it as a backdrop. */
    public void paintBackdrop(Graphics2D g, ChapterPalette palette) {
        background.paint(g, palette);
    }

    /** A backdrop that fades from one look to another. amount: 0 = from, 1 = to. */
    public void paintBackdropBlend(Graphics2D g, ChapterPalette from, ChapterPalette to, double amount) {
        background.paintBlend(g, from, to, amount);
    }

    private void drawBoat(Graphics2D g, Boat boat) {
        int cx = (int) boat.getX();
        int waterline = (int) boat.getY();
        int half = GameConstants.BOAT_WIDTH / 2;
        int deck = waterline - GameConstants.BOAT_HEIGHT;

        g.setColor(CABIN);
        g.fillRect(cx - 25, deck - 22, 40, 24);
        g.setColor(WINDOW);
        g.fillRect(cx - 18, deck - 16, 12, 10);

        // Hull: a trapezoid that sits slightly in the water
        int[] hullX = {cx - half, cx + half, cx + half - 18, cx - half + 18};
        int[] hullY = {deck, deck, waterline + 4, waterline + 4};
        g.setColor(HULL);
        g.fillPolygon(hullX, hullY, 4);
        g.setColor(HULL_OUTLINE);
        g.drawPolygon(hullX, hullY, 4);
    }

    private void drawHook(Graphics2D g, Hook hook) {
        int ax = (int) hook.getAnchorX();
        int ay = (int) hook.getAnchorY();
        int tx = (int) hook.getTipX();
        int ty = (int) hook.getTipY();

        g.setColor(ROPE);
        g.setStroke(ROPE_STROKE);
        g.drawLine(ax, ay, tx, ty);

        g.setColor(HOOK_METAL);
        g.setStroke(HOOK_STROKE);
        g.drawArc(tx - 7, ty - 3, 14, 14, 180, 200);   // hook curve
        g.fillOval(tx - 3, ty - 3, 6, 6);
        g.setStroke(THIN);
    }
}
