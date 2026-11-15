package ui.render;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import model.ObjectView;

/**
 * Draws a stingray: a flat diamond with a whip tail, flapping slowly. While
 * it feels the Magnet, jagged blue sparks flicker around it as a warning.
 */
public final class StingrayPainter implements ObjectPainter {

    // Sand-coloured on top, so it blends with the sea floor until it moves.
    private static final Paint BODY_SHADED = new GradientPaint(0, -10, new Color(176, 150, 112), 0, 10, new Color(112, 92, 70));
    private static final Color EDGE = new Color(78, 62, 48);
    private static final Color SPOT = new Color(214, 196, 160);
    private static final Color EYE = new Color(20, 20, 26);
    private static final Color SPARK = new Color(150, 220, 255);
    private static final Color SPARK_CORE = Color.WHITE;
    private static final Stroke TAIL_STROKE = new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    private static final Stroke SPARK_STROKE = new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    private static final double FLAP = 0.18;              // how much the wings rise and fall
    private static final double SPARK_FLICKERS_PER_SECOND = 14;
    private static final int SPARKS = 4;

    private static final int[] BODY_X = {-22, -2, 26, -2};
    private static final int[] BODY_Y = {0, -11, 0, 11};
    // One jagged spark, pointing up from the middle of the ray; it is turned to make the others.
    private static final int[] SPARK_X = {0, 5, -3, 4, 0};
    private static final int[] SPARK_Y = {-14, -21, -27, -34, -41};

    @Override
    public void paint(Graphics2D g, ObjectView object, double time) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.translate(object.getX(), object.getY());
        g2.scale(object.getFacing(), 1);

        // Whip tail, trailing behind and swaying a little.
        int tailTipY = (int) Math.round(5 * Math.sin(3 * time + object.getPhase()));
        g2.setColor(EDGE);
        g2.setStroke(TAIL_STROKE);
        g2.drawLine(-20, 0, -46, tailTipY);

        // The body is squeezed and stretched up and down to flap the wings.
        Graphics2D body = (Graphics2D) g2.create();
        body.scale(1, 1 + FLAP * Math.sin(2.4 * time + object.getPhase()));
        body.setPaint(BODY_SHADED);
        body.fillPolygon(BODY_X, BODY_Y, 4);
        body.setColor(EDGE);
        body.drawPolygon(BODY_X, BODY_Y, 4);
        body.setColor(SPOT);
        body.fillOval(-6, -5, 4, 4);
        body.fillOval(3, 2, 4, 4);
        body.fillOval(-10, 2, 3, 3);
        body.setColor(EYE);
        body.fillOval(13, -4, 3, 3);
        body.fillOval(13, 1, 3, 3);
        body.dispose();

        if (object.getAlert() > 0) {
            paintSparks(g2, time);
        }
        g2.dispose();
    }

    /** Jagged sparks around the ray; a different set lights up on every flicker. */
    private void paintSparks(Graphics2D g2, double time) {
        int flicker = (int) (time * SPARK_FLICKERS_PER_SECOND);
        g2.setStroke(SPARK_STROKE);
        for (int i = 0; i < SPARKS; i++) {
            if ((flicker + i) % 2 != 0) {
                continue;   // this spark is dark right now
            }
            Graphics2D spark = (Graphics2D) g2.create();
            spark.rotate(Math.toRadians(-60 + i * 40));
            spark.setColor(SPARK);
            spark.drawPolyline(SPARK_X, SPARK_Y, SPARK_X.length);
            spark.setColor(SPARK_CORE);
            spark.drawLine(SPARK_X[0], SPARK_Y[0], SPARK_X[1], SPARK_Y[1]);
            spark.dispose();
        }
    }
}
