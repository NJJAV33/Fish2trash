/**
 * Small assertion helper for the tests, so no test library is needed.
 * A check that does not hold prints a FAIL line and is counted;
 * TestRunner reads the totals at the end.
 */
public final class Check {

    private static int checks;
    private static int failures;

    private Check() {
        // only static methods
    }

    public static void assertTrue(String what, boolean condition) {
        checks++;
        if (!condition) {
            failures++;
            System.out.println("  FAIL: " + what);
        }
    }

    public static void assertEquals(String what, int expected, int actual) {
        assertTrue(what + " (expected " + expected + ", got " + actual + ")", expected == actual);
    }

    /** For decimal values: equal means "within tolerance of each other". */
    public static void assertEquals(String what, double expected, double actual, double tolerance) {
        assertTrue(what + " (expected " + expected + ", got " + actual + ")",
                Math.abs(expected - actual) <= tolerance);
    }

    public static void assertEquals(String what, Object expected, Object actual) {
        assertTrue(what + " (expected " + expected + ", got " + actual + ")",
                expected == null ? actual == null : expected.equals(actual));
    }

    /** Records a failure directly, e.g. when a test crashes. */
    public static void fail(String what) {
        assertTrue(what, false);
    }

    public static int getChecks() {
        return checks;
    }

    public static int getFailures() {
        return failures;
    }
}
