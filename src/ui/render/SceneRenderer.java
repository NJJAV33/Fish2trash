package ui.render;

import game.GameView;
import java.awt.Graphics2D;
import model.Boat;
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

    private final BackgroundPainter background = new BackgroundPainter();
    private final WaterPainter water = new WaterPainter();
    private final ParticleSystem bubbles = new ParticleSystem();
    private double time;   // seconds of animation so far
    private final PainterRegistry painters = new PainterRegistry();
    private final BoatPainter boatPainter = new BoatPainter();
    private final HookPainter hookPainter = new HookPainter(painters);

    /**
     * Moves the animation forward. Called every frame, also on menus and
     * while the game is paused, so the water never freezes.
     */
    public void update(double dt) {
        time += dt;
        bubbles.update(dt);
    }

    /** The painters for objects and tools, for screens that draw one on its own. */
    public PainterRegistry getPainters() {
        return painters;
    }

    /** Seconds of animation so far. */
    public double getTime() {
        return time;
    }

    public void paint(Graphics2D g, GameView game) {
        ChapterPalette palette = ChapterPalette.forChapter(game.getChapter().getNumber());
        background.paint(g, palette);
        water.paintBackWaves(g, palette, time);

        Boat boat = game.getBoat();
        boatPainter.paint(g, boat.getX(), boat.getY(), boat.getFacing(), time);
        hookPainter.paintRope(g, boat.getHook(), time);
        for (ObjectView object : game.getObjects()) {
            painters.paint(g, object, time);   // the registry picks the painter for this kind
        }
        hookPainter.paintHead(g, boat.getHook(), time);

        paintWaterOnTop(g, palette);
    }

    /** A backdrop with the boat idling at the given place. The menu uses it. */
    public void paintBackdropWithBoat(Graphics2D g, ChapterPalette palette, double boatX) {
        background.paint(g, palette);
        water.paintBackWaves(g, palette, time);
        boatPainter.paint(g, boatX, GameConstants.WATER_Y, 1, time);
        paintWaterOnTop(g, palette);
    }

    /** The scene without boat or objects. Menus and story screens use it as a backdrop. */
    public void paintBackdrop(Graphics2D g, ChapterPalette palette) {
        background.paint(g, palette);
        water.paintBackWaves(g, palette, time);
        paintWaterOnTop(g, palette);
    }

    /** A backdrop that fades from one look to another. amount: 0 = from, 1 = to. */
    public void paintBackdropBlend(Graphics2D g, ChapterPalette from, ChapterPalette to, double amount) {
        background.paintBlend(g, from, to, amount);
        water.paintBackWaves(g, to, time);
        paintWaterOnTop(g, to);
    }

    /** What lies in front of the boat and the objects: near waves, sunbeams and bubbles. */
    private void paintWaterOnTop(Graphics2D g, ChapterPalette palette) {
        water.paintFrontWaves(g, palette, time);
        water.paintRays(g, palette, time);
        bubbles.paint(g);
    }
}
