package ui.render;

import java.awt.Color;
import java.awt.Graphics2D;
import model.ObjectView;

/** Draws a small orange fish that faces the way it swims. */
public final class SmallFishPainter implements ObjectPainter {

    private static final Color BODY = new Color(255, 145, 40);

    @Override
    public void paint(Graphics2D g, ObjectView object) {
        int cx = (int) object.getX();
        int cy = (int) object.getY();
        int facing = object.getFacing();

        g.setColor(BODY);
        g.fillOval(cx - 18, cy - 11, 36, 22);

        int tailBase = cx - facing * 15;   // the tail sits behind the body
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
