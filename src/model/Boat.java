package model;

/**
 * The boat floating at the top of the ocean.
 *
 * The boat owns its hook: the Hook is created inside the Boat constructor and
 * hangs from the middle of the hull (composition - a Boat "has a" Hook).
 */
public class Boat {

    private final double x;   // center of the boat
    private final double y;   // waterline (bottom of the hull)
    private final Hook hook;

    public Boat(double x, double y) {
        this.x = x;
        this.y = y;
        this.hook = new Hook(x, y);
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public Hook getHook() {
        return hook;
    }
}
