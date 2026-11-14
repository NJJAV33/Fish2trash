package ui.render;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.AffineTransform;
import model.ObjectView;

/** Draws a long, thin barracuda. It flashes white while it warns and charges. */
public final class BarracudaPainter implements ObjectPainter {

    // The silver side: bright where the light falls on it, darker underneath.
    private static final Paint BODY_SHADED = new GradientPaint(0, -5, new Color(228, 236, 242), 0, 8, new Color(140, 156, 170));
    private static final Color BACK = new Color(84, 98, 110);
    private static final Color FLASH = new Color(255, 245, 235);
    private static final Color FLASH_BACK = new Color(255, 120, 100);
    private static final Color EYE = new Color(255, 60, 50);
    private static final Color TEETH = Color.WHITE;
    private static final double WAG = 0.3;
    private static final double BLINKS_PER_SECOND = 8;

    private static final int[] TAIL_X = {-30, -46, -40};
    private static final int[] TAIL_UP_Y = {0, -11, 0};
    private static final int[] TAIL_DOWN_Y = {0, 11, 0};
    private static final int[] JAW_X = {22, 38, 22};
    private static final int[] JAW_Y = {1, 4, 7};
    private static final int[] TOOTH_1_X = {25, 28, 31};
    private static final int[] TOOTH_1_Y = {2, 5, 3};
    private static final int[] TOOTH_2_X = {30, 33, 36};
    private static final int[] TOOTH_2_Y = {3, 6, 4};

    @Override
    public void paint(Graphics2D g, ObjectView object, double time) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.translate(object.getX(), object.getY());
        g2.scale(object.getFacing(), 1);

        // While alarmed it blinks between its warning colours and its normal ones.
        boolean lit = object.getAlert() > 0 && (int) (time * BLINKS_PER_SECOND * 2) % 2 == 0;
        Color back = lit ? FLASH_BACK : BACK;

        AffineTransform beforeTail = g2.getTransform();
        g2.rotate(WAG * Math.sin(16 * time + object.getPhase()), -30, 0);
        g2.setColor(back);
        g2.fillPolygon(TAIL_X, TAIL_UP_Y, 3);
        g2.fillPolygon(TAIL_X, TAIL_DOWN_Y, 3);
        g2.setTransform(beforeTail);

        g2.fillOval(-33, -8, 66, 14);   // dark back
        if (lit) {
            g2.setColor(FLASH);
        } else {
            g2.setPaint(BODY_SHADED);
        }
        g2.fillOval(-33, -5, 66, 13);   // silver side

        // Open jaw with teeth
        g2.setColor(back);
        g2.fillPolygon(JAW_X, JAW_Y, 3);
        g2.setColor(TEETH);
        g2.fillPolygon(TOOTH_1_X, TOOTH_1_Y, 3);
        g2.fillPolygon(TOOTH_2_X, TOOTH_2_Y, 3);

        g2.setColor(EYE);
        g2.fillOval(18, -5, 6, 6);

        g2.dispose();
    }
}
