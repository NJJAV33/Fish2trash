# FISH2TRASH (Swing demo)

Clean the ocean: hook trash, avoid fish. Reach 50 points before time or health runs out.

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

**Controls:** `Space` launch the hook · `R` restart

## Folder structure

```
FISH2TRASH/
├── README.md
├── src/
│   ├── Main.java              opens the window on the Swing thread
│   ├── model/                 the things in the game
│   │   ├── Player.java        private health and score
│   │   ├── Boat.java          position, owns the Hook
│   │   ├── Hook.java          swing → extend → retract
│   │   ├── OceanObject.java   abstract base: position, movement, collision
│   │   ├── OceanTrash.java    +10 points when caught
│   │   └── Fish.java          −1 health when caught
│   ├── game/
│   │   └── GameManager.java   rules: objects, collisions, countdown, win/lose
│   └── ui/
│       └── GamePanel.java     drawing + key bindings + frame timer
├── test/                      tests, run by scripts/test
│   ├── TestRunner.java        runs every test, exits with 1 if one fails
│   ├── Check.java             assertTrue / assertEquals helpers
│   └── PlayerTest.java
├── scripts/                   build, run and test (.sh and .bat)
├── out/                       compiled game (created by the build, not in git)
└── out-test/                  compiled tests (not in git)
```

## How the classes work together

1. `Main` builds a `JFrame` on the event-dispatch thread and adds a `GamePanel`.
2. `GamePanel` runs a `javax.swing.Timer` (~60 fps). Every tick it measures the real
   time since the last tick (`dt`), calls `manager.update(dt)`, then `repaint()`.
   `paintComponent` only *reads* the game state — updating and drawing stay separate.
3. `GameManager` owns the `Player`, the `Boat` (which owns the `Hook`), and a
   `List<OceanObject>` holding both trash and fish. Each update it counts down the timer,
   moves the hook and objects, and checks if the hook tip touches an object.
4. On a hit, the manager marks the object caught and calls `object.onCaught(player)`.
   It doesn't know or care whether it's trash or a fish — the object decides.
5. The hook drags the object back to the boat, where it's removed and a new one spawns.
6. Score ≥ 50 → win immediately. Health 0 or time 0 → lose. After that, `update()`
   does nothing until `R` calls `restart()`, which rebuilds every piece of state.

### OOP concepts, and where to point at them

| Concept | Where |
|---|---|
| Encapsulation | `Player` — `health`/`score` are private; only `addPoints()` and `loseHealth()` change them, and they enforce the rules. `GameManager.getObjects()` returns a read-only list. |
| Inheritance | `OceanTrash` and `Fish` both `extends OceanObject` and reuse its movement and collision code. |
| Abstraction | `OceanObject` is `abstract` with abstract `onCaught()` and `draw()` — it defines *what* every ocean object does, not *how*. |
| Polymorphism | `object.onCaught(player)` in `GameManager` and `object.draw(g2)` in `GamePanel` — same call, different behavior per subclass. |
| Composition | `Boat` creates and owns its `Hook`. |

## Manual testing checklist

**Catching trash**
- [ ] Hook swings left and right under the boat by itself.
- [ ] Space launches it straight in the direction it was pointing.
- [ ] Hitting a can gives +10 score; the can rides the hook back and disappears.
- [ ] A new object appears after the catch.
- [ ] Space does nothing while the hook is already out.

**Catching fish**
- [ ] Hitting a fish removes 1 health; score doesn't change.
- [ ] The hook catches only one object per launch, even if it passes others on the way back.

**Missing**
- [ ] A launch that hits nothing goes to max distance and returns empty.

**Winning**
- [ ] Catching the 5th can (50 points) shows **YOU WIN!** right away, before the hook gets back.
- [ ] Timer, hook and objects freeze; Space does nothing.

**Losing**
- [ ] Catching 3 fish shows **GAME OVER — You caught too many fish!**
- [ ] Letting the timer hit 0 shows **GAME OVER — Time's up!**
- [ ] Gameplay freezes after losing.

**Restarting**
- [ ] R after a win or loss: score 0, health 3, time 60, fresh objects, hook swinging.
- [ ] R in the middle of a game also resets everything, including a hook that was out.

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
