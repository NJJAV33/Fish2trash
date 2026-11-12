/**
 * Runs every test class and prints a summary.
 * The program exits with 0 if all checks passed and 1 if any failed,
 * so scripts/test.sh can tell whether the tests are green.
 *
 * To add a test: write a class with a static run() method and list it in main().
 */
public final class TestRunner {

    private TestRunner() {
        // only static methods
    }

    public static void main(String[] args) {
        run("ArchitectureTest", ArchitectureTest::run);
        run("BoatHookTest", BoatHookTest::run);
        run("ChapterFlowTest", ChapterFlowTest::run);
        run("DangerousFishTest", DangerousFishTest::run);
        run("EquipmentTest", EquipmentTest::run);
        run("GameEventTest", GameEventTest::run);
        run("LevelSpawnTest", LevelSpawnTest::run);
        run("MathxTest", MathxTest::run);
        run("ModelCatchTest", ModelCatchTest::run);
        run("OceanObjectTest", OceanObjectTest::run);
        run("PlayerTest", PlayerTest::run);
        run("RenderTest", RenderTest::run);
        run("ScreenFlowTest", ScreenFlowTest::run);
        run("SessionTest", SessionTest::run);

        System.out.println();
        System.out.println(Check.getChecks() + " checks, " + Check.getFailures() + " failed");
        System.exit(Check.getFailures() == 0 ? 0 : 1);
    }

    private static void run(String name, Runnable test) {
        int failuresBefore = Check.getFailures();
        try {
            test.run();
        } catch (RuntimeException e) {
            // A crash counts as a failure, and the other tests still run.
            Check.fail(name + " crashed: " + e);
        }
        boolean passed = Check.getFailures() == failuresBefore;
        System.out.println((passed ? "PASS  " : "FAIL  ") + name);
    }
}
