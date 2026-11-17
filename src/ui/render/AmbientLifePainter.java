package ui.render;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.QuadCurve2D;
import java.awt.geom.Rectangle2D;
import java.util.EnumMap;
import java.util.Map;
import util.GameConstants;

/**
 * Draws the life that is only there to be looked at: gulls and clouds in the
 * sky, far-off schools of fish, a turtle, jellyfish and drifting specks in
 * the water, and seagrass, kelp and a crab on the sea floor.
 *
 * None of it takes part in the game, so it must never look like something
 * the player can hook. Everything in the water is drawn as if far away:
 * small, flat, in one see-through colour, without outlines or eyes. The
 * plants and the crab stay down on the sea floor.
 *
 * Where a thing is depends only on the time, the drift and the current that
 * are passed in, so the same values always give the same picture. Each
 * creature is drawn once into pictures of a few poses and copied from then
 * on (see SpriteCache), which keeps a whole school as cheap as one shape.
 */
public final class AmbientLifePainter {

    /**
     * What lives in each kind of scenery: few and dull creatures in the murky
     * shallows, jellyfish in the deep, a turtle and many schools on the reef.
     */
    private enum Cast {
        SHALLOWS(ChapterPalette.Scenery.SHALLOWS, 1, 1, 0, 0, 16, 3,
                new Color(18, 34, 22, 70), new Color(214, 222, 190, 60), new Color(206, 220, 200, 48),
                new Color(236, 238, 226, 150), new Color(66, 104, 62), new Color(60, 96, 58, 120),
                new Color(150, 100, 76), new double[] {44, 152, 270, 404, 522, 652, 758}, new double[] {}, 330),
        WRECK(ChapterPalette.Scenery.WRECK, 3, 2, 0, 1, 9, 2,
                new Color(8, 40, 58, 84), new Color(206, 236, 246, 66), new Color(226, 244, 250, 46),
                new Color(255, 255, 255, 170), new Color(40, 122, 100), new Color(30, 104, 90, 120),
                new Color(176, 96, 70), new double[] {34, 122, 214, 346}, new double[] {14, 272}, 486),
        DEEP(ChapterPalette.Scenery.DEEP, 0, 1, 0, 3, 14, 6,
                new Color(120, 162, 226, 46), new Color(150, 206, 255, 84), new Color(186, 214, 255, 62),
                new Color(140, 156, 196, 110), new Color(44, 66, 112), new Color(64, 92, 150, 120),
                new Color(96, 84, 110), new double[] {}, new double[] {212, 474, 778}, -1),
        REEF(ChapterPalette.Scenery.REEF, 3, 4, 1, 1, 7, 2,
                new Color(6, 58, 112, 86), new Color(226, 244, 255, 70), new Color(255, 255, 255, 50),
                new Color(255, 255, 255, 185), new Color(46, 170, 112), new Color(34, 150, 110, 125),
                new Color(214, 100, 72), new double[] {30, 142, 238, 332, 454, 562, 652, 766},
                new double[] {16, 374, 786}, 300);

        private final ChapterPalette.Scenery scenery;
        private final int gulls;
        private final int schools;
        private final int turtles;
        private final int jellyfish;
        private final int specks;
        private final double speckSink;   // pixels per second the specks sink
        private final Color far;          // far-off animals: the sea's own colour, a little darker or lighter
        private final Color jelly;
        private final Color speck;
        private final Color cloud;
        private final Color grass;
        private final Color kelp;
        private final Color crab;
        private final double[] grassX;    // where the clumps of seagrass stand
        private final double[] kelpX;     // where the tall fronds stand
        private final double crabX;       // the middle of the crab's walk; below 0 = no crab here

        Cast(ChapterPalette.Scenery scenery, int gulls, int schools, int turtles, int jellyfish, int specks,
             double speckSink, Color far, Color jelly, Color speck, Color cloud, Color grass, Color kelp, Color crab,
             double[] grassX, double[] kelpX, double crabX) {
            this.scenery = scenery;
            this.gulls = gulls;
            this.schools = schools;
            this.turtles = turtles;
            this.jellyfish = jellyfish;
            this.specks = specks;
            this.speckSink = speckSink;
            this.far = far;
            this.jelly = jelly;
            this.speck = speck;
            this.cloud = cloud;
            this.grass = grass;
            this.kelp = kelp;
            this.crab = crab;
            this.grassX = grassX;
            this.kelpX = kelpX;
            this.crabX = crabX;
        }
    }

    private static final int W = GameConstants.WORLD_WIDTH;
    private static final int WATER_Y = GameConstants.WATER_Y;
    private static final int SEABED_Y = GameConstants.SEABED_Y;

    private static final Cast[] CAST_LIST = Cast.values();
    private static final Map<ChapterPalette.Scenery, Cast> CASTS = castsByScenery();

    // ---- Sky ----
    private static final int CLOUDS = 2;
    private static final double[] CLOUD_Y = {82, 98};
    private static final double CLOUD_SPEED = 3.5;            // pixels per second: barely moving
    private static final Color CLOUD_SHADE = new Color(120, 130, 150, 40);
    private static final int GULL_STEPS = 9;                  // wing positions from down to up
    private static final Color GULL = new Color(58, 66, 78, 170);
    private static final BasicStroke GULL_STROKE = new BasicStroke(1.3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);

    // ---- Far-off life ----
    private static final Shape TINY_FISH = tinyFish();
    private static final int SCHOOL_FRAMES = 6;               // the fish shift a little from frame to frame
    private static final int SCHOOL_LAYOUTS = 2;              // a small school and a large one
    private static final double[] SCHOOL_Y = {250, 392, 318, 452};
    private static final int TURTLE_STEPS = 8;                // flipper positions
    private static final double TURTLE_EVERY_S = 52;          // a turtle sets out this often
    private static final double TURTLE_CROSSING_S = 36;       // and takes this long to cross
    private static final Shape TURTLE_SHELL = turtleShell();
    private static final Shape TURTLE_HEAD = new Ellipse2D.Double(8.5, -3.4, 7, 5);
    private static final Shape TURTLE_FLIPPER = turtleFlipper();
    private static final int JELLY_STEPS = 8;                 // from bell wide open to pulled in
    private static final double[] JELLY_X = {610, 180, 420};
    private static final double[] JELLY_Y = {300, 380, 230};
    private static final BasicStroke TENTACLE = new BasicStroke(0.9f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    private static final double SPECK_TOP = WATER_Y + 24;
    private static final double SPECK_RANGE = SEABED_Y - 30 - SPECK_TOP;

    // ---- Sea floor ----
    private static final double MAX_CURRENT = 45;             // the strongest current, in pixels per second
    private static final int LEAN_STEPS = 33;                 // from bent fully left to fully right
    private static final int GRASS_LOOKS = 3;
    private static final int KELP_LOOKS = 2;
    private static final double FLOOR_Y = SEABED_Y + 5;       // on the sand in front
    private static final BasicStroke BLADE = new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    private static final BasicStroke STALK = new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    private static final int CRAB_FRAMES = 4;                 // leg positions of one step
    private static final double CRAB_RANGE = 70;              // how far it walks to each side
    private static final BasicStroke CRAB_LEG = new BasicStroke(1.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    private static final Color CRAB_EYE = new Color(30, 24, 22);

    private final SpriteCache cloudSprites = new SpriteCache(CAST_LIST.length * CLOUDS, 56, 18, 8,
            AmbientLifePainter::drawCloud);
    private final SpriteCache gullSprites = new SpriteCache(GULL_STEPS, 9, 8, 7, AmbientLifePainter::drawGull);
    private final SpriteCache schoolSprites = new SpriteCache(CAST_LIST.length * SCHOOL_LAYOUTS * 2 * SCHOOL_FRAMES,
            52, 22, 22, AmbientLifePainter::drawSchool);
    private final SpriteCache turtleSprites = new SpriteCache(CAST_LIST.length * 2 * TURTLE_STEPS, 22, 14, 20,
            AmbientLifePainter::drawTurtle);
    private final SpriteCache jellySprites = new SpriteCache(CAST_LIST.length * JELLY_STEPS, 16, 16, 24,
            AmbientLifePainter::drawJellyfish);
    private final SpriteCache grassSprites = new SpriteCache(CAST_LIST.length * GRASS_LOOKS * LEAN_STEPS, 38, 40, 6,
            AmbientLifePainter::drawGrass);
    private final SpriteCache kelpSprites = new SpriteCache(CAST_LIST.length * KELP_LOOKS * LEAN_STEPS, 56, 86, 6,
            AmbientLifePainter::drawKelp);
    private final SpriteCache crabSprites = new SpriteCache(CAST_LIST.length * CRAB_FRAMES, 15, 14, 3,
            AmbientLifePainter::drawCrab);

    // One reusable square for the specks, so no new shape object is made every frame.
    private final Rectangle2D.Double speckBox = new Rectangle2D.Double();

    /** Things in the sky: gulls, drifting clouds. Drawn right after the background. */
    public void paintSky(Graphics2D g, ChapterPalette palette, double time) {
        Cast cast = CASTS.get(palette.getScenery());
        for (int i = 0; i < CLOUDS; i++) {
            double x = wrap(180 + i * 430 + CLOUD_SPEED * (1 + 0.5 * i) * time, W + 160) - 80;
            cloudSprites.paint(g, x, CLOUD_Y[i], cast.ordinal() * CLOUDS + i);
        }
        for (int i = 0; i < cast.gulls; i++) {
            // Each gull crosses the sky at its own speed and height, rising and falling a little.
            double x = wrap(120 + i * 290 + (15 + 5 * i) * time, W + 40) - 20;
            double y = 76 + 11 * i + 5 * Math.sin(0.6 * time + 2 * i);
            int flap = SpriteCache.step(Math.sin(7 * time + 1.9 * i), GULL_STEPS);
            gullSprites.paint(g, x, y, flap);
        }
    }

    /**
     * Far-off life behind the boat and the game objects: dim schools of tiny fish, a turtle
     * passing now and then, jellyfish, drifting specks.
     *
     * @param driftX how far the current has carried loose things so far, in pixels (it grows and
     *               shrinks as the current changes)
     */
    public void paintBehind(Graphics2D g, ChapterPalette palette, double time, double driftX) {
        Cast cast = CASTS.get(palette.getScenery());
        paintSpecks(g, cast, time, driftX);

        for (int i = 0; i < cast.schools; i++) {
            int facing = i % 2 == 0 ? 1 : -1;
            // Fish swim on their own, so the current only carries them a little.
            double x = wrap(140 + i * 250 + facing * (13 + 4 * i) * time + 0.35 * driftX, W + 200) - 100;
            double y = SCHOOL_Y[i] + 16 * Math.sin(0.21 * time + 1.9 * i);
            int frame = (int) wrap(5 * time + 2 * i, SCHOOL_FRAMES);
            int look = (cast.ordinal() * SCHOOL_LAYOUTS + i % SCHOOL_LAYOUTS) * 2 + (facing > 0 ? 1 : 0);
            schoolSprites.paint(g, x, y, look * SCHOOL_FRAMES + frame);
        }

        for (int i = 0; i < cast.turtles; i++) {
            double trip = Math.floor(time / TURTLE_EVERY_S);          // which crossing this is
            double done = (time - trip * TURTLE_EVERY_S) / TURTLE_CROSSING_S;
            if (done < 1) {
                int facing = trip % 2 == 0 ? 1 : -1;                  // back and forth by turns
                double x = W / 2.0 + facing * (done - 0.5) * (W + 60);
                double y = 250 + 110 * scatter((int) trip) + 14 * Math.sin(9 * done);
                int step = SpriteCache.step(Math.sin(2.4 * time), TURTLE_STEPS);
                turtleSprites.paint(g, x, y, cast.ordinal() * 2 * TURTLE_STEPS + SpriteCache.pose(step, facing));
            }
        }

        for (int i = 0; i < cast.jellyfish; i++) {
            // Jellyfish cannot swim against the water: they go where the current goes.
            double pulse = Math.sin(1.7 * time + 2.2 * i);
            double x = wrap(JELLY_X[i] + 0.8 * driftX + 14 * Math.sin(0.13 * time + i), W + 60) - 30;
            double y = JELLY_Y[i] + 28 * Math.sin(0.09 * time + 1.3 * i) - 2 * pulse;
            jellySprites.paint(g, x, y, cast.ordinal() * JELLY_STEPS + SpriteCache.step(pulse, JELLY_STEPS));
        }
    }

    /**
     * Specks hanging in the water, carried by the current and sinking slowly. They are tiny
     * squares drawn without smoothing, which costs next to nothing.
     */
    private void paintSpecks(Graphics2D g, Cast cast, double time, double driftX) {
        Graphics2D plain = (Graphics2D) g.create();
        plain.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        plain.setColor(cast.speck);
        for (int i = 0; i < cast.specks; i++) {
            double size = 1.4 + scatter(i + 40);
            double x = wrap(scatter(i) * W + (0.6 + 0.4 * scatter(i + 20)) * driftX + 6 * Math.sin(0.5 * time + i), W);
            double y = SPECK_TOP + wrap(scatter(i + 60) * SPECK_RANGE + cast.speckSink * (0.6 + scatter(i + 80)) * time,
                    SPECK_RANGE);
            speckBox.setRect(x, y, size, size);
            plain.fill(speckBox);
        }
        plain.dispose();
    }

    /**
     * Life on the sea floor, drawn in front of the game objects: swaying seagrass and kelp, a crab.
     *
     * @param current how hard the water is flowing sideways right now, in pixels per second, about
     *                -45 to 45; plants lean with it
     */
    public void paintFront(Graphics2D g, ChapterPalette palette, double time, double current) {
        Cast cast = CASTS.get(palette.getScenery());
        double push = 0.7 * Math.max(-1, Math.min(1, current / MAX_CURRENT));

        for (int i = 0; i < cast.kelpX.length; i++) {
            // The current bends a plant over; on top of that each one sways on its own rhythm.
            double lean = push + 0.3 * Math.sin(0.9 * time + 0.05 * cast.kelpX[i]);
            int look = cast.ordinal() * KELP_LOOKS + i % KELP_LOOKS;
            kelpSprites.paint(g, cast.kelpX[i], FLOOR_Y, look * LEAN_STEPS + SpriteCache.step(lean, LEAN_STEPS));
        }
        for (int i = 0; i < cast.grassX.length; i++) {
            double lean = push + 0.3 * Math.sin(1.3 * time + 0.05 * cast.grassX[i]);
            int look = cast.ordinal() * GRASS_LOOKS + i % GRASS_LOOKS;
            grassSprites.paint(g, cast.grassX[i], FLOOR_Y, look * LEAN_STEPS + SpriteCache.step(lean, LEAN_STEPS));
        }
        if (cast.crabX >= 0) {
            // The crab walks to one end, waits, and walks back. Its legs move with the distance covered.
            double walked = CRAB_RANGE * Math.max(-1, Math.min(1, 1.5 * Math.sin(0.11 * time)));
            int frame = (int) wrap(0.6 * walked, CRAB_FRAMES);
            crabSprites.paint(g, cast.crabX + walked, FLOOR_Y, cast.ordinal() * CRAB_FRAMES + frame);
        }
    }

    // ---- The pictures: each method draws one pose around (0, 0) ----

    /** A puffy cloud: a few round shapes joined into one, a little darker underneath. */
    private static void drawCloud(Graphics2D g, int pose) {
        double[][] puffs = pose % CLOUDS == 0
                ? new double[][] {{-44, -4, 40, 12}, {-26, -12, 34, 20}, {-4, -16, 36, 24}, {18, -9, 30, 17}}
                : new double[][] {{-34, -3, 30, 10}, {-18, -10, 30, 17}, {4, -7, 30, 14}};
        Area cloud = new Area();
        for (double[] puff : puffs) {
            cloud.add(new Area(new Ellipse2D.Double(puff[0], puff[1], puff[2], puff[3])));
        }
        cloud.intersect(new Area(new Rectangle2D.Double(-60, -30, 120, 34)));   // a flat base
        g.setColor(CAST_LIST[pose / CLOUDS].cloud);
        g.fill(cloud);
        g.clip(cloud);
        g.setColor(CLOUD_SHADE);
        g.fill(new Rectangle2D.Double(-60, -1, 120, 6));
    }

    /** A gull far away: just the line of its two wings. */
    private static void drawGull(Graphics2D g, int pose) {
        double tip = -4.5 * SpriteCache.swingOf(pose, GULL_STEPS);   // how high the wing tips are
        Path2D wings = new Path2D.Double();
        wings.moveTo(-7, tip);
        wings.quadTo(-3.5, tip * 0.3 - 2.5, 0, 0);
        wings.quadTo(3.5, tip * 0.3 - 2.5, 7, tip);
        g.setColor(GULL);
        g.setStroke(GULL_STROKE);
        g.draw(wings);
    }

    /** A school of tiny fish, spread over an oval. In each frame every fish has moved a little. */
    private static void drawSchool(Graphics2D g, int pose) {
        int frame = pose % SCHOOL_FRAMES;
        int look = pose / SCHOOL_FRAMES;
        int layout = look / 2 % SCHOOL_LAYOUTS;
        boolean large = layout == 1;
        double beat = 2 * Math.PI * frame / SCHOOL_FRAMES;
        g.scale(SpriteCache.facingOf(look), 1);
        g.setColor(CAST_LIST[look / 2 / SCHOOL_LAYOUTS].far);
        AffineTransform middle = g.getTransform();
        int fishCount = large ? 15 : 9;
        for (int i = 0; i < fishCount; i++) {
            int seed = i * 7 + layout * 100;
            double angle = 2 * Math.PI * scatter(seed);
            double reach = Math.sqrt(scatter(seed + 1));
            double size = 0.75 + 0.5 * scatter(seed + 2);
            g.translate(Math.cos(angle) * reach * (large ? 40 : 28) + 1.6 * Math.cos(beat + 2.4 * i),
                    Math.sin(angle) * reach * (large ? 15 : 11) + 1.1 * Math.sin(beat + 1.7 * i));
            g.scale(size, size);
            g.fill(TINY_FISH);
            g.setTransform(middle);
        }
    }

    /** A sea turtle from the side: shell, head and two flippers, joined into one flat shape. */
    private static void drawTurtle(Graphics2D g, int pose) {
        int look = pose % (2 * TURTLE_STEPS);
        double stroke = SpriteCache.swingOf(SpriteCache.stepOf(look), TURTLE_STEPS);
        Area turtle = new Area(TURTLE_SHELL);
        turtle.add(new Area(TURTLE_HEAD));
        // The front flipper sweeps like a wing; the small rear one steers.
        turtle.add(new Area(AffineTransform.getRotateInstance(0.8 + 0.55 * stroke, 5, 1)
                .createTransformedShape(TURTLE_FLIPPER)));
        AffineTransform rear = AffineTransform.getTranslateInstance(-13, 1);
        rear.rotate(1.2 - 0.2 * stroke, 5, 1);
        rear.scale(0.55, 0.55);
        turtle.add(new Area(rear.createTransformedShape(TURTLE_FLIPPER)));
        g.scale(SpriteCache.facingOf(look), 1);
        g.setColor(CAST_LIST[pose / (2 * TURTLE_STEPS)].far);
        g.fill(turtle);
    }

    /** A jellyfish: a bell that pulls in and opens again, trailing four thin tentacles. */
    private static void drawJellyfish(Graphics2D g, int pose) {
        double squeeze = SpriteCache.swingOf(pose % JELLY_STEPS, JELLY_STEPS);   // 1 = pulled in
        double half = 7 * (1 - 0.16 * squeeze);
        double height = 8.5 * (1 + 0.12 * squeeze);
        Path2D bell = new Path2D.Double();
        bell.moveTo(-half, 0);
        bell.curveTo(-half, -height * 1.3, half, -height * 1.3, half, 0);
        bell.quadTo(0, -2.5, -half, 0);
        bell.closePath();
        Area jelly = new Area(bell);
        for (int i = 0; i < 4; i++) {
            double x = half * (-0.66 + 0.44 * i);
            double sway = 1.6 + 1.2 * squeeze;
            jelly.add(new Area(TENTACLE.createStrokedShape(new CubicCurve2D.Double(
                    x, -1.5, x - sway, 5, x + sway, 10, x - 0.4 * x * squeeze, 15 + 4 * scatter(i + 7)))));
        }
        g.setColor(CAST_LIST[pose / JELLY_STEPS].jelly);
        g.fill(jelly);
    }

    /** A clump of seagrass: blades from one root, all bent the same way by the current. */
    private static void drawGrass(Graphics2D g, int pose) {
        double lean = SpriteCache.swingOf(pose % LEAN_STEPS, LEAN_STEPS);
        int look = pose / LEAN_STEPS % GRASS_LOOKS;
        Color color = CAST_LIST[pose / LEAN_STEPS / GRASS_LOOKS].grass;
        int blades = 5 + look;
        g.setStroke(BLADE);
        for (int i = 0; i < blades; i++) {
            double root = (i - (blades - 1) / 2.0) * 2.6;
            double height = 15 + 13 * scatter(look * 20 + i);
            double tipX = root * 2.4 + lean * height * 0.55;
            double tipY = -height * (1 - 0.2 * lean * lean);   // a bent blade does not reach as high
            g.setColor(i % 2 == 0 ? color : color.darker());
            g.draw(new QuadCurve2D.Double(root, 3, root * 1.4 + lean * height * 0.1, -height * 0.55, tipX, tipY));
        }
    }

    /**
     * A tall frond of kelp: a thin stalk with small leaves to both sides. It is joined into one
     * shape and filled once, so the see-through colour is even where leaves overlap.
     */
    private static void drawKelp(Graphics2D g, int pose) {
        double lean = SpriteCache.swingOf(pose % LEAN_STEPS, LEAN_STEPS);
        int look = pose / LEAN_STEPS % KELP_LOOKS;
        double height = look == 0 ? 62 : 78;
        CubicCurve2D stalk = new CubicCurve2D.Double(0, 4, -3 + lean * height * 0.04, -height * 0.35,
                4 + lean * height * 0.28, -height * 0.7, lean * height * 0.42, -height);
        Area frond = new Area(STALK.createStrokedShape(stalk));
        int leaves = look == 0 ? 7 : 9;
        for (int i = 1; i <= leaves; i++) {
            double t = i / (leaves + 0.3);
            double u = 1 - t;
            // The point on the stalk where this leaf grows.
            double x = u * u * u * stalk.getX1() + 3 * u * u * t * stalk.getCtrlX1() + 3 * u * t * t * stalk.getCtrlX2()
                    + t * t * t * stalk.getX2();
            double y = u * u * u * stalk.getY1() + 3 * u * u * t * stalk.getCtrlY1() + 3 * u * t * t * stalk.getCtrlY2()
                    + t * t * t * stalk.getY2();
            int side = i % 2 == 0 ? 1 : -1;
            AffineTransform place = AffineTransform.getTranslateInstance(x, y);
            place.rotate(-Math.PI / 2 + side * 0.95 + lean * 0.7);
            frond.add(new Area(place.createTransformedShape(new Ellipse2D.Double(0, -1.6, 11 - 4 * t, 3.2))));
        }
        g.setColor(CAST_LIST[pose / LEAN_STEPS / KELP_LOOKS].kelp);
        g.fill(frond);
    }

    /** A small crab: a round shell, two raised claws, eyes on stalks and six legs in mid-step. */
    private static void drawCrab(Graphics2D g, int pose) {
        int frame = pose % CRAB_FRAMES;
        Color shell = CAST_LIST[pose / CRAB_FRAMES].crab;
        g.setColor(shell.darker());
        g.setStroke(CRAB_LEG);
        for (int side = -1; side <= 1; side += 2) {
            for (int leg = 0; leg < 3; leg++) {
                int beat = (frame + 2 * leg + (side > 0 ? 0 : 1)) % CRAB_FRAMES;   // neighbouring legs take turns
                Path2D limb = new Path2D.Double();
                limb.moveTo(side * (3 + leg), -4);
                limb.lineTo(side * (7 + 1.5 * leg), -6.5);
                limb.lineTo(side * (8.5 + 2 * leg) + (beat < 2 ? 1 : -1), beat == 0 ? -1.5 : 0);
                g.draw(limb);
            }
            g.draw(new QuadCurve2D.Double(side * 4.5, -6, side * 8, -6, side * 8.5, -9.5));   // the arm of a claw
        }
        g.setColor(shell);
        g.fill(new Ellipse2D.Double(-11, -12.5, 5, 4));
        g.fill(new Ellipse2D.Double(6, -12.5, 5, 4));
        g.fill(new Ellipse2D.Double(-6, -8.5, 12, 7));
        g.setColor(shell.brighter());
        g.fill(new Ellipse2D.Double(-4, -8, 8, 2.4));
        g.setColor(CRAB_EYE);
        g.fill(new Ellipse2D.Double(-3.2, -10.6, 1.8, 2.6));
        g.fill(new Ellipse2D.Double(1.4, -10.6, 1.8, 2.6));
    }

    // ---- Shapes made once ----

    /** A fish as seen from far away: a body and a tail, about 9 pixels long, facing right. */
    private static Shape tinyFish() {
        Path2D fish = new Path2D.Double();
        fish.moveTo(4, 0);
        fish.quadTo(1, -2.2, -2.5, -0.4);
        fish.lineTo(-5, -2);
        fish.lineTo(-4.4, 0);
        fish.lineTo(-5, 2);
        fish.lineTo(-2.5, 0.4);
        fish.quadTo(1, 2.2, 4, 0);
        fish.closePath();
        return fish;
    }

    /** The turtle's shell from the side: a dome on top, flatter underneath. */
    private static Shape turtleShell() {
        Path2D shell = new Path2D.Double();
        shell.moveTo(-10, 1);
        shell.curveTo(-9, -8.5, 7, -8.5, 10, 0);
        shell.curveTo(6, 4.5, -6, 4.5, -10, 1);
        shell.closePath();
        return shell;
    }

    /** A flipper hanging straight down from the shoulder at (5, 1); it is turned into place when drawn. */
    private static Shape turtleFlipper() {
        Path2D flipper = new Path2D.Double();
        flipper.moveTo(3, 1);
        flipper.quadTo(2.5, 9, 5, 14);
        flipper.quadTo(8.5, 8, 7, 1);
        flipper.closePath();
        return flipper;
    }

    // ---- Small helpers ----

    private static Map<ChapterPalette.Scenery, Cast> castsByScenery() {
        Map<ChapterPalette.Scenery, Cast> casts = new EnumMap<>(ChapterPalette.Scenery.class);
        for (Cast cast : CAST_LIST) {
            casts.put(cast.scenery, cast);
        }
        return casts;
    }

    /** Brings a value into the range 0 to span, so that what leaves on one side comes back on the other. */
    private static double wrap(double value, double span) {
        return value - Math.floor(value / span) * span;
    }

    /** A number from 0 to 1 that looks random but is always the same for the same n. */
    private static double scatter(int n) {
        double wild = Math.sin(n * 12.9898 + 4.1) * 43758.5453;
        return wild - Math.floor(wild);
    }
}
