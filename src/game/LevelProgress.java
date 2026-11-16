package game;

import model.LevelData;
import util.ProgressStore;

/**
 * Which levels the player may pick from the level select. Levels are cleared
 * in order, so one number says it all: the furthest level cleared. Every
 * level up to it is cleared, and the one after it is open to try next.
 * It is saved between launches.
 */
public final class LevelProgress {

    private final ProgressStore store;
    private int furthestCleared;

    public LevelProgress(ProgressStore store) {
        this.store = store;
        // A damaged file could hold any number, so keep it inside the story.
        this.furthestCleared = Math.max(0, Math.min(LevelData.LEVEL_COUNT, store.read()));
    }

    /** Remembers that a level was cleared, and saves it if it is further than before. */
    public void recordCleared(int levelNumber) {
        if (levelNumber > furthestCleared) {
            furthestCleared = Math.min(levelNumber, LevelData.LEVEL_COUNT);
            store.write(furthestCleared);
        }
    }

    /** True for a level the player has already cleared. */
    public boolean isCleared(int levelNumber) {
        return levelNumber >= 1 && levelNumber <= furthestCleared;
    }

    /** True for a level that can be picked: a cleared one, or the next one after the furthest. */
    public boolean isUnlocked(int levelNumber) {
        return levelNumber >= 1 && levelNumber <= Math.min(furthestCleared + 1, LevelData.LEVEL_COUNT);
    }

    public int getFurthestCleared() {
        return furthestCleared;
    }
}
