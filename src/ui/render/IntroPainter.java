package ui.render;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Path2D;
import java.util.function.Consumer;
import model.Condition;
import model.EquipmentType;
import model.ObjectKind;
import model.ObjectView;
import util.GameConstants;
import util.Mathx;
import util.StoryText;

/**
 * Draws the opening animation, one scene at a time: the healthy sanctuary,
 * a fisher who turns back at its edge, the storm that fills it with trash,
 * the cleanup boat and its permit, a protected fish near the hook, and the
 * title.
 *
 * Every picture is worked out from the scene's own clock, so the same moment
 * always looks the same. The sea, the boat, the fish and the trash are drawn
 * by the game's own painters; this class only places them, moves the camera,
 * and adds what exists only in the opening: buoys, a fishing canoe, rain,
 * lightning, the permit and the title.
 */
public final class IntroPainter {

    // ---- Moments inside the scenes, in seconds. The opening plays its sounds at the same moments. ----
    /** In the storm: the lightning strike that hides the change from a healthy reef to a choked one. */
    public static final double STORM_FLASH_S = 2.8;
    /** In the permit scene: when the stamp comes down. */
    public static final double STAMP_AT_S = 4.1;
    /** In the protected-fish scene: from when the fish is close to the hook... */
    public static final double DANGER_FROM_S = 2.0;
    /** ...until when. */
    public static final double DANGER_TO_S = 3.8;
    /** In the title scene: when the title appears. */
    public static final double TITLE_AT_S = 0.3;

    // ---- The frame: black bars above and below the picture, like a film ----
    private static final int TOP_BAR = 34;
    private static final int BOTTOM_BAR = 512;
    private static final int W = GameConstants.WORLD_WIDTH;
    private static final int H = GameConstants.WORLD_HEIGHT;
    private static final int WATER_Y = GameConstants.WATER_Y;
    private static final double FADE_S = 0.4;           // the dip to black between scenes
    private static final double CAPTION_FADE_S = 0.45;
    private static final double REEF_VIEW = 74;         // how far the camera looks down to see the sea floor

    // ---- Where things are ----
    private static final int SIGN_X = 430;              // the buoy with the no-take sign
    private static final int[] MARKER_X = {150, 660};   // the plain marker buoys
    private static final int BOAT_X = 400;              // where M/B Tala stops
    private static final double ROPE = 64;              // the line under the boat in the protected-fish scene
    private static final double SWING = 0.9;            // the widest swing of that line, in radians
    private static final double STORM_SWELL = 0.22;     // the storm's waves, as the swell the game would give them
    private static final double STORM_CURRENT = 32;     // the storm's current, in pixels per second

    // The school of fusiliers: where each fish swims next to the leader.
    private static final double[] SCHOOL_DX = {0, -26, -30, -52, -56, -60, -84};
    private static final double[] SCHOOL_DY = {0, -18, 16, -28, 2, 30, -8};

    // The trash the storm brings: when each piece hits the water, where, and how deep it sinks.
    private static final double[] SINK_START = {0.9, 1.2, 1.5, 1.8, 2.1, 2.4, 3.1, 3.5};
    private static final double[] SINK_X = {180, 520, 330, 640, 90, 450, 260, 710};
    private static final double[] SINK_DEPTH = {450, 548, 380, 545, 500, 420, 550, 470};
    private static final double SINK_S = 3.0;

    // The painters' number of designs per kind (see ObjectPainter.designOf).
    private static final int FISH_DESIGNS = 5;
    private static final int TRASH_DESIGNS = 5;
    private static final int NET_DESIGNS = 4;
    private static final int CLOWNFISH = 0;
    private static final int BLUE_TANG = 1;
    private static final int BUTTERFLYFISH = 2;
    private static final int FUSILIER = 4;
    private static final int GROUPER = 0;
    private static final int PARROTFISH = 1;

    // ---- Colours and strokes ----
    private static final Color OUTLINE = new Color(30, 30, 34);
    private static final Color BUOY = new Color(250, 190, 40);
    private static final Color BUOY_BAND = new Color(214, 58, 48);
    private static final Color POLE = new Color(90, 84, 78);
    private static final Color FLOAT = new Color(245, 140, 40);
    private static final Color SIGN = new Color(248, 246, 240);
    private static final Color SIGN_RED = new Color(200, 40, 40);
    private static final Color SIGN_FISH = new Color(60, 120, 170);
    private static final Color CANOE_HULL = new Color(92, 64, 44);
    private static final Color CANOE_STRIPE = new Color(70, 150, 200);
    private static final Color BAMBOO = new Color(206, 180, 110);
    private static final Color BAMBOO_DARK = new Color(130, 108, 60);
    private static final Color SHIRT = new Color(200, 70, 60);
    private static final Color SKIN = new Color(176, 120, 84);
    private static final Color SALAKOT = new Color(222, 196, 120);
    private static final Color STORM_SKY = new Color(18, 24, 38);
    private static final Color STORM_SEA = new Color(10, 20, 26);
    private static final Color RAIN = new Color(220, 230, 240);
    private static final Color DAWN = new Color(255, 170, 90);
    private static final Color PAPER = new Color(250, 244, 226);
    private static final Color PAPER_EDGE = new Color(190, 170, 130);
    private static final Color APPROVED = new Color(30, 140, 80);
    private static final Color WARNING = new Color(255, 70, 60);
    private static final Color ROPE_EDGE = new Color(30, 30, 30);
    private static final Color ROPE_CORE = new Color(225, 215, 190);
    private static final BasicStroke THIN = new BasicStroke(1);
    private static final BasicStroke MEDIUM = new BasicStroke(2);
    private static final BasicStroke THICK = new BasicStroke(3);
    private static final BasicStroke ROUND = new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    private static final BasicStroke BOLT_GLOW = new BasicStroke(7, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    private static final BasicStroke BOLT_CORE = new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    private static final BasicStroke RAIN_STROKE = new BasicStroke(1.2f);
    private static final Shape CANOE = canoeShape();
    private static final Shape BOOM_FRONT = boom(26);
    private static final Shape BOOM_BACK = boom(-26);

    private final SceneRenderer renderer;
    private final PainterRegistry painters;
    private final BoatPainter boatPainter = new BoatPainter();
    private final Path2D.Double bolt = new Path2D.Double();   // reused for every lightning strike

    // The actors. They are not part of any game: the script places them every frame.
    private final Prop[] school = new Prop[SCHOOL_DX.length];
    private final Prop clownfish = new Prop(ObjectKind.SMALL_FISH, CLOWNFISH, FISH_DESIGNS, 1);
    private final Prop butterflyfish = new Prop(ObjectKind.SMALL_FISH, BUTTERFLYFISH, FISH_DESIGNS, 2);
    private final Prop blueTang = new Prop(ObjectKind.SMALL_FISH, BLUE_TANG, FISH_DESIGNS, 3);
    private final Prop grouper = new Prop(ObjectKind.BIG_FISH, GROUPER, FISH_DESIGNS, 1);
    private final Prop parrotfish = new Prop(ObjectKind.BIG_FISH, PARROTFISH, FISH_DESIGNS, 2);
    private final Prop[] trash = new Prop[SINK_START.length];
    private final Prop ghostNet = new Prop(ObjectKind.FISHING_NET, 1, NET_DESIGNS, 0);

    public IntroPainter(SceneRenderer renderer) {
        this.renderer = renderer;
        this.painters = renderer.getPainters();
        for (int i = 0; i < school.length; i++) {
            school[i] = new Prop(ObjectKind.SMALL_FISH, FUSILIER, FISH_DESIGNS, i + 4);
        }
        for (int i = 0; i < trash.length; i++) {
            // Bottles and cans take turns, each in another design.
            ObjectKind kind = (i % 2 == 0) ? ObjectKind.PLASTIC_BOTTLE : ObjectKind.CAN;
            trash[i] = new Prop(kind, (i / 2) % TRASH_DESIGNS, TRASH_DESIGNS, i);
        }
    }

    // =====================================================================
    // The scenes. t is the scene's own clock, in seconds.
    // =====================================================================

    /** A healthy reef. The camera tilts down from the sanctuary buoys to the corals. */
    public void paintSanctuary(Graphics2D g, double t) {
        double time = renderer.getTime();
        double camY = REEF_VIEW * smooth(ramp(t, 0.8, 3.2));
        paintWorld(g, ChapterPalette.REEF, camY, 0, 0, world -> {
            paintMarkers(world, time);
            paintSchool(world, 120 + 45 * t, 300, 1, time);
            paint(world, parrotfish.at(720 - 30 * t, 385, -1, -30), time);
            paint(world, butterflyfish.at(470 - 16 * t, 440 + 4 * Math.sin(t), -1, -16), time);
            paint(world, blueTang.at(240 + 22 * t, 462, 1, 22), time);
            paint(world, clownfish.at(615 + 12 * Math.sin(0.9 * t), 478, -1, -6), time);
        });
        paintPlaceCard(g, t);
    }

    /** A fishing canoe comes up to the edge of the sanctuary, sees the sign, and turns back. */
    public void paintNoTake(Graphics2D g, double t) {
        double time = renderer.getTime();
        double canoeX;
        int canoeFacing;
        if (t < 2.2) {
            canoeX = 880 - 360 * Mathx.easeOut(t / 2.2);
            canoeFacing = -1;
        } else if (t < 2.9) {
            canoeX = 520;   // stops at the sign
            canoeFacing = -1;
        } else {
            canoeX = 520 + 60 * (t - 2.9) * (t - 2.9);   // turned round, it speeds away
            canoeFacing = 1;
        }
        paintWorld(g, ChapterPalette.REEF, 24, 0, 0, world -> {
            paintSchool(world, 340 + 45 * t, 330, 1, time);
            paint(world, grouper.at(130 + 18 * t, 440, 1, 18), time);
            paint(world, clownfish.at(600 - 10 * t, 462, -1, -10), time);
            paint(world, butterflyfish.at(250 + 14 * t, 400, 1, 14), time);
            paintMarkers(world, time);
            paintCanoe(world, canoeX, canoeFacing, time);
        });
    }

    /**
     * A storm darkens the reef and sends fish fleeing while trash rains into
     * the water. At the big lightning strike the picture cuts to the same
     * place years later: a murky sea floor covered in trash.
     */
    public void paintStorm(Graphics2D g, double t) {
        double time = renderer.getTime();
        boolean after = t >= STORM_FLASH_S;
        double storm = smooth(ramp(t, 0, 1.4)) * (1 - 0.45 * smooth(ramp(t, 4.6, 1.8)));
        double camY = after ? REEF_VIEW : 0;
        ChapterPalette palette = after ? ChapterPalette.forChapter(1) : ChapterPalette.REEF;

        paintWorld(g, palette, camY, STORM_SWELL * storm, STORM_CURRENT * storm, world -> {
            if (!after) {
                paintSchool(world, 330 - (40 * t + 55 * t * t), 300, -1, time);   // the fish flee
                paint(world, parrotfish.at(600 + 60 * t + 40 * t * t, 390, 1, 60 + 80 * t), time);
            }
            paint(world, ghostNet.at(-80 + 40 * t, 320 + 8 * Math.sin(0.7 * t), 1, 40, ramp(t, 0.6, 0.6)), time);
            for (int i = 0; i < trash.length; i++) {
                double since = t - SINK_START[i];
                if (since > 0) {
                    double sunk = Mathx.easeOut(Math.min(1, since / SINK_S));
                    double x = SINK_X[i] + 9 * Math.sin(1.6 * since + i) * (1 - 0.5 * sunk) + 10 * since * storm;
                    double y = WATER_Y + 6 + (SINK_DEPTH[i] - WATER_Y - 6) * sunk;
                    paint(world, trash[i].at(x, y, (i % 3 == 0) ? -1 : 1, 0, ramp(since, 0, 0.25)), time);
                }
            }
        });

        int skyBottom = (int) Math.round(WATER_Y - camY);
        shade(g, STORM_SKY, 0.62 * storm, 0, skyBottom);
        shade(g, STORM_SEA, 0.28 * storm, skyBottom, BOTTOM_BAR);
        paintRain(g, t, storm, skyBottom);
        if (t >= 1.55 && t < 1.68) {   // a far-off flicker first
            shade(g, Color.WHITE, 0.18, 0, skyBottom);
            paintBolt(g, 640, skyBottom, 3, 0.6);
        }
        if (t >= STORM_FLASH_S - 0.14 && t < STORM_FLASH_S) {
            paintBolt(g, 300, skyBottom, 7, 1);
        }
        if (after) {
            shade(g, Color.WHITE, 1 - ramp(t, STORM_FLASH_S, 0.7), 0, H);
        }
    }

    /** Dawn after the storm. M/B Tala sails in, and the town's cleanup permit is stamped. */
    public void paintPermit(Graphics2D g, double t) {
        double time = renderer.getTime();
        double arrived = Mathx.easeOut(ramp(t, 0, 3.0));
        double boatX = -80 + (BOAT_X + 80) * arrived;
        paintWorld(g, ChapterPalette.forChapter(1), 0, 0, 0, world -> {
            paint(world, trash[0].at(160 + 6 * t, 152 + 2 * Math.sin(time), 1, 6), time);
            paint(world, trash[2].at(575 - 4 * t, 160 + 2 * Math.sin(time + 1), -1, -4), time);
            paint(world, trash[4].at(700, 148 + 2 * Math.sin(time + 2), 1, 0), time);
            paint(world, trash[1].at(250, 470, 1, 0), time);
            paint(world, trash[3].at(530, 505, -1, 0), time);
            paint(world, trash[5].at(660, 430, 1, 0), time);
            paintWake(world, boatX, 1 - arrived);
            boatPainter.paint(world, boatX, WATER_Y, 1, time);
        });
        shade(g, DAWN, 0.16, 0, WATER_Y);
        paintPermitCard(g, t);
    }

    /** M/B Tala's line swings, and a protected clownfish swims right past it. */
    public void paintProtected(Graphics2D g, double t) {
        double time = renderer.getTime();
        double angle = SWING * Math.sin(1.8 * t + 0.6);
        double tipX = BOAT_X + Math.sin(angle) * ROPE;
        double tipY = WATER_Y + Math.cos(angle) * ROPE;
        double fishX = 60 + 110 * t;
        double fishY = 222 + 5 * Math.sin(2 * t);
        double danger = dangerAt(t);
        paintWorld(g, ChapterPalette.forChapter(1), 10, 0, 0, world -> {
            paint(world, trash[2].at(170, 156 + 2 * Math.sin(time), 1, 0), time);
            paint(world, trash[7].at(640, 480, -1, 0), time);
            boatPainter.paint(world, BOAT_X, WATER_Y, 1, time);
            paintLine(world, tipX, tipY);
            paint(world, clownfish.at(fishX, fishY, 1, 110), time);

            Graphics2D tool = (Graphics2D) world.create();
            tool.translate(tipX, tipY);
            tool.rotate(-angle);   // drawn hanging straight down, then turned to the line's angle
            painters.paintTool(tool, EquipmentType.HOOK, time, false);
            tool.dispose();

            if (danger > 0) {
                paintWarning(world, fishX, fishY, clownfish.getRadius(), danger, time);
            }
        });
        paintMiniPermit(g, danger, time);
    }

    /** The title over the boat at rest: the game's name, its promise, and the first chapter. */
    public void paintTitle(Graphics2D g, double t) {
        double time = renderer.getTime();
        paintWorld(g, ChapterPalette.forChapter(1), 0, 0, 0,
                world -> boatPainter.paint(world, BOAT_X, WATER_Y, 1, time));
        shade(g, Color.BLACK, 0.5 * smooth(ramp(t, 0, 0.8)), 0, H);

        double name = smooth(ramp(t, TITLE_AT_S, 0.7));
        if (name > 0) {
            Graphics2D title = faded(g, name);
            title.translate(W / 2.0, 250);
            double grow = 1.12 - 0.12 * name;   // it settles into place as it appears
            title.scale(grow, grow);
            title.setFont(Fonts.TITLE);
            title.setColor(Color.BLACK);
            TextDraw.centered(title, StoryText.GAME_TITLE, 3, 25);
            title.setColor(UiPalette.TEXT);
            TextDraw.centered(title, StoryText.GAME_TITLE, 0, 22);
            title.dispose();
        }
        double tagline = smooth(ramp(t, 1.1, 0.6));
        if (tagline > 0) {
            Graphics2D line = faded(g, tagline);
            line.setFont(Fonts.BODY);
            line.setColor(UiPalette.DIM_TEXT);
            TextDraw.centered(line, StoryText.TAGLINE, W / 2, 300);
            line.dispose();
        }
        double divider = smooth(ramp(t, 1.6, 0.6));
        if (divider > 0) {
            g.setColor(UiPalette.GOLD);
            g.setStroke(MEDIUM);
            int half = (int) Math.round(130 * divider);
            g.drawLine(W / 2 - half, 324, W / 2 + half, 324);
            g.setStroke(THIN);
        }
        double chapter = smooth(ramp(t, 1.8, 0.6));
        if (chapter > 0) {
            Graphics2D heading = faded(g, chapter);
            heading.setFont(Fonts.HEADING);
            heading.setColor(UiPalette.ACCENT);
            TextDraw.centered(heading, StoryText.chapterTitle(1), W / 2, 364);
            heading.dispose();
        }
    }

    /**
     * What every scene has: a dip to black at its start and end, the black
     * bars, the line of the opening under the picture, and where the player
     * is in the opening.
     *
     * @param caption the scene's line, or "" for the title, which asks to begin instead
     * @param last    true for the last scene, which does not dip out (the screen fades on its own)
     */
    public void paintFrame(Graphics2D g, String caption, int scene, int scenes, double t, double seconds,
                           boolean last) {
        double dark = 1 - ramp(t, 0, FADE_S);
        if (!last) {
            dark = Math.max(dark, ramp(t, seconds - FADE_S, FADE_S));
        }
        shade(g, Color.BLACK, dark, 0, H);

        g.setColor(Color.BLACK);
        g.fillRect(0, 0, W, TOP_BAR);
        g.fillRect(0, BOTTOM_BAR, W, H - BOTTOM_BAR);

        // One dot per scene, the current one lit.
        for (int i = 0; i < scenes; i++) {
            g.setColor(i == scene ? UiPalette.ACCENT : UiPalette.PANEL_EDGE);
            g.fillOval(20 + 14 * i, 13, 8, 8);
        }
        g.setFont(Fonts.SMALL);
        g.setColor(UiPalette.DIM_TEXT);
        TextDraw.rightAligned(g, StoryText.INTRO_PROMPT, W - 20, 22);

        double shown;
        String text;
        Font font;
        if (caption.isEmpty()) {
            // The title asks to begin, with a slow pulse.
            shown = ramp(t, 2.4, 0.5) * (0.6 + 0.4 * Math.sin(4 * t));
            text = StoryText.INTRO_BEGIN;
            font = Fonts.LABEL;
        } else {
            shown = ramp(t, CAPTION_FADE_S, CAPTION_FADE_S) * (1 - ramp(t, seconds - CAPTION_FADE_S, CAPTION_FADE_S));
            text = caption;
            font = Fonts.BODY;
        }
        if (shown > 0) {
            Graphics2D words = faded(g, shown);
            words.setFont(font);
            words.setColor(caption.isEmpty() ? UiPalette.ACCENT : UiPalette.TEXT);
            TextDraw.centered(words, text, W / 2, 562);
            words.dispose();
        }
    }

    // =====================================================================
    // Pieces of the scenes
    // =====================================================================

    /** The scene's world, seen with the camera moved camY pixels down. */
    private void paintWorld(Graphics2D g, ChapterPalette palette, double camY, double swell, double current,
                            Consumer<Graphics2D> middle) {
        Graphics2D world = (Graphics2D) g.create();
        world.translate(0, -camY);
        renderer.paintScene(world, palette, swell, current, middle);
        world.dispose();
    }

    private void paint(Graphics2D g, Prop prop, double time) {
        painters.paint(g, prop, time);
    }

    /** The fusiliers, in a loose school that sways as it swims. */
    private void paintSchool(Graphics2D g, double leaderX, double y, int facing, double time) {
        for (int i = 0; i < school.length; i++) {
            double x = leaderX + facing * SCHOOL_DX[i];
            double fishY = y + SCHOOL_DY[i] + 5 * Math.sin(1.3 * time + 1.7 * i);
            paint(g, school[i].at(x, fishY, facing, 45 * facing), time);
        }
    }

    /** "BANTAY REEF" in the top corner, like a place name at the start of a film. */
    private void paintPlaceCard(Graphics2D g, double t) {
        double shown = smooth(ramp(t, 0.7, 0.5)) * (1 - smooth(ramp(t, 4.2, 0.6)));
        if (shown <= 0) {
            return;
        }
        Graphics2D card = faded(g, shown);
        card.setColor(UiPalette.PANEL);   // a dark backing, so it reads on a bright sky
        card.fillRoundRect(18, 50, 190, 52, 10, 10);
        card.setColor(UiPalette.ACCENT);
        card.fillRect(26, 56, 3, 40);
        card.setFont(Fonts.SUBHEADING);
        card.setColor(UiPalette.TEXT);
        card.drawString(StoryText.INTRO_PLACE, 38, 74);
        card.setFont(Fonts.SMALL);
        card.setColor(UiPalette.DIM_TEXT);
        card.drawString(StoryText.INTRO_PLACE_SUB, 38, 92);
        card.dispose();
    }

    /** The edge of the sanctuary: a line of small floats, two marker buoys and the buoy with the sign. */
    private void paintMarkers(Graphics2D g, double time) {
        g.setColor(FLOAT);
        for (int x = 40; x <= W - 40; x += 24) {
            int y = (int) Math.round(WATER_Y - 2 + 1.5 * Math.sin(1.7 * time + x * 0.03));
            g.fillOval(x - 3, y - 3, 7, 6);
        }
        for (int x : MARKER_X) {
            paintBuoy(g, x, time, false);
        }
        paintBuoy(g, SIGN_X, time, true);
    }

    /** A buoy half in the water: a flag on top, or the no-take sign. */
    private void paintBuoy(Graphics2D g, int x, double time, boolean sign) {
        Graphics2D b = (Graphics2D) g.create();
        b.translate(x, WATER_Y + 2 * Math.sin(1.7 * time + x * 0.03));
        b.rotate(0.05 * Math.sin(1.3 * time + x));
        b.setColor(POLE);
        b.fillRect(-1, sign ? -42 : -36, 3, 28);
        b.setColor(BUOY);
        b.fillRoundRect(-9, -16, 18, 22, 8, 8);
        b.setColor(BUOY_BAND);
        b.fillRect(-9, -10, 18, 5);
        b.setColor(OUTLINE);
        b.setStroke(THIN);
        b.drawRoundRect(-9, -16, 18, 22, 8, 8);
        if (sign) {
            paintSign(b);
        } else {
            b.setColor(BUOY_BAND);
            b.fillPolygon(new int[] {2, 16, 2}, new int[] {-36, -31, -26}, 3);
        }
        b.dispose();
    }

    /** The white board on the sign buoy: a crossed-out fish and "NO-TAKE ZONE". Its foot is at (0, -42). */
    private void paintSign(Graphics2D b) {
        b.setColor(SIGN);
        b.fillRect(-64, -68, 128, 26);
        b.setColor(SIGN_RED);
        b.setStroke(MEDIUM);
        b.drawRect(-64, -68, 128, 26);
        b.setColor(SIGN_FISH);
        b.fillOval(-58, -59, 12, 7);
        b.fillPolygon(new int[] {-47, -41, -41}, new int[] {-55, -59, -51}, 3);
        b.setColor(SIGN_RED);
        b.drawLine(-58, -50, -42, -62);
        b.setFont(Fonts.LABEL);
        TextDraw.centered(b, StoryText.INTRO_SIGN, 9, -50);
        b.setStroke(THIN);
    }

    /** A small outrigger canoe with a fisher in a salakot hat holding a rod. facing 1 = bow to the right. */
    private void paintCanoe(Graphics2D g, double x, int facing, double time) {
        Graphics2D c = (Graphics2D) g.create();
        c.translate(x, WATER_Y + 1.6 * Math.sin(1.9 * time));
        c.rotate(0.035 * Math.sin(1.9 * time + 0.8));
        c.scale(facing, 1);
        c.setColor(BAMBOO);
        c.setStroke(ROUND);
        c.draw(BOOM_FRONT);
        c.draw(BOOM_BACK);
        c.setColor(CANOE_HULL);
        c.fill(CANOE);
        c.setColor(CANOE_STRIPE);
        c.fillRect(-44, -11, 88, 2);
        c.setColor(OUTLINE);
        c.setStroke(THIN);
        c.draw(CANOE);
        c.setColor(BAMBOO);
        c.fillRoundRect(-62, -2, 124, 5, 5, 5);   // the outrigger float on the near side
        c.setColor(BAMBOO_DARK);
        c.drawRoundRect(-62, -2, 124, 5, 5, 5);

        c.setColor(SHIRT);
        c.fillRoundRect(-6, -27, 11, 15, 4, 4);
        c.setColor(SKIN);
        c.fillOval(-5, -35, 9, 9);
        c.setColor(SALAKOT);
        c.fillPolygon(new int[] {-11, 0, 10}, new int[] {-32, -41, -32}, 3);
        c.setColor(BAMBOO_DARK);
        c.drawLine(4, -22, 34, -48);   // the rod
        c.dispose();
    }

    /** Rain over the sea, and the rings the drops make where they land. */
    private void paintRain(Graphics2D g, double t, double strength, int skyBottom) {
        if (strength <= 0 || skyBottom <= TOP_BAR) {
            return;
        }
        Graphics2D rain = (Graphics2D) g.create();
        rain.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);   // thin lines: speed over smoothness
        rain.setColor(withAlpha(RAIN, 0.55 * strength));
        rain.setStroke(RAIN_STROKE);
        int drops = (int) (80 * strength);
        double height = skyBottom - TOP_BAR + 12;
        for (int i = 0; i < drops; i++) {
            double x = (i * 137.3 + t * 90) % (W + 60) - 30;
            double y = TOP_BAR + (i * 71.9 + t * 640) % height;
            if (y < skyBottom) {
                rain.drawLine((int) x, (int) y, (int) x - 5, (int) y + 15);
            }
        }
        rain.dispose();
        for (int i = 0; i < 14; i++) {
            double grown = (t * 2.2 + i * 0.37) % 1;   // each ring grows and fades, then starts again
            int width = (int) (4 + 12 * grown);
            int x = (int) ((i * 157.1) % W);
            g.setColor(withAlpha(RAIN, 0.5 * strength * (1 - grown)));
            g.drawOval(x - width / 2, skyBottom - 1, width, 3);
        }
    }

    /** A jagged bolt of lightning from the top of the picture down to the sea. */
    private void paintBolt(Graphics2D g, double x, int seaY, int seed, double strength) {
        bolt.reset();
        bolt.moveTo(x, TOP_BAR);
        int steps = 7;
        for (int k = 1; k <= steps; k++) {
            double jag = ((seed * 31 + k * 17) % 9 - 4) * 6;
            bolt.lineTo(x + jag + k * 3, TOP_BAR + (seaY - TOP_BAR) * (double) k / steps);
        }
        g.setStroke(BOLT_GLOW);
        g.setColor(withAlpha(RAIN, 0.3 * strength));
        g.draw(bolt);
        g.setStroke(BOLT_CORE);
        g.setColor(withAlpha(Color.WHITE, strength));
        g.draw(bolt);
        g.setStroke(THIN);
    }

    /** White streaks of foam behind a moving boat. */
    private void paintWake(Graphics2D g, double boatX, double speed) {
        if (speed <= 0.02) {
            return;
        }
        g.setStroke(MEDIUM);
        for (int k = 0; k < 4; k++) {
            g.setColor(withAlpha(Color.WHITE, speed * (0.8 - 0.18 * k)));
            int x = (int) (boatX - 64 - 16 * k);
            int length = 12 - 2 * k;
            g.drawLine(x, WATER_Y + 3 + k, x - length, WATER_Y + 4 + k);
        }
        g.setStroke(THIN);
    }

    /** The cleanup permit: it slides in over the water, then the stamp comes down on it. */
    private void paintPermitCard(Graphics2D g, double t) {
        double shown = smooth(ramp(t, 3.0, 0.45));
        if (shown <= 0) {
            return;
        }
        double sinceStamp = t - STAMP_AT_S;
        double shake = (sinceStamp > 0 && sinceStamp < 0.18) ? 3 * Math.sin(90 * sinceStamp) : 0;
        Graphics2D card = faded(g, shown);
        card.translate(W / 2.0 + shake, 300 + 30 * (1 - shown));
        card.setColor(PAPER);
        card.fillRoundRect(-170, -95, 340, 206, 10, 10);
        card.setColor(PAPER_EDGE);
        card.setStroke(MEDIUM);
        card.drawRoundRect(-170, -95, 340, 206, 10, 10);
        card.drawLine(-150, -54, 150, -54);
        card.setFont(Fonts.SUBHEADING);
        card.setColor(UiPalette.INK);
        TextDraw.centered(card, StoryText.INTRO_PERMIT_TITLE, 0, -64);
        card.setFont(Fonts.BODY);
        TextDraw.centeredLines(card, StoryText.INTRO_PERMIT_BODY, 0, -28, 22);
        card.setColor(PAPER_EDGE);
        card.drawOval(-150, 58, 34, 34);   // the town's seal
        card.drawOval(-145, 63, 24, 24);

        if (sinceStamp > 0) {
            double landed = Mathx.easeOut(Math.min(1, sinceStamp / 0.22));
            Graphics2D stamp = (Graphics2D) card.create();
            stamp.translate(82, 74);   // below the text, so it covers none of it
            stamp.rotate(-0.21);
            double size = 2.2 - 1.2 * landed;   // it comes down from above, big, and lands at its own size
            stamp.scale(size, size);
            stamp.setComposite(AlphaComposite.SrcOver.derive((float) (shown * landed)));
            stamp.setColor(APPROVED);
            stamp.setStroke(THICK);
            stamp.drawRoundRect(-74, -20, 148, 36, 8, 8);
            stamp.setFont(Fonts.HEADING);
            TextDraw.centered(stamp, StoryText.INTRO_PERMIT_STAMP, 0, 8);
            stamp.dispose();
        }
        card.dispose();
    }

    /** The line from the boat down to the hook: dark at the edges, pale in the middle. */
    private void paintLine(Graphics2D g, double tipX, double tipY) {
        int x1 = BOAT_X;
        int y1 = WATER_Y;
        int x2 = (int) Math.round(tipX);
        int y2 = (int) Math.round(tipY);
        g.setColor(ROPE_EDGE);
        g.setStroke(THICK);
        g.drawLine(x1, y1, x2, y2);
        g.setColor(ROPE_CORE);
        g.setStroke(THIN);
        g.drawLine(x1, y1, x2, y2);
    }

    /** A red ring and an exclamation mark around a fish that is about to be hurt. */
    private void paintWarning(Graphics2D g, double x, double y, double radius, double strength, double time) {
        double ring = radius + 10 + 3 * Math.sin(12 * time);
        g.setColor(withAlpha(WARNING, strength));
        g.setStroke(ROUND);
        g.drawOval((int) (x - ring), (int) (y - ring), (int) (2 * ring), (int) (2 * ring));
        int markX = (int) x;
        int markY = (int) (y - ring - 22);
        g.fillRoundRect(markX - 2, markY, 5, 12, 3, 3);
        g.fillOval(markX - 2, markY + 15, 5, 5);
        g.setStroke(THIN);
    }

    /** A small copy of the permit in the corner. While a fish is in danger it turns red and shakes. */
    private void paintMiniPermit(Graphics2D g, double danger, double time) {
        int shake = (int) Math.round(2 * danger * Math.sin(40 * time));
        int x = 654 + shake;
        int y = 48;
        g.setColor(PAPER);
        g.fillRoundRect(x, y, 124, 38, 8, 8);
        if (danger > 0) {
            g.setColor(withAlpha(WARNING, 0.25 * danger));
            g.fillRoundRect(x, y, 124, 38, 8, 8);
        }
        g.setColor(danger > 0.5 ? WARNING : PAPER_EDGE);
        g.setStroke(MEDIUM);
        g.drawRoundRect(x, y, 124, 38, 8, 8);
        g.setFont(Fonts.LABEL);
        g.setColor(UiPalette.INK);
        g.drawString(StoryText.INTRO_PERMIT_SHORT, x + 12, y + 24);
        if (danger > 0.5) {
            g.setColor(WARNING);   // an exclamation mark
            g.fillRoundRect(x + 101, y + 8, 5, 14, 3, 3);
            g.fillOval(x + 101, y + 25, 5, 5);
        } else {
            g.setColor(APPROVED);   // a tick
            g.setStroke(ROUND);
            g.drawPolyline(new int[] {x + 96, x + 102, x + 112}, new int[] {y + 20, y + 26, y + 12}, 3);
        }
        g.setStroke(THIN);
    }

    // =====================================================================
    // Helpers
    // =====================================================================

    /** How close the fish is to the hook in the protected-fish scene: 0 = safe, 1 = right next to it. */
    private static double dangerAt(double t) {
        return smooth(ramp(t, DANGER_FROM_S, 0.3)) * (1 - smooth(ramp(t, DANGER_TO_S - 0.3, 0.3)));
    }

    /** A see-through layer of colour over part of the screen, from y top to y bottom. */
    private static void shade(Graphics2D g, Color color, double amount, int top, int bottom) {
        if (amount <= 0 || bottom <= top) {
            return;
        }
        g.setColor(withAlpha(color, amount));
        g.fillRect(0, top, W, bottom - top);
    }

    /** A copy of the graphics that draws everything at the given opacity. */
    private static Graphics2D faded(Graphics2D g, double opacity) {
        Graphics2D copy = (Graphics2D) g.create();
        copy.setComposite(AlphaComposite.SrcOver.derive((float) Mathx.clamp(opacity, 0, 1)));
        return copy;
    }

    private static Color withAlpha(Color color, double amount) {
        int alpha = (int) Math.round(255 * Mathx.clamp(amount, 0, 1));
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    }

    /** 0 before from, 1 after from + seconds, a straight line in between. */
    private static double ramp(double t, double from, double seconds) {
        return Mathx.clamp((t - from) / seconds, 0, 1);
    }

    /** Eases a value from 0 to 1 in and out, so movements start and stop softly. */
    private static double smooth(double value) {
        double u = Mathx.clamp(value, 0, 1);
        return u * u * (3 - 2 * u);
    }

    /** The canoe's hull: long and narrow, both ends curving up. The waterline is y = 0. */
    private static Shape canoeShape() {
        Path2D hull = new Path2D.Double();
        hull.moveTo(-50, -12);
        hull.lineTo(46, -12);
        hull.quadTo(56, -14, 58, -22);
        hull.quadTo(52, -4, 40, 1);
        hull.lineTo(-40, 1);
        hull.quadTo(-52, -4, -56, -20);
        hull.quadTo(-54, -13, -50, -12);
        hull.closePath();
        return hull;
    }

    /** One of the curved booms that join the hull to the outrigger float. */
    private static Shape boom(double x) {
        Path2D boom = new Path2D.Double();
        boom.moveTo(x, -12);
        boom.quadTo(x + Math.signum(x) * 4, -22, x + Math.signum(x) * 8, -1);
        return boom;
    }

    /**
     * A fish or a piece of trash that belongs to no game: the opening's
     * script says where it is every frame. It hands the game's painters the
     * same view a real ocean object would.
     */
    private static final class Prop implements ObjectView {

        private final ObjectKind kind;
        private final double radius;
        private final double phase;   // picks the painter's design; see ObjectPainter.designOf
        private double x;
        private double y;
        private int facing = 1;
        private double speedX;
        private double opacity = 1;

        /**
         * @param design  which of the painter's designs it gets
         * @param designs how many designs that painter has
         * @param variant makes props of the same design move out of step with each other
         */
        Prop(ObjectKind kind, int design, int designs, int variant) {
            this.kind = kind;
            this.radius = kind.create(0, 0, 0).getRadius();
            this.phase = (design + designs * 7 * variant + 0.5) / 97;
        }

        /** Puts it somewhere, fully visible. */
        Prop at(double newX, double newY, int newFacing, double newSpeedX) {
            return at(newX, newY, newFacing, newSpeedX, 1);
        }

        Prop at(double newX, double newY, int newFacing, double newSpeedX, double newOpacity) {
            x = newX;
            y = newY;
            facing = newFacing;
            speedX = newSpeedX;
            opacity = newOpacity;
            return this;
        }

        @Override
        public ObjectKind getKind() {
            return kind;
        }

        @Override
        public double getX() {
            return x;
        }

        @Override
        public double getY() {
            return y;
        }

        @Override
        public double getRadius() {
            return radius;
        }

        @Override
        public int getFacing() {
            return facing;
        }

        @Override
        public double getSpeedX() {
            return speedX;
        }

        @Override
        public double getPhase() {
            return phase;
        }

        @Override
        public boolean isCaught() {
            return false;
        }

        @Override
        public double getAlert() {
            return 0;
        }

        @Override
        public Condition getCondition() {
            return Condition.NORMAL;
        }

        @Override
        public double getOpacity() {
            return opacity;
        }
    }
}
