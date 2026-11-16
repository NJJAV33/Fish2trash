package ui.render;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.RadialGradientPaint;
import java.awt.Shape;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.QuadCurve2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Draws the small portrait beside a story page, built from shapes: a lit
 * backdrop, shoulders, a shaded face with eyes, brows and a mouth, and the
 * hair, hat or gear that makes each crew member recognisable.
 *
 * Which portrait to show is read from the page itself: the first line that
 * starts with a speaker's name, like "Dolor: ...".
 */
public final class PortraitPainter {

    /** Everyone who can appear in a portrait, with the name written under it. */
    private enum Speaker {
        NARRATOR("M/B Tala"),
        NICO("Nico"),
        DOLOR("Capt. Dolor"),
        SANTOS("Dr. Santos"),
        MIGO("Migo"),
        REEF("The Reef");

        private final String label;

        Speaker(String label) {
            this.label = label;
        }
    }

    /** The tones of one skin colour: the colour itself, a lit and a shaded tone, and the outline. */
    private static final class Skin {
        private final Color fill;
        private final Color shade;
        private final Color line;
        private final Paint face;   // lit from the upper left

        Skin(Color fill) {
            this.fill = fill;
            this.shade = mix(fill, Color.BLACK, 0.22);
            this.line = mix(fill, Color.BLACK, 0.55);
            this.face = new GradientPaint(44, 30, mix(fill, Color.WHITE, 0.16), 78, 76, fill);
        }
    }

    // The figure is drawn in a 120 x 98 area above the name plate.
    private static final int WIDTH = 120;
    private static final int HEIGHT = 98;

    private static final Color FRAME = new Color(20, 60, 85);
    private static final Color FRAME_EDGE = new Color(255, 255, 255, 90);
    private static final Color GLOW = new Color(255, 255, 255, 50);
    private static final Color CLEAR = new Color(255, 255, 255, 0);
    private static final float[] GLOW_STOPS = {0f, 1f};
    private static final Color EYE_WHITE = new Color(250, 250, 245);
    private static final Color PUPIL = new Color(20, 16, 14);
    private static final Color MOUTH = new Color(110, 30, 35);
    private static final Color TONGUE = new Color(225, 105, 110);
    private static final Color BLUSH = new Color(240, 110, 110, 60);
    private static final Color WHITE = new Color(246, 248, 250);

    private static final Skin NICO_SKIN = new Skin(new Color(214, 160, 120));
    private static final Skin DOLOR_SKIN = new Skin(new Color(160, 108, 78));
    private static final Skin SANTOS_SKIN = new Skin(new Color(222, 172, 132));
    private static final Skin MIGO_SKIN = new Skin(new Color(172, 116, 82));

    private static final Color DARK_HAIR = new Color(38, 30, 27);
    private static final Color GREY_HAIR = new Color(200, 200, 206);
    private static final Color BROWN_EYES = new Color(96, 62, 36);
    private static final Color DARK_EYES = new Color(58, 40, 28);
    private static final Color SLICKER = new Color(245, 200, 50);
    private static final Color NAVY = new Color(32, 48, 92);
    private static final Color GOLD = new Color(232, 186, 70);
    private static final Color TEAL = new Color(38, 160, 158);
    private static final Color FRAMES = new Color(28, 86, 96);
    private static final Color ORANGE = new Color(240, 130, 50);
    private static final Color BANDANA = new Color(210, 50, 60);
    private static final Color TOWEL_STRIPE = new Color(60, 120, 200);
    private static final Color CORAL_PINK = new Color(255, 111, 145);
    private static final Color CORAL_ORANGE = new Color(255, 150, 100);
    private static final Color SEA_FAN = new Color(186, 92, 200);
    private static final Color SEAGRASS = new Color(70, 170, 100);
    private static final Color SAND = new Color(232, 208, 152);
    private static final Color FISH_YELLOW = new Color(255, 205, 60);
    private static final Color FISH_BLUE = new Color(80, 170, 240);
    private static final Color HULL = new Color(190, 70, 52);

    private static final BasicStroke LINE = new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    private static final BasicStroke FINE = new BasicStroke(1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    private static final BasicStroke BROW = new BasicStroke(2.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    private static final BasicStroke RIM = new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    private static final BasicStroke BRANCH = new BasicStroke(5, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    private static final BasicStroke BLADE = new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);

    // Shapes every person shares. The face is centred at x = 60.
    private static final Shape FACE = face();
    private static final Shape SHOULDERS = shoulders();
    private static final Shape NECK = new RoundRectangle2D.Double(52, 62, 16, 22, 6, 6);
    private static final Shape[] EARS = {new Ellipse2D.Double(34.5, 44, 8, 13), new Ellipse2D.Double(77.5, 44, 8, 13)};
    private static final double[] EYE_X = {50, 70};
    private static final double EYE_Y = 50;

    // The words a line can start with, and who that is. Checked in this order.
    private final Map<String, Speaker> prefixes = new LinkedHashMap<>();

    public PortraitPainter() {
        prefixes.put("Nico:", Speaker.NICO);
        prefixes.put("Dolor:", Speaker.DOLOR);
        prefixes.put("Santos:", Speaker.SANTOS);
        prefixes.put("Migo:", Speaker.MIGO);
        prefixes.put("The reef:", Speaker.REEF);
    }

    /** Draws the portrait for the page into a square of 120 x 120 pixels at (x, y). */
    public void paint(Graphics2D g, String pageText, int x, int y) {
        Speaker speaker = speakerOf(pageText);

        Graphics2D g2 = (Graphics2D) g.create();
        g2.translate(x, y);
        g2.setColor(FRAME);
        g2.fillRoundRect(0, 0, 120, 120, 14, 14);
        g2.clip(new RoundRectangle2D.Double(0, 0, 120, 120, 14, 14));
        g2.clipRect(0, 0, WIDTH, HEIGHT);   // keep the figure above the name plate

        switch (speaker) {
            case NICO:
                drawNico(g2);
                break;
            case DOLOR:
                drawDolor(g2);
                break;
            case SANTOS:
                drawSantos(g2);
                break;
            case MIGO:
                drawMigo(g2);
                break;
            case REEF:
                drawReef(g2);
                break;
            default:
                drawBoat(g2);
                break;
        }
        g2.dispose();

        // Name plate along the bottom of the frame.
        g.setColor(UiPalette.PANEL_SOLID);
        g.fillRoundRect(x, y + 98, 120, 22, 10, 10);
        g.setColor(FRAME_EDGE);
        g.drawRoundRect(x, y, 120, 120, 14, 14);
        g.setColor(UiPalette.TEXT);
        g.setFont(Fonts.LABEL);
        TextDraw.centered(g, speaker.label, x + 60, y + 114);
    }

    private Speaker speakerOf(String pageText) {
        for (String line : pageText.split("\n")) {
            for (Map.Entry<String, Speaker> entry : prefixes.entrySet()) {
                if (line.startsWith(entry.getKey())) {
                    return entry.getValue();
                }
            }
        }
        return Speaker.NARRATOR;
    }

    // ---- The crew -------------------------------------------------------

    /** Nico: the rookie, in a yellow slicker with the hood up, a bit nervous but smiling. */
    private static void drawNico(Graphics2D g) {
        backdrop(g, new Color(70, 146, 180), new Color(24, 70, 100));

        // The hood frames the head; its dark inside shows around the hair.
        Shape hood = new Ellipse2D.Double(27, 17, 66, 72);
        g.setPaint(new GradientPaint(30, 20, mix(SLICKER, Color.WHITE, 0.15), 90, 80, mix(SLICKER, Color.BLACK, 0.15)));
        g.fill(hood);
        outline(g, hood, mix(SLICKER, Color.BLACK, 0.5));
        g.setColor(mix(SLICKER, Color.BLACK, 0.35));
        g.fill(new Ellipse2D.Double(33, 23, 54, 60));

        g.setColor(DARK_HAIR);
        g.fill(new Ellipse2D.Double(37, 24, 46, 40));
        shoulders(g, SLICKER);
        // Reflective stripe across the chest, as on real work gear.
        Graphics2D chest = (Graphics2D) g.create();
        chest.clip(SHOULDERS);
        chest.setColor(new Color(215, 220, 226));
        chest.fill(new Rectangle2D.Double(0, 91, WIDTH, 3));
        chest.dispose();
        Path2D collar = new Path2D.Double();
        collar.moveTo(60, 86);
        collar.lineTo(45, 77);
        collar.lineTo(43, 87);
        collar.closePath();
        collar.moveTo(60, 86);
        collar.lineTo(75, 77);
        collar.lineTo(77, 87);
        collar.closePath();
        g.setColor(mix(SLICKER, Color.WHITE, 0.25));
        g.fill(collar);
        outline(g, collar, mix(SLICKER, Color.BLACK, 0.5));
        g.setColor(mix(SLICKER, Color.BLACK, 0.5));
        g.setStroke(FINE);
        g.draw(new Line2D.Double(60, 86, 60, 98));   // the zip

        head(g, NICO_SKIN);
        // Messy fringe with a few spikes.
        Path2D fringe = new Path2D.Double();
        fringe.moveTo(38, 47);
        fringe.curveTo(35, 30, 46, 21, 60, 21);
        fringe.curveTo(74, 21, 85, 30, 82, 47);
        fringe.lineTo(78, 39);
        fringe.lineTo(74, 43);
        fringe.lineTo(70, 35);
        fringe.lineTo(64, 40);
        fringe.lineTo(60, 33);
        fringe.lineTo(54, 39);
        fringe.lineTo(50, 34);
        fringe.lineTo(45, 41);
        fringe.lineTo(42, 37);
        fringe.closePath();
        hair(g, fringe, DARK_HAIR);

        // Eyebrows raised: keen, and a little nervous.
        brow(g, DARK_HAIR, 45, 43, 50, 39, 55, 42);
        brow(g, DARK_HAIR, 65, 42, 70, 39, 75, 43);
        eyes(g, BROWN_EYES, NICO_SKIN.line);
        nose(g, NICO_SKIN);
        blush(g);
        g.setColor(NICO_SKIN.shade);   // freckles
        for (double[] dot : new double[][] {{44, 56}, {47, 58.5}, {43, 59}, {73, 56}, {76, 58.5}, {77, 55.5}}) {
            g.fill(new Ellipse2D.Double(dot[0], dot[1], 1.6, 1.6));
        }
        openSmile(g, 14, NICO_SKIN.line);
    }

    /** Captain Dolor: grey hair under a white captain's cap, navy jacket with gold trim. */
    private static void drawDolor(Graphics2D g) {
        backdrop(g, new Color(44, 84, 124), new Color(16, 38, 64));

        g.setColor(GREY_HAIR);
        g.fill(new Ellipse2D.Double(35, 28, 50, 44));
        shoulders(g, NAVY);

        // White shirt and dark tie between the lapels.
        Path2D shirt = new Path2D.Double();
        shirt.moveTo(48, 78);
        shirt.lineTo(60, 97);
        shirt.lineTo(72, 78);
        shirt.closePath();
        g.setColor(WHITE);
        g.fill(shirt);
        Path2D tie = new Path2D.Double();
        tie.moveTo(58, 81);
        tie.lineTo(62, 81);
        tie.lineTo(63.5, 98);
        tie.lineTo(56.5, 98);
        tie.closePath();
        g.setColor(new Color(22, 26, 44));
        g.fill(tie);
        Path2D lapels = new Path2D.Double();
        lapels.moveTo(47, 78);
        lapels.lineTo(60, 99);
        lapels.lineTo(50, 99);
        lapels.lineTo(40, 81);
        lapels.closePath();
        lapels.moveTo(73, 78);
        lapels.lineTo(60, 99);
        lapels.lineTo(70, 99);
        lapels.lineTo(80, 81);
        lapels.closePath();
        g.setColor(mix(NAVY, Color.WHITE, 0.12));
        g.fill(lapels);
        outline(g, lapels, mix(NAVY, Color.BLACK, 0.5));
        // Gold stripes on the shoulders and two buttons.
        g.setColor(GOLD);
        g.setStroke(BLADE);
        g.draw(new Line2D.Double(17, 91, 33, 83));
        g.draw(new Line2D.Double(103, 91, 87, 83));
        g.fill(new Ellipse2D.Double(42, 91, 4, 4));
        g.fill(new Ellipse2D.Double(74, 91, 4, 4));

        head(g, DOLOR_SKIN);
        // Grey hair showing at the temples, under the cap.
        Path2D temples = new Path2D.Double();
        temples.moveTo(38, 36);
        temples.quadTo(35, 48, 40, 57);
        temples.lineTo(43, 42);
        temples.closePath();
        temples.moveTo(82, 36);
        temples.quadTo(85, 48, 80, 57);
        temples.lineTo(77, 42);
        temples.closePath();
        hair(g, temples, GREY_HAIR);

        // The captain's cap: white top, navy band, shiny black peak and a gold badge.
        Path2D crown = new Path2D.Double();
        crown.moveTo(35, 37);
        crown.curveTo(28, 24, 40, 13, 60, 13);
        crown.curveTo(80, 13, 92, 24, 85, 37);
        crown.closePath();
        g.setPaint(new GradientPaint(40, 14, Color.WHITE, 80, 38, new Color(208, 212, 220)));
        g.fill(crown);
        outline(g, crown, new Color(120, 126, 140));
        Shape band = new Rectangle2D.Double(36, 32, 48, 6);
        g.setColor(NAVY);
        g.fill(band);
        outline(g, band, mix(NAVY, Color.BLACK, 0.5));
        Path2D peak = new Path2D.Double();
        peak.moveTo(37, 38);
        peak.quadTo(60, 49, 83, 38);
        peak.quadTo(60, 42, 37, 38);
        peak.closePath();
        g.setPaint(new GradientPaint(0, 38, new Color(70, 74, 86), 0, 45, new Color(12, 12, 18)));
        g.fill(peak);
        g.setColor(GOLD);
        g.fill(new Ellipse2D.Double(55, 22, 10, 10));
        g.setColor(NAVY);   // a small anchor on the badge
        g.setStroke(FINE);
        g.draw(new Line2D.Double(60, 23.5, 60, 30.5));
        g.draw(new Arc2D.Double(56.5, 26, 7, 4.5, 180, 180, Arc2D.OPEN));
        g.draw(new Line2D.Double(58, 25, 62, 25));

        // Level grey brows, laughter lines: dry, but warm.
        brow(g, GREY_HAIR, 44, 44, 50, 42.5, 55, 43.5);
        brow(g, GREY_HAIR, 65, 43.5, 70, 42.5, 76, 44);
        eyes(g, DARK_EYES, DOLOR_SKIN.line);
        g.setColor(DOLOR_SKIN.line);
        g.setStroke(FINE);
        for (int side = -1; side <= 1; side += 2) {
            double x = 60 + side * 16;   // outer corner of the eye
            g.draw(new Line2D.Double(x, 49, x + side * 3, 48));
            g.draw(new Line2D.Double(x, 51.5, x + side * 3, 52.5));
        }
        nose(g, DOLOR_SKIN);
        g.setColor(DOLOR_SKIN.shade);
        g.setStroke(LINE);
        g.draw(new QuadCurve2D.Double(51, 59, 49, 63, 52, 67));
        g.draw(new QuadCurve2D.Double(69, 59, 71, 63, 68, 67));
        closedSmile(g, DOLOR_SKIN.line);
    }

    /** Dr. Santos: the marine biologist, hair in a bun, round glasses and a lab coat. */
    private static void drawSantos(Graphics2D g) {
        backdrop(g, new Color(52, 142, 140), new Color(18, 62, 70));

        Shape bun = new Ellipse2D.Double(49, 7, 22, 19);
        hair(g, bun, DARK_HAIR);
        g.setColor(TEAL);
        g.fill(new RoundRectangle2D.Double(53, 22, 14, 4, 3, 3));   // hair tie
        g.setColor(DARK_HAIR);
        g.fill(new Ellipse2D.Double(36, 22, 48, 50));
        shoulders(g, WHITE);

        Path2D blouse = new Path2D.Double();
        blouse.moveTo(49, 78);
        blouse.lineTo(60, 95);
        blouse.lineTo(71, 78);
        blouse.closePath();
        g.setColor(TEAL);
        g.fill(blouse);
        Path2D lapels = new Path2D.Double();
        lapels.moveTo(48, 78);
        lapels.lineTo(60, 99);
        lapels.lineTo(52, 99);
        lapels.lineTo(41, 81);
        lapels.closePath();
        lapels.moveTo(72, 78);
        lapels.lineTo(60, 99);
        lapels.lineTo(68, 99);
        lapels.lineTo(79, 81);
        lapels.closePath();
        g.setColor(new Color(224, 230, 236));
        g.fill(lapels);
        outline(g, lapels, new Color(140, 150, 160));
        g.setColor(new Color(140, 150, 160));   // a pen in the coat pocket
        g.setStroke(FINE);
        g.draw(new Line2D.Double(80, 89, 93, 89));
        g.setColor(new Color(50, 90, 200));
        g.fill(new RoundRectangle2D.Double(84, 83, 3, 9, 2, 2));

        head(g, SANTOS_SKIN);
        // Side-swept fringe.
        Path2D fringe = new Path2D.Double();
        fringe.moveTo(38, 49);
        fringe.curveTo(35, 28, 50, 21, 62, 21);
        fringe.curveTo(76, 21, 85, 31, 82, 47);
        fringe.curveTo(77, 35, 65, 31, 53, 35);
        fringe.curveTo(46, 38, 41, 43, 38, 49);
        fringe.closePath();
        hair(g, fringe, DARK_HAIR);

        // Brows lifted in delight.
        brow(g, DARK_HAIR, 45, 42.5, 50, 38.5, 55, 41.5);
        brow(g, DARK_HAIR, 65, 41.5, 70, 38.5, 75, 42.5);
        eyes(g, BROWN_EYES, SANTOS_SKIN.line);

        // Round glasses.
        Shape[] lenses = {new RoundRectangle2D.Double(42, 44, 16, 12, 7, 7), new RoundRectangle2D.Double(62, 44, 16, 12, 7, 7)};
        g.setStroke(RIM);
        for (Shape lens : lenses) {
            g.setColor(new Color(255, 255, 255, 40));
            g.fill(lens);
            g.setColor(FRAMES);
            g.draw(lens);
        }
        g.draw(new QuadCurve2D.Double(58, 49, 60, 47, 62, 49));
        g.draw(new Line2D.Double(42, 48, 38, 47));
        g.draw(new Line2D.Double(78, 48, 82, 47));

        nose(g, SANTOS_SKIN);
        blush(g);
        openSmile(g, 12, SANTOS_SKIN.line);
    }

    /** Migo: the deckhand, red bandana, towel on the shoulder and a big grin. */
    private static void drawMigo(Graphics2D g) {
        backdrop(g, new Color(88, 152, 164), new Color(28, 80, 96));

        // The bandana's knot ends stick out behind the head.
        Path2D tails = new Path2D.Double();
        tails.moveTo(80, 37);
        tails.lineTo(99, 29);
        tails.lineTo(95, 38);
        tails.lineTo(82, 40);
        tails.closePath();
        tails.moveTo(81, 40);
        tails.lineTo(97, 46);
        tails.lineTo(91, 51);
        tails.lineTo(80, 43);
        tails.closePath();
        g.setColor(mix(BANDANA, Color.BLACK, 0.12));
        g.fill(tails);
        outline(g, tails, mix(BANDANA, Color.BLACK, 0.5));

        g.setColor(DARK_HAIR);
        g.fill(new Ellipse2D.Double(37, 24, 46, 38));
        shoulders(g, ORANGE);
        g.setColor(mix(ORANGE, Color.BLACK, 0.4));   // the T-shirt's neckline
        g.setStroke(LINE);
        g.draw(new QuadCurve2D.Double(48, 79, 60, 89, 72, 79));

        // A towel over the shoulder.
        Path2D towel = new Path2D.Double();
        towel.moveTo(17, 99);
        towel.lineTo(25, 81);
        towel.lineTo(40, 78);
        towel.lineTo(33, 99);
        towel.closePath();
        g.setColor(WHITE);
        g.fill(towel);
        Graphics2D stripes = (Graphics2D) g.create();
        stripes.clip(towel);
        stripes.setColor(TOWEL_STRIPE);
        stripes.setStroke(BLADE);
        stripes.draw(new Line2D.Double(15, 88, 45, 84));
        stripes.draw(new Line2D.Double(15, 95, 45, 91));
        stripes.dispose();
        outline(g, towel, new Color(140, 150, 160));

        head(g, MIGO_SKIN);
        Path2D crop = new Path2D.Double();   // short hair
        crop.moveTo(38, 44);
        crop.curveTo(37, 30, 47, 23, 60, 23);
        crop.curveTo(73, 23, 83, 30, 82, 44);
        crop.lineTo(80, 38);
        crop.quadTo(60, 30, 40, 38);
        crop.closePath();
        hair(g, crop, DARK_HAIR);

        Path2D bandana = new Path2D.Double();
        bandana.moveTo(37, 35);
        bandana.quadTo(60, 24, 83, 35);
        bandana.lineTo(83, 41);
        bandana.quadTo(60, 30, 37, 41);
        bandana.closePath();
        g.setPaint(new GradientPaint(0, 28, mix(BANDANA, Color.WHITE, 0.15), 0, 41, mix(BANDANA, Color.BLACK, 0.15)));
        g.fill(bandana);
        outline(g, bandana, mix(BANDANA, Color.BLACK, 0.5));
        g.setColor(WHITE);   // white dots on the cloth
        for (double[] dot : new double[][] {{44, 35}, {52, 32}, {60, 31}, {68, 32}, {76, 35}}) {
            g.fill(new Ellipse2D.Double(dot[0] - 1.3, dot[1] - 1.3, 2.6, 2.6));
        }
        Shape knot = new Ellipse2D.Double(78, 35, 8, 8);
        g.setColor(BANDANA);
        g.fill(knot);
        outline(g, knot, mix(BANDANA, Color.BLACK, 0.5));

        // One brow up: always joking.
        brow(g, DARK_HAIR, 45, 44, 50, 40.5, 55, 43);
        brow(g, DARK_HAIR, 65, 44.5, 70, 43, 75, 44.5);
        eyes(g, DARK_EYES, MIGO_SKIN.line);
        nose(g, MIGO_SKIN);
        openSmile(g, 19, MIGO_SKIN.line);
    }

    /** The Reef: corals, a sea fan, seagrass and a few fish in clear, lit water. */
    private static void drawReef(Graphics2D g) {
        g.setPaint(new GradientPaint(0, 0, new Color(70, 180, 200), 0, HEIGHT, new Color(20, 92, 124)));
        g.fillRect(0, 0, WIDTH, HEIGHT);
        g.setColor(new Color(255, 255, 255, 30));   // light rays from the surface
        g.fill(polygon(18, 0, 32, 0, 52, 98, 30, 98));
        g.fill(polygon(58, 0, 68, 0, 84, 98, 70, 98));
        g.fill(polygon(92, 0, 100, 0, 112, 98, 102, 98));

        // A sea fan at the back.
        Path2D fan = new Path2D.Double();
        fan.moveTo(24, 86);
        fan.curveTo(2, 70, 4, 40, 22, 34);
        fan.curveTo(40, 40, 44, 70, 24, 86);
        g.setColor(new Color(186, 92, 200, 90));
        g.fill(fan);
        g.setColor(SEA_FAN);
        g.setStroke(FINE);
        for (double[] tip : new double[][] {{8, 54}, {12, 42}, {22, 36}, {32, 42}, {37, 54}}) {
            g.draw(new QuadCurve2D.Double(24, 86, (24 + tip[0]) / 2 + 2, (86 + tip[1]) / 2, tip[0], tip[1]));
        }

        // Sandy floor with ripples.
        Path2D sand = new Path2D.Double();
        sand.moveTo(0, 84);
        sand.quadTo(30, 76, 60, 82);
        sand.quadTo(90, 88, 120, 80);
        sand.lineTo(120, HEIGHT);
        sand.lineTo(0, HEIGHT);
        sand.closePath();
        g.setPaint(new GradientPaint(0, 78, SAND, 0, HEIGHT, mix(SAND, Color.BLACK, 0.2)));
        g.fill(sand);
        g.setColor(mix(SAND, Color.BLACK, 0.25));
        g.draw(new QuadCurve2D.Double(8, 90, 20, 87, 32, 90));
        g.draw(new QuadCurve2D.Double(84, 92, 96, 89, 108, 92));

        // Seagrass on the right.
        g.setColor(SEAGRASS);
        g.setStroke(BLADE);
        g.draw(new QuadCurve2D.Double(102, 86, 98, 70, 104, 56));
        g.draw(new QuadCurve2D.Double(108, 86, 112, 72, 108, 60));
        g.draw(new QuadCurve2D.Double(113, 86, 116, 76, 118, 66));

        // Branching coral.
        Path2D branches = new Path2D.Double();
        branches.moveTo(42, 90);
        branches.quadTo(40, 72, 43, 52);
        branches.moveTo(42, 76);
        branches.quadTo(32, 70, 30, 58);
        branches.moveTo(43, 64);
        branches.quadTo(50, 58, 53, 46);
        branches.moveTo(31, 64);
        branches.lineTo(25, 58);
        g.setColor(CORAL_PINK);
        g.setStroke(BRANCH);
        g.draw(branches);
        g.setColor(new Color(255, 190, 210));
        g.setStroke(FINE);
        g.draw(branches);

        // Brain coral: a dome with winding grooves.
        Shape dome = new Arc2D.Double(60, 70, 36, 30, 0, 180, Arc2D.CHORD);
        g.setPaint(new GradientPaint(62, 70, mix(CORAL_ORANGE, Color.WHITE, 0.2), 94, 86, mix(CORAL_ORANGE, Color.BLACK, 0.15)));
        g.fill(dome);
        Graphics2D grooves = (Graphics2D) g.create();
        grooves.clip(dome);
        grooves.setColor(mix(CORAL_ORANGE, Color.BLACK, 0.3));
        grooves.setStroke(FINE);
        for (int i = 0; i < 4; i++) {
            Path2D groove = new Path2D.Double();
            groove.moveTo(60, 76 + i * 4);
            for (int x = 64; x <= 96; x += 8) {
                groove.quadTo(x - 2, 72 + i * 4, x + 2, 76 + i * 4);
            }
            grooves.draw(groove);
        }
        grooves.dispose();

        // Fish swimming above, and rising bubbles.
        fish(g, 66, 34, 20, FISH_YELLOW);
        fish(g, 92, 22, 11, FISH_BLUE);
        fish(g, 102, 30, 10, FISH_BLUE);
        g.setColor(new Color(255, 255, 255, 170));
        g.setStroke(FINE);
        g.draw(new Ellipse2D.Double(28, 26, 5, 5));
        g.draw(new Ellipse2D.Double(32, 17, 4, 4));
        g.draw(new Ellipse2D.Double(29, 9, 3, 3));
    }

    /** The narrator: the boat M/B Tala at dawn, with the sun coming up behind it. */
    private static void drawBoat(Graphics2D g) {
        g.setPaint(new GradientPaint(0, 0, new Color(108, 158, 210), 0, 62, new Color(255, 204, 150)));
        g.fillRect(0, 0, WIDTH, 62);
        g.setPaint(new RadialGradientPaint(90f, 60f, 30f, GLOW_STOPS,
                new Color[] {new Color(255, 236, 170, 200), new Color(255, 236, 170, 0)}));
        g.fillRect(56, 26, 64, 36);
        g.setColor(new Color(255, 232, 160));
        g.fill(new Ellipse2D.Double(80, 50, 20, 20));
        g.setColor(new Color(60, 90, 110, 160));   // an island far away
        g.fill(new Arc2D.Double(-6, 52, 40, 20, 0, 180, Arc2D.CHORD));

        g.setPaint(new GradientPaint(0, 62, new Color(64, 150, 190), 0, HEIGHT, new Color(24, 78, 118)));
        g.fillRect(0, 62, WIDTH, HEIGHT - 62);
        g.setColor(new Color(255, 230, 160, 150));   // the sun's reflection
        g.setStroke(BLADE);
        g.draw(new Line2D.Double(84, 66, 96, 66));
        g.draw(new Line2D.Double(86, 71, 94, 71));
        g.draw(new Line2D.Double(88, 76, 92, 76));

        // Mast and pennant.
        g.setColor(new Color(70, 60, 55));
        g.setStroke(RIM);
        g.draw(new Line2D.Double(46, 40, 46, 20));
        g.setColor(SLICKER);
        g.fill(polygon(46, 20, 30, 25, 46, 30));

        // Cabin with windows.
        Shape cabin = new Rectangle2D.Double(36, 40, 34, 20);
        g.setPaint(new GradientPaint(0, 40, Color.WHITE, 0, 60, new Color(205, 208, 204)));
        g.fill(cabin);
        outline(g, cabin, new Color(120, 120, 120));
        g.setColor(mix(HULL, Color.BLACK, 0.35));
        g.fill(new Rectangle2D.Double(33, 37, 40, 4));
        g.setPaint(new GradientPaint(0, 45, new Color(170, 215, 240), 0, 53, new Color(80, 135, 180)));
        g.fill(new Rectangle2D.Double(41, 45, 10, 8));
        g.fill(new Rectangle2D.Double(55, 45, 10, 8));

        // Crew member at the bow, in a yellow slicker.
        g.setColor(SLICKER);
        g.fill(new RoundRectangle2D.Double(80, 47, 8, 13, 4, 4));
        g.setColor(NICO_SKIN.fill);
        g.fill(new Ellipse2D.Double(80.5, 41, 7, 7));
        g.setColor(SLICKER);
        g.fill(new Arc2D.Double(80, 39.5, 8, 7, 0, 180, Arc2D.CHORD));

        // Hull with a raised bow, sitting in the water.
        Path2D hull = new Path2D.Double();
        hull.moveTo(14, 59);
        hull.lineTo(104, 56);
        hull.quadTo(96, 72, 86, 76);
        hull.lineTo(28, 76);
        hull.quadTo(18, 70, 14, 59);
        hull.closePath();
        g.setPaint(new GradientPaint(0, 56, mix(HULL, Color.WHITE, 0.1), 0, 76, mix(HULL, Color.BLACK, 0.35)));
        g.fill(hull);
        g.setColor(WHITE);
        g.setStroke(BLADE);
        g.draw(new Line2D.Double(18, 63.5, 100, 61));
        outline(g, hull, mix(HULL, Color.BLACK, 0.5));

        g.setColor(new Color(255, 255, 255, 200));   // foam along the waterline
        g.setStroke(RIM);
        g.draw(new Arc2D.Double(6, 72, 22, 8, 0, 180, Arc2D.OPEN));
        g.draw(new Arc2D.Double(84, 72, 26, 8, 0, 180, Arc2D.OPEN));
        g.draw(new Arc2D.Double(40, 84, 16, 6, 0, 180, Arc2D.OPEN));
    }

    // ---- Shared parts ---------------------------------------------------

    /** A background that darkens toward the bottom, with a soft light behind the head. */
    private static void backdrop(Graphics2D g, Color top, Color bottom) {
        g.setPaint(new GradientPaint(0, 0, top, 0, HEIGHT, bottom));
        g.fillRect(0, 0, WIDTH, HEIGHT);
        g.setPaint(new RadialGradientPaint(60f, 44f, 48f, GLOW_STOPS, new Color[] {GLOW, CLEAR}));
        g.fillRect(0, 0, WIDTH, HEIGHT);
    }

    /** Shoulders in a piece of clothing, lit from the upper left. */
    private static void shoulders(Graphics2D g, Color cloth) {
        g.setPaint(new GradientPaint(30, 78, mix(cloth, Color.WHITE, 0.12), 90, 100, mix(cloth, Color.BLACK, 0.2)));
        g.fill(SHOULDERS);
        outline(g, SHOULDERS, mix(cloth, Color.BLACK, 0.5));
    }

    /** Neck, ears and face. The neck is in shade, under the chin. */
    private static void head(Graphics2D g, Skin skin) {
        g.setColor(skin.shade);
        g.fill(NECK);
        outline(g, NECK, skin.line);
        for (Shape ear : EARS) {
            g.setColor(skin.fill);
            g.fill(ear);
            outline(g, ear, skin.line);
        }
        g.setPaint(skin.face);
        g.fill(FACE);
        outline(g, FACE, skin.line);
    }

    /** Fills a lock of hair with a darker outline and a streak of light on it. */
    private static void hair(Graphics2D g, Shape shape, Color colour) {
        g.setColor(colour);
        g.fill(shape);
        outline(g, shape, mix(colour, Color.BLACK, 0.45));
        Graphics2D shine = (Graphics2D) g.create();
        shine.clip(shape);
        shine.setColor(mix(colour, Color.WHITE, 0.3));
        shine.setStroke(BLADE);
        shine.draw(new Arc2D.Double(42, 22, 30, 24, 100, 50, Arc2D.OPEN));
        shine.dispose();
    }

    /** One eyebrow: a thick stroke from (x1, y1) to (x2, y2), bent toward (cx, cy). */
    private static void brow(Graphics2D g, Color colour, double x1, double y1, double cx, double cy, double x2, double y2) {
        g.setColor(mix(colour, Color.BLACK, 0.15));
        g.setStroke(BROW);
        g.draw(new QuadCurve2D.Double(x1, y1, cx, cy, x2, y2));
    }

    /** Two eyes, each with a white, a coloured iris, a pupil, a glint and an upper lid. */
    private static void eyes(Graphics2D g, Color iris, Color line) {
        for (double cx : EYE_X) {
            Shape white = new Ellipse2D.Double(cx - 5, EYE_Y - 3.5, 10, 7);
            g.setColor(EYE_WHITE);
            g.fill(white);
            Graphics2D inside = (Graphics2D) g.create();
            inside.clip(white);
            inside.setColor(iris);
            inside.fill(new Ellipse2D.Double(cx - 3.2, EYE_Y - 3.4, 6.4, 6.4));
            inside.setColor(PUPIL);
            inside.fill(new Ellipse2D.Double(cx - 1.7, EYE_Y - 1.9, 3.4, 3.4));
            inside.setColor(Color.WHITE);
            inside.fill(new Ellipse2D.Double(cx - 0.3, EYE_Y - 2.6, 1.8, 1.8));
            inside.dispose();
            g.setColor(line);
            g.setStroke(LINE);
            g.draw(new Arc2D.Double(cx - 5.5, EYE_Y - 4, 11, 8, 10, 160, Arc2D.OPEN));
        }
    }

    private static void nose(Graphics2D g, Skin skin) {
        g.setColor(skin.shade);
        g.setStroke(LINE);
        g.draw(new QuadCurve2D.Double(59, 52, 55.5, 59.5, 61, 60.5));
    }

    private static void blush(Graphics2D g) {
        g.setColor(BLUSH);
        g.fill(new Ellipse2D.Double(41, 56, 9, 5));
        g.fill(new Ellipse2D.Double(70, 56, 9, 5));
    }

    /** An open, happy mouth: dark inside, top teeth and a tongue. */
    private static void openSmile(Graphics2D g, double width, Color line) {
        double left = 60 - width / 2;
        double right = 60 + width / 2;
        Path2D mouth = new Path2D.Double();
        mouth.moveTo(left, 63);
        mouth.quadTo(60, 66, right, 63);
        mouth.quadTo(60, 77, left, 63);
        mouth.closePath();
        g.setColor(MOUTH);
        g.fill(mouth);
        Graphics2D inside = (Graphics2D) g.create();
        inside.clip(mouth);
        inside.setColor(Color.WHITE);
        inside.fill(new Rectangle2D.Double(left, 62, width, 4.6));
        inside.setColor(TONGUE);
        inside.fill(new Ellipse2D.Double(60 - width / 4, 67.5, width / 2, 6));
        inside.dispose();
        outline(g, mouth, line);
    }

    /** A closed smile: one curved line. */
    private static void closedSmile(Graphics2D g, Color line) {
        g.setColor(line);
        g.setStroke(LINE);
        g.draw(new QuadCurve2D.Double(53, 64, 60, 69, 67, 64));
    }

    /** A small side-on fish facing right, for the reef picture. */
    private static void fish(Graphics2D g, double x, double y, double length, Color colour) {
        double h = length * 0.55;
        Path2D body = new Path2D.Double();
        body.moveTo(x + length / 2, y);
        body.quadTo(x, y - h, x - length / 2, y);
        body.quadTo(x, y + h, x + length / 2, y);
        body.closePath();
        Path2D tail = polygon(x - length / 2 + 1, y, x - length * 0.8, y - h / 2, x - length * 0.8, y + h / 2);
        g.setColor(mix(colour, Color.BLACK, 0.15));
        g.fill(tail);
        g.setColor(colour);
        g.fill(body);
        outline(g, body, mix(colour, Color.BLACK, 0.5));
        g.setColor(PUPIL);
        g.fill(new Ellipse2D.Double(x + length * 0.22, y - length * 0.1, length * 0.1, length * 0.1));
    }

    private static void outline(Graphics2D g, Shape shape, Color colour) {
        g.setColor(colour);
        g.setStroke(LINE);
        g.draw(shape);
    }

    /** A closed shape through the given x, y pairs. */
    private static Path2D polygon(double... points) {
        Path2D shape = new Path2D.Double();
        shape.moveTo(points[0], points[1]);
        for (int i = 2; i < points.length; i += 2) {
            shape.lineTo(points[i], points[i + 1]);
        }
        shape.closePath();
        return shape;
    }

    /** Mixes colour a toward colour b: 0 keeps a, 1 gives b. */
    private static Color mix(Color a, Color b, double amount) {
        return new Color(
                (int) Math.round(a.getRed() + (b.getRed() - a.getRed()) * amount),
                (int) Math.round(a.getGreen() + (b.getGreen() - a.getGreen()) * amount),
                (int) Math.round(a.getBlue() + (b.getBlue() - a.getBlue()) * amount),
                a.getAlpha());
    }

    /** The face: rounder at the top, narrowing to the chin. */
    private static Shape face() {
        Path2D face = new Path2D.Double();
        face.moveTo(60, 26);
        face.curveTo(74, 26, 82, 36, 82, 50);
        face.curveTo(82, 64, 72, 76, 60, 77);
        face.curveTo(48, 76, 38, 64, 38, 50);
        face.curveTo(38, 36, 46, 26, 60, 26);
        face.closePath();
        return face;
    }

    private static Shape shoulders() {
        Path2D shoulders = new Path2D.Double();
        shoulders.moveTo(8, 100);
        shoulders.curveTo(10, 86, 24, 80, 42, 78);
        shoulders.lineTo(78, 78);
        shoulders.curveTo(96, 80, 110, 86, 112, 100);
        shoulders.closePath();
        return shoulders;
    }
}
