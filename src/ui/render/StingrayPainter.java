package ui.render;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import model.ObjectView;

/** Plays the Blender-rendered swim cycle and draws the live Magnet warning over it. */
public final class StingrayPainter implements ObjectPainter {

    private static final int FRAMES = 12;
    private static final int WIDTH = 88;
    private static final int HEIGHT = 56;
    // The tail occupies the left of each frame. The disc is centred on the collision circle.
    private static final int DISC_X = 56;
    private static final int DISC_Y = 30;
    private static final double SWIM_RADIANS_PER_SECOND = 2.4;
    private static final BufferedImage SWIM = loadSwimSheet();

    private static final Color SPARK = new Color(150, 220, 255);
    private static final Color SPARK_CORE = Color.WHITE;
    private static final Stroke SPARK_STROKE = new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    private static final double SPARK_FLICKERS_PER_SECOND = 14;
    private static final int SPARKS = 4;
    private static final int[] SPARK_X = {0, 5, -3, 4, 0};
    private static final int[] SPARK_Y = {-14, -21, -27, -34, -41};

    private static BufferedImage loadSwimSheet() {
        try (InputStream input = StingrayPainter.class.getResourceAsStream("stingray.png")) {
            if (input == null) {
                throw new IllegalStateException("Missing stingray.png from the game build");
            }
            BufferedImage sheet = ImageIO.read(input);
            if (sheet == null || sheet.getWidth() * HEIGHT != FRAMES * WIDTH * sheet.getHeight()) {
                throw new IllegalStateException("Expected a single row of 12 stingray swim frames");
            }
            return sheet;
        } catch (IOException e) {
            throw new IllegalStateException("Could not read stingray.png", e);
        }
    }

    @Override
    public void paint(Graphics2D g, ObjectView object, double time) {
        double phase = time * SWIM_RADIANS_PER_SECOND + object.getPhase();
        int frame = Math.floorMod((int) Math.floor(phase * FRAMES / (2 * Math.PI)), FRAMES);
        int frameWidth = SWIM.getWidth() / FRAMES;
        int start = frame * frameWidth;

        Graphics2D g2 = (Graphics2D) g.create();
        g2.translate(object.getX(), object.getY());
        g2.scale(object.getFacing(), 1);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(SWIM, -DISC_X, -DISC_Y, WIDTH - DISC_X, HEIGHT - DISC_Y,
                start, 0, start + frameWidth, SWIM.getHeight(), null);
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
