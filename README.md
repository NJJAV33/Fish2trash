# FISH2TRASH

Clean the ocean: hook the trash, spare the fish. Reach 100 points before time or health runs out.

## Requirements

JDK 17 or newer. Check yours with `java -version`. No libraries needed.

## Run

| | Linux / macOS | Windows |
|---|---|---|
| Play the game | `scripts/run.sh` | `scripts\run.bat` |
| Only compile | `scripts/build.sh` | `scripts\build.bat` |
| Run the tests | `scripts/test.sh` | `scripts\test.bat` |

The scripts work from any folder. They look for a JDK 17+ in `JAVA_HOME` first,
then on the `PATH`, and stop with "Need JDK 17+" if there isn't one.

To build by hand instead, from the project folder:

```bash
mkdir -p out
javac --release 17 -d out $(find src -name '*.java')
java -cp out Main
```

## Controls

| Key | Action |
|---|---|
| `A` / `D` or arrow keys | Move the boat (only while the hook is swinging) |
| `Space` | Drop the hook. Press again while it is going out to pull it back |
| `R` | Restart |

## Rules

- Trash earns points: bottle 10, can 20, fishing net 50.
- Hooking a fish costs 1 health. The fish swims away.
- Reach the quota of 100 and the clock stops; you win when that catch reaches the boat.
- You lose if time runs out or health reaches 0.

## Folder structure

```
FISH2TRASH/
├── src/
│   ├── Main.java            opens the window on the Swing thread
│   ├── model/               the things in the game - no drawing code
│   │   ├── OceanObject      abstract base: position, movement, collision
│   │   ├── OceanTrash       abstract: gives points, rides the hook
│   │   │   └── PlasticBottle, Can, FishingNet
│   │   ├── Fish             abstract: costs health, swims away
│   │   │   └── SmallFish
│   │   ├── ObjectKind       enum: builds each object, lookup key for painters
│   │   ├── ObjectView       read-only view of an object, for the ui
│   │   ├── Player           private health and score
│   │   ├── Boat, Hook       the boat owns the hook
│   │   ├── TimeLimit        the countdown
│   │   └── Equipment, PlainHook, Loadout, CatchOutcome, CatchResolver
│   ├── game/                the rules
│   │   ├── GameManager      one chapter attempt: collisions, quota, win/lose
│   │   ├── ChapterStatus    running / cleared / failed
│   │   └── GameEvent, EventType, GameListener   (observer pattern)
│   ├── ui/
│   │   ├── GamePanel        frame timer, keys, HUD
│   │   ├── InputState       which movement keys are held
│   │   └── render/          SceneRenderer, PainterRegistry and one painter per kind
│   └── util/
│       ├── GameConstants    every tuning number in one place
│       └── Mathx            clamp, lerp, easing
├── test/                    tests, run by scripts/test
├── scripts/                 build, run and test (.sh and .bat)
├── out/                     compiled game (created by the build, not in git)
└── out-test/                compiled tests (not in git)
```

## How the classes work together

1. `Main` builds a `JFrame` on the event-dispatch thread and adds a `GamePanel`.
2. `GamePanel` runs a `javax.swing.Timer` (~60 fps). Every tick it measures the real
   time since the last tick (`dt`), passes the held movement keys to the game, calls
   `manager.update(dt)`, then `repaint()`. Painting only *reads* the game.
3. `GameManager` owns the `Player`, the `Boat` (which owns the `Hook`), a `TimeLimit`
   and a `List<OceanObject>` holding bottles, cans, nets and fish.
4. When the hook tip touches an object, the tool on the line (`Equipment`) answers with
   a `CatchOutcome`: catch, slip or pass. The outcome calls back into the manager.
5. On a catch the manager calls `object.onCaught(player, hook)`. Trash adds points and
   attaches itself to the hook; a fish costs health and swims away. The manager never
   checks which one it was.
6. The manager announces what happened as `GameEvent`s to its `GameListener`s.
7. `SceneRenderer` draws the world. For each object it asks `PainterRegistry` for the
   painter of that object's `ObjectKind`.

### OOP concepts, and where to point at them

| Concept | Where |
|---|---|
| Encapsulation | Every field is private. `Player` only changes through `addPoints()` and `loseHealth()`, which enforce the rules. `GameManager.getObjects()` returns a read-only list. |
| Inheritance | `OceanObject` → `OceanTrash` → `PlasticBottle`, `Can`, `FishingNet`, and `OceanObject` → `Fish` → `SmallFish`. `Equipment` → `PlainHook`. |
| Abstraction | `OceanObject`, `OceanTrash`, `Fish` and `Equipment` are abstract. `ObjectView`, `GameListener`, `CatchResolver` and `ObjectPainter` are interfaces that hide what is behind them. |
| Polymorphism | `object.onCaught(player, hook)`, `tool.attempt(object)`, `outcome.resolve(...)` and `painter.paint(...)`: one call, different behaviour per class. |
| Composition | `Boat` owns its `Hook`; the `Hook` carries a `Loadout` of tools. |

Design rules, checked by `ArchitectureTest` on every test run: `model` and `game` have
no drawing, window or sound imports; nothing uses `instanceof`; no public fields
except constants.

## Manual testing checklist

**Boat and hook**
- [ ] `A`/`D` and the arrow keys move the boat; it stops near both screen edges.
- [ ] The boat cannot move while the hook is out.
- [ ] `Space` launches the hook straight along its swing angle.
- [ ] `Space` again while it is going out pulls it back empty.
- [ ] A launch that hits nothing goes to full length and returns empty.

**Catching**
- [ ] Bottle +10, can +20, net +50. The trash rides the hook up and a new piece appears.
- [ ] A fish costs 1 health, gives no points, and swims away; the hook comes back empty.
- [ ] Only one object is touched per launch.

**Winning and losing**
- [ ] At 100 points the timer stops; **YOU WIN!** shows when the catch reaches the boat.
- [ ] Three fish show **GAME OVER** with "You caught too many fish!".
- [ ] Letting the timer hit 0 shows **GAME OVER** with "Time's up!".
- [ ] Nothing moves after the end; `Space` does nothing.

**Restarting**
- [ ] `R` at any time starts a fresh game: score 0, health 3, time 65.

## Contributing

- Work on a branch, not on `main`. Name it `<type>/<short-name>`, for example
  `feat/boat-movement` or `fix/hook-collision`.
- Run the tests before you commit (`scripts/test.sh` or `scripts\test.bat`).
  The build must finish with no warnings.
- Keep commits small, one change each. Start the message with a type and write
  it as an instruction, in lowercase: `feat: add boat movement`,
  `fix: stop hook passing through cans`. Types: `feat`, `fix`, `refactor`,
  `test`, `docs`, `chore`, `build`.
- Push your branch and open a pull request into `main`, then ask a teammate to
  review it.
