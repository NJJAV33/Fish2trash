package ui.render;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Paint;
import util.GameConstants;

/**
 * Draws the clean-up boat M/B Tala: hull, cabin, a mast with a pennant and a
 * crew member in a yellow slicker. The boat bobs and rolls on the waves.
 */
public final class BoatPainter {

    private static final int DECK_Y = -GameConstants.BOAT_HEIGHT;   // y of the deck; the waterline is y = 0

    // Lit from above: the hull, the cabin and the windows are brighter at the top.
    private static final Paint HULL = new GradientPaint(0, DECK_Y, new Color(200, 78, 58), 0, 4, new Color(128, 40, 30));
    private static final Color HULL_OUTLINE = new Color(110, 35, 25);
    private static final Color HULL_STRIPE = new Color(245, 240, 230);
    private static final Paint CABIN = new GradientPaint(
            0, DECK_Y - 22, new Color(250, 250, 246), 0, DECK_Y + 2, new Color(205, 208, 204));
    private static final Color CABIN_ROOF = new Color(110, 35, 25);
    private static final Paint WINDOW = new GradientPaint(
            0, DECK_Y - 16, new Color(160, 210, 240), 0, DECK_Y - 6, new Color(80, 135, 180));
    private static final Color MAST = new Color(70, 60, 55);
    private static final Color PENNANT = new Color(245, 200, 50);
    private static final Color SLICKER = new Color(245, 200, 50);
    private static final Color SKIN = new Color(214, 160, 120);
    private static final BasicStroke THIN = new BasicStroke(1);
    private static final BasicStroke MAST_STROKE = new BasicStroke(2);

    private static final double BOB_PIXELS = 2.5;
    private static final double ROLL_RADIANS = 0.025;
    private static final double BOB_SPEED = 1.6;

    private static final int HALF = GameConstants.BOAT_WIDTH / 2;
    private static final int DECK = DECK_Y;

    // The hull: a trapezoid that sits slightly in the water.
    private static final int[] HULL_X = {-HALF, HALF, HALF - 18, -HALF + 18};
    private static final int[] HULL_Y = {DECK, DECK, 4, 4};

    // The pennant's points are rewritten every frame, in arrays made once.
    private final int[] pennantX = new int[3];
    private final int[] pennantY = new int[3];

    /**
     * @param x         the middle of the boat
     * @param waterline the y where the hull meets the water
     * @param facing    1 = bow to the right, -1 = bow to the left
     * @param time      seconds of animation so far
     */
    public void paint(Graphics2D g, double x, double waterline, int facing, double time) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.translate(x, waterline + BOB_PIXELS * Math.sin(BOB_SPEED * time));
        g2.rotate(ROLL_RADIANS * Math.sin(BOB_SPEED * time + 1.2));
        g2.scale(facing, 1);   // drawn with the bow to the right

        // Mast and pennant. The free corner of the pennant flutters.
        g2.setColor(MAST);
        g2.setStroke(MAST_STROKE);
        g2.drawLine(-5, DECK - 22, -5, DECK - 40);
        int flutter = (int) (3 * Math.sin(8 * time));
        pennantX[0] = -5;
        pennantY[0] = DECK - 40;
        pennantX[1] = -23;
        pennantY[1] = DECK - 35 + flutter;
        pennantX[2] = -5;
        pennantY[2] = DECK - 30;
        g2.setColor(PENNANT);
        g2.fillPolygon(pennantX, pennantY, 3);

        // Cabin
        g2.setPaint(CABIN);
        g2.fillRect(-25, DECK - 22, 40, 24);
        g2.setColor(CABIN_ROOF);
        g2.fillRect(-28, DECK - 25, 46, 4);
        g2.setPaint(WINDOW);
        g2.fillRect(-18, DECK - 16, 12, 10);
        g2.fillRect(-2, DECK - 16, 12, 10);

        // Crew member at the bow, in a yellow slicker
        g2.setColor(SLICKER);
        g2.fillRoundRect(30, DECK - 17, 11, 18, 5, 5);
        g2.setColor(SKIN);
        g2.fillOval(31, DECK - 26, 9, 9);
        g2.setColor(SLICKER);
        g2.fillArc(30, DECK - 28, 11, 9, 0, 180);   // hood

        // Hull
        g2.setPaint(HULL);
        g2.fillPolygon(HULL_X, HULL_Y, 4);
        g2.setColor(HULL_STRIPE);
        g2.fillRect(-HALF + 4, DECK + 5, GameConstants.BOAT_WIDTH - 8, 3);
        g2.setColor(HULL_OUTLINE);
        g2.setStroke(THIN);
        g2.drawPolygon(HULL_X, HULL_Y, 4);

        g2.dispose();
    }
}
