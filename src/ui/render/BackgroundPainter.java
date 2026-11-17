package ui.render;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.RadialGradientPaint;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.QuadCurve2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import util.GameConstants;

/**
 * Draws the part of the scene that never moves: sky, sun, a far island, the
 * sea's colour and the sea floor with its scenery, darkened a little toward
 * the corners.
 *
 * Painting all of this every frame would be slow, so each look is drawn once
 * into an image and that image is reused (a cache). A look only costs time
 * the first time it is shown, which is why the scenery can be detailed. The
 * images are made at the size the window shows them, so they are drawn again
 * when the window changes size.
 *
 * Everything on the sea floor is low and close to the sea's own colour, so
 * that nothing down there can be taken for a piece of trash.
 */
public final class BackgroundPainter {

    private static final int W = GameConstants.WORLD_WIDTH;
    private static final int H = GameConstants.WORLD_HEIGHT;
    private static final int WATER_Y = GameConstants.WATER_Y;
    private static final int SEABED_Y = GameConstants.SEABED_Y;

    private static final Color[] CORAL = {
        new Color(255, 111, 145), new Color(255, 150, 113), new Color(255, 199, 95),
        new Color(195, 74, 155), new Color(79, 209, 197)
    };
    private static final Color BLEACHED = new Color(186, 180, 158);     // coral that is not doing well
    private static final Color LEAF = new Color(84, 150, 84);
    private static final Color PALM = new Color(46, 78, 66);
    private static final Color OLD_WOOD = new Color(124, 96, 62);
    private static final Color BARNACLE = new Color(214, 212, 190);
    private static final Color SHELL = new Color(246, 232, 214);
    private static final Color STARFISH = new Color(226, 120, 78);
    private static final Color SPONGE = new Color(150, 106, 190);
    private static final Color GLOW = new Color(110, 235, 225);
    private static final Color SHADOW = new Color(0, 0, 0, 60);
    private static final Color CLEAR = new Color(0, 0, 0, 0);

    private static final BasicStroke HAIR = stroke(0.8f);
    private static final BasicStroke THIN = stroke(1.2f);
    private static final BasicStroke GRASS_STROKE = stroke(2);

    // The vignette: clear in the middle, darker toward the corners, which pulls the eye to the play area.
    private static final float VIGNETTE_RADIUS = 560;
    private static final float[] VIGNETTE_STOPS = {0f, 0.55f, 1f};
    private static final Color[] VIGNETTE_COLORS = {
        new Color(0, 0, 0, 0), new Color(0, 0, 0, 0), new Color(0, 0, 0, 105)
    };

    // A chapter shows up to eleven looks as its sea clears. Only the latest few are kept.
    private static final int MAX_CACHED = 6;

    @SuppressWarnings("serial")   // a LinkedHashMap is Serializable by inheritance; this one is never saved
    private final Map<ChapterPalette, BufferedImage> cache =
            new LinkedHashMap<ChapterPalette, BufferedImage>(16, 0.75f, true) {   // true: ordered by last use
                @Override
                protected boolean removeEldestEntry(Map.Entry<ChapterPalette, BufferedImage> eldest) {
                    return size() > MAX_CACHED;
                }
            };
    private double cacheScale = 1;   // the size the cached images were made for

    // Which method draws which scenery, looked up instead of an if/else chain.
    private final Map<ChapterPalette.Scenery, BiConsumer<Graphics2D, ChapterPalette>> sceneryPainters =
            new EnumMap<>(ChapterPalette.Scenery.class);
    // How each place looks before any cleaning; see cleanliness().
    private final Map<ChapterPalette.Scenery, ChapterPalette> dirtyLooks = new EnumMap<>(ChapterPalette.Scenery.class);

    public BackgroundPainter() {
        sceneryPainters.put(ChapterPalette.Scenery.SHALLOWS, this::drawShallows);
        sceneryPainters.put(ChapterPalette.Scenery.WRECK, this::drawWreck);
        sceneryPainters.put(ChapterPalette.Scenery.DEEP, this::drawDeep);
        sceneryPainters.put(ChapterPalette.Scenery.REEF, this::drawReef);
        dirtyLooks.put(ChapterPalette.Scenery.SHALLOWS, ChapterPalette.SHALLOWS);
        dirtyLooks.put(ChapterPalette.Scenery.WRECK, ChapterPalette.WRECK);
        dirtyLooks.put(ChapterPalette.Scenery.DEEP, ChapterPalette.DEEP);
        dirtyLooks.put(ChapterPalette.Scenery.REEF, ChapterPalette.REEF);
    }

    public void paint(Graphics2D g, ChapterPalette palette) {
        double scale = PixelImages.scaleOf(g);
        if (scale != cacheScale) {
            cache.clear();   // the window shows the game at another size now
            cacheScale = scale;
        }
        BufferedImage image = cache.get(palette);
        if (image == null) {
            image = render(palette, scale);
            cache.put(palette, image);
        }
        PixelImages.paint(g, image, 0, 0, scale);
    }

    /** How many looks are in the cache right now. */
    public int getCachedCount() {
        return cache.size();
    }

    private BufferedImage render(ChapterPalette palette, double scale) {
        BufferedImage image = PixelImages.create(W, H, scale, false);
        Graphics2D g = PixelImages.draw(image, scale);

        g.setPaint(new GradientPaint(0, 0, palette.getSkyTop(), 0, WATER_Y, palette.getSkyBottom()));
        g.fillRect(0, 0, W, WATER_Y);

        // The sun sits below the HUD bar, with a soft glow around it.
        Color sun = palette.getSun();
        g.setColor(new Color(sun.getRed(), sun.getGreen(), sun.getBlue(), 60));
        g.fillOval(W - 112, 56, 76, 76);
        g.setColor(sun);
        g.fillOval(W - 100, 68, 52, 52);
        drawHorizon(g, palette);

        // The sea gets darker with depth, in three steps.
        g.setPaint(new LinearGradientPaint(0, WATER_Y, 0, SEABED_Y, new float[] {0f, 0.5f, 1f},
                new Color[] {palette.getSeaTop(), palette.getSeaMiddle(), palette.getSeaBottom()}));
        g.fillRect(0, WATER_Y, W, H - WATER_Y);

        sceneryPainters.get(palette.getScenery()).accept(g, palette);

        g.setPaint(new RadialGradientPaint(W / 2f, H / 2f, VIGNETTE_RADIUS, VIGNETTE_STOPS, VIGNETTE_COLORS));
        g.fillRect(0, 0, W, H);

        g.dispose();
        return image;
    }

    // ---- Sky: thin clouds, a far island with palms and a small outrigger boat ----

    /** Everything here is mixed with the sky's colour, which is how far-off things look through haze. */
    private void drawHorizon(Graphics2D g, ChapterPalette palette) {
        Color sky = palette.getSkyBottom();
        int sea = WATER_Y + 4;   // the waves cover whatever is drawn below the waterline

        // Long thin streaks of cloud low in the sky.
        g.setColor(withAlpha(mix(sky, Color.WHITE, 0.7), 120));
        g.fill(new Ellipse2D.Double(300, 104, 230, 6));
        g.fill(new Ellipse2D.Double(380, 98, 150, 5));
        g.fill(new Ellipse2D.Double(560, 112, 210, 5));
        g.fill(new Ellipse2D.Double(-40, 84, 170, 6));

        // A second island, further away and paler.
        g.setColor(mix(sky, PALM, 0.2));
        Path2D far = new Path2D.Double();
        far.moveTo(262, sea);
        far.curveTo(300, sea - 14, 330, sea - 17, 352, sea - 13);
        far.curveTo(380, sea - 9, 400, sea - 6, 436, sea);
        far.closePath();
        g.fill(far);

        // The island: two green humps with a strip of beach.
        Path2D island = new Path2D.Double();
        island.moveTo(52, sea);
        island.curveTo(86, sea - 8, 108, sea - 27, 150, sea - 29);
        island.curveTo(176, sea - 30, 186, sea - 18, 206, sea - 17);
        island.curveTo(226, sea - 16, 240, sea - 23, 256, sea - 21);
        island.curveTo(280, sea - 18, 294, sea - 7, 322, sea);
        island.closePath();
        g.setColor(mix(sky, PALM, 0.45));
        g.fill(island);
        g.setColor(mix(sky, new Color(240, 226, 184), 0.6));
        g.fill(new Ellipse2D.Double(48, sea - 7, 280, 5));

        // Palm trees on the ridge.
        g.setColor(mix(sky, PALM, 0.62));
        double[][] palms = {{104, 15, 11, -1}, {124, 23, 13, 1}, {166, 28, 12, -1}, {196, 17, 10, 1}, {238, 21, 12, 1}};
        for (double[] palm : palms) {
            drawPalm(g, palm[0], sea - palm[1], palm[2], palm[3]);
        }
        drawBangka(g, 585, WATER_Y - 4, sky);
    }

    /** A palm: a bent trunk and five drooping fronds. */
    private void drawPalm(Graphics2D g, double x, double y, double height, double lean) {
        double topX = x + lean * 3;
        double topY = y - height;
        g.setStroke(THIN);
        g.draw(new QuadCurve2D.Double(x, y, x, y - height * 0.6, topX, topY));
        for (int side = -1; side <= 1; side += 2) {
            g.draw(new QuadCurve2D.Double(topX, topY, topX + side * 4, topY - 4, topX + side * 8, topY + 2));
            g.draw(new QuadCurve2D.Double(topX, topY, topX + side * 3, topY - 6, topX + side * 6, topY - 3));
        }
        g.draw(new Line2D.Double(topX, topY, topX + lean, topY - 5));
    }

    /** A bangka, the outrigger boat of the Visayas: a slim hull, a float beside it, a mast and a small sail. */
    private void drawBangka(Graphics2D g, double x, double y, Color sky) {
        Graphics2D boat = (Graphics2D) g.create();
        boat.translate(x, y);
        boat.setColor(mix(sky, Color.WHITE, 0.75));
        Path2D sail = new Path2D.Double();
        sail.moveTo(0, -17);
        sail.lineTo(0, -5);
        sail.lineTo(10, -6);
        sail.closePath();
        boat.fill(sail);
        boat.setColor(mix(sky, new Color(64, 50, 44), 0.6));
        Path2D hull = new Path2D.Double();
        hull.moveTo(-14, -5);
        hull.quadTo(0, 2, 14, -5);
        hull.quadTo(0, -2, -14, -5);
        hull.closePath();
        boat.fill(hull);
        boat.setStroke(HAIR);
        boat.draw(new Line2D.Double(-0.5, -3, -0.5, -18));    // mast
        boat.draw(new Line2D.Double(-11, 0, 11, 0));          // the float, held by two bamboo arms
        boat.draw(new Line2D.Double(-6, -3, -7, 0));
        boat.draw(new Line2D.Double(6, -3, 7, 0));
        boat.fill(new Ellipse2D.Double(-8, -7.5, 3, 4));      // the fisherman
        boat.dispose();
    }

    // ---- Scenery: low, quiet shapes on the sea floor, so they never look like trash ----

    /** Chapter 1: sand ripples, rocks, shells, starfish, seagrass and a few low corals that start out sickly. */
    private void drawShallows(Graphics2D g, ChapterPalette palette) {
        Color water = palette.getSeaBottom();
        Color sand = palette.getSeabed();
        Color stone = mix(sand, water, 0.45);
        Color grass = mix(LEAF, water, 0.5);

        drawRock(g, 150, SEABED_Y, 190, 40, mix(sand, water, 0.78), 3);    // far mounds, faded by the water
        drawRock(g, 520, SEABED_Y, 240, 52, mix(sand, water, 0.78), 5);
        drawRock(g, 745, SEABED_Y, 150, 36, mix(sand, water, 0.78), 9);
        drawDunes(g, mix(sand, water, 0.55), 15);

        for (int i = 0; i < 9; i++) {
            drawGrass(g, 36 + i * 88 + scatter(i) * 30, SEABED_Y - 4, 16 + scatter(i + 30) * 16, shade(grass, 0.85), i);
        }
        // The corals turn from a bleached grey to their own colours as the sea is cleaned.
        double health = cleanliness(palette);
        double[][] corals = {{112, 20}, {298, 15}, {452, 18}, {566, 22}, {668, 15}};
        for (int i = 0; i < corals.length; i++) {
            Color color = mix(mix(BLEACHED, CORAL[i % CORAL.length], 0.15 + 0.6 * health), water, 0.5 - 0.15 * health);
            drawBranchCoral(g, corals[i][0], SEABED_Y - 2, corals[i][1], color, 40 + i);
        }
        double[][] rocks = {{70, 48, 22}, {212, 30, 13}, {330, 66, 30}, {372, 30, 14}, {480, 38, 16},
            {596, 56, 25}, {704, 34, 16}, {774, 52, 24}};
        for (int i = 0; i < rocks.length; i++) {
            drawRock(g, rocks[i][0], SEABED_Y, rocks[i][1], rocks[i][2], stone, 60 + i * 7);
        }
        for (int i = 0; i < 7; i++) {
            drawGrass(g, 60 + i * 112 + scatter(i + 70) * 40, SEABED_Y, 12 + scatter(i + 80) * 14, grass, i + 20);
        }
        drawSand(g, palette);
        drawSandLife(g, palette, new double[] {186, 418, 548, 742}, new double[] {252, 640});
    }

    /** Chapter 2: the posts of an old pier and a sunken boat, on the right where the cans collect. */
    private void drawWreck(Graphics2D g, ChapterPalette palette) {
        Color water = palette.getSeaBottom();
        Color sand = palette.getSeabed();
        Color wood = mix(shade(water, 0.7), OLD_WOOD, 0.2);   // close to a silhouette, as the wreck must stay quiet
        Color lit = shade(wood, 1.4);
        Color dark = shade(wood, 0.7);
        int floor = SEABED_Y;

        drawRock(g, 120, floor, 210, 40, mix(sand, water, 0.86), 14);
        drawDunes(g, mix(sand, water, 0.6), 14);

        // What is left of the pier further out: stumps getting shorter to the left.
        drawPost(g, 250, floor - 22, 9, wood, lit, dark, 1);
        drawPost(g, 320, floor - 38, 10, wood, lit, dark, 2);
        drawPost(g, 394, floor - 58, 11, wood, lit, dark, 3);

        // Cross braces behind the posts, one of them snapped.
        g.setColor(dark);
        g.setStroke(new BasicStroke(4));
        g.draw(new Line2D.Double(476, floor - 106, 556, floor - 24));
        g.draw(new Line2D.Double(556, floor - 106, 476, floor - 24));
        g.draw(new Line2D.Double(636, floor - 106, 716, floor - 30));
        g.draw(new Line2D.Double(716, floor - 106, 690, floor - 78));

        drawHull(g, wood, lit, dark);

        // The pier: four posts and the beam that joined them.
        for (int i = 0; i < 4; i++) {
            drawPost(g, 470 + i * 80, floor - 150 + (i % 2) * 25, 12, wood, lit, dark, 10 + i);
        }
        g.setColor(wood);
        g.fill(new Rectangle2D.Double(464, floor - 110, 264, 8));
        g.setColor(lit);
        g.fill(new Rectangle2D.Double(464, floor - 110, 264, 2));
        g.setColor(dark);
        g.fill(new Rectangle2D.Double(464, floor - 104, 264, 2));
        g.setStroke(GRASS_STROKE);
        g.setColor(mix(wood, LEAF, 0.25));
        for (int i = 0; i < 16; i++) {   // weed hanging from the beam
            double x = 470 + scatter(i + 5) * 250;
            g.draw(new Line2D.Double(x, floor - 102, x + scatter(i) * 4 - 2, floor - 96 + scatter(i + 40) * 9));
        }

        drawAnchor(g, wood, lit, dark);
        double[][] rocks = {{44, 54, 22}, {160, 34, 15}, {206, 22, 10}, {362, 30, 13}, {446, 26, 11}, {788, 40, 22}};
        for (int i = 0; i < rocks.length; i++) {
            drawRock(g, rocks[i][0], floor, rocks[i][1], rocks[i][2], mix(sand, water, 0.62), 90 + i * 3);
        }
        Color grass = mix(LEAF, water, 0.5);
        double[] tufts = {22, 96, 132, 228, 286, 344, 420, 592, 668, 770};
        for (int i = 0; i < tufts.length; i++) {
            drawGrass(g, tufts[i], floor, 12 + scatter(i + 11) * 14, grass, i + 50);
        }
        drawSand(g, palette);
        drawSandLife(g, palette, new double[] {70, 300, 500}, new double[] {196});
    }

    /** One wooden post with a broken top, lit from the right, with barnacles near its foot. */
    private void drawPost(Graphics2D g, double x, double top, double width, Color wood, Color lit, Color dark, int seed) {
        double height = SEABED_Y + 6 - top;
        Path2D brokenTop = new Path2D.Double();
        brokenTop.moveTo(x, top + 1);
        brokenTop.lineTo(x + width * 0.3, top - 5);
        brokenTop.lineTo(x + width * 0.55, top - 1);
        brokenTop.lineTo(x + width, top - 6);
        brokenTop.lineTo(x + width, top + 1);
        brokenTop.closePath();
        g.setColor(wood);
        g.fill(brokenTop);
        g.fill(new Rectangle2D.Double(x, top, width, height));
        g.setColor(lit);
        g.fill(new Rectangle2D.Double(x + width - 3, top, 3, height));
        g.setColor(dark);
        g.fill(new Rectangle2D.Double(x, top, 2.5, height));
        g.setColor(mix(wood, BARNACLE, 0.22));
        for (int i = 0; i < 10; i++) {
            double up = scatter(seed * 31 + i) * scatter(seed * 17 + i) * Math.min(height - 8, 70);   // most sit low
            double size = 1.8 + scatter(seed + i * 7) * 1.8;
            g.fill(new Ellipse2D.Double(x + scatter(seed * 13 + i) * (width - size), SEABED_Y - 6 - up, size, size));
        }
    }

    /** The sunken boat: stern in the sand, bow up, with planks, a hole that shows its ribs, a cabin and a broken mast. */
    private void drawHull(Graphics2D g, Color wood, Color lit, Color dark) {
        int floor = SEABED_Y;
        Path2D hull = new Path2D.Double();
        hull.moveTo(548, floor + 6);
        hull.lineTo(520, floor - 44);
        hull.quadTo(640, floor - 52, 768, floor - 80);
        hull.quadTo(762, floor - 40, 722, floor + 6);
        hull.closePath();

        // The mast and the cabin stand at the same tilt as the deck.
        Graphics2D deck = (Graphics2D) g.create();
        deck.translate(650, floor - 57);
        deck.rotate(Math.atan2(-36, 248));
        deck.setColor(withAlpha(dark, 150));
        deck.setStroke(HAIR);
        deck.draw(new QuadCurve2D.Double(-66, -98, 20, -50, 112, -8));   // a slack rope from the mast to the bow
        deck.setStroke(new BasicStroke(3));
        deck.setColor(dark);
        deck.draw(new Line2D.Double(-64, -66, -40, -44));                 // a spar hanging loose
        Path2D mast = new Path2D.Double();
        mast.moveTo(-68, 4);
        mast.lineTo(-68, -96);
        mast.lineTo(-66, -104);
        mast.lineTo(-65, -97);
        mast.lineTo(-62, -101);
        mast.lineTo(-62, 4);
        mast.closePath();
        deck.setColor(wood);
        deck.fill(mast);
        deck.setColor(lit);
        deck.fill(new Rectangle2D.Double(-64, -96, 2, 100));
        deck.setColor(wood);
        deck.fill(new Rectangle2D.Double(-30, -30, 60, 34));              // cabin
        deck.setColor(lit);
        deck.fill(new Rectangle2D.Double(26, -30, 4, 34));
        deck.setColor(dark);
        deck.fill(new Rectangle2D.Double(-34, -34, 68, 5));               // its roof
        deck.fill(new Rectangle2D.Double(-22, -22, 13, 10));              // an empty window
        deck.fill(new Rectangle2D.Double(4, -22, 12, 26));                // and the door
        deck.dispose();

        g.setColor(wood);
        g.fill(hull);
        Graphics2D side = (Graphics2D) g.create();
        side.clip(hull);
        // Light from above: the hull is brighter near the deck.
        side.setPaint(new GradientPaint(0, floor - 80, withAlpha(lit, 150), 0, floor - 14, withAlpha(lit, 0)));
        side.fill(hull);
        side.setColor(dark);
        side.setStroke(HAIR);
        for (int plank = 1; plank <= 5; plank++) {
            int down = plank * 12;
            side.draw(new QuadCurve2D.Double(516, floor - 44 + down, 640, floor - 52 + down, 770, floor - 80 + down * 1.1));
        }
        for (int i = 0; i < 12; i++) {   // where one plank ends and the next begins
            double x = 540 + scatter(i + 3) * 210;
            double y = floor - 60 + (i % 5) * 12;
            side.draw(new Line2D.Double(x, y, x, y + 12));
        }
        Path2D hole = new Path2D.Double();
        hole.moveTo(596, floor - 30);
        hole.lineTo(612, floor - 38);
        hole.lineTo(620, floor - 32);
        hole.lineTo(634, floor - 41);
        hole.lineTo(654, floor - 30);
        hole.lineTo(648, floor - 20);
        hole.lineTo(664, floor - 12);
        hole.lineTo(640, floor - 3);
        hole.lineTo(622, floor - 9);
        hole.lineTo(604, floor - 5);
        hole.closePath();
        side.setColor(shade(dark, 0.75));
        side.fill(hole);
        side.clip(hole);
        side.setColor(shade(wood, 0.9));
        side.setStroke(new BasicStroke(1.8f));
        for (int rib = 0; rib < 5; rib++) {
            side.draw(new QuadCurve2D.Double(604 + rib * 13, floor - 44, 600 + rib * 13, floor - 20, 606 + rib * 13, floor));
        }
        side.dispose();

        g.setColor(lit);
        g.setStroke(new BasicStroke(2.2f));
        g.draw(new QuadCurve2D.Double(520, floor - 44, 640, floor - 52, 768, floor - 80));   // the rail along the deck
        g.setColor(shade(dark, 0.75));
        g.fill(new Ellipse2D.Double(712, floor - 56, 10, 10));                               // a porthole
        g.setColor(lit);
        g.setStroke(THIN);
        g.draw(new Ellipse2D.Double(712, floor - 56, 10, 10));
    }

    /** A length of chain from the stern to an anchor half buried in the sand. */
    private void drawAnchor(Graphics2D g, Color wood, Color lit, Color dark) {
        int floor = SEABED_Y;
        QuadCurve2D hang = new QuadCurve2D.Double(528, floor - 30, 500, floor + 8, 440, floor - 24);
        g.setStroke(THIN);
        for (int link = 0; link <= 26; link++) {
            double t = link / 26.0;
            double x = (1 - t) * (1 - t) * hang.getX1() + 2 * t * (1 - t) * hang.getCtrlX() + t * t * hang.getX2();
            double y = (1 - t) * (1 - t) * hang.getY1() + 2 * t * (1 - t) * hang.getCtrlY() + t * t * hang.getY2();
            g.setColor(link % 2 == 0 ? dark : mix(wood, lit, 0.5));
            g.draw(new Ellipse2D.Double(x - 2, y - 1.5, 4, 3));
        }
        Graphics2D anchor = (Graphics2D) g.create();
        anchor.translate(432, floor - 12);
        anchor.rotate(-0.55);
        anchor.scale(1.35, 1.35);
        anchor.setColor(dark);
        anchor.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        anchor.draw(new Line2D.Double(0, -14, 0, 12));                    // shank
        anchor.draw(new QuadCurve2D.Double(-12, 3, 0, 18, 12, 3));        // the two arms
        anchor.draw(new Line2D.Double(-6, -10, 6, -10));                  // stock
        anchor.setStroke(THIN);
        anchor.draw(new Ellipse2D.Double(-3, -20, 6, 6));                 // the ring the chain ends in
        anchor.setColor(mix(wood, lit, 0.5));
        anchor.draw(new Line2D.Double(1, -13, 1, 10));
        anchor.dispose();
    }

    /** Chapter 3: a rock wall with ledges on the left, rock spires, tube sponges, sea fans and faintly glowing anemones. */
    private void drawDeep(Graphics2D g, ChapterPalette palette) {
        Color water = palette.getSeaBottom();
        Color rock = mix(shade(palette.getSeabed(), 1.25), palette.getSeaMiddle(), 0.6);
        Color farRock = mix(rock, water, 0.6);
        int floor = SEABED_Y;

        double[][] farSpires = {{214, 60, 88}, {352, 70, 60}, {468, 54, 94}, {600, 74, 66}, {716, 60, 86}};
        for (int i = 0; i < farSpires.length; i++) {
            drawSpire(g, farSpires[i][0], farSpires[i][1], farSpires[i][2], farRock, shade(farRock, 0.85), 7 + i);
        }
        drawDunes(g, mix(palette.getSeabed(), water, 0.5), 12);

        drawSeaFan(g, 186, floor - 20, 30, -0.2, mix(new Color(170, 70, 110), water, 0.66), 3);
        drawSeaFan(g, 640, floor - 30, 28, 0.15, mix(new Color(170, 70, 110), water, 0.66), 5);
        drawRockWall(g, rock);
        for (int i = 0; i < 7; i++) {
            int height = 24 + (i * 29) % 46;
            drawSpire(g, 170 + i * 96 + scatter(i) * 18, 62 + scatter(i + 9) * 30, height, rock, shade(rock, 0.76), 20 + i);
        }

        Color sponge = mix(SPONGE, water, 0.62);
        drawSponges(g, 62, floor - 158, 18, sponge, 1);
        drawSponges(g, 296, floor - 4, 24, sponge, 2);
        drawSponges(g, 530, floor - 2, 20, sponge, 3);
        drawSponges(g, 762, floor - 6, 26, sponge, 4);
        drawSeaFan(g, 110, floor - 104, 22, 0.3, mix(new Color(190, 90, 70), water, 0.66), 8);
        drawSeaFan(g, 436, floor - 6, 30, 0.1, mix(new Color(190, 90, 70), water, 0.66), 11);

        drawSand(g, palette);
        // The anemones give off a little light of their own.
        Color glow = mix(GLOW, palette.getAccent(), 0.3);
        double[][] anemones = {{142, floor - 48, 9}, {244, floor - 2, 11}, {388, floor - 1, 9}, {588, floor - 3, 12},
            {700, floor - 1, 9}};
        for (int i = 0; i < anemones.length; i++) {
            g.setPaint(new RadialGradientPaint((float) anemones[i][0], (float) anemones[i][1] - 6, 26,
                    new float[] {0f, 1f}, new Color[] {withAlpha(glow, 46), withAlpha(glow, 0)}));
            g.fill(new Ellipse2D.Double(anemones[i][0] - 26, anemones[i][1] - 32, 52, 52));
            drawAnemone(g, anemones[i][0], anemones[i][1], anemones[i][2], mix(glow, water, 0.72), mix(glow, water, 0.3), 30 + i);
        }
    }

    /** The drop-off: a cliff in three steps. The top of each ledge catches the light, the rock under it is in shadow. */
    private void drawRockWall(Graphics2D g, Color rock) {
        int floor = SEABED_Y;
        double[][] ledges = {{0, 214, 30, 208, 46, 194}, {40, 166, 76, 158, 90, 146}, {84, 112, 114, 104, 128, 90},
            {124, 58, 152, 50, 168, 34}};
        Path2D wall = new Path2D.Double();
        wall.moveTo(0, floor + 8);
        for (double[] ledge : ledges) {
            wall.lineTo(ledge[0], floor - ledge[1]);
            wall.lineTo(ledge[2], floor - ledge[3]);
            wall.lineTo(ledge[4], floor - ledge[5]);
        }
        wall.lineTo(180, floor + 8);
        wall.closePath();
        g.setPaint(new GradientPaint(0, 0, shade(rock, 0.72), 170, 0, rock));
        g.fill(wall);

        Graphics2D face = (Graphics2D) g.create();
        face.clip(wall);
        for (double[] ledge : ledges) {
            double top = floor - ledge[5];
            face.setPaint(new GradientPaint(0, (float) top, SHADOW, 0, (float) top + 26, CLEAR));
            face.fill(new Rectangle2D.Double(0, top, ledge[4], 26));
        }
        face.setColor(shade(rock, 0.72));
        face.setStroke(HAIR);
        for (int i = 0; i < 9; i++) {   // cracks running down the rock
            double x = 8 + scatter(i + 60) * 140;
            double y = floor - 20 - scatter(i + 61) * 170;
            face.draw(new QuadCurve2D.Double(x, y, x + 5, y + 10, x - 2 + scatter(i) * 8, y + 22));
        }
        face.setColor(shade(rock, 1.45));
        face.setStroke(stroke(3));
        for (double[] ledge : ledges) {
            Path2D top = new Path2D.Double();
            top.moveTo(ledge[0], floor - ledge[1]);
            top.lineTo(ledge[2], floor - ledge[3]);
            top.lineTo(ledge[4], floor - ledge[5]);
            face.draw(top);
        }
        face.dispose();
    }

    /** A pointed rock: dark on the left of its ridge, lighter on the right. */
    private void drawSpire(Graphics2D g, double x, double width, double height, Color light, Color dark, int seed) {
        double base = SEABED_Y + 8;
        double tipX = x + width * (0.36 + 0.2 * scatter(seed));
        double tipY = SEABED_Y - height;
        double ridgeX = x + width * 0.55;
        Path2D left = new Path2D.Double();
        left.moveTo(x, base);
        left.lineTo(x + width * 0.16, SEABED_Y - height * (0.3 + 0.2 * scatter(seed + 1)));
        left.lineTo(x + width * 0.24, SEABED_Y - height * 0.62);
        left.lineTo(tipX, tipY);
        left.lineTo(ridgeX, base);
        left.closePath();
        Path2D right = new Path2D.Double();
        right.moveTo(tipX, tipY);
        right.lineTo(x + width * 0.66, SEABED_Y - height * (0.5 + 0.2 * scatter(seed + 2)));
        right.lineTo(x + width * 0.8, SEABED_Y - height * 0.3);
        right.lineTo(x + width, base);
        right.lineTo(ridgeX, base);
        right.closePath();
        g.setColor(light);
        g.fill(right);
        g.setColor(dark);
        g.fill(left);
    }

    /** The ending: branching coral, brain coral, sea fans, sponges and anemones, bright and healthy. */
    private void drawReef(Graphics2D g, ChapterPalette palette) {
        Color water = palette.getSeaBottom();
        Color sand = palette.getSeabed();
        int floor = SEABED_Y;

        // Reef further away, faded to the sea's blue.
        for (int i = 0; i < 9; i++) {
            Color faded = mix(CORAL[(i + 2) % CORAL.length], water, 0.78);
            drawRock(g, 30 + i * 96, floor, 110, 34 + scatter(i + 2) * 26, faded, 100 + i);
            drawBranchCoral(g, 62 + i * 96, floor - 30, 26 + scatter(i) * 14, shade(faded, 1.08), 120 + i);
        }
        drawDunes(g, mix(sand, water, 0.5), 16);

        // Rocks that the coral grows on.
        Color stone = mix(mix(sand, water, 0.5), new Color(120, 96, 130), 0.3);
        double[][] rocks = {{90, 90, 26}, {250, 70, 20}, {420, 110, 30}, {610, 84, 24}, {748, 96, 28}};
        for (int i = 0; i < rocks.length; i++) {
            drawRock(g, rocks[i][0], floor, rocks[i][1], rocks[i][2], stone, 140 + i * 5);
        }
        for (int i = 0; i < 4; i++) {
            Color fan = mix(CORAL[i == 1 ? 1 : 3], water, 0.25);
            drawSeaFan(g, 160 + i * 172, floor - 8 - (i % 2) * 8, 40 + (i % 2) * 8, i % 2 == 0 ? -0.15 : 0.2, fan, 150 + i);
        }
        double[][] branching = {{40, 30, 0}, {208, 26, 1}, {300, 34, 3}, {392, 24, 2}, {478, 30, 0}, {566, 26, 4},
            {664, 34, 1}, {770, 28, 3}};
        for (int i = 0; i < branching.length; i++) {
            Color color = mix(CORAL[(int) branching[i][2]], water, 0.12);
            drawBranchCoral(g, branching[i][0], floor - 8 - scatter(i + 3) * 12, branching[i][1], color, 160 + i);
        }
        double[][] sponges = {{128, 22}, {512, 26}, {716, 20}};
        for (int i = 0; i < sponges.length; i++) {
            drawSponges(g, sponges[i][0], floor - 16, sponges[i][1], mix(i == 1 ? SPONGE : CORAL[2], water, 0.15), 170 + i);
        }
        double[][] brains = {{72, 44, 0}, {262, 34, 2}, {434, 52, 1}, {622, 38, 4}, {744, 46, 2}};
        for (int i = 0; i < brains.length; i++) {
            Color color = mix(CORAL[(int) brains[i][2]], water, 0.18);
            drawBrainCoral(g, brains[i][0], floor - 6, brains[i][1], color);
        }
        Color grass = mix(new Color(60, 190, 120), water, 0.2);
        for (int i = 0; i < 12; i++) {
            drawGrass(g, 22 + i * 68 + scatter(i + 4) * 30, floor, 14 + scatter(i + 14) * 16, grass, i + 180);
        }
        drawSand(g, palette);
        double[][] anemones = {{168, 11, 0}, {346, 12, 4}, {536, 10, 3}, {690, 12, 1}};
        for (int i = 0; i < anemones.length; i++) {
            Color color = CORAL[(int) anemones[i][2]];
            drawAnemone(g, anemones[i][0], floor - 1, anemones[i][1], mix(color, water, 0.3), mix(color, Color.WHITE, 0.45), 190 + i);
        }
        drawSandLife(g, palette, new double[] {118, 320, 470, 596, 780}, new double[] {226, 384, 660});
    }

    // ---- Parts the sceneries share ----

    /** Low dunes behind everything else, in a colour faded toward the sea's. */
    private void drawDunes(Graphics2D g, Color color, double height) {
        Path2D dunes = new Path2D.Double();
        dunes.moveTo(0, H);
        for (int x = 0; x <= W; x += 16) {
            dunes.lineTo(x, SEABED_Y - height + 6 * Math.sin(x * 0.009 + 4) + 3 * Math.sin(x * 0.023 + 1));
        }
        dunes.lineTo(W, H);
        dunes.closePath();
        g.setColor(color);
        g.fill(dunes);
    }

    /** The sand in front: a gently uneven floor, lighter where it faces up, with rows of ripples. */
    private void drawSand(Graphics2D g, ChapterPalette palette) {
        Color sand = palette.getSeabed();
        Path2D floor = new Path2D.Double();
        Path2D crest = new Path2D.Double();
        floor.moveTo(0, H);
        for (int x = 0; x <= W; x += 16) {
            double y = SEABED_Y - 4 + 3.5 * Math.sin(x * 0.013 + 1) + 2 * Math.sin(x * 0.037 + 2.3);
            floor.lineTo(x, y);
            if (x == 0) {
                crest.moveTo(x, y);
            } else {
                crest.lineTo(x, y);
            }
        }
        floor.lineTo(W, H);
        floor.closePath();
        g.setPaint(new GradientPaint(0, SEABED_Y - 8, shade(sand, 1.04), 0, H, shade(sand, 0.78)));
        g.fill(floor);
        g.setColor(withAlpha(shade(sand, 1.2), 150));
        g.setStroke(THIN);
        g.draw(crest);

        for (int i = 0; i < 60; i++) {
            double x = scatter(i) * (W + 20) - 20;
            double y = SEABED_Y + 2 + scatter(i + 100) * 11;
            double length = 9 + scatter(i + 200) * 16;
            g.setColor(withAlpha(shade(sand, 0.78), 120));
            g.draw(new QuadCurve2D.Double(x, y, x + length / 2, y - 2, x + length, y));
            g.setColor(withAlpha(shade(sand, 1.18), 90));
            g.draw(new QuadCurve2D.Double(x, y + 1.2, x + length / 2, y - 0.8, x + length, y + 1.2));
        }
    }

    /** Small things lying on the sand: shells at the given x positions, starfish, and a scatter of pebbles. */
    private void drawSandLife(Graphics2D g, ChapterPalette palette, double[] shells, double[] starfish) {
        Color sand = palette.getSeabed();
        for (int i = 0; i < 26; i++) {
            double size = 1.6 + scatter(i + 300) * 2.2;
            g.setColor(shade(sand, i % 3 == 0 ? 1.16 : 0.74));
            g.fill(new Ellipse2D.Double(scatter(i + 310) * W, SEABED_Y + 1 + scatter(i + 320) * 11, size * 1.3, size));
        }
        for (int i = 0; i < shells.length; i++) {
            drawShell(g, shells[i], SEABED_Y + 3 + scatter(i + 330) * 7, mix(SHELL, sand, 0.45), i);
        }
        for (int i = 0; i < starfish.length; i++) {
            drawStarfish(g, starfish[i], SEABED_Y + 5 + scatter(i + 340) * 4, 5.5, mix(STARFISH, sand, 0.4), i * 0.7);
        }
    }

    /**
     * A lumpy rock sitting on the floor. It is filled dark, then twice more a
     * little up and to the right in lighter colours, which leaves a dark side
     * at the lower left and a light side toward the sun.
     */
    private void drawRock(Graphics2D g, double x, double base, double width, double height, Color color, int seed) {
        int points = 7;
        double[] px = new double[points + 1];
        double[] py = new double[points + 1];
        for (int k = 0; k <= points; k++) {
            double angle = Math.PI - Math.PI * k / points;   // from the left end over the top to the right end
            double reach = 0.75 + 0.35 * scatter(seed * 11 + k);
            px[k] = x + Math.cos(angle) * width / 2;
            py[k] = base - Math.sin(angle) * height * reach;
        }
        Path2D rock = new Path2D.Double();
        rock.moveTo(px[0], base + 8);
        rock.lineTo(px[0], py[0]);
        for (int k = 1; k < points; k++) {
            rock.quadTo(px[k], py[k], (px[k] + px[k + 1]) / 2, (py[k] + py[k + 1]) / 2);
        }
        rock.lineTo(px[points], base + 8);
        rock.closePath();

        g.setColor(shade(color, 0.74));
        g.fill(rock);
        Graphics2D lit = (Graphics2D) g.create();
        lit.clip(rock);
        lit.translate(width * 0.07, -height * 0.12);
        lit.setColor(color);
        lit.fill(rock);
        lit.translate(width * 0.12, -height * 0.2);
        lit.setColor(shade(color, 1.16));
        lit.fill(rock);
        lit.dispose();
    }

    /** A tuft of seagrass: a few blades bending away from the middle. */
    private void drawGrass(Graphics2D g, double x, double base, double height, Color color, int seed) {
        g.setStroke(GRASS_STROKE);
        int blades = 4 + (int) (scatter(seed * 3) * 3);
        for (int i = 0; i < blades; i++) {
            double spread = (i - (blades - 1) / 2.0) * 3.2;
            double tall = height * (0.6 + 0.4 * scatter(seed * 7 + i));
            g.setColor(i % 2 == 0 ? color : shade(color, 0.85));
            g.draw(new QuadCurve2D.Double(x + spread, base + 4, x + spread * 1.3, base - tall * 0.6,
                    x + spread * 2.6 + scatter(seed + i) * 4 - 2, base - tall));
        }
    }

    /** A low coral of forking branches, drawn dark first and then lighter on the side that faces the light. */
    private void drawBranchCoral(Graphics2D g, double x, double base, double size, Color color, int seed) {
        g.setColor(shade(color, 0.72));
        drawBranch(g, x, base + 4, -Math.PI / 2, size * 0.42, (float) (size * 0.2), 4, seed, 1);
        g.setColor(color);
        drawBranch(g, x + 0.8, base + 3.2, -Math.PI / 2, size * 0.42, (float) (size * 0.2), 4, seed, 0.5);
    }

    /** One branch and, from its end, two or three thinner ones, until the depth runs out. */
    private void drawBranch(Graphics2D g, double x, double y, double angle, double length, float width, int depth,
                            int seed, double widthFactor) {
        double endX = x + Math.cos(angle) * length;
        double endY = y + Math.sin(angle) * length;
        g.setStroke(stroke((float) Math.max(1, width * widthFactor)));
        g.draw(new Line2D.Double(x, y, endX, endY));
        if (depth == 0) {
            return;
        }
        int forks = 2 + (scatter(seed) > 0.6 ? 1 : 0);
        for (int i = 0; i < forks; i++) {
            double turn = (i - (forks - 1) / 2.0) * 0.75 + (scatter(seed * 5 + i) - 0.5) * 0.4;
            drawBranch(g, endX, endY, angle + turn, length * (0.68 + 0.14 * scatter(seed + i)), width * 0.72f, depth - 1,
                    seed * 3 + i + 1, widthFactor);
        }
    }

    /** A brain coral: a dome covered in winding grooves. */
    private void drawBrainCoral(Graphics2D g, double x, double base, double width, Color color) {
        double height = width * 0.56;
        Arc2D dome = new Arc2D.Double(x - width / 2, base - height, width, height * 2, 0, 180, Arc2D.CHORD);
        g.setPaint(new GradientPaint(0, (float) (base - height), shade(color, 1.12), 0, (float) base, shade(color, 0.72)));
        g.fill(dome);
        Graphics2D grooves = (Graphics2D) g.create();
        grooves.clip(dome);
        grooves.setColor(withAlpha(shade(color, 0.6), 170));
        grooves.setStroke(THIN);
        for (double row = base - height + 4; row < base; row += 5) {
            Path2D groove = new Path2D.Double();
            groove.moveTo(x - width / 2, row);
            for (double dx = -width / 2; dx <= width / 2; dx += 3) {
                groove.lineTo(x + dx, row + 2 * Math.sin(dx * 0.7 + row) + 1.5 * Math.sin(dx * 0.31 + row * 2));
            }
            grooves.draw(groove);
        }
        grooves.dispose();
    }

    /** A sea fan: a flat web of fine branches, like a small leafless tree. */
    private void drawSeaFan(Graphics2D g, double x, double base, double size, double lean, Color color, int seed) {
        g.setColor(withAlpha(color, 46));
        AffineTransform upright = g.getTransform();
        g.rotate(lean, x, base);
        g.fill(new Arc2D.Double(x - size * 0.75, base - size * 1.05, size * 1.5, size * 2, 20, 140, Arc2D.PIE));
        g.setTransform(upright);
        g.setColor(color);
        for (int rib = -2; rib <= 2; rib++) {
            drawBranch(g, x, base + 3, -Math.PI / 2 + lean + rib * 0.36, size * 0.32, 2.2f, 4, seed + rib * 13, 1);
        }
    }

    /** A cluster of tube sponges: upright tubes with a dark opening on top. */
    private void drawSponges(Graphics2D g, double x, double base, double height, Color color, int seed) {
        for (int i = 0; i < 4; i++) {
            double tubeX = x + (i - 1.5) * 7 + scatter(seed * 9 + i) * 3;
            double tall = height * (0.5 + 0.5 * scatter(seed * 5 + i));
            double wide = 5.5 + scatter(seed + i) * 2.5;
            Path2D tube = new Path2D.Double();
            tube.moveTo(tubeX - wide * 0.35, base + 4);
            tube.lineTo(tubeX - wide / 2, base - tall);
            tube.lineTo(tubeX + wide / 2, base - tall);
            tube.lineTo(tubeX + wide * 0.35, base + 4);
            tube.closePath();
            g.setPaint(new GradientPaint((float) (tubeX - wide / 2), 0, shade(color, 0.72), (float) (tubeX + wide / 2), 0,
                    shade(color, 1.1)));
            g.fill(tube);
            g.setColor(shade(color, 1.1));
            g.fill(new Ellipse2D.Double(tubeX - wide / 2, base - tall - 1.6, wide, 3.2));
            g.setColor(shade(color, 0.5));
            g.fill(new Ellipse2D.Double(tubeX - wide / 2 + 1.2, base - tall - 0.9, wide - 2.4, 1.8));
        }
    }

    /** An anemone: a short stalk with a crown of tentacles, lighter at their tips. */
    private void drawAnemone(Graphics2D g, double x, double base, double size, Color body, Color tips, int seed) {
        g.setColor(shade(body, 0.8));
        g.fill(new Rectangle2D.Double(x - size * 0.35, base - size * 0.5, size * 0.7, size * 0.5 + 4));
        g.setStroke(stroke((float) (size * 0.16)));
        int tentacles = 9;
        for (int i = 0; i < tentacles; i++) {
            double fan = (i - (tentacles - 1) / 2.0) / ((tentacles - 1) / 2.0);   // -1 at the left, 1 at the right
            double tipX = x + fan * size * 0.95 + (scatter(seed * 3 + i) - 0.5) * 2;
            double tipY = base - size * (1.25 - 0.55 * fan * fan);
            g.setColor(body);
            g.draw(new QuadCurve2D.Double(x + fan * size * 0.3, base - size * 0.45, x + fan * size * 0.4, base - size, tipX, tipY));
            g.setColor(tips);
            g.fill(new Ellipse2D.Double(tipX - size * 0.11, tipY - size * 0.11, size * 0.22, size * 0.22));
        }
    }

    /** A scallop shell: a small fan with ribs. */
    private void drawShell(Graphics2D g, double x, double y, Color color, int seed) {
        AffineTransform upright = g.getTransform();
        g.rotate(scatter(seed + 400) * 1.2 - 0.6, x, y);
        g.setColor(color);
        g.fill(new Arc2D.Double(x - 4, y - 4, 8, 8, 0, 180, Arc2D.PIE));
        g.setColor(shade(color, 0.76));
        g.setStroke(HAIR);
        for (int rib = -1; rib <= 1; rib++) {
            g.draw(new Line2D.Double(x, y, x + rib * 2.4, y - 3.4));
        }
        g.setTransform(upright);
    }

    /** A starfish: five arms around a middle. */
    private void drawStarfish(Graphics2D g, double x, double y, double size, Color color, double turn) {
        Path2D star = new Path2D.Double();
        for (int i = 0; i < 10; i++) {
            double angle = turn + Math.PI * i / 5;
            double reach = i % 2 == 0 ? size : size * 0.42;
            double px = x + Math.cos(angle) * reach;
            double py = y + Math.sin(angle) * reach * 0.7;   // squashed: it lies flat, seen from the side
            if (i == 0) {
                star.moveTo(px, py);
            } else {
                star.lineTo(px, py);
            }
        }
        star.closePath();
        g.setColor(color);
        g.setStroke(THIN);
        g.fill(star);
        g.draw(star);   // the round line softens the points
        g.setColor(shade(color, 1.2));
        g.fill(new Ellipse2D.Double(x - 1, y - 0.8, 2, 1.6));
    }

    // ---- Small helpers ----

    /**
     * How clean a look is: 0 for a chapter's dirty start, 1 for as clean as it
     * gets. The palette does not say so itself, but its sunbeams grow stronger
     * with every step of cleaning, so their strength tells.
     */
    private double cleanliness(ChapterPalette palette) {
        ChapterPalette dirty = dirtyLooks.get(palette.getScenery());
        int range = dirty.getCleaned().getRayAlpha() - dirty.getRayAlpha();
        if (range == 0) {
            return 1;   // the reef of the ending is clean from the start
        }
        return Math.max(0, Math.min(1, (palette.getRayAlpha() - dirty.getRayAlpha()) / (double) range));
    }

    /**
     * A number from 0 to 1 that looks random but is always the same for the
     * same n. Scenery is placed with it, so it never jumps between two looks.
     */
    private static double scatter(int n) {
        double wild = Math.sin(n * 12.9898 + 4.1) * 43758.5453;
        return wild - Math.floor(wild);
    }

    private static BasicStroke stroke(float width) {
        return new BasicStroke(width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    }

    /** A darker (factor below 1) or lighter (above 1) version of a colour. */
    private static Color shade(Color color, double factor) {
        return new Color(
                Math.min(255, (int) (color.getRed() * factor)),
                Math.min(255, (int) (color.getGreen() * factor)),
                Math.min(255, (int) (color.getBlue() * factor)),
                color.getAlpha());
    }

    /** A colour between two others: amount 0 gives the first, 1 the second. */
    private static Color mix(Color from, Color to, double amount) {
        return new Color(
                (int) (from.getRed() + (to.getRed() - from.getRed()) * amount),
                (int) (from.getGreen() + (to.getGreen() - from.getGreen()) * amount),
                (int) (from.getBlue() + (to.getBlue() - from.getBlue()) * amount));
    }

    private static Color withAlpha(Color color, int alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    }
}
