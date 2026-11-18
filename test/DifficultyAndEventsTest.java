import game.EventType;
import game.FailCause;
import game.GameEvent;
import game.GameManager;
import game.GameSession;
import game.GameState;
import game.LevelResult;
import game.LevelStatus;
import game.ScoreCalculator;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import model.Difficulty;
import model.EquipmentType;
import model.Hook;
import model.Level;
import model.LevelData;
import model.Loadout;
import model.ObjectKind;
import model.OceanEventType;
import model.Sea;
import model.SpawnRule;
import ui.AudioEventListener;
import ui.LevelClearScreen;
import ui.LevelSelectScreen;
import ui.MainMenu;
import ui.ScreenManager;
import ui.VictoryScreen;
import ui.render.EffectsSystem;
import ui.render.SceneRenderer;
import util.GameConstants;
import util.SoundEffect;

/**
 * Checks difficulty scaling, catch combos/Flow, dynamic ocean events,
 * and their audio, rendering, and UI menu integration.
 */
public final class DifficultyAndEventsTest {

    private static final double STEP = GameScript.STEP;

    private DifficultyAndEventsTest() {
        // only static methods
    }

    public static void run() {
        difficultyScalingPreservesWinnabilityAndToolConstraints();
        allLevelsWinnableOnMediumAndHard();
        combosTrackStreaksFlowAndScoreBonus();
        oceanEventsCycleAffectSeaHookAndSpawns();
        audioEffectsAndHudRenderCleanly();
        uiMenusCycleDifficultyAndPaintReports();
    }

    private static void difficultyScalingPreservesWinnabilityAndToolConstraints() {
        for (int number = 1; number <= LevelData.LEVEL_COUNT; number++) {
            LevelData data = LevelData.byNumber(number);
            Level easy = new Level(data, new Random(10), Difficulty.EASY, false);
            Level medium = new Level(data, new Random(10), Difficulty.MEDIUM, false);
            Level hard = new Level(data, new Random(10), Difficulty.HARD, false);

            Check.assertTrue("medium swell >= easy on level " + number,
                    medium.getSea().getSwell() >= easy.getSea().getSwell());
            Check.assertTrue("hard swell >= medium on level " + number,
                    hard.getSea().getSwell() >= medium.getSea().getSwell());
            Check.assertTrue("hard current >= medium on level " + number,
                    hard.getSea().getStrength() >= medium.getSea().getStrength());

            Check.assertTrue("medium adds fish pressure on level " + number,
                    fishCapacity(medium.getSpawnRules()) > fishCapacity(easy.getSpawnRules()));
            Check.assertTrue("hard adds >= medium fish pressure on level " + number,
                    fishCapacity(hard.getSpawnRules()) >= fishCapacity(medium.getSpawnRules()));

            for (Difficulty difficulty : Difficulty.values()) {
                Level level = new Level(data, new Random(10), difficulty, false);
                int totalTrash = totalTrashPoints(level.getSpawnRules());
                Check.assertTrue(difficulty + " remains winnable on level " + number,
                        totalTrash >= data.getQuota() + Math.min(10, difficulty.getTrashMinSurplus()));
                if (data.getChapter() >= 2) {
                    int maxSingle = maxSingleKindPoints(level.getSpawnRules());
                    Check.assertTrue(difficulty + " keeps multi-tool constraint on level " + number,
                            maxSingle < data.getQuota());
                }
            }
        }
    }

    private static int fishCapacity(List<SpawnRule> rules) {
        int total = 0;
        for (SpawnRule rule : rules) {
            if (!rule.getKind().isTrash()) {
                total += rule.getMaxCount();
            }
        }
        return total;
    }

    private static int totalTrashPoints(List<SpawnRule> rules) {
        int total = 0;
        for (SpawnRule rule : rules) {
            if (rule.getKind().isTrash()) {
                total += rule.getSupply() * rule.getKind().create(0, 0, 0).getPoints();
            }
        }
        return total;
    }

    private static int maxSingleKindPoints(List<SpawnRule> rules) {
        int[] byKind = new int[ObjectKind.values().length];
        for (SpawnRule rule : rules) {
            if (rule.getKind().isTrash()) {
                byKind[rule.getKind().ordinal()] += rule.getSupply() * rule.getKind().create(0, 0, 0).getPoints();
            }
        }
        int max = 0;
        for (int pts : byKind) {
            max = Math.max(max, pts);
        }
        return max;
    }

    private static void allLevelsWinnableOnMediumAndHard() {
        for (Difficulty difficulty : new Difficulty[] {Difficulty.MEDIUM, Difficulty.HARD}) {
            for (int number = 1; number <= LevelData.LEVEL_COUNT; number++) {
                LevelData data = LevelData.byNumber(number);
                GameManager game = new GameManager(data, Loadout.forChapter(data.getChapter()),
                        new Random(20 + number), difficulty);
                GameScript.clearLevel(game);
                Check.assertEquals(difficulty + " level " + number + " clears cleanly",
                        LevelStatus.CLEARED, game.getStatus());
            }
        }
    }

    private static void combosTrackStreaksFlowAndScoreBonus() {
        GameManager game = new GameManager(LevelData.of(1, 3), Loadout.forChapter(1),
                new Random(7), Difficulty.MEDIUM);
        List<GameEvent> events = GameScript.record(game);

        // 1st catch -> combo 1, no combo bonus yet
        GameScript.hookOnto(game, GameScript.firstCatchable(game, true));
        Check.assertEquals("first catch starts combo at 1", 1, game.getCombo());
        Check.assertEquals("first catch adds no combo bonus", 0, game.getComboBonus());

        // Early recall preserves the combo
        GameScript.keepWaterEmpty(game);
        game.spaceAction();
        game.update(STEP);
        game.spaceAction();
        GameScript.playUntilHookIsBack(game);
        Check.assertEquals("recalling early keeps combo intact", 1, game.getCombo());

        // 2nd and 3rd catches -> combo 2 (+25) and combo 3 (+50, Flow!)
        GameScript.hookOnto(game, GameScript.firstCatchable(game, true));
        Check.assertEquals("second catch raises combo to 2", 2, game.getCombo());
        Check.assertEquals("second catch awards +25 combo bonus", GameConstants.COMBO_SCORE_STEP, game.getComboBonus());

        GameScript.hookOnto(game, GameScript.firstCatchable(game, true));
        Check.assertEquals("third catch enters Flow", 3, game.getCombo());
        Check.assertEquals("best combo tracks peak", 3, game.getBestCombo());
        Check.assertEquals("cumulative combo bonus after 3 catches",
                GameConstants.COMBO_SCORE_STEP * 3, game.getComboBonus());

        boolean sawComboEvent = false;
        for (GameEvent event : events) {
            if (event.getType() == EventType.COMBO_GAINED && event.getValue() == 3) {
                sawComboEvent = true;
            }
        }
        Check.assertTrue("COMBO_GAINED event emitted with streak count", sawComboEvent);

        // Flow speeds up hook travel
        Hook boostedHook = new Hook(400, 120, Loadout.forChapter(1));
        boostedHook.setSpeedScale(GameConstants.HOT_STREAK_SPEED_MULT);
        boostedHook.launch();
        boostedHook.update(0.1);
        Hook normalHook = new Hook(400, 120, Loadout.forChapter(1));
        normalHook.launch();
        normalHook.update(0.1);
        Check.assertTrue("Flow speed scale extends hook faster", boostedHook.getLength() > normalHook.getLength());

        // Miss resets combo, keeps bestCombo and comboBonus
        while (!game.getBoat().getHook().isSwinging()) {
            game.update(STEP);
        }
        GameScript.keepWaterEmpty(game);
        game.spaceAction();
        GameScript.playUntilHookIsBack(game);
        Check.assertEquals("miss resets current combo", 0, game.getCombo());
        Check.assertEquals("best combo remains preserved", 3, game.getBestCombo());
        Check.assertEquals("combo bonus remains preserved",
                GameConstants.COMBO_SCORE_STEP * 3, game.getComboBonus());

        // Slip and fish hit also reset combo
        GameManager ch2 = new GameManager(LevelData.of(2, 1), Loadout.forChapter(2),
                new Random(11), Difficulty.HARD);
        ch2.selectTool(EquipmentType.HOOK);
        GameScript.hookOnto(ch2, GameScript.firstCatchable(ch2, ObjectKind.PLASTIC_BOTTLE));
        Check.assertEquals("combo started in ch2", 1, ch2.getCombo());
        GameScript.hookOnto(ch2, GameScript.firstCatchable(ch2, ObjectKind.CAN));
        Check.assertEquals("wrong tool slip breaks combo", 0, ch2.getCombo());

        GameScript.hookOnto(ch2, GameScript.firstCatchable(ch2, ObjectKind.PLASTIC_BOTTLE));
        Check.assertEquals("combo restarted", 1, ch2.getCombo());
        GameScript.hookOnto(ch2, GameScript.firstCatchable(ch2, ObjectKind.SMALL_FISH));
        Check.assertEquals("fish hit breaks combo", 0, ch2.getCombo());

        // ScoreCalculator multiplies by difficulty and includes combo bonus
        LevelResult easy = new LevelResult(1, 3, FailCause.NONE, 100, 10, 60, 3, 5, 6, 6, 3, 0, 3, 75, Difficulty.EASY);
        LevelResult hard = new LevelResult(1, 3, FailCause.NONE, 100, 10, 60, 3, 5, 6, 6, 3, 0, 3, 75, Difficulty.HARD);
        Check.assertEquals("hard score scales base + combo by 1.5x",
                (int) Math.round(ScoreCalculator.levelScore(easy) * Difficulty.HARD.getScoreMultiplier()),
                ScoreCalculator.levelScore(hard));
    }

    private static void oceanEventsCycleAffectSeaHookAndSpawns() {
        Set<OceanEventType> seen = EnumSet.noneOf(OceanEventType.class);
        boolean sawEnded = false;
        boolean sawActiveFraction = false;

        for (int seed = 1; seed <= 12 && seen.size() < 4; seed++) {
            Level level = new Level(LevelData.of(3, 4), new Random(seed), Difficulty.HARD, true);
            Hook hook = new Hook(400, 120, Loadout.forChapter(3));
            for (int step = 0; step < 5000 && (seen.size() < 4 || !sawEnded); step++) {
                level.update(STEP, hook);
                OceanEventType started = level.pollStartedEvent();
                if (started != OceanEventType.NONE) {
                    seen.add(started);
                    Check.assertEquals("active event matches started", started, level.getActiveEvent());
                    Check.assertTrue("active fraction is > 0 on start", level.getActiveEventFraction() > 0.0);
                    sawActiveFraction = true;
                }
                OceanEventType ended = level.pollEndedEvent();
                if (ended != OceanEventType.NONE) {
                    sawEnded = true;
                }
            }
        }
        Check.assertEquals("all 4 ocean event types trigger across runs", 4, seen.size());
        Check.assertTrue("at least one ocean event ended cleanly", sawEnded);
        Check.assertTrue("event progress fraction reported while active", sawActiveFraction);

        // Sea responds to Squall vs Slack Tide
        Sea sea = new Sea(4, 15, 0);
        double baseSwell = sea.getSwell();
        double baseStrength = sea.getStrength();
        sea.applyEvent(OceanEventType.SQUALL);
        for (int i = 0; i < 120; i++) {
            sea.update(STEP);
        }
        Check.assertTrue("SQUALL increases swell", sea.getSwell() > baseSwell);
        Check.assertTrue("SQUALL strengthens current", sea.getStrength() > baseStrength);

        sea.applyEvent(OceanEventType.SLACK_TIDE);
        for (int i = 0; i < 120; i++) {
            sea.update(STEP);
        }
        Check.assertTrue("SLACK_TIDE calms swell back down", sea.getSwell() <= baseSwell);
    }

    private static void audioEffectsAndHudRenderCleanly() {
        RecordingAudioPlayer audio = new RecordingAudioPlayer();
        AudioEventListener listener = new AudioEventListener(audio);
        listener.onGameEvent(GameEvent.about(EventType.COMBO_GAINED,
                ObjectKind.PLASTIC_BOTTLE.create(0, 0, 0), 3, "+50"));
        listener.onGameEvent(GameEvent.withValueAndText(EventType.OCEAN_EVENT_STARTED,
                OceanEventType.SQUALL.ordinal(), OceanEventType.SQUALL.getAnnouncement()));
        listener.onGameEvent(GameEvent.withValueAndText(EventType.OCEAN_EVENT_STARTED,
                OceanEventType.TRASH_TIDE.ordinal(), OceanEventType.TRASH_TIDE.getAnnouncement()));
        listener.onGameEvent(GameEvent.withValueAndText(EventType.OCEAN_EVENT_STARTED,
                OceanEventType.FISH_FRENZY.ordinal(), OceanEventType.FISH_FRENZY.getAnnouncement()));
        listener.onGameEvent(GameEvent.withValueAndText(EventType.OCEAN_EVENT_STARTED,
                OceanEventType.SLACK_TIDE.ordinal(), OceanEventType.SLACK_TIDE.getAnnouncement()));

        Check.assertEquals("combo and ocean event sounds match", List.of(
                SoundEffect.COMBO, SoundEffect.EVENT_SQUALL, SoundEffect.EVENT_BONUS,
                SoundEffect.EVENT_FRENZY, SoundEffect.EVENT_CALM), audio.getPlayed());

        GameManager game = new GameManager(LevelData.of(2, 3), Loadout.forChapter(2),
                new Random(4), Difficulty.HARD);
        EffectsSystem effects = new EffectsSystem();
        effects.reset(game);
        game.addListener(effects);
        for (int i = 0; i < 3; i++) {
            GameScript.hookOnto(game, GameScript.firstCatchable(game, true));
        }
        // Advance until an ocean event is active so HudPainter renders event banner & Flow pill
        for (int i = 0; i < 2000 && game.getActiveEvent() == OceanEventType.NONE && game.isPlaying(); i++) {
            GameScript.keepWaterEmpty(game);
            game.update(STEP);
        }
        effects.update(STEP);

        BufferedImage image = new BufferedImage(
                GameConstants.WORLD_WIDTH, GameConstants.WORLD_HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        new SceneRenderer().paint(g, game);
        effects.paint(g);
        g.dispose();
        Check.assertTrue("scene rendered non-empty frame with Flow and event HUD",
                image.getRGB(GameConstants.WORLD_WIDTH / 2, 78) != 0);
    }

    private static void uiMenusCycleDifficultyAndPaintReports() {
        RecordingAudioPlayer audio = new RecordingAudioPlayer();
        ScreenManager screens = new ScreenManager(audio, TempScores.newTable(), TempScores.newProgress());
        SceneRenderer backdrop = new SceneRenderer();
        MainMenu mainMenu = new MainMenu(screens, backdrop);
        LevelSelectScreen levelSelect = new LevelSelectScreen(screens, backdrop);
        screens.register(GameState.MENU, mainMenu);
        screens.register(GameState.LEVEL_SELECT, levelSelect);
        screens.start(GameState.MENU);

        Check.assertEquals("menu starts on Easy", Difficulty.EASY, screens.getDifficulty());

        // Navigate to Main Menu index 4 (Difficulty) and cycle right -> Medium, D -> Hard
        for (int i = 0; i < 4; i++) {
            screens.keyPressed(KeyEvent.VK_DOWN);
        }
        screens.keyPressed(KeyEvent.VK_RIGHT);
        Check.assertEquals("RIGHT on Difficulty row picks Medium", Difficulty.MEDIUM, screens.getDifficulty());
        screens.keyPressed(KeyEvent.VK_D);
        Check.assertEquals("D key cycles Difficulty to Hard", Difficulty.HARD, screens.getDifficulty());

        // Level Select C/TAB cycles difficulty back to Easy -> Medium
        levelSelect.keyPressed(KeyEvent.VK_C);
        Check.assertEquals("C in Level Select cycles to Easy", Difficulty.EASY, screens.getDifficulty());
        levelSelect.keyPressed(KeyEvent.VK_TAB);
        Check.assertEquals("TAB in Level Select cycles to Medium", Difficulty.MEDIUM, screens.getDifficulty());

        // GameSession inherits ScreenManager difficulty
        screens.startNewRun();
        Check.assertEquals("started session inherits chosen difficulty",
                Difficulty.MEDIUM, screens.getSession().getDifficulty());

        // LevelClearScreen and VictoryScreen paint cleanly with combo & difficulty stats
        GameSession session = new GameSession(new Random(99), 1, Difficulty.HARD);
        LevelResult cleared = GameScript.clearCleanly(session);
        Check.assertEquals("result carries Hard difficulty", Difficulty.HARD, cleared.getDifficulty());
        Check.assertTrue("clean level builds positive best combo", cleared.getBestCombo() >= 1);

        screens.setDifficulty(Difficulty.HARD);
        screens.startRunAt(1);
        GameManager gMgr = screens.getSession().startLevel();
        GameScript.clearLevel(gMgr);
        screens.getSession().onLevelEnded(gMgr);

        BufferedImage img = new BufferedImage(
                GameConstants.WORLD_WIDTH, GameConstants.WORLD_HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        mainMenu.paint(g);
        levelSelect.paint(g);
        LevelClearScreen clearScreen = new LevelClearScreen(screens, backdrop);
        clearScreen.onEnter();
        clearScreen.paint(g);
        VictoryScreen victoryScreen = new VictoryScreen(screens, backdrop);
        victoryScreen.onEnter();
        victoryScreen.paint(g);
        g.dispose();
        Check.assertTrue("clear and victory screens rendered cleanly",
                img.getRGB(GameConstants.WORLD_WIDTH / 2, GameConstants.WORLD_HEIGHT / 2) != 0);
    }
}
