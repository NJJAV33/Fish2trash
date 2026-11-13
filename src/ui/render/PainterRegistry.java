package ui.render;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.util.EnumMap;
import java.util.Map;
import model.ObjectKind;
import model.ObjectView;

/**
 * Knows which painter draws which kind of object.
 *
 * The lookup is a map from ObjectKind to painter, so drawing needs no
 * if/else chain and no type checks.
 */
public final class PainterRegistry {

    private final Map<ObjectKind, ObjectPainter> painters = new EnumMap<>(ObjectKind.class);

    public PainterRegistry() {
        painters.put(ObjectKind.PLASTIC_BOTTLE, new BottlePainter());
        painters.put(ObjectKind.CAN, new CanPainter());
        painters.put(ObjectKind.FISHING_NET, new NetPainter());
        painters.put(ObjectKind.SMALL_FISH, new SmallFishPainter());
        painters.put(ObjectKind.BIG_FISH, new BigFishPainter());
        painters.put(ObjectKind.PUFFERFISH, new PufferPainter());
        painters.put(ObjectKind.BARRACUDA, new BarracudaPainter());

        // Fail at startup, not in the middle of a game, if a new kind was
        // added without a painter.
        for (ObjectKind kind : ObjectKind.values()) {
            if (!painters.containsKey(kind)) {
                throw new IllegalStateException("No painter registered for " + kind);
            }
        }
    }

    public void paint(Graphics2D g, ObjectView object, double time) {
        ObjectPainter painter = painters.get(object.getKind());
        float opacity = (float) object.getOpacity();
        if (opacity >= 1f) {
            painter.paint(g, object, time);
            return;
        }
        // Still fading in: draw it see-through on a copy of the graphics.
        Graphics2D faded = (Graphics2D) g.create();
        faded.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0f, opacity)));
        painter.paint(faded, object, time);
        faded.dispose();
    }
}
