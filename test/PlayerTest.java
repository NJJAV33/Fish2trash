/**
 * Checks that Player protects its own rules (encapsulation):
 * points only go up, and health never drops below zero.
 */
public final class PlayerTest {

    private PlayerTest() {
        // only static methods
    }

    public static void run() {
        Player player = new Player(3);
        Check.assertEquals("starting health", 3, player.getHealth());
        Check.assertEquals("starting score", 0, player.getScore());

        player.addPoints(10);
        player.addPoints(-50);
        Check.assertEquals("negative points are ignored", 10, player.getScore());

        player.loseHealth(1);
        Check.assertEquals("health after one hit", 2, player.getHealth());
        Check.assertTrue("still alive at 2 health", player.isAlive());

        player.loseHealth(99);
        Check.assertEquals("health stops at zero", 0, player.getHealth());
        Check.assertTrue("not alive at 0 health", !player.isAlive());
    }
}
