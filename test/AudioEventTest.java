import game.EventType;
import game.GameEvent;
import game.GameManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import model.ChapterData;
import model.Loadout;
import model.ObjectKind;
import ui.AudioEventListener;
import util.GameConstants;
import util.SoundEffect;

/** Checks that the right sound is chosen for each game event, and the low-time ticks. */
public final class AudioEventTest {

    private AudioEventTest() {
        // only static methods
    }

    public static void run() {
        eachEventHasItsSound();
        theClockTicksOncePerSecondAtTheEnd();
        aPlayedChapterSoundsRight();
    }

    private static void eachEventHasItsSound() {
        RecordingAudioPlayer audio = new RecordingAudioPlayer();
        AudioEventListener listener = new AudioEventListener(audio);

        listener.onGameEvent(GameEvent.of(EventType.HOOK_LAUNCHED));
        listener.onGameEvent(GameEvent.about(EventType.OBJECT_CAUGHT, ObjectKind.PLASTIC_BOTTLE.create(0, 0, 0), 10));
        listener.onGameEvent(GameEvent.about(EventType.OBJECT_CAUGHT, ObjectKind.CAN.create(0, 0, 0), 20));
        listener.onGameEvent(GameEvent.about(EventType.OBJECT_CAUGHT, ObjectKind.FISHING_NET.create(0, 0, 0), 50));
        listener.onGameEvent(GameEvent.about(EventType.HEALTH_LOST, ObjectKind.SMALL_FISH.create(0, 0, 0), 1));
        listener.onGameEvent(GameEvent.about(EventType.SLIPPED, ObjectKind.CAN.create(0, 0, 0), 0));
        listener.onGameEvent(GameEvent.withText(EventType.TOOL_SWITCHED, "Magnet"));
        listener.onGameEvent(GameEvent.of(EventType.SWAP_DENIED));
        listener.onGameEvent(GameEvent.withText(EventType.HINT_SHOWN, "a hint"));
        listener.onGameEvent(GameEvent.of(EventType.QUOTA_REACHED));
        listener.onGameEvent(GameEvent.of(EventType.CHAPTER_CLEARED));
        listener.onGameEvent(GameEvent.of(EventType.CHAPTER_FAILED));
        listener.onGameEvent(GameEvent.withValue(EventType.LOW_TIME_TICK, 8));
        listener.onGameEvent(GameEvent.withValue(EventType.LOW_TIME_TICK, 3));
        Check.assertEquals("sounds for the events, in order", List.of(
                SoundEffect.HOOK_LAUNCH, SoundEffect.CATCH_PLASTIC, SoundEffect.CATCH_CAN, SoundEffect.CATCH_NET,
                SoundEffect.FISH_HIT, SoundEffect.WRONG_TOOL, SoundEffect.TOOL_SWITCH, SoundEffect.WRONG_TOOL,
                SoundEffect.HINT, SoundEffect.QUOTA, SoundEffect.CHAPTER_CLEAR, SoundEffect.CHAPTER_FAIL,
                SoundEffect.LOW_TICK, SoundEffect.LOW_TICK_URGENT), audio.getPlayed());

        // Events that have no sound must be skipped quietly.
        int before = audio.getPlayed().size();
        listener.onGameEvent(GameEvent.of(EventType.DELIVERED));
        listener.onGameEvent(GameEvent.of(EventType.MISSED));
        listener.onGameEvent(GameEvent.of(EventType.HOOK_RECALLED));
        Check.assertEquals("events without a sound play nothing", before, audio.getPlayed().size());
    }

    private static void theClockTicksOncePerSecondAtTheEnd() {
        GameManager game = new GameManager(ChapterData.forChapter(1), Loadout.forChapter(1), new Random(1));
        List<Integer> ticks = new ArrayList<>();
        game.addListener(event -> {
            if (event.getType() == EventType.LOW_TIME_TICK) {
                ticks.add(event.getValue());
            }
        });

        while (game.isPlaying()) {
            game.update(0.016);
        }
        Check.assertEquals("one tick per second for the last ten seconds",
                List.of(10, 9, 8, 7, 6, 5, 4, 3, 2, 1), ticks);
        Check.assertEquals("the ticks start at the low-time mark", GameConstants.LOW_TIME_S, ticks.get(0).intValue());
    }

    private static void aPlayedChapterSoundsRight() {
        RecordingAudioPlayer audio = new RecordingAudioPlayer();
        GameManager game = new GameManager(ChapterData.forChapter(1), Loadout.forChapter(1), new Random(2));
        game.addListener(new AudioEventListener(audio));

        GameScript.hookOnto(game, GameScript.firstCatchable(game, true));
        Check.assertTrue("dropping the hook is heard", audio.getPlayed().contains(SoundEffect.HOOK_LAUNCH));
        Check.assertTrue("catching a bottle is heard", audio.getPlayed().contains(SoundEffect.CATCH_PLASTIC));

        while (game.isPlaying()) {
            GameScript.hookOnto(game, GameScript.firstCatchable(game, true));
        }
        List<SoundEffect> played = audio.getPlayed();
        Check.assertTrue("the quota jingle comes before the clear jingle",
                played.indexOf(SoundEffect.QUOTA) >= 0
                        && played.indexOf(SoundEffect.QUOTA) < played.indexOf(SoundEffect.CHAPTER_CLEAR));
        Check.assertTrue("a won chapter never plays the fail jingle", !played.contains(SoundEffect.CHAPTER_FAIL));
        Check.assertTrue("the clock never ticked, because the quota froze it in time",
                !played.contains(SoundEffect.LOW_TICK));
    }
}
