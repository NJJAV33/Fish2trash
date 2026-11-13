# Playtest sheet

The numbers of the game (time, health, trash, fish) were chosen on paper and
checked by a test bot. A bot is not a person. Before the demo, let a few people
play and use this page to decide whether a number should change.

## What to aim for

- A player who has seen the game once clears each chapter with about 20% to 30% of
  the time left.
- A first-time player clears chapter 1 by the second attempt.
- A careless player (hooks fish, uses the wrong tool) gets fired now and then.

## How to run a playtest

1. Start a fresh run. Do not explain anything; let the story and the tips do it.
2. Watch, and write down one line per chapter attempt. The chapter report shows most
   of these numbers.
3. Three players are enough to see a pattern. Try to include one who has never
   played.

| Player | Chapter | Attempt | Cleared? | Time left | Health left | Fish hooked | Slips | Hints seen | Badge? |
|---|---|---|---|---|---|---|---|---|---|
| | | | | | | | | | |
| | | | | | | | | | |
| | | | | | | | | | |

Also note anything a player said out loud: confusion about a tool, a tip nobody
read, a fish that felt unfair.

## What to change, in this order

Change one number at a time, then play again. Every number is in
`src/util/GameConstants.java` unless the table says otherwise.

| Order | What you saw | Number | Now | Change |
|---|---|---|---|---|
| 1 | Players run out of time, or have far too much left | `TIME_CH1_S`, `TIME_CH2_S`, `TIME_CH3_S` | 80, 90, 105 | 5 to 10 seconds at a time |
| 2 | Chapter 3 ends by health too often | `HEALTH_CH3` | 5 | one at a time |
| 3 | Not enough trash to aim at, or the water is crowded | the `count(...)` and `respawn(...)` values in `src/model/ChapterData.java` | see file | one piece at a time |
| 4 | Changing tools feels slow or too free | `TOOL_SWAP_COOLDOWN_S` | 1.0 | 0.25 at a time |
| 5 | Fish are impossible to avoid, or no threat | the `speed(...)` values of the fish in `ChapterData.java` | see file | 10 to 20 at a time |
| last | Only if nothing above helps | `QUOTA_CH1`, `QUOTA_CH2`, `QUOTA_CH3` | 100, 200, 350 | the proposal promises these, so avoid it |

Other numbers that players may comment on:

| What | Number | Now |
|---|---|---|
| Hints come too often or too rarely | `HINT_COOLDOWN_S`, `HINT_MAX_PER_CHAPTER`, `HINT_IDLE_S`, `HINT_FISH_STREAK`, `HINT_SLIP_STREAK` | 20, 3, 12, 3, 2 |
| A badge is too easy or out of reach | `SORTER_MAX_SLIPS`, `SORTER_MIN_ACCURACY`, `CHAMPION_TIME_FRACTION`, `CHAMPION_MAX_HP_LOST` | 1, 0.60, 0.40, 1 |
| The score feels wrong | `SCORE_POINT_MULT`, `TIME_BONUS_PER_S`, `HEALTH_BONUS_PER_HP`, `RETRY_PENALTY_BASE` | 10, 20, 100, 500 |

After every change, run `scripts/test.sh` (or `scripts\test.bat`):

- `LevelSpawnTest` checks the time, health and quota of each chapter against the
  values it expects. Update the three arrays in its `chapterNumbersMatchThePlan`
  method to match.
- `BalanceBotTest` must stay green. It prints one line per chapter for each bot.
- Update the chapter table in the README if a time, health or quota changed.

## What the bots say

`BalanceBotTest` plays each chapter 100 times with two computer players. Neither
looks at the fish, so both lose more health than a person would.

- The **bot** aims perfectly and never waits.
- The **careless bot** waits 2 seconds after every throw and aims up to 40 pixels
  off, so it misses often (about a third of its throws in chapter 1).

| Chapter | Bot: cleared | Bot: time left | Careless: cleared | Careless: ran out of time | Careless: time left |
|---|---|---|---|---|---|
| 1 (80 s) | 84 of 100 | 57 s | 49 of 100 | 6 | 22 s |
| 2 (90 s) | 100 of 100 | 63 s | 100 of 100 | 0 | 36 s |
| 3 (105 s) | 83 of 100 | 82 s | 56 of 100 | 0 | 57 s |

"Time left" is the average over the runs that were cleared. Every run that was not
cleared and did not run out of time ended by health.

## What has been tuned so far, and what has not

**Changed: chapter 1 now has 80 seconds instead of 65.**
Chapter 1 asked for the fastest pace of the three: 10 bottles in 65 seconds is 6.5
seconds per bottle, while chapter 2 allows 9 seconds per can and chapter 3 allows
15 seconds per net. Bottles are also the smallest target. With 65 seconds the
careless bot ran out of time in 24 of 100 runs of chapter 1 and never in the
others. With 80 seconds (8 seconds per bottle) it has about a quarter of the time
left, which is the target above.

**Changed: the chapter 2 tip about big fish.**
It told the player to wait for a gap between the fish, but the Magnet goes through
fish. It now says so.

**Not changed, because no person has played yet:**

- The clocks of chapters 2 and 3. The bots have a lot of time left there, but a
  person steers and aims more slowly than either bot.
- Health. The bots cannot tell us anything about it, because they never avoid a fish.
- Hints, badges and score values. They are the planned first values.

**Things to watch for in a playtest:**

- In chapter 2, a player who keeps the Magnet on cannot lose health at all, because
  the Magnet ignores fish. The only way to fail is the clock. If testers find the
  chapter dull, shorten `TIME_CH2_S` first.
- A fast player finishes chapter 3 in under half a minute. The pufferfish and the
  barracuda only arrive after 20 seconds (`start(20)` in `ChapterData.java`), so such
  a player hardly meets them. If that happens, lower those start times.
- The bot earns the Sorter badge in every run. Check whether people do too.
