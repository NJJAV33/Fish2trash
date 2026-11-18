package model;

/**
 * How tough the bay is during a run.
 *
 * Easy matches the default story balance. Medium and Hard make the waves
 * bigger, add more and faster fish, and leave less spare trash in the water
 * while keeping every level winnable.
 */
public enum Difficulty {

    EASY("Easy", 1.00, 0.0, 1.00, 0.0, 1.00, 0, 1.00, 1.00, 0, 0, 18.0, 24.0, 1.00),
    MEDIUM("Medium", 1.45, 3.5, 1.35, 14.0, 1.35, 1, 1.15, 0.50, 20, 1, 14.0, 19.0, 1.25),
    HARD("Hard", 1.90, 7.0, 1.70, 24.0, 1.70, 2, 1.30, 0.20, 10, 1, 11.0, 16.0, 1.50);

    private final String displayName;
    private final double swellScale;
    private final double minSwellDegrees;
    private final double currentScale;
    private final double minCurrentSpeed;
    private final double fishScale;
    private final int extraFishMin;
    private final double fishSpeedScale;
    private final double trashSurplusKeep;
    private final int trashMinSurplus;
    private final int trashScreenTrim;
    private final double eventDelayMin;
    private final double eventDelayMax;
    private final double scoreMultiplier;

    Difficulty(String displayName, double swellScale, double minSwellDegrees,
               double currentScale, double minCurrentSpeed,
               double fishScale, int extraFishMin, double fishSpeedScale,
               double trashSurplusKeep, int trashMinSurplus, int trashScreenTrim,
               double eventDelayMin, double eventDelayMax, double scoreMultiplier) {
        this.displayName = displayName;
        this.swellScale = swellScale;
        this.minSwellDegrees = minSwellDegrees;
        this.currentScale = currentScale;
        this.minCurrentSpeed = minCurrentSpeed;
        this.fishScale = fishScale;
        this.extraFishMin = extraFishMin;
        this.fishSpeedScale = fishSpeedScale;
        this.trashSurplusKeep = trashSurplusKeep;
        this.trashMinSurplus = trashMinSurplus;
        this.trashScreenTrim = trashScreenTrim;
        this.eventDelayMin = eventDelayMin;
        this.eventDelayMax = eventDelayMax;
        this.scoreMultiplier = scoreMultiplier;
    }

    public String getDisplayName() {
        return displayName;
    }

    /** Cycles Easy -> Medium -> Hard -> Easy. */
    public Difficulty next() {
        Difficulty[] all = values();
        return all[(ordinal() + 1) % all.length];
    }

    /** Cycles Easy -> Hard -> Medium -> Easy. */
    public Difficulty previous() {
        Difficulty[] all = values();
        return all[(ordinal() - 1 + all.length) % all.length];
    }

    public double scaleSwellDegrees(double baseDegrees) {
        if (this == EASY) {
            return baseDegrees;
        }
        return Math.max(baseDegrees * swellScale, minSwellDegrees);
    }

    public double scaleCurrentSpeed(double baseSpeed) {
        if (this == EASY) {
            return baseSpeed;
        }
        return Math.max(baseSpeed * currentScale, minCurrentSpeed);
    }

    public double getFishScale() {
        return fishScale;
    }

    public int getExtraFishMin() {
        return extraFishMin;
    }

    public double getFishSpeedScale() {
        return fishSpeedScale;
    }

    public double getTrashSurplusKeep() {
        return trashSurplusKeep;
    }

    public int getTrashMinSurplus() {
        return trashMinSurplus;
    }

    public int getTrashScreenTrim() {
        return trashScreenTrim;
    }

    public double getEventDelayMin() {
        return eventDelayMin;
    }

    public double getEventDelayMax() {
        return eventDelayMax;
    }

    public double getScoreMultiplier() {
        return scoreMultiplier;
    }
}
