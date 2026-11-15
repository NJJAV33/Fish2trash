package ui.render;

import game.GameView;
import java.awt.Graphics2D;
import model.Boat;
import model.LevelData;
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
        paint(g, game, 0);
    }

    /**
     * @param catchSquash how much wider than normal the caught object is
     *                    drawn (0 = as it is); see EffectsSystem.getCatchSquash()
     */
    public void paint(Graphics2D g, GameView game, double catchSquash) {
        // The sea clears up over the five levels of a chapter, a little with every catch.
        LevelData level = game.getLevelData();
        double levelsDone = level.getLevel() - 1 + (double) game.getScore() / game.getQuota();
        ChapterPalette palette = ChapterPalette.forChapter(level.getChapter())
                .cleanedBy(levelsDone / LevelData.LEVELS_PER_CHAPTER);
        background.paint(g, palette);
        water.paintBackWaves(g, palette, time);

        Boat boat = game.getBoat();
        boatPainter.paint(g, boat.getX(), boat.getY(), boat.getFacing(), time);
        hookPainter.paintRope(g, boat.getHook(), time);
        for (ObjectView object : game.getObjects()) {
            if (catchSquash != 0 && object.isCaught()) {
                paintSquashed(g, object, catchSquash);
            } else {
                painters.paint(g, object, time);   // the registry picks the painter for this kind
            }
        }
        hookPainter.paintHead(g, boat.getHook(), time);

        paintWaterOnTop(g, palette);
    }

    /** Draws an object stretched sideways and flattened by the same factor, around its own middle. */
    private void paintSquashed(Graphics2D g, ObjectView object, double squash) {
        double wider = 1 + squash;
        Graphics2D stretched = (Graphics2D) g.create();
        stretched.translate(object.getX(), object.getY());
        stretched.scale(wider, 1 / wider);   // what it gains in width it loses in height
        stretched.translate(-object.getX(), -object.getY());
        painters.paint(stretched, object, time);
        stretched.dispose();
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

    /** What lies in front of the boat and the objects: near waves, sunbeams and bubbles. */
    private void paintWaterOnTop(Graphics2D g, ChapterPalette palette) {
        water.paintFrontWaves(g, palette, time);
        water.paintRays(g, palette, time);
        bubbles.paint(g);
    }
}
