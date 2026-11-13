package ui.render;

import java.awt.Graphics2D;

/**
 * Draws one tool. Each EquipmentType has its own painter, looked up in the
 * PainterRegistry, so the drawing code never asks which tool it has.
 */
public interface ToolPainter {

    /**
     * Draws the tool hanging straight down, with the end of the line at (0, 0).
     *
     * @param time    seconds, for anything that moves
     * @param working true while the tool is on its way out
     */
    void paint(Graphics2D g, double time, boolean working);
}
