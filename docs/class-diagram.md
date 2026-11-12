# Class diagram

The diagrams below are drawn from the code as it is now. `DiagramTest` (run by
`scripts/test`) fails if a class in `src/` is missing from this page, or if this
page names a class that no longer exists, so the page cannot quietly go out of date.

To keep the pictures readable, each one shows a part of the game, and getters and
setters are left out.

On GitHub a large diagram is shown cropped. Use the buttons at its top right corner
to zoom, move around, or open it at full size.

**How to read them**

| Arrow | Meaning | Example |
|---|---|---|
| `A <\|-- B` solid line, hollow triangle | B **extends** A (inheritance) | `OceanTrash <\|-- Can` |
| `A <\|.. B` dashed line, hollow triangle | B **implements** interface A | `GameListener <\|.. SessionStats` |
| `A *-- B` filled diamond | A **owns** B; B is created by A and lives with it (composition) | `Boat *-- Hook` |
| `A o-- B` hollow diamond | A **holds** B, but B is made elsewhere (aggregation) | `Hook o-- Loadout` |
| `A ..> B` dashed arrow | A **uses** B for a moment (dependency) | `ObjectKind ..> OceanObject` |

`+` public, `-` private, `#` protected. *Italic* names and a `*` after a method mean abstract.

## 1. Things in the ocean (package `model`)

The inheritance tree. `onCaught`, `move` and `sense` are the polymorphic methods:
the game calls them on an `OceanObject` and each subclass answers in its own way.

```mermaid
classDiagram
    direction TB

    class ObjectView {
        <<interface>>
        +getKind() ObjectKind
        +getX() double
        +getY() double
        +getRadius() double
        +getFacing() int
        +getAlert() double
        +getOpacity() double
        +isCaught() boolean
    }

    class OceanObject {
        <<abstract>>
        -x double
        -y double
        -radius double
        -speedX double
        -facing int
        -caught boolean
        -immuneTimer double
        -age double
        +update(dt, worldWidth)
        #move(dt)
        +sense(hook)
        +collidesWith(x, y, radius) boolean
        +isCatchable() boolean
        +release(immunitySeconds)
        +getPoints() int
        +getKind()* ObjectKind
        +onCaught(player, hook)*
    }

    class OceanTrash {
        <<abstract>>
        -points int
        +onCaught(player, hook)
        +getPoints() int
    }

    class Fish {
        <<abstract>>
        -damage int
        +onCaught(player, hook)
    }

    class PlasticBottle
    class Can
    class FishingNet
    class SmallFish
    class BigFish

    class Pufferfish {
        -homeY double
        -inflate double
        #move(dt)
        +sense(hook)
        +getRadius() double
        +getAlert() double
    }

    class Barracuda {
        -phase Phase
        -phaseTime double
        #move(dt)
        +sense(hook)
        +getAlert() double
    }

    class ObjectKind {
        <<enumeration>>
        PLASTIC_BOTTLE
        CAN
        FISHING_NET
        SMALL_FISH
        BIG_FISH
        PUFFERFISH
        BARRACUDA
        +isTrash() boolean
        +create(x, y, speedX)* OceanObject
    }

    class HookSensor {
        <<interface>>
        +getTipX() double
        +getTipY() double
        +isLineOut() boolean
    }

    ObjectView <|.. OceanObject
    OceanObject <|-- OceanTrash
    OceanObject <|-- Fish
    OceanTrash <|-- PlasticBottle
    OceanTrash <|-- Can
    OceanTrash <|-- FishingNet
    Fish <|-- SmallFish
    Fish <|-- BigFish
    Fish <|-- Pufferfish
    Fish <|-- Barracuda
    ObjectKind ..> OceanObject : creates
    OceanObject ..> HookSensor : senses
```

## 2. Player, boat, hook and tools (package `model`)

```mermaid
classDiagram
    direction LR

    class Player {
        -maxHealth int
        -health int
        -score int
        +addPoints(points)
        +loseHealth(amount)
        +loseHealth()
        +isAlive() boolean
    }

    class Boat {
        -x double
        -y double
        -facing int
        +moveBy(direction, dt)
    }

    class Hook {
        -state State
        -angle double
        -length double
        -anchorX double
        +launch()
        +recall()
        +attach(object)
        +beginRetractEmpty()
        +update(dt)
    }

    class HookSensor {
        <<interface>>
    }

    class Loadout {
        -selected int
        +standard()$ Loadout
        +getSelected() Equipment
    }

    class Equipment {
        <<abstract>>
        -displayName String
        -tipRadius double
        +attempt(object) CatchOutcome
        #outcomeFor(kind)* CatchOutcome
    }

    class PlainHook {
        -outcomes Map
        #outcomeFor(kind) CatchOutcome
    }

    class CatchOutcome {
        <<enumeration>>
        CATCH
        SLIP
        PASS
        +resolve(resolver, object)*
    }

    class CatchResolver {
        <<interface>>
        +onCatch(object)
        +onSlip(object)
        +onPass(object)
    }

    class TimeLimit {
        -total double
        -remaining double
        -frozen boolean
        +tick(dt)
        +freeze()
        +isTimeUp() boolean
    }

    class OceanObject {
        <<abstract>>
    }

    Boat *-- Hook : owns
    HookSensor <|.. Hook
    Hook o-- Loadout : carries
    Hook o-- OceanObject : caught, 0 or 1
    Loadout *-- Equipment : tools, 1 or more
    Equipment <|-- PlainHook
    Equipment ..> CatchOutcome : answers with
    CatchOutcome ..> CatchResolver : calls back
    OceanObject ..> Player : onCaught
    OceanObject ..> Hook : onCaught
```

## 3. Chapters and levels (package `model`)

A chapter is data. `Level` reads it and keeps the water stocked.

```mermaid
classDiagram
    direction LR

    class ChapterData {
        +CHAPTER_COUNT int$
        -number int
        -title String
        -quota int
        -timeLimit double
        -startHealth int
        +forChapter(number)$ ChapterData
    }

    class SpawnRule {
        -initialCount int
        -maxCount int
        -minCount int
        -respawning boolean
        -startSeconds double
        +of(kind)$ Builder
    }

    class TutorialPrompt {
        -atSeconds double
        -text String
        +getText(gearEnabled) String
    }

    class Level {
        -elapsed double
        +update(dt, hook, chapterScore)
        +remove(object)
        +pollTutorialPrompts() List
        +getObjects() List
    }

    class ObjectKind {
        <<enumeration>>
    }

    class OceanObject {
        <<abstract>>
    }

    class HookSensor {
        <<interface>>
    }

    ChapterData *-- SpawnRule : 1 or more
    ChapterData *-- TutorialPrompt : 0 or more
    ChapterData --> ObjectKind : primary kind
    SpawnRule --> ObjectKind : spawns
    Level --> ChapterData : reads
    Level o-- OceanObject : 0 or more
    Level ..> HookSensor : keeps spawns away from
```

## 4. The rules (package `game`)

`GameManager` plays one chapter attempt. `GameSession` remembers the whole run.
The manager announces `GameEvent`s to its listeners (the observer pattern).

```mermaid
classDiagram
    direction TB

    class GameSession {
        -chapterNumber int
        -retries int
        +startChapter() GameManager
        +onChapterEnded(game) ChapterResult
        +isFinished() boolean
        +getTotalPoints() int
    }

    class GameManager {
        -quota int
        -boatDirection int
        -quotaReached boolean
        +update(dt)
        +spaceAction()
        +setBoatDirection(direction)
        +addListener(listener)
        +onCatch(object)
        +onSlip(object)
        +onPass(object)
    }

    class GameView {
        <<interface>>
        +getChapter() ChapterData
        +getBoat() Boat
        +getObjects() List
        +getScore() int
        +getQuota() int
        +getHealth() int
        +getTimeLeft() double
        +getStatus() ChapterStatus
    }

    class CatchResolver {
        <<interface>>
    }

    class GameListener {
        <<interface>>
        +onGameEvent(event)
    }

    class SessionStats {
        -counts Map
        +onGameEvent(event)
        +getLaunches() int
        +getCatches() int
        +getSlips() int
        +getFishHits() int
    }

    class GameEvent {
        -x double
        -y double
        -value int
        -text String
        +of(type)$ GameEvent
        +about(type, object, value)$ GameEvent
        +withText(type, text)$ GameEvent
    }

    class EventType {
        <<enumeration>>
        HOOK_LAUNCHED
        HOOK_RECALLED
        OBJECT_CAUGHT
        HEALTH_LOST
        SLIPPED
        MISSED
        DELIVERED
        TUTORIAL_PROMPT
        QUOTA_REACHED
        CHAPTER_CLEARED
        CHAPTER_FAILED
    }

    class ChapterStatus {
        <<enumeration>>
        RUNNING
        CLEARED
        FAILED_TIME
        FAILED_HEALTH
        +getFailCause() FailCause
        +isOver() boolean
    }

    class FailCause {
        <<enumeration>>
        NONE
        TIME
        HEALTH
    }

    class ChapterResult {
        -chapter int
        -cleared boolean
        -points int
        -timeLeft double
        -healthLeft int
        -catches int
        -fishHits int
        +from(game)$ ChapterResult
    }

    class GameState {
        <<enumeration>>
        MENU
        HOW_TO
        STORY
        PLAYING
        CHAPTER_CLEAR
        GAME_OVER
        ENDING
    }

    class Player
    class Boat
    class Level
    class TimeLimit
    class Loadout
    class ChapterData

    GameSession ..> GameManager : creates
    GameSession *-- ChapterResult : cleared chapters
    GameSession *-- Loadout : crew tools
    GameView <|.. GameManager
    CatchResolver <|.. GameManager
    GameManager *-- Player
    GameManager *-- Boat
    GameManager *-- Level
    GameManager *-- TimeLimit
    GameManager *-- SessionStats
    GameManager --> ChapterData : plays
    GameManager o-- GameListener : listeners, 0 or more
    GameListener <|.. SessionStats
    GameManager ..> GameEvent : announces
    GameEvent --> EventType
    GameManager --> ChapterStatus
    ChapterStatus --> FailCause
    ChapterResult --> FailCause
    ChapterResult ..> GameManager : reads when over
```

## 5. Screens (packages `ui` and `ui.render`)

`ScreenManager` keeps one `Screen` for each `GameState` and shows one at a time.

```mermaid
classDiagram
    direction TB

    class GamePanel {
        -lastTickNanos long
        +start()
        +advance(dt)
        +getState() GameState
    }

    class InputState {
        +press(keyCode, when) boolean
        +release(keyCode, when)
        +getDirection() int
    }

    class ScreenManager {
        -current GameState
        -darkness double
        +register(state, screen)
        +go(target)
        +pause(overlay)
        +resume()
        +startNewRun()
        +getSession() GameSession
    }

    class Screen {
        <<interface>>
        +onEnter()
        +onExit()
        +update(dt)
        +paint(g)*
        +keyPressed(keyCode)
    }

    class MainMenu
    class HowToScreen
    class StoryScreen
    class GameScreen
    class ChapterClearScreen
    class GameOverScreen
    class VictoryScreen
    class PauseOverlay
    class MenuList
    class TextPager

    class SceneRenderer {
        +paint(g, game)
        +paintBackground(g)
    }

    class PainterRegistry {
        -painters Map
        +paint(g, object)
    }

    class ObjectPainter {
        <<interface>>
        +paint(g, object)
    }

    class HudPainter {
        +paint(g, game, time)
        +paintToast(g, text, age)
    }

    class BackgroundPainter {
        -cache Map
        +paint(g, palette)
        +paintBlend(g, from, to, amount)
    }

    class ChapterPalette {
        +SHALLOWS ChapterPalette$
        +WRECK ChapterPalette$
        +DEEP ChapterPalette$
        +REEF ChapterPalette$
        +forChapter(chapter)$ ChapterPalette
        +getCleaned() ChapterPalette
    }

    class StoryBoxPainter
    class PortraitPainter
    class GameSession
    class GameManager

    GamePanel *-- ScreenManager
    GamePanel *-- InputState
    ScreenManager o-- Screen : one per GameState
    ScreenManager *-- GameSession : the run
    Screen <|.. MainMenu
    Screen <|.. HowToScreen
    Screen <|.. StoryScreen
    Screen <|.. GameScreen
    Screen <|.. ChapterClearScreen
    Screen <|.. GameOverScreen
    Screen <|.. VictoryScreen
    Screen <|.. PauseOverlay
    MainMenu *-- MenuList
    PauseOverlay *-- MenuList
    StoryScreen *-- TextPager
    VictoryScreen *-- TextPager
    StoryScreen *-- StoryBoxPainter
    VictoryScreen *-- StoryBoxPainter
    StoryBoxPainter *-- PortraitPainter
    GameScreen *-- PauseOverlay
    GameScreen *-- HudPainter
    GameScreen --> GameManager : plays
    GameScreen --> SceneRenderer
    GameScreen --> InputState : reads
    SceneRenderer *-- BackgroundPainter
    BackgroundPainter ..> ChapterPalette : one cached picture per look
    SceneRenderer *-- PainterRegistry
    PainterRegistry o-- ObjectPainter : one per ObjectKind
```

`ObjectPainter` has one implementation for each `ObjectKind`: `BottlePainter`,
`CanPainter`, `NetPainter`, `SmallFishPainter`, `BigFishPainter`, `PufferPainter`
and `BarracudaPainter`.

## Classes without a box

These hold shared constants or helpers and have no relationships worth drawing:

- `Main` opens the window and starts the `GamePanel`.
- `GameConstants` holds every tuning number, `StoryText` every line of text,
  and `Mathx` a few math helpers (package `util`).
- `Fonts`, `UiPalette` and `TextDraw` hold the fonts, colours and text-placing
  helpers the screens share (package `ui.render`).
