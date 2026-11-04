import java.awt.Color;
import java.awt.Graphics2D;

/**
 * A piece of trash (a soda can). Catching it cleans the ocean and earns points.
 *
 * INHERITANCE: OceanTrash "is an" OceanObject, so it gets position, movement
 * and collision for free and only adds what makes trash different.
 */
public class OceanTrash extends OceanObject {

    public static final int POINTS = 10;

    public OceanTrash(double x, double y, double speedX) {
        super(x, y, 14, speedX);
    }

    @Override
    public void onCaught(Player player) {
        player.addPoints(POINTS);
    }

    @Override
    public void draw(Graphics2D g) {
        int left = (int) (x - 10);
        int top = (int) (y - 14);

        g.setColor(new Color(175, 180, 186));      // metal can
        g.fillRoundRect(left, top, 20, 28, 6, 6);
        g.setColor(new Color(205, 60, 60));        // label
        g.fillRect(left, top + 9, 20, 10);
        g.setColor(new Color(90, 95, 100));        // outline
        g.drawRoundRect(left, top, 20, 28, 6, 6);
    }
}
