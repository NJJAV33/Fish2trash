package game;

import model.Difficulty;
import model.LevelData;

/**
 * The numbers from one finished attempt at a level: whether it was cleared,
 * what was caught and lost, streak bonus, difficulty, and how much time and
 * health were left.
 *
 * All fields are final, so once written down a result never changes.
 */
public final class LevelResult {

    private final int chapter;
    private final int level;
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
    private final int bestCombo;
    private final int comboBonus;
    private final Difficulty difficulty;

    /**
     * @param chapter   the chapter the level belongs to
     * @param level     the level's place inside that chapter
     * @param failCause why the attempt was lost, or FailCause.NONE for a cleared level
     */
    public LevelResult(int chapter, int level, FailCause failCause, int points, double timeLeft, double timeTotal,
            int healthLeft, int maxHealth, int launches, int catches, int slips, int fishHits) {
        this(chapter, level, failCause, points, timeLeft, timeTotal,
                healthLeft, maxHealth, launches, catches, slips, fishHits, 0, 0, Difficulty.EASY);
    }

    public LevelResult(int chapter, int level, FailCause failCause, int points, double timeLeft, double timeTotal,
            int healthLeft, int maxHealth, int launches, int catches, int slips, int fishHits,
            int bestCombo, int comboBonus, Difficulty difficulty) {
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
        this.bestCombo = bestCombo;
        this.comboBonus = comboBonus;
        this.difficulty = difficulty;
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
                game.getStats().getSlips(), game.getStats().getFishHits(),
                game.getBestCombo(), game.getComboBonus(), game.getDifficulty());
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

    public int getBestCombo() {
        return bestCombo;
    }

    public int getComboBonus() {
        return comboBonus;
    }

    public Difficulty getDifficulty() {
        return difficulty;
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
