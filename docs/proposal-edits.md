# Proposal updates

The proposal ("OOP_Game Proposal.pdf") was written before the game became a story
with chapters, and it still carries text from an earlier idea (a sorting game with
bins). This page lists what to change so the proposal matches the game.

Tick each line off as you edit the document. Items marked **(later)** describe
features that are planned but not in the game yet; change those lines when the
feature is merged, or move the feature to "Future work" if it is dropped.

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

- [ ] **1.1 The Idea:** add that the game is a story in three chapters, that the
      player is Nico Abella, a new member of a reef clean-up crew, and that clearing
      all three chapters brings the reef back.
- [ ] **1.3 heading:** "Why 2D and Why Console First //do this" becomes "Why 2D".
- [ ] **1.3 text:** remove the plan to play a text version in the console first, and
      the JavaFX sentence. Say instead that the game rules live in `model` and `game`
      with no graphics code, and that automated tests run those rules without a window.
- [ ] **Project goals (3):** "players who learn the four waste categories" becomes
      "players who learn why plastic, metal and lost fishing gear harm the sea".
      The game has three kinds of trash, not four categories.

## 2. Gameplay and Gamification

- [ ] **2.1 How to Play:** add the story pages before each chapter, the pause menu,
      and recalling the hook (Space while it is going out).
- [ ] **2.1 Advance:** failing a chapter lets the player retry that chapter; chapters
      already cleared stay cleared.
- [ ] **Figure 1:** replace the mockup with a real screenshot. Ready-made ones are in
      `docs/screenshots/` (`chapter1.png`, `chapter2.png`, `chapter3.png`, `menu.png`, `story.png`,
      `ending.png`). Change the caption from "Concept mockup" to "The game in chapter 1".
- [ ] **2.2 Objects in Water:** the first column header "Objective" should be "Category".
      Add two rows under Marine Wildlife: "Pufferfish (dangerous)" and "Barracuda (dangerous)".
- [ ] **2.3 Levels:** rename to "Story chapters" and use this table:

  | Chapter | Main trash | Tool | Quota | Time | Health | What is new |
  |---|---|---|---|---|---|---|
  | 1. Plastic Shallows | Plastic bottles | Hook | 100 | 65 s | 3 | Basic fish and trash |
  | 2. Old Pier Wreck | Cans | Magnet | 200 | 90 s | 4 | Big, fast fish |
  | 3. Ghost Net Deep | Fishing nets | Net Cutter | 350 | 105 s | 5 | Pufferfish and barracuda |

- [ ] **2.4 Scoring:** the points are right (10, 20, 50). Add: "Reaching the quota stops
      the clock; the chapter is cleared when that catch reaches the boat."
- [ ] **2.4 Optional extras:** sound effects, a tool for each kind of trash, and hints
      after repeated mistakes are in: list them as "Included extras". Badges and a
      high-score list are planned.
      **(later)** Move each one to the "Included extras" line when it is merged.
- [ ] **2.5 Penalties:** "Accidentally catch a fish: -1 health" stays. Add a row:
      "Catch a pufferfish or barracuda: -2 health". Add that the fish swims away.
- [ ] **2.5 Penalties:** add a row "Wrong tool for the trash: the trash slips off, the
      throw is wasted, no health lost".
- [ ] **2.1 How to Play:** add the tools. "Bottles need the Hook, cans the Magnet, nets
      the Net Cutter. Keys 1, 2, 3 (or Q / E) change the tool while the line swings.
      The Magnet ignores fish; the Hook and the Net Cutter do not." The full table is
      in the README under "How to play".

## 3. Object-Oriented Design

- [ ] **3.1 Encapsulation example:** the real methods are `Player.addPoints()` and
      `Player.loseHealth()`, and `Hook.launch()`, `Hook.recall()` and `Hook.update()`.
      Replace `collectTrash()`, `takeDamage()`, `swing()` and `extend()`.
- [ ] **3.1 Inheritance:** add `SmallFish`, `BigFish`, `Pufferfish`, `Barracuda` under
      `Fish`, and `PlainHook`, `Magnet`, `NetCutter` under `Equipment`.
- [ ] **3.1 Polymorphism example:** name the real call, `object.onCaught(player, hook)`,
      and add `move()` and `sense()` (a pufferfish weaves and swells, a barracuda charges).
- [ ] **3.1 Abstraction:** add the interfaces `GameView`, `ObjectView`, `Screen` and
      `GameListener`.
- [ ] **3.1 Other Java concepts:** remove "//double check". Replace `enum (BinType)` with
      `enum (ObjectKind, EquipmentType, EventType, ChapterStatus, GameState, CatchOutcome)`.
      Replace `BinFullException` with `EquipmentLockedException` (thrown when the player
      asks for a tool the crew does not have yet). **(later:** a second one arrives with
      the high-score file). `ArrayList` and `HashMap` are used;
      add `EnumMap`, which the game uses for lookups by enum.
- [ ] **3.2 Main Classes, Player:** remove "and quota". The quota belongs to the chapter
      (`ChapterData`), not the player.
- [ ] **3.2 Fish:** kind is "abstract class", with the four concrete fish under it.
- [ ] **3.2 TimeLimit:** it only counts down and can be frozen. `GameManager` decides
      whether the quota was met.
- [ ] **3.2 Level:** it reads the chapter's spawn table from `ChapterData` and keeps the
      water stocked.
- [ ] **3.2 add rows:** `ChapterData` (a chapter's numbers as data), `GameSession` (the
      run across chapters), `ScreenManager` and `Screen` (the screens), `Equipment`
      (the tool on the line), `GameEvent` and `GameListener` (the observer pattern).
- [ ] **Figure 2:** redraw it from `docs/class-diagram.md`. The current figure shows
      methods that do not exist (`collectTrash`, `collectFish`, `onHookHit`, `getType`)
      and a `gameState: String`; the code uses `onCaught`, `ObjectKind` and enums.
- [ ] **Figure 3:** delete it and its caption ("// to make"). It belongs to the old
      sorting game (`GameController`, `Bin`, `WasteItem`, `Meter`, `StreetLevel`,
      `MarketLevel`, `TyphoonLevel`).
- [ ] **3.3 Package Layout:**
  - `model`: add `ObjectKind`, `SmallFish`, `BigFish`, `Pufferfish`, `Barracuda`,
    `ChapterData`, `SpawnRule`, `Equipment`, `PlainHook`, `Magnet`, `NetCutter`,
    `EquipmentType`, `Loadout`. Change "No JavaFX imports" to
    "No Swing or AWT imports".
  - `game`: remove `GameController`. Add `GameSession`, `GameEvent`, `GameListener`.
  - `ui`: the screens are `MainMenu`, `StoryScreen`, `GameScreen`, `ChapterClearScreen`,
    `GameOverScreen`, `VictoryScreen`, `HowToScreen`, `PauseOverlay`. Change "JavaFX
    screens" to "Swing screens".
  - `util`: `GameConstants`, `StoryText` (all in-game text) and `Mathx`.
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
- [ ] **Team Roles:** add the new classes to each member's list: chapters, session and
      story screens (Llenos); tools, screens and key handling (Marata); the fish
      subclasses, `ObjectKind` and the painters (Villarito). Remove "Lore master" or
      rename it "Story text".

## 5. Deliverables and Evaluation

- [ ] Remove the "// ?" from the heading.
- [ ] **First row:** "Working EcoSort game ... Four bins, 10+ item types, three levels,
      score, meters and feedback" becomes "Working Fish2Trash game ... three story
      chapters, three kinds of trash, four kinds of fish, score, health and feedback".
- [ ] **Class diagram and README:** still right. The diagram lives in
      `docs/class-diagram.md` and a test keeps it in step with the code.

## 6. Tools and Assets

- [ ] "game assets will be made by the team using simple shapes and sprites or obtained
      from free-license sources with proper credits" becomes "everything on screen is
      drawn in code with Java2D; the game uses no external images, fonts or libraries".
- [ ] The line already says Java Swing. Keep it.

## 7. Risks and Mitigation

- [ ] "JavaFX is new to some members" becomes "Java2D drawing is new to some members".
      Plan: "Each object has its own small painter class, tested by drawing off-screen".
      Remove "develop the console version first".
- [ ] Add a risk: "The chapters are too hard or too easy". Plan: "A test bot plays each
      chapter 100 times; all numbers are in one constants class and are tuned in week 4".

## Notes at the end of the document

- [ ] `//add complexity; different equipment for diff types of garbage`: done, as the
      three tools (hook, magnet, net cutter). Remove the note; describe the tools in 2.1.
- [ ] `//story levels represent different types of waste`: done. Each chapter has its
      own main trash. Remove the note.
- [ ] `//add lore`: done. Remove the note.
- [ ] `//endless mode if possible`: not planned. Remove the note, or list it under
      "Future work".
