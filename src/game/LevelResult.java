package game;

import model.LevelData;

/**
 * The outcome of one finished attempt at a level, kept after the GameManager
 * that played it is gone. Immutable: it is a record of what happened.
 */
public final class LevelResult {

    private final int chapter;   // 1 to 3
    private final int level;     // 1 to 5 inside the chapter
    private final boolean cleared;
    private final FailCause failCause;
    private final int points;
    private final double timeLeft;
    private final double timeTotal;
    private final int healthLeft;
    private final int maxHealth;
    private final int launches;
    private final int catches;
    private final int slips;
    private final int fishHits;

    /**
     * @param chapter   the chapter the level belongs to
     * @param level     the level's place inside that chapter
     * @param failCause why the attempt was lost, or FailCause.NONE for a cleared level
     */
    public LevelResult(int chapter, int level, FailCause failCause, int points, double timeLeft, double timeTotal,
            int healthLeft, int maxHealth, int launches, int catches, int slips, int fishHits) {
        this.chapter = chapter;
        this.level = level;
        this.cleared = failCause == FailCause.NONE;
        this.failCause = failCause;
        this.points = points;
        this.timeLeft = timeLeft;
        this.timeTotal = timeTotal;
        this.healthLeft = healthLeft;
        this.maxHealth = maxHealth;
        this.launches = launches;
        this.catches = catches;
        this.slips = slips;
        this.fishHits = fishHits;
    }

    /** Writes down how a finished attempt went. */
    public static LevelResult from(GameManager game) {
        if (!game.getStatus().isOver()) {
            throw new IllegalStateException("The level is still being played");
        }
        LevelData played = game.getLevelData();
        return new LevelResult(played.getChapter(), played.getLevel(), game.getStatus().getFailCause(),
                game.getPlayer().getScore(), game.getTimeLeft(), game.getTimeTotal(),
                game.getPlayer().getHealth(), game.getPlayer().getMaxHealth(),
                game.getStats().getLaunches(), game.getStats().getCatches(),
                game.getStats().getSlips(), game.getStats().getFishHits());
    }

    public int getChapter() {
        return chapter;
    }

    public int getLevel() {
        return level;
    }

    /** True for the last level of a chapter, where the chapter's badge is decided. */
    public boolean isChapterFinale() {
        return level == LevelData.LEVELS_PER_CHAPTER;
    }

    public boolean isCleared() {
        return cleared;
    }

    public FailCause getFailCause() {
        return failCause;
    }

    public int getPoints() {
        return points;
    }

    public double getTimeLeft() {
        return timeLeft;
    }

    public double getTimeTotal() {
        return timeTotal;
    }

    public int getHealthLeft() {
        return healthLeft;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public int getLaunches() {
        return launches;
    }

    public int getCatches() {
        return catches;
    }

    public int getSlips() {
        return slips;
    }

    public int getFishHits() {
        return fishHits;
    }

    /** The share of throws that brought up trash, from 0 to 1. */
    public double getAccuracy() {
        return launches == 0 ? 0 : (double) catches / launches;
    }

    /** The share of the clock that was left, from 0 to 1. */
    public double getTimeFractionLeft() {
        return timeLeft / timeTotal;
    }

    public int getHealthLost() {
        return maxHealth - healthLeft;
    }

    @Override
    public String toString() {
        return "Level " + chapter + "-" + level + (cleared ? " cleared" : " failed (" + failCause + ")")
                + ", " + points + " points";
    }
}
