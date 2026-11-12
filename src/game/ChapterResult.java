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

    private ChapterResult(GameManager game) {
        this.chapter = game.getChapter().getNumber();
        this.cleared = game.getStatus() == ChapterStatus.CLEARED;
        this.failCause = game.getStatus().getFailCause();
        this.points = game.getPlayer().getScore();
        this.timeLeft = game.getTimeLeft();
        this.timeTotal = game.getTimeTotal();
        this.healthLeft = game.getPlayer().getHealth();
        this.maxHealth = game.getPlayer().getMaxHealth();
        this.launches = game.getStats().getLaunches();
        this.catches = game.getStats().getCatches();
        this.slips = game.getStats().getSlips();
        this.fishHits = game.getStats().getFishHits();
    }

    /** Writes down how a finished attempt went. */
    public static ChapterResult from(GameManager game) {
        if (!game.getStatus().isOver()) {
            throw new IllegalStateException("The chapter is still being played");
        }
        return new ChapterResult(game);
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

    @Override
    public String toString() {
        return "Chapter " + chapter + (cleared ? " cleared" : " failed (" + failCause + ")")
                + ", " + points + " points";
    }
}
