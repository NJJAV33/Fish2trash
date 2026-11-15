# Playtest sheet

The numbers of the game (time, health, trash, fish) were chosen on paper and
checked by a test bot. A bot is not a person. Before the demo, let a few people
play and use this page to decide whether a number should change.

## What to aim for

- A player who has seen the game once clears each level with about 20% to 30% of
  the time left.
- A first-time player clears level 1-1 at the first try and 1-5 by the second.
- The last level of each chapter (1-5, 2-5, 3-5) needs a few tries. The others
  should not.
- A careless player (hooks fish, keeps the Magnet on near a ray, uses the Net
  Cutter in a crowd) gets fired now and then.

## How to run a playtest

1. Start a fresh run. Do not explain anything; let the story and the tips do it.
2. Watch, and write down one line per attempt at a level. The level report shows
   most of these numbers.
3. Three players are enough to see a pattern. Try to include one who has never
   played.

| Player | Level | Attempt | Cleared? | Time left | Health left | Fish hit | Ray hits | Slips | Hints seen |
|---|---|---|---|---|---|---|---|---|---|
| | | | | | | | | | |
| | | | | | | | | | |
| | | | | | | | | | |

Also note anything a player said out loud: confusion about a tool, a tip nobody
read, a fish that felt unfair, a ray they did not see coming.

## What to change, in this order

Change one number at a time, then play again. The level tables are in
`src/model/LevelData.java`; every other number is in `src/util/GameConstants.java`.

| Order | What you saw | Number | Where | Change |
|---|---|---|---|---|
| 1 | Players run out of time, or have far too much left | the level's time (4th number in `new LevelData(...)`) | `LevelData` | 5 to 10 seconds at a time |
| 2 | Too many fish to find a gap | remove one fish rule, or lower its `count(...)` | `LevelData` | one fish at a time |
| 3 | Players run out of trash before the quota | the trash rules' `supply(...)` | `LevelData` | one or two pieces at a time |
| 4 | The stingray feels unfair or never matters | `STINGRAY_SENSE_RANGE`, `STINGRAY_WARNING_S`, `STINGRAY_CHASE_SPEED` | `GameConstants` | 20 px, 0.2 s, 20 px/s at a time |
| 5 | The Net Cutter is never worth the risk, or hurts too much | `CUTTER_FISH_DAMAGE` | `GameConstants` | 2 is the plan; 1 makes it as safe as the Hook |
| 6 | Chapter 3 ends by health too often | `HEALTH_CH3` | `GameConstants` | one at a time |
| 7 | Changing tools feels slow or too free | `TOOL_SWAP_COOLDOWN_S` | `GameConstants` | 0.25 at a time |
| last | Only if nothing above helps | the quotas | `LevelData` | the last level of each chapter must stay 100, 200 and 350: the proposal promises these |

Other numbers that players may comment on:

| What | Number | Now |
|---|---|---|
| Hints come too often or too rarely | `HINT_COOLDOWN_S`, `HINT_MAX_PER_LEVEL`, `HINT_IDLE_S`, `HINT_FISH_STREAK`, `HINT_SLIP_STREAK` | 20, 3, 12, 3, 2 |
| A badge is too easy or out of reach | `SORTER_MAX_SLIPS`, `SORTER_MIN_ACCURACY`, `CHAMPION_TIME_FRACTION`, `CHAMPION_MAX_HP_LOST` | 1, 0.60, 0.40, 1 |
| The score feels wrong | `SCORE_POINT_MULT`, `TIME_BONUS_PER_S`, `HEALTH_BONUS_PER_HP`, `RETRY_PENALTY_BASE` | 10, 20, 100, 500 |

After every change, run `scripts/test.sh` (or `scripts\test.bat`):

- `LevelSpawnTest` checks the quota and health of every level against the list in
  its `levelNumbersMatchThePlan` method, that every level holds at least 1.3 times
  its quota in trash, that from chapter 2 on no single kind of trash is enough, and
  that fish only get more inside a chapter.
- `StoryTextTest` checks that each level's story page names its real quota. If you
  change a quota, change the "Quota: ... points" line in `StoryText` too.
- `BalanceBotTest` must stay green. It prints one line per level for each bot.
- Update the level tables in the README if a time, health or quota changed.

## What the bots say

`BalanceBotTest` plays every level 50 times with each of four computer players:

- The **careful bot** picks the right tool, steers over the trash, waits until no
  fish is about to cross the line, pulls back from a charging barracuda, and takes
  the Magnet off when a ray comes up. It must clear each level at least 30 times.
- The **slow bot** is the careful bot with a 1.5 second pause after every throw and
  an aim up to 30 pixels off. Outside the last level of a chapter it must not run
  out of time more than 30 times.
- The **blind bot** never looks at the fish. In the last level of each chapter it
  must lose more often than it wins, and where there are rays it must get stung.
- The **one-tool bot** never changes tools. From chapter 2 on it must never win.

| Level | Time | Careful: cleared | Careful: time left | Slow: cleared | Slow: out of time | Blind: cleared | Blind: ray hits per run |
|---|---|---|---|---|---|---|---|
| 1-1 | 50 s | 50 | 39 s | 49 | 1 | 50 | - |
| 1-2 | 60 s | 48 | 41 s | 49 | 1 | 48 | - |
| 1-3 | 70 s | 47 | 43 s | 47 | 3 | 42 | - |
| 1-4 | 80 s | 50 | 44 s | 40 | 10 | 37 | - |
| 1-5 | 95 s | 44 | 50 s | 36 | 14 | 11 | - |
| 2-1 | 65 s | 50 | 40 s | 46 | 4 | 49 | - |
| 2-2 | 70 s | 46 | 39 s | 40 | 10 | 43 | - |
| 2-3 | 85 s | 49 | 45 s | 36 | 14 | 27 | 0.4 |
| 2-4 | 95 s | 46 | 47 s | 36 | 13 | 14 | 0.8 |
| 2-5 | 115 s | 47 | 45 s | 23 | 25 | 9 | 0.9 |
| 3-1 | 75 s | 49 | 49 s | 46 | 4 | 46 | - |
| 3-2 | 100 s | 45 | 46 s | 30 | 20 | 20 | - |
| 3-3 | 110 s | 45 | 46 s | 26 | 24 | 17 | - |
| 3-4 | 140 s | 46 | 53 s | 21 | 29 | 4 | 0.2 |
| 3-5 | 150 s | 39 | 54 s | 8 | 42 | 2 | 0.2 |

All counts are out of 50. "Time left" is the average over the cleared runs. The
careful bot almost never loses health, so health is decided by the blind bot.

## What has been tuned so far, and what has not

**Tuned with the bots:** every level's clock, and the number of fish in the last
levels. The first tables had too many fish in 1-5, 2-5 and chapter 3: the careful
bot spent most of its time waiting for a gap and ran out of time in up to 49 of 50
runs of 3-5. One or two fish were taken out of those levels and the clocks of the
long levels were raised until the careful bot cleared at least 3 runs in 5.

**Not tuned, because no person has played yet:**

- Health. The bots tell us little about it: the careful one avoids almost every
  fish and the blind one hits almost every one.
- The stingray. The careful bot simply never puts the Magnet near one. Watch
  whether people notice the sparks in time, and whether "the Magnet wakes the ray"
  is clear from the story page and the tip alone.
- Hints, badges and score values.

**Things to watch for in a playtest:**

- The slow bot runs out of time in most runs of 3-5 (42 of 50). That level is meant
  to be the hardest, but if real players feel the same, give it 10 more seconds.
- The Net Cutter costs 2 for every fish. If players stop using it near any fish at
  all, chapter 3 may feel slow; try `CUTTER_FISH_DAMAGE = 1` and compare.
- Trash is limited now. If a player misses a lot, a level can run low on one kind;
  the supply is at least 1.3 times the quota, so it should not run out, but note it
  if it does.
