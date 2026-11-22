# Proposal updates

The proposal ("OOP_Game Proposal.pdf") was written before the game became a story
with chapters, and it still carries text from an earlier idea (a sorting game with
bins). This page lists what to change so the proposal matches the game.

Tick each line off as you edit the document. Everything listed here is in the game
now; nothing on this page is still planned.

## Whole document

- [ ] The cover says 5 October 2026 and the header table says September 29, 2026. Use one date.
- [ ] Replace every "JavaFX" with "Java Swing". The game uses Swing and Java2D only.
- [ ] Remove the leftover author notes that start with `//` (listed per section below).
- [ ] Replace every "EcoSort" with "Fish2Trash".

## Header table (page 2)

- [ ] **Game type:** remove `//most important tool (logic also playable in the console)`.
      Write: "2D single-player arcade story game".
- [ ] **Duration** and **Date:** check they are still right.

## 1. Project Overview

- [ ] **1.1 The Idea:** add that the game is a story in three chapters of five
      levels, that the player is Nico Abella, a new member of a reef clean-up crew,
      and that clearing all fifteen levels brings the reef back.
- [ ] **1.3 heading:** "Why 2D and Why Console First //do this" becomes "Why 2D".
- [ ] **1.3 text:** remove the plan to play a text version in the console first, and
      the JavaFX sentence. Say instead that the game rules live in `model` and `game`
      with no graphics code, and that automated tests run those rules without a window.
- [ ] **Project goals (3):** "players who learn the four waste categories" becomes
      "players who learn why plastic, metal and lost fishing gear harm the sea".
      The game has three kinds of trash, not four categories.

## 2. Gameplay and Gamification

- [ ] **2.1 How to Play:** add the story pages before each level, the pause menu,
      and recalling the hook (Space while it is going out).
- [ ] **2.1 Advance:** the game has fifteen levels in three chapters, each harder
      than the one before. Failing a level lets the player retry that level; levels
      already cleared stay cleared.
- [ ] **2.1 How to Play:** add the tools. "Bottles need the Hook, cans the Magnet, nets
      the Net Cutter. Keys 1, 2, 3 (or Q / E) change the tool while the line swings.
      Every tool hurts a fish it touches. The Net Cutter's blades cost 2 health on any
      fish, and the Magnet wakes stingrays, which swim up at it. Trash is limited, and
      from chapter 2 on no single kind is enough for the quota, so the player has to
      change tools." The full table is in the README under "How to play".
- [ ] **Figure 1:** replace the mockup with a real screenshot. Ready-made ones are in
      `docs/screenshots/` (`chapter1.png`, `chapter2.png`, `chapter3.png`, `menu.png`,
      `story.png`, `ending.png`, `cleared.png`, `report.png`, `name.png`,
      `highscores.png`). Change the caption from "Concept mockup" to "The game in
      chapter 1".
- [ ] **2.2 Objects in Water:** the first column header "Objective" should be "Category".
      Add three rows under Marine Wildlife: "Pufferfish (dangerous)", "Barracuda
      (dangerous)" and "Stingray (dangerous, wakes when the Magnet is near)".
- [ ] **2.3 Levels:** rename to "Chapters and levels". The proposal's three levels
      became three chapters; each has five levels, and the last level of each keeps
      the proposal's quota:

  | Chapter | Main trash | Tool | Quotas of its 5 levels | Health | What is new |
  |---|---|---|---|---|---|
  | 1. Plastic Shallows | Plastic bottles | Hook | 40, 60, 70, 80, 100 | 3 | Small fish, then big, fast fish |
  | 2. Old Pier Wreck | Cans and bottles | Magnet | 100, 120, 150, 170, 200 | 4 | Stingrays |
  | 3. Ghost Net Deep | Nets, cans and bottles | Net Cutter | 200, 240, 280, 320, 350 | 5 | Pufferfish and barracuda |

  Each level's clock is in the README (50 s for the first, up to 150 s for the last).

- [ ] **2.4 Scoring:** the points are right (10, 20, 50). Add: "Reaching the quota stops
      the clock; the level is cleared when that catch reaches the boat."
- [ ] **2.4 Optional extras:** sound effects, a tool for each kind of trash, hints
      after repeated mistakes, a badge per chapter (decided in its last level) and a
      high-score list are all in:
      list them as "Included extras".
- [ ] **2.4 Scoring:** add the run score. "Each cleared level is worth 10 per point,
      20 per second left and 100 per health left. Badges add 300, 500 and 1000. Failed
      attempts cost 500, then 1000 more, then 1500 more. The ten best runs are kept in
      a high-score list that is saved to a file."
- [ ] **2.4 Scoring:** add the badges. "Eco Rookie: clear level 1-5. Sorter: clear
      level 2-5 with at most 1 slip and at least 60% of throws bringing up trash.
      Champion: clear level 3-5 with at least 40% of the time left and at most 1
      health lost."
- [ ] **2.5 Penalties:** "Accidentally catch a fish: -1 health" stays; say it holds
      for every tool. Add rows: "Touch a pufferfish, barracuda or stingray: -2 health"
      and "Touch any fish with the Net Cutter: -2 health". Add that the fish swims away.
- [ ] **2.5 Penalties:** add a row "Wrong tool for the trash: the trash slips off, the
      throw is wasted, no health lost".

## 3. Object-Oriented Design

- [ ] **3.1 Encapsulation example:** the real methods are `Player.addPoints()` and
      `Player.loseHealth()`, and `Hook.launch()`, `Hook.recall()` and `Hook.update()`.
      Replace `collectTrash()`, `takeDamage()`, `swing()` and `extend()`.
- [ ] **3.1 Inheritance:** add `SmallFish`, `BigFish`, `Pufferfish`, `Barracuda`, `Stingray` under
      `Fish`, and `PlainHook`, `Magnet`, `NetCutter` under `Equipment`.
- [ ] **3.1 Polymorphism example:** name the real call, `object.onCaught(player, hook)`,
      and add `move()` and `sense()` (a pufferfish weaves and swells, a barracuda charges).
      Two more examples: `rule.isTriggered(...)` (five `HintRule` classes) and
      `badge.isEarnedBy(result)` (each `Badge` has its own rule).
- [ ] **3.1 Abstraction:** add the interfaces `GameView`, `ObjectView`, `HookSensor`,
      `CatchResolver`, `Screen`, `GameListener`, `HintRule`, `ObjectPainter`, `ToolPainter`
      and `AudioPlayer`.
- [ ] **3.1 Other Java concepts:** remove "//double check". Replace `enum (BinType)` with
      `enum (ObjectKind, EquipmentType, Condition, Difficulty, OceanEventType, EventType, LevelStatus, FailCause, GameState, CatchOutcome, Badge, SoundEffect, SoundState)`.
      Replace `BinFullException` with `EquipmentLockedException` (thrown when the player
      asks for a tool the crew does not have yet) and `HighScoreException` (thrown when
      the high-score file cannot be read or written, or a line of it is damaged).
      `ArrayList` and `HashMap` are used; add `EnumMap` and `EnumSet`, which the game
      uses for lookups and sets by enum, `Comparable` (`ScoreEntry`, to sort the
      high-score list) and file reading and writing (`HighScoreStore`).
- [ ] **3.2 Main Classes, Player:** remove "and quota". The quota belongs to the level
      (`LevelData`), not the player.
- [ ] **3.2 Fish:** kind is "abstract class", with the five concrete fish under it.
- [ ] **3.2 TimeLimit:** it only counts down and can be frozen. `GameManager` decides
      whether the quota was met.
- [ ] **3.2 Level:** it reads the level's spawn table from `LevelData` and keeps the
      water stocked.
- [ ] **3.2 add rows:** `LevelData` (the fifteen levels as data), `GameSession` (the
      run across chapters), `ScreenManager` and `Screen` (the screens), `Equipment`
      (the tool on the line), `GameEvent` and `GameListener` (the observer pattern).
      Also `Badge` (one per chapter), `ScoreCalculator` (the run score), `ScoreEntry`
      and `HighScoreTable` (the high-score list).
- [ ] **Figure 2:** redraw it from `docs/class-diagram.md`. The current figure shows
      methods that do not exist (`collectTrash`, `collectFish`, `onHookHit`, `getType`)
      and a `gameState: String`; the code uses `onCaught`, `ObjectKind` and enums.
- [ ] **Figure 3:** delete it and its caption ("// to make"). It belongs to the old
      sorting game (`GameController`, `Bin`, `WasteItem`, `Meter`, `StreetLevel`,
      `MarketLevel`, `TyphoonLevel`).
- [ ] **3.3 Package Layout:**
  - `model`: add `ObjectKind`, `SmallFish`, `BigFish`, `Pufferfish`, `Barracuda`,
    `Stingray`, `Condition`, `Sea`, `Difficulty`, `OceanEventType`, `ObjectView`,
    `HookSensor`, `CatchResolver`, `LevelData`, `SpawnRule`, `TutorialPrompt`,
    `Equipment`, `PlainHook`, `Magnet`, `NetCutter`, `EquipmentType`, `Loadout`,
    `CatchOutcome`, `EquipmentLockedException`. Change "No JavaFX imports" to
    "No Swing or AWT imports".
  - `game`: remove `GameController`. Add `GameSession`, `GameEvent`, `GameListener`,
    `HintEngine` (with its `HintRule`s), `Badge`, `LevelProgress`, `ScoreCalculator`,
    `ScoreEntry` and `HighScoreTable`.
  - `ui`: the screens are `MainMenu`, `LevelSelectScreen`, `StoryScreen`, `GameScreen`,
    `LevelClearScreen`, `GameOverScreen`, `VictoryScreen`, `NameEntryScreen`,
    `HighScoreScreen`, `HowToScreen`, `PauseOverlay`. Change "JavaFX screens" to
    "Swing screens".
  - `util`: `GameConstants`, `StoryText` (all in-game text), `Mathx`, the sound classes
    (`SoundEffect`, `ToneSynth`, `AudioPlayer`, `SoundState`), `HighScoreStore`,
    `HighScoreException` and `ProgressStore`.
- [ ] **3.4 Illustrative Code:** remove "// to update" and replace the sample
      (`WasteItem`, `BinType`, `GameController`) with real code, for example:

  ```java
  public abstract class OceanTrash extends OceanObject {
      private final int points;

      @Override
      public void onCaught(Player player, Hook hook) {
          markCaught();
          player.addPoints(points);
          hook.attach(this);
      }
  }

  public class Can extends OceanTrash {
      public Can(double x, double y, double speedX) {
          super(x, y, GameConstants.RADIUS_CAN, speedX, GameConstants.POINTS_CAN);
      }
  }

  // In GameManager: no if/else on the object's type
  object.onCaught(player, boat.getHook());
  ```

## 4. Four-Week Plan

- [ ] Update the weekly deliverables to what was built: (1) project setup, packages,
      tests; (2) core rework and the three story chapters; (3) polish, sound and
      extras; (4) balancing, documents and the presentation.
- [ ] **Team Roles:** add the new classes to each member's list: chapters, session,
      story screens, hints, badges and high scores (Llenos); tools, screens, key handling
      and sound (Marata); the fish subclasses, `ObjectKind`, the painters and the tests
      (Villarito). Remove "Lore master" or rename it "Story text".

## 5. Deliverables and Evaluation

- [ ] Remove the "// ?" from the heading.
- [ ] **First row:** "Working EcoSort game ... Four bins, 10+ item types, three levels,
      score, meters and feedback" becomes "Working Fish2Trash game ... fifteen levels
      in three story chapters, three kinds of trash, five kinds of fish, score, health
      and feedback".
- [ ] **Class diagram and README:** still right. The diagram lives in
      `docs/class-diagram.md` and a test keeps it in step with the code.

## 6. Tools and Assets

- [ ] "game assets will be made by the team using simple shapes and sprites or obtained
      from free-license sources with proper credits" becomes "everything except the
      bundled 12-frame `stingray.png` sprite sheet (`art/models/Stingray.blend` and
      `art/models/export_stingray.py`) is drawn in code with Java2D and every sound
      is built in code with `javax.sound.sampled`; the game uses no external fonts,
      sound files or third-party libraries".
- [ ] The line already says Java Swing. Keep it.

## 7. Risks and Mitigation

- [ ] "JavaFX is new to some members" becomes "Java2D drawing is new to some members".
      Plan: "Each object has its own small painter class, tested by drawing off-screen".
      Remove "develop the console version first".
- [ ] Add a risk: "The levels are too hard or too easy". Plan: "Four test bots play
      every level 50 times; the level tables and all other numbers are in two classes
      and are tuned from playtests (`docs/playtest.md`)".
- [ ] Add a risk: "No working sound on the demo computer". Plan: "The game checks for a
      sound device when it starts and runs silent without one".

## Notes at the end of the document

- [ ] `//add complexity; different equipment for diff types of garbage`: done, as the
      three tools (hook, magnet, net cutter). Remove the note; describe the tools in 2.1.
- [ ] `//story levels represent different types of waste`: done. Each chapter has its
      own main trash, and each of its five levels continues the story. Remove the note.
- [ ] `//add lore`: done. Remove the note.
- [ ] `//endless mode if possible`: not planned. Remove the note, or list it under
      "Future work".
