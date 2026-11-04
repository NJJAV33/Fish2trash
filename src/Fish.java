import java.awt.Color;
import java.awt.Graphics2D;

/**
 * A fish. Catching one hurts the ocean, so the player loses health.
 *
 * INHERITANCE: Fish "is an" OceanObject, just like OceanTrash, but it
 * overrides onCaught() and draw() with its own behavior.
 */
public class Fish extends OceanObject {

    public static final int DAMAGE = 1;

    public Fish(double x, double y, double speedX) {
        super(x, y, 16, speedX);
    }

    @Override
    public void onCaught(Player player) {
        player.loseHealth(DAMAGE);
    }

    @Override
    public void draw(Graphics2D g) {
        int cx = (int) x;
        int cy = (int) y;
        int facing = (speedX >= 0) ? 1 : -1;   // 1 = facing right, -1 = facing left

        g.setColor(new Color(255, 145, 40));
        g.fillOval(cx - 18, cy - 11, 36, 22);          // body

        int tailBase = cx - facing * 15;                // tail sits behind the body
        int tailEnd = cx - facing * 30;
        g.fillPolygon(new int[] {tailBase, tailEnd, tailEnd},
                      new int[] {cy, cy - 10, cy + 10}, 3);

        int eyeX = cx + facing * 9;
        g.setColor(Color.WHITE);
        g.fillOval(eyeX - 4, cy - 7, 8, 8);
        g.setColor(Color.BLACK);
        g.fillOval(eyeX - 2, cy - 5, 4, 4);
    }
}
