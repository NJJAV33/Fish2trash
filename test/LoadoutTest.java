import game.EventType;
import game.GameEvent;
import game.GameManager;
import java.util.List;
import java.util.Random;
import model.ChapterData;
import model.EquipmentLockedException;
import model.EquipmentType;
import model.Loadout;
import model.ObjectKind;
import model.OceanObject;
import util.GameConstants;

/** Checks which tools each chapter has, and the rules for changing between them. */
public final class LoadoutTest {

    private LoadoutTest() {
        // only static methods
    }

    public static void run() {
        eachChapterAddsOneToolAndStartsWithIt();
        aLockedToolCannotBeSelected();
        swappingNeedsAShortWait();
        steppingSkipsLockedToolsAndWrapsAround();
        theGameAnnouncesToolChanges();
        noToolChangeWhileTheLineIsOut();
        theMagnetGoesStraightPastAFish();
    }

    private static void eachChapterAddsOneToolAndStartsWithIt() {
        EquipmentType[] all = EquipmentType.values();
        for (int chapter = 1; chapter <= ChapterData.CHAPTER_COUNT; chapter++) {
            Loadout loadout = Loadout.forChapter(chapter);
            for (EquipmentType type : all) {
                Check.assertEquals("chapter " + chapter + " has the " + type,
                        type.getUnlockChapter() <= chapter, loadout.isUnlocked(type));
            }
            Check.assertEquals("chapter " + chapter + " starts with its new tool",
                    all[chapter - 1], loadout.getSelectedType());
            Check.assertEquals("the tool on the line matches the selected type",
                    loadout.getSelectedType(), loadout.getSelected().getType());
        }
    }

    private static void aLockedToolCannotBeSelected() {
        Loadout loadout = Loadout.forChapter(1);
        try {
            loadout.select(EquipmentType.MAGNET);
            Check.fail("selecting a locked tool should throw");
        } catch (EquipmentLockedException e) {
            Check.assertEquals("the exception names the tool", EquipmentType.MAGNET, e.getType());
            Check.assertTrue("the message says when it arrives: " + e.getMessage(),
                    e.getMessage().contains("Magnet") && e.getMessage().contains("2"));
        }
        Check.assertEquals("the hook stays on the line", EquipmentType.HOOK, loadout.getSelectedType());
    }

    private static void swappingNeedsAShortWait() {
        Loadout loadout = Loadout.forChapter(3);
        try {
            Check.assertTrue("no wait at the start", loadout.canSwap());
            Check.assertTrue("selecting the tool already on the line changes nothing",
                    !loadout.select(EquipmentType.NET_CUTTER) && loadout.canSwap());

            Check.assertTrue("first swap works", loadout.select(EquipmentType.HOOK));
            Check.assertEquals("the wait starts full", 1.0, loadout.getCooldownFraction(), 1e-9);
            Check.assertTrue("a second swap right away is refused", !loadout.select(EquipmentType.MAGNET));
            Check.assertEquals("refused swap keeps the tool", EquipmentType.HOOK, loadout.getSelectedType());

            loadout.tick(GameConstants.TOOL_SWAP_COOLDOWN_S / 2);
            Check.assertEquals("half the wait is left", 0.5, loadout.getCooldownFraction(), 1e-9);
            Check.assertTrue("still too soon", !loadout.canSwap());

            loadout.tick(GameConstants.TOOL_SWAP_COOLDOWN_S / 2 + 0.01);
            Check.assertTrue("the wait is over", loadout.canSwap());
            Check.assertTrue("now the swap works", loadout.select(EquipmentType.MAGNET));
        } catch (EquipmentLockedException e) {
            Check.fail("chapter 3 has every tool: " + e.getMessage());
        }
    }

    private static void steppingSkipsLockedToolsAndWrapsAround() {
        Loadout onlyHook = Loadout.forChapter(1);
        Check.assertEquals("with one tool, next is the same tool", EquipmentType.HOOK, onlyHook.next(1));
        Check.assertEquals("with one tool, previous is the same tool", EquipmentType.HOOK, onlyHook.next(-1));

        Loadout two = Loadout.forChapter(2);   // hook and magnet, magnet on the line
        Check.assertEquals("next from the magnet skips the locked cutter", EquipmentType.HOOK, two.next(1));
        Check.assertEquals("previous from the magnet", EquipmentType.HOOK, two.next(-1));

        Loadout three = Loadout.forChapter(3);   // cutter on the line
        Check.assertEquals("next wraps around to the hook", EquipmentType.HOOK, three.next(1));
        Check.assertEquals("previous from the cutter", EquipmentType.MAGNET, three.next(-1));
    }

    private static void theGameAnnouncesToolChanges() {
        GameManager game = new GameManager(ChapterData.forChapter(2), Loadout.forChapter(2), new Random(3));
        List<GameEvent> events = GameScript.record(game);

        game.selectTool(EquipmentType.MAGNET);       // already on the line: nothing happens
        game.selectTool(EquipmentType.HOOK);         // works
        game.selectTool(EquipmentType.MAGNET);       // too soon
        game.selectTool(EquipmentType.NET_CUTTER);   // not unlocked in chapter 2

        Check.assertEquals("events for the four requests",
                List.of(EventType.TOOL_SWITCHED, EventType.SWAP_DENIED, EventType.SWAP_DENIED), GameScript.types(events));
        Check.assertEquals("the switch names the tool", "Hook", events.get(0).getText());
        Check.assertEquals("too soon has no message", "", events.get(1).getText());
        Check.assertTrue("a locked tool says when it arrives: " + events.get(2).getText(),
                events.get(2).getText().contains("Net Cutter"));
        Check.assertEquals("the game shows the tool on the line", EquipmentType.HOOK, game.getSelectedTool());
        Check.assertTrue("the game shows the wait", game.getSwapCooldownFraction() > 0.9);

        for (int i = 0; i < 70; i++) {
            game.update(GameScript.STEP);   // a bit more than one second
        }
        Check.assertEquals("the wait runs out while playing", 0.0, game.getSwapCooldownFraction(), 1e-9);
        game.cycleTool(1);
        Check.assertEquals("stepping on from the hook", EquipmentType.MAGNET, game.getSelectedTool());
    }

    private static void noToolChangeWhileTheLineIsOut() {
        GameManager game = new GameManager(ChapterData.forChapter(2), Loadout.forChapter(2), new Random(3));
        GameScript.clearTheWater(game);
        List<GameEvent> events = GameScript.record(game);

        game.spaceAction();
        game.update(GameScript.STEP);
        game.selectTool(EquipmentType.HOOK);

        Check.assertEquals("events", List.of(EventType.HOOK_LAUNCHED, EventType.SWAP_DENIED), GameScript.types(events));
        Check.assertEquals("the magnet stays on the line", EquipmentType.MAGNET, game.getSelectedTool());
    }

    private static void theMagnetGoesStraightPastAFish() {
        GameManager game = new GameManager(ChapterData.forChapter(2), Loadout.forChapter(2), new Random(3));
        OceanObject fish = GameScript.firstCatchable(game, false);
        Check.assertTrue("found a fish", !fish.getKind().isTrash() && fish.getKind() != ObjectKind.CAN);
        List<GameEvent> events = GameScript.record(game);

        GameScript.hookOnto(game, fish);   // the magnet is on the line in chapter 2

        Check.assertEquals("the throw ends as a miss", List.of(EventType.HOOK_LAUNCHED, EventType.MISSED), GameScript.types(events));
        Check.assertEquals("no health lost", game.getMaxHealth(), game.getHealth());
    }
}
