package model;

import util.GameConstants;

/**
 * One line of a chapter's spawn table: which kind of object appears, how
 * many, how fast, where, and from when.
 *
 * A rule is immutable. It is put together with a small builder:
 *
 *   SpawnRule.of(ObjectKind.CAN).count(3, 4).respawn(3, 5).speed(35, 70).build()
 */
public final class SpawnRule {

    private final ObjectKind kind;
    private final int initialCount;     // how many appear when the rule starts
    private final int maxCount;         // respawning refills up to this many
    private final int minCount;         // never fewer than this on screen
    private final double respawnMin;    // seconds until a missing one is replaced...
    private final double respawnMax;    // ...picked at random in this range
    private final boolean respawning;
    private final double speedMin;      // pixels per second
    private final double speedMax;
    private final double xMin;
    private final double xMax;
    private final double yMin;
    private final double yMax;
    private final double startSeconds;  // the rule is asleep until this moment

    private SpawnRule(Builder builder) {
        this.kind = builder.kind;
        this.initialCount = builder.initialCount;
        this.maxCount = builder.maxCount;
        this.minCount = builder.minCount;
        this.respawnMin = builder.respawnMin;
        this.respawnMax = builder.respawnMax;
        this.respawning = builder.respawning;
        this.speedMin = builder.speedMin;
        this.speedMax = builder.speedMax;
        this.xMin = builder.xMin;
        this.xMax = builder.xMax;
        this.yMin = builder.yMin;
        this.yMax = builder.yMax;
        this.startSeconds = builder.startSeconds;
    }

    /** Starts a rule for the given kind: one object, anywhere, standing still, from second 0. */
    public static Builder of(ObjectKind kind) {
        return new Builder(kind);
    }

    public ObjectKind getKind() {
        return kind;
    }

    public int getInitialCount() {
        return initialCount;
    }

    public int getMaxCount() {
        return maxCount;
    }

    public int getMinCount() {
        return minCount;
    }

    /** False for objects that are never replaced (fish are never removed, so they never need to be). */
    public boolean isRespawning() {
        return respawning;
    }

    public double getRespawnMin() {
        return respawnMin;
    }

    public double getRespawnMax() {
        return respawnMax;
    }

    public double getSpeedMin() {
        return speedMin;
    }

    public double getSpeedMax() {
        return speedMax;
    }

    public double getXMin() {
        return xMin;
    }

    public double getXMax() {
        return xMax;
    }

    public double getYMin() {
        return yMin;
    }

    public double getYMax() {
        return yMax;
    }

    public double getStartSeconds() {
        return startSeconds;
    }

    @Override
    public String toString() {
        return kind + " x" + initialCount + " (max " + maxCount + ") from " + startSeconds + "s";
    }

    /**
     * Collects the settings for a rule step by step. Each method returns the
     * builder itself, so the calls can be chained; build() makes the rule.
     */
    public static final class Builder {

        private final ObjectKind kind;
        private int initialCount = 1;
        private int maxCount = 1;
        private int minCount = 0;
        private double respawnMin = 0;
        private double respawnMax = 0;
        private boolean respawning = false;
        private double speedMin = 0;
        private double speedMax = 0;
        private double xMin = GameConstants.SPAWN_MARGIN_X;
        private double xMax = GameConstants.WORLD_WIDTH - GameConstants.SPAWN_MARGIN_X;
        private double yMin = GameConstants.MIN_SPAWN_Y;
        private double yMax = GameConstants.MAX_SPAWN_Y;
        private double startSeconds = 0;

        private Builder(ObjectKind kind) {
            this.kind = kind;
        }

        public Builder count(int initial, int max) {
            this.initialCount = initial;
            this.maxCount = max;
            return this;
        }

        public Builder min(int min) {
            this.minCount = min;
            return this;
        }

        /** Replace a missing object after a random delay between the two values. */
        public Builder respawn(double minSeconds, double maxSeconds) {
            this.respawnMin = minSeconds;
            this.respawnMax = maxSeconds;
            this.respawning = true;
            return this;
        }

        public Builder speed(double min, double max) {
            this.speedMin = min;
            this.speedMax = max;
            return this;
        }

        public Builder x(double min, double max) {
            this.xMin = min;
            this.xMax = max;
            return this;
        }

        public Builder y(double min, double max) {
            this.yMin = min;
            this.yMax = max;
            return this;
        }

        public Builder start(double seconds) {
            this.startSeconds = seconds;
            return this;
        }

        public SpawnRule build() {
            return new SpawnRule(this);
        }
    }
}
