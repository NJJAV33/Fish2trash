package model;

/**
 * Dynamic mid-game ocean events that briefly reshape the bay during a shift.
 */
public enum OceanEventType {

    NONE("", "", 0.0, 1.0, 0.0, 1.0, 0.0, 1.0),
    SQUALL("SQUALL", "SQUALL! Heavy waves & surging current!", 10.0, 1.65, 8.0, 1.70, 30.0, 1.0),
    TRASH_TIDE("TRASH TIDE", "TRASH TIDE! Bonus debris drifting in!", 10.0, 1.15, 0.0, 1.35, 18.0, 1.0),
    FISH_FRENZY("FEEDING FRENZY", "FEEDING FRENZY! Watch your line!", 9.0, 1.0, 0.0, 1.0, 0.0, 1.0),
    SLACK_TIDE("SLACK TIDE", "SLACK TIDE! Calm water & fast reel!", 9.0, 0.35, 0.0, 0.30, 0.0, 1.30);

    private final String label;
    private final String announcement;
    private final double durationSeconds;
    private final double swellScale;
    private final double minSwellDegrees;
    private final double currentScale;
    private final double minCurrentSpeed;
    private final double hookSpeedScale;

    OceanEventType(String label, String announcement, double durationSeconds,
                   double swellScale, double minSwellDegrees,
                   double currentScale, double minCurrentSpeed, double hookSpeedScale) {
        this.label = label;
        this.announcement = announcement;
        this.durationSeconds = durationSeconds;
        this.swellScale = swellScale;
        this.minSwellDegrees = minSwellDegrees;
        this.currentScale = currentScale;
        this.minCurrentSpeed = minCurrentSpeed;
        this.hookSpeedScale = hookSpeedScale;
    }

    public String getLabel() {
        return label;
    }

    public String getAnnouncement() {
        return announcement;
    }

    public double getDurationSeconds() {
        return durationSeconds;
    }

    public double getSwellScale() {
        return swellScale;
    }

    public double getMinSwellDegrees() {
        return minSwellDegrees;
    }

    public double getCurrentScale() {
        return currentScale;
    }

    public double getMinCurrentSpeed() {
        return minCurrentSpeed;
    }

    public double getHookSpeedScale() {
        return hookSpeedScale;
    }
}
