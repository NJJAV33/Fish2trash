import util.Mathx;

/** Checks the math helpers at their edges. */
public final class MathxTest {

    private static final double TINY = 1e-9;

    private MathxTest() {
        // only static methods
    }

    public static void run() {
        Check.assertEquals("clamp below range", 0.0, Mathx.clamp(-5, 0, 10), TINY);
        Check.assertEquals("clamp above range", 10.0, Mathx.clamp(50, 0, 10), TINY);
        Check.assertEquals("clamp inside range", 7.0, Mathx.clamp(7, 0, 10), TINY);

        Check.assertEquals("lerp start", 20.0, Mathx.lerp(20, 40, 0), TINY);
        Check.assertEquals("lerp end", 40.0, Mathx.lerp(20, 40, 1), TINY);
        Check.assertEquals("lerp halfway", 30.0, Mathx.lerp(20, 40, 0.5), TINY);

        Check.assertEquals("easeOutBack starts at 0", 0.0, Mathx.easeOutBack(0), TINY);
        Check.assertEquals("easeOutBack ends at 1", 1.0, Mathx.easeOutBack(1), TINY);
        Check.assertTrue("easeOutBack overshoots on the way", Mathx.easeOutBack(0.7) > 1.0);
    }
}
