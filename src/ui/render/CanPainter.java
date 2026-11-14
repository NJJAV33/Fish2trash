package ui.render;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.Paint;
import model.ObjectView;

/** Draws a soda can: a gray body with a red label, tipping slowly from side to side. */
public final class CanPainter implements ObjectPainter {

    // A can is a cylinder: dark at both edges, with a bright streak where the light hits it.
    private static final float[] ROUND = {0f, 0.3f, 0.65f, 1f};
    private static final Paint METAL = new LinearGradientPaint(-10, 0, 10, 0, ROUND, new Color[] {
        new Color(120, 126, 134), new Color(232, 235, 238), new Color(172, 178, 185), new Color(110, 116, 124)});
    private static final Paint LABEL = new LinearGradientPaint(-10, 0, 10, 0, ROUND, new Color[] {
        new Color(150, 35, 35), new Color(240, 100, 95), new Color(200, 55, 55), new Color(135, 30, 30)});
    private static final Color RIM = new Color(208, 211, 216);
    private static final Color OUTLINE = new Color(90, 95, 100);
    private static final double TIP = 0.14;   // how far it tips, in radians

    @Override
    public void paint(Graphics2D g, ObjectView object, double time) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.translate(object.getX(), object.getY());
        g2.rotate(TIP * Math.sin(1.6 * time + object.getPhase()));

        g2.setPaint(METAL);
        g2.fillRoundRect(-10, -14, 20, 28, 6, 6);
        g2.setPaint(LABEL);
        g2.fillRect(-10, -5, 20, 10);
        g2.setColor(RIM);
        g2.fillOval(-9, -15, 18, 5);   // the top of the can
        g2.setColor(OUTLINE);
        g2.drawRoundRect(-10, -14, 20, 28, 6, 6);

        g2.dispose();
    }
}
