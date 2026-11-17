package ui.render;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.EnumMap;
import java.util.Map;
import model.Condition;
import model.EquipmentType;
import model.ObjectKind;
import model.ObjectView;
import util.GameConstants;

/**
 * Knows which painter draws which kind of object and which tool.
 *
 * Each lookup is a map from an enum to a painter, so drawing needs no
 * if/else chain and no type checks.
 */
public final class PainterRegistry {

    private static final double DEPTH_HAZE = 0.25;   // how much of an object the sea hides at the very bottom
    private static final float LIFELESS_OPACITY = 0.7f;   // a dead fish is pale
    private static final double HOOKED_HANG = 0.8;        // a hooked fish hangs this many radii below the tool
    private static final double HOOKED_THRASH = 0.28;     // and swings this far each way, in radians
    private static final double DAZED_TILT = 0.5;         // a dazed fish hangs nose down by this much, in radians
    private static final Color STAR = new Color(255, 225, 90);

    private final Map<ObjectKind, ObjectPainter> painters = new EnumMap<>(ObjectKind.class);
    private final Map<EquipmentType, ToolPainter> toolPainters = new EnumMap<>(EquipmentType.class);

    public PainterRegistry() {
        painters.put(ObjectKind.PLASTIC_BOTTLE, new BottlePainter());
        painters.put(ObjectKind.CAN, new CanPainter());
        painters.put(ObjectKind.FISHING_NET, new NetPainter());
        painters.put(ObjectKind.SMALL_FISH, new SmallFishPainter());
        painters.put(ObjectKind.BIG_FISH, new BigFishPainter());
        painters.put(ObjectKind.PUFFERFISH, new PufferPainter());
        painters.put(ObjectKind.BARRACUDA, new BarracudaPainter());
        painters.put(ObjectKind.STINGRAY, new StingrayPainter());

        toolPainters.put(EquipmentType.HOOK, new HookClawPainter());
        toolPainters.put(EquipmentType.MAGNET, new MagnetPainter());
        toolPainters.put(EquipmentType.NET_CUTTER, new CutterPainter());

        // Fail at startup, not in the middle of a game, if a new kind or tool
        // was added without a painter.
        for (ObjectKind kind : ObjectKind.values()) {
            if (!painters.containsKey(kind)) {
                throw new IllegalStateException("No painter registered for " + kind);
            }
        }
        for (EquipmentType type : EquipmentType.values()) {
            if (!toolPainters.containsKey(type)) {
                throw new IllegalStateException("No painter registered for " + type);
            }
        }
    }

    public void paint(Graphics2D g, ObjectView object, double time) {
        ObjectPainter painter = painters.get(object.getKind());
        Condition condition = object.getCondition();
        boolean onTheHook = object.isCaught() && !object.getKind().isTrash();
        float opacity = (float) (object.getOpacity() * clarityAt(object.getY()));
        if (condition == Condition.LIFELESS) {
            opacity *= LIFELESS_OPACITY;
        }
        if (opacity >= 1f && condition == Condition.NORMAL && !onTheHook) {
            painter.paint(g, object, time);
            return;
        }

        // Faded, or a fish in trouble: draw it on a copy of the graphics, see-through or turned.
        Graphics2D posed = (Graphics2D) g.create();
        if (opacity < 1f) {
            posed.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0f, opacity)));
        }
        double x = object.getX();
        double y = object.getY();
        if (onTheHook) {
            // Hooked by the mouth: it hangs head up under the tool and thrashes.
            y += object.getRadius() * HOOKED_HANG;
            posed.translate(0, object.getRadius() * HOOKED_HANG);
            posed.rotate(-object.getFacing() * Math.PI / 2 + HOOKED_THRASH * Math.sin(time * 19 + object.getPhase()), x, y);
        } else if (condition == Condition.DAZED) {
            // Knocked out: nose down, rocking slowly.
            posed.rotate(object.getFacing() * (DAZED_TILT + 0.2 * Math.sin(time * 2.4 + object.getPhase())), x, y);
        } else if (condition == Condition.LIFELESS) {
            // Belly up.
            posed.rotate(0.12 * Math.sin(time * 1.4 + object.getPhase()), x, y);
            posed.translate(0, 2 * y);
            posed.scale(1, -1);
        }
        painter.paint(posed, object, time);
        posed.dispose();

        if (condition == Condition.DAZED) {
            paintStars(g, object, time);
        }
    }

    /** Three small stars circling over a dazed fish's head. */
    private void paintStars(Graphics2D g, ObjectView object, double time) {
        g.setColor(STAR);
        double top = object.getY() - object.getRadius() - 9;
        for (int i = 0; i < 3; i++) {
            double angle = time * 4 + i * 2 * Math.PI / 3;
            int starX = (int) Math.round(object.getX() + Math.cos(angle) * object.getRadius() * 0.8);
            int starY = (int) Math.round(top + Math.sin(angle) * 3);
            g.fillOval(starX - 2, starY - 2, 5, 5);
        }
    }

    /**
     * How clearly something shows at depth y: 1 at the surface, a little
     * less at the sea floor. A deep object lets some of the sea's colour
     * through, which is how things look through a lot of water.
     */
    static double clarityAt(double y) {
        double depth = (y - GameConstants.WATER_Y) / (GameConstants.SEABED_Y - GameConstants.WATER_Y);
        return 1 - DEPTH_HAZE * Math.max(0, Math.min(1, depth));
    }

    /**
     * Draws a tool hanging down from (0, 0). The caller moves and turns the
     * graphics to where the tool should be.
     */
    public void paintTool(Graphics2D g, EquipmentType type, double time, boolean working) {
        Graphics2D g2 = (Graphics2D) g.create();   // a copy, so the painter's strokes and colours do not leak out
        toolPainters.get(type).paint(g2, time, working);
        g2.dispose();
    }
}
