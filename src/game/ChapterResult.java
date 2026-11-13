package game;

/**
 * The outcome of one finished chapter attempt, kept after the GameManager
 * that played it is gone. Immutable: it is a record of what happened.
 */
public final class ChapterResult {

    private final int chapter;
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
     * @param failCause why the attempt was lost, or FailCause.NONE for a cleared chapter
     */
    public ChapterResult(int chapter, FailCause failCause, int points, double timeLeft, double timeTotal,
            int healthLeft, int maxHealth, int launches, int catches, int slips, int fishHits) {
        this.chapter = chapter;
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
    public static ChapterResult from(GameManager game) {
        if (!game.getStatus().isOver()) {
            throw new IllegalStateException("The chapter is still being played");
        }
        return new ChapterResult(game.getChapter().getNumber(), game.getStatus().getFailCause(),
                game.getPlayer().getScore(), game.getTimeLeft(), game.getTimeTotal(),
                game.getPlayer().getHealth(), game.getPlayer().getMaxHealth(),
                game.getStats().getLaunches(), game.getStats().getCatches(),
                game.getStats().getSlips(), game.getStats().getFishHits());
    }

    public int getChapter() {
        return chapter;
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
        return "Chapter " + chapter + (cleared ? " cleared" : " failed (" + failCause + ")")
                + ", " + points + " points";
    }
}
