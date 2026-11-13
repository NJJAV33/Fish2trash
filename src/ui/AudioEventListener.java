package ui;

import game.EventType;
import game.GameEvent;
import game.GameListener;
import java.util.EnumMap;
import java.util.Map;
import model.ObjectKind;
import util.AudioPlayer;
import util.GameConstants;
import util.SoundEffect;

/**
 * Turns game events into sounds.
 *
 * OBSERVER: like the effects and the stats, this is one more listener. The
 * game rules never mention sound; they announce events, and this class looks
 * up which sound belongs to each one.
 */
public final class AudioEventListener implements GameListener {

    private final AudioPlayer audio;
    private final Map<EventType, SoundEffect> soundForEvent = new EnumMap<>(EventType.class);
    private final Map<ObjectKind, SoundEffect> soundForCatch = new EnumMap<>(ObjectKind.class);

    public AudioEventListener(AudioPlayer audio) {
        this.audio = audio;

        soundForEvent.put(EventType.HOOK_LAUNCHED, SoundEffect.HOOK_LAUNCH);
        soundForEvent.put(EventType.HEALTH_LOST, SoundEffect.FISH_HIT);
        soundForEvent.put(EventType.SLIPPED, SoundEffect.WRONG_TOOL);
        soundForEvent.put(EventType.QUOTA_REACHED, SoundEffect.QUOTA);
        soundForEvent.put(EventType.CHAPTER_CLEARED, SoundEffect.CHAPTER_CLEAR);
        soundForEvent.put(EventType.CHAPTER_FAILED, SoundEffect.CHAPTER_FAIL);
        soundForEvent.put(EventType.TUTORIAL_PROMPT, SoundEffect.HINT);
        soundForEvent.put(EventType.LOW_TIME_TICK, SoundEffect.LOW_TICK);

        // Each kind of trash has its own catch sound.
        soundForCatch.put(ObjectKind.PLASTIC_BOTTLE, SoundEffect.CATCH_PLASTIC);
        soundForCatch.put(ObjectKind.CAN, SoundEffect.CATCH_CAN);
        soundForCatch.put(ObjectKind.FISHING_NET, SoundEffect.CATCH_NET);
    }

    @Override
    public void onGameEvent(GameEvent event) {
        SoundEffect effect = soundForEvent.get(event.getType());
        if (event.getType() == EventType.OBJECT_CAUGHT) {
            effect = soundForCatch.get(event.getKind());
        }
        if (event.getType() == EventType.LOW_TIME_TICK && event.getValue() <= GameConstants.URGENT_TIME_S) {
            effect = SoundEffect.LOW_TICK_URGENT;   // the last seconds tick higher
        }
        if (effect != null) {
            audio.play(effect);   // events with no sound are simply skipped
        }
    }
}
