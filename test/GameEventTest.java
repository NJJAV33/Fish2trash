import game.EventType;
import game.GameEvent;
import game.GameManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import model.OceanObject;

/**
 * Checks that the game tells its listeners what happened, in the right order
 * (the observer pattern).
 */
public final class GameEventTest {

    private GameEventTest() {
        // only static methods
    }

    public static void run() {
        catchingTrashReportsCatchThenDelivery();
        hookingAFishReportsHealthLost();
        aMissIsReported();
        aRecallIsNotAMiss();
    }

    private static void catchingTrashReportsCatchThenDelivery() {
        GameManager game = new GameManager(new Random(7));
        List<GameEvent> events = record(game);
        OceanObject trash = GameScript.firstCatchable(game, true);
        int objectsBefore = game.getObjects().size();

        GameScript.hookOnto(game, trash);

        Check.assertEquals("events for a trash catch",
                List.of(EventType.HOOK_LAUNCHED, EventType.OBJECT_CAUGHT, EventType.DELIVERED), types(events));
        GameEvent caught = events.get(1);
        Check.assertEquals("event names the caught kind", trash.getKind(), caught.getKind());
        Check.assertEquals("event carries the points", game.getPlayer().getScore(), caught.getValue());
        Check.assertTrue("delivered trash left the water", !game.getObjects().contains(trash));
        Check.assertEquals("a new object replaced it", objectsBefore, game.getObjects().size());
    }

    private static void hookingAFishReportsHealthLost() {
        GameManager game = new GameManager(new Random(7));
        List<GameEvent> events = record(game);
        OceanObject fish = GameScript.firstCatchable(game, false);

        GameScript.hookOnto(game, fish);

        Check.assertEquals("events for a fish hit",
                List.of(EventType.HOOK_LAUNCHED, EventType.HEALTH_LOST), types(events));
        Check.assertEquals("event carries the damage", 1, events.get(1).getValue());
        Check.assertTrue("the fish is still in the water", game.getObjects().contains(fish));
        Check.assertEquals("score unchanged by a fish", 0, game.getPlayer().getScore());
    }

    private static void aMissIsReported() {
        GameManager game = new GameManager(new Random(7));
        List<GameEvent> events = record(game);
        GameScript.clearTheWater(game);

        game.spaceAction();
        GameScript.playUntilHookIsBack(game);

        Check.assertEquals("events for a miss",
                List.of(EventType.HOOK_LAUNCHED, EventType.MISSED), types(events));
    }

    private static void aRecallIsNotAMiss() {
        GameManager game = new GameManager(new Random(7));
        List<GameEvent> events = record(game);
        GameScript.clearTheWater(game);

        game.spaceAction();
        game.update(0.2);
        game.spaceAction();   // pull it back
        GameScript.playUntilHookIsBack(game);
        game.spaceAction();   // and it can be dropped again

        Check.assertEquals("events for a recall",
                List.of(EventType.HOOK_LAUNCHED, EventType.HOOK_RECALLED, EventType.HOOK_LAUNCHED), types(events));
    }

    /** Registers a listener that simply writes every event down. */
    private static List<GameEvent> record(GameManager game) {
        List<GameEvent> events = new ArrayList<>();
        game.addListener(events::add);
        return events;
    }

    private static List<EventType> types(List<GameEvent> events) {
        List<EventType> types = new ArrayList<>();
        for (GameEvent event : events) {
            types.add(event.getType());
        }
        return types;
    }
}
