package model;

/**
 * A short tip that appears at a set moment in a chapter.
 *
 * Some tips talk about a tool. Those carry a second wording for when the
 * tools are switched off and the plain hook does everything.
 */
public final class TutorialPrompt {

    private final double atSeconds;
    private final String text;
    private final String textWithoutGear;

    private TutorialPrompt(double atSeconds, String text, String textWithoutGear) {
        this.atSeconds = atSeconds;
        this.text = text;
        this.textWithoutGear = textWithoutGear;
    }

    /** A tip that reads the same whether or not tools are in play. */
    public static TutorialPrompt at(double seconds, String text) {
        return new TutorialPrompt(seconds, text, text);
    }

    /** A tip about a tool, with the wording to use when tools are switched off. */
    public static TutorialPrompt aboutGear(double seconds, String text, String textWithoutGear) {
        return new TutorialPrompt(seconds, text, textWithoutGear);
    }

    public double getAtSeconds() {
        return atSeconds;
    }

    public String getText(boolean gearEnabled) {
        return gearEnabled ? text : textWithoutGear;
    }
}
