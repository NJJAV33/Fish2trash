package ui;

import java.awt.Graphics2D;
import ui.render.IntroPainter;
import util.AudioPlayer;
import util.SoundEffect;
import util.StoryText;

/**
 * The scenes of the opening animation, in the order they play.
 *
 * Each scene knows how long it lasts and which line of the opening it shows
 * under the picture. How it is drawn and which sounds it plays are
 * constant-specific methods, so the opening never needs a switch over scenes.
 */
public enum IntroScene {

    /** A healthy reef full of fish. The camera tilts down from the buoys to the corals. */
    SANCTUARY(5.5, 0) {
        @Override
        void paint(IntroPainter painter, Graphics2D g, double t) {
            painter.paintSanctuary(g, t);
        }

        @Override
        void playSounds(AudioPlayer audio, double before, double after) {
            playAt(audio, SoundEffect.EVENT_CALM, 0.3, before, after);
        }
    },

    /** A fishing canoe comes up to the sanctuary's sign and turns back. */
    NO_TAKE(5.0, 1) {
        @Override
        void paint(IntroPainter painter, Graphics2D g, double t) {
            painter.paintNoTake(g, t);
        }
    },

    /** A storm sends the fish away and trash into the water; lightning cuts to the choked sea floor. */
    STORM(6.5, 2) {
        @Override
        void paint(IntroPainter painter, Graphics2D g, double t) {
            painter.paintStorm(g, t);
        }

        @Override
        void playSounds(AudioPlayer audio, double before, double after) {
            playAt(audio, SoundEffect.EVENT_SQUALL, 0.1, before, after);
            playAt(audio, SoundEffect.THUNDER, IntroPainter.STORM_FLASH_S, before, after);
        }
    },

    /** M/B Tala arrives at dawn and the cleanup permit is stamped. */
    PERMIT(6.0, 3) {
        @Override
        void paint(IntroPainter painter, Graphics2D g, double t) {
            painter.paintPermit(g, t);
        }

        @Override
        void playSounds(AudioPlayer audio, double before, double after) {
            playAt(audio, SoundEffect.PERMIT_STAMP, IntroPainter.STAMP_AT_S, before, after);
        }
    },

    /** The boat's line swings while a protected fish swims past it, and the permit is in danger. */
    PROTECTED(5.5, 4) {
        @Override
        void paint(IntroPainter painter, Graphics2D g, double t) {
            painter.paintProtected(g, t);
        }

        @Override
        void playSounds(AudioPlayer audio, double before, double after) {
            playAt(audio, SoundEffect.THREAT_ALERT, IntroPainter.DANGER_FROM_S, before, after);
        }
    },

    /** The game's name and the first chapter, over the boat at rest. */
    TITLE(4.5, IntroScene.NO_LINE) {
        @Override
        void paint(IntroPainter painter, Graphics2D g, double t) {
            painter.paintTitle(g, t);
        }

        @Override
        void playSounds(AudioPlayer audio, double before, double after) {
            playAt(audio, SoundEffect.TITLE_STING, IntroPainter.TITLE_AT_S, before, after);
        }
    };

    private static final int NO_LINE = -1;

    private final double seconds;
    private final int line;   // which line of StoryText's opening it shows, or NO_LINE

    IntroScene(double seconds, int line) {
        this.seconds = seconds;
        this.line = line;
    }

    /** Draws the scene as it looks t seconds after it began. */
    abstract void paint(IntroPainter painter, Graphics2D g, double t);

    /**
     * Plays the sounds whose moment falls inside one step of the scene's
     * clock, from before (included) to after. Silent unless a scene says otherwise.
     */
    void playSounds(AudioPlayer audio, double before, double after) {
        // no sounds by default
    }

    /** Plays the effect if its moment falls inside the step, so it is heard exactly once. */
    static void playAt(AudioPlayer audio, SoundEffect effect, double moment, double before, double after) {
        if (before <= moment && moment < after) {
            audio.play(effect);
        }
    }

    public double getSeconds() {
        return seconds;
    }

    /** The line shown under the picture, or "" for a scene that has none. */
    public String getCaption() {
        return line == NO_LINE ? "" : StoryText.introLine(line);
    }

    /** How long the whole opening lasts when nobody skips. */
    public static double totalSeconds() {
        double total = 0;
        for (IntroScene scene : values()) {
            total += scene.seconds;
        }
        return total;
    }
}
